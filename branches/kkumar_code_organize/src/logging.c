/**
 * This file contains the functions of the logging system.
 *
 * Jakub S�awi�ski 2006-06-14
 * jeremian@poczta.fm
 */

#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include <time.h>
#include <stdio.h>
#include <stdint.h>
#include <sys/time.h>
#include <sys/socket.h>
#include <netdb.h>

/* #ifdef HAVE_ZLIB_H */
#include <zlib.h>
/* #endif */

#include "logging.h"
#include "testoptions.h"
#include "strlutils.h"

static int _debuglevel = 0;
static char* _programname = "";
static char* LogFileName = BASEDIR"/"LOGFILE;
static char ProtocolLogFileName[FILENAME_SIZE] = BASEDIR"/"PROTOLOGFILE;
static char* ProtocolLogDirName = BASEDIR"/"LOGDIR;
static char protocollogfilestore[FILENAME_SIZE];
static char enableprotologging = 0;
static I2ErrHandle _errorhandler_nl = NULL;
static I2ErrHandle _errorhandler = NULL;
static I2LogImmediateAttr _immediateattr_nl;
static I2LogImmediateAttr _immediateattr;
static time_t timestamp;
static long int utimestamp;

/**
 * Compress snaplog, tcpdump, and cputime files to save disk space.
 * These files compress by 2 to 3 orders of
 * magnitude (100 - 1000 times).  This can save a lot of disk space.
 *   9/9/09  RAC
 *
 *  @param pointer to string containing the source file name
 *  @return integer 0 on success, error code on failure
 *   Possible error codes are:
 *  	-3: Failure to open source file for reading
 *  	-4: Failure to open destination file for writing
 *  	-1: Z_ERRNO as defined by zlib library
 *  	other error codes as defined by zlib's deflateInit(0 method:
 *  		Z_MEM_ERROR if there was not enough memory,
 *  		Z_STREAM_ERROR if level is not a valid compression level,
 *  		Z_VERSION_ERROR if the zlib library version (zlib_version)
 *  			is incompatible with the version assumed by the caller (ZLIB_VERSION)
 *
 */

int zlib_def(char *src_fn) {

	int ret, flush, level = Z_DEFAULT_COMPRESSION;
	char dest_fn[256];
	FILE *dest, *source;
	unsigned have;
	z_stream strm;
	unsigned char in[16384];
	unsigned char out[16384];

	// allocate deflate state
	strm.zalloc = Z_NULL;
	strm.zfree = Z_NULL;
	strm.opaque = Z_NULL;
	ret = deflateInit(&strm, level);
	if (ret != Z_OK) {
		log_println(6, "zlib deflateInit routine failed with %d", ret);
		return ret;
	}

	sprintf(dest_fn, "%s.gz", src_fn);
	if ((source = fopen(src_fn, "r")) == NULL) {
		log_println(6, "zlib_def(): failed to open src file '%s' for reading",
				src_fn);
		return -3;
	}
	if ((dest = fopen(dest_fn, "w")) == NULL) {
		log_println(6, "zlib_def(): failed to open dest file '%s' for writing",
				dest_fn);
		return -4;
	}
	// compress until end of file
	do {
		strm.avail_in = fread(in, 1, 16384, source);
		if (ferror(source)) {
			(void) deflateEnd(&strm);
			return Z_ERRNO;
		}
		flush = feof(source) ? Z_FINISH : Z_NO_FLUSH;
		strm.next_in = in;

		// run deflate() on input until output buffer not full, finish
		//   compression if all of source has been read in
		do {
			strm.avail_out = 16384;
			strm.next_out = out;

			ret = deflate(&strm, flush); /* no bad return value */
			assert(ret != Z_STREAM_ERROR); /* state not clobbered */
			have = 16384 - strm.avail_out;
			if (fwrite(out, 1, have, dest) != have || ferror(dest)) {
				(void) deflateEnd(&strm);
				return Z_ERRNO;
			}

		} while (strm.avail_out == 0);
		assert(strm.avail_in == 0); /* all input will be used */

		/* done when last data in file processed */
	} while (flush != Z_FINISH);
	assert(ret == Z_STREAM_END); /* stream will be complete */

	/* clean up and return */
	(void) deflateEnd(&strm);

	/* compressed version of file is now created, remove the original uncompressed version */
	remove(src_fn);

	return Z_OK;
}
/* #endif */

/**
 * Initialize the logging system.
 * @param progname The name of the program
 * @param debuglvl The debug level
 */

void log_init(char* progname, int debuglvl) {
	assert(progname);

	_programname =
			(_programname = strrchr(progname, '/')) ?
					_programname + 1 : progname;

	_immediateattr.fp = _immediateattr_nl.fp = stderr;
	_immediateattr.line_info = I2MSG | I2NONL;
	_immediateattr_nl.line_info = I2MSG;
	_immediateattr.tformat = _immediateattr_nl.tformat = NULL;

	_errorhandler = I2ErrOpen(progname, I2ErrLogImmediate, &_immediateattr,
			NULL, NULL);
	_errorhandler_nl = I2ErrOpen(progname, I2ErrLogImmediate,
			&_immediateattr_nl, NULL, NULL);

	if (!_errorhandler || !_errorhandler_nl) {
		fprintf(stderr, "%s : Couldn't init error module\n", progname);
		exit(1);
	}

	_debuglevel = debuglvl;
}

/**
 * Free malloc'ed memmory after child process ends.  Allocation without
 * a corresponding free() causes a memory leak, and the main process never ends so
 * memory is not free'ed on a close.
 * Added RAC 10/13/09
 */
void log_free(void) {
	free(_errorhandler);
	free(_errorhandler_nl);
}

/**
 * Set the debug level to the given value.
 * @param debuglvl new debug level to use
 */

void set_debuglvl(int debuglvl) {
	_debuglevel = debuglvl;
}

/**
 * Set the log filename.
 * @param  new log filename
 */

void set_logfile(char* filename) {
	LogFileName = filename;
}

/**
 * Set the protocol log directory. The log directory is accepted as
 * a command line argument using the -u option.
 * @param filename The new protocol log filename
 *
 */
void set_protologdir(char* dirname) {
	char * localstr[256];

	// Protocol log location being set
	if (dirname == NULL) {
		//use default of BASEDIR/LOGDIR
		log_println(5, "PV: 1: NULL proto location =%s;\n", ProtocolLogDirName);
		return;
	} else if (dirname[0] != '/') {
		sprintf(localstr, "%s/%s/", BASEDIR, dirname);
		ProtocolLogDirName = localstr;
		log_println(5, "PV: 2: non-dir proto location. So=%s;\n", dirname);
	} //end protocol dir name
	else {
		sprintf(localstr, "%s", dirname);
		ProtocolLogDirName = dirname;
		log_println(5, "PV33: proto location=%s;\n", ProtocolLogDirName);
	}

}

/**
 * Get the directory where the protocol log will be placed
 * @return directory where protocol logs are placed
 */
char* get_protologdir() {
	printf("PV34: proto location=%s;\n", ProtocolLogDirName);
	return ProtocolLogDirName;
}

/**
 * Sets the protocol log filename.
 * @param filename The new protocol log filename
 *
 */
void set_protologfile(char* client_ip, char* protologlocalarr) {
	FILE * fp;

	if (ProtocolLogDirName == NULL) {
		ProtocolLogDirName = BASEDIR;
	}
	sprintf(protologlocalarr, "%s/%s%s%s%s", ProtocolLogDirName, PROTOLOGPREFIX,
			client_ip, PROTOLOGSUFFIX, "\0");

	strlcpy(ProtocolLogFileName, protologlocalarr, sizeof(ProtocolLogFileName));
	log_println(5, "Protocol filename: %s: %s\n", ProtocolLogFileName,
			ProtocolLogDirName);
}

/**
 * Return the protocol validation log filename.
 * @return The protocol log filename
 */

char*
get_protologfile() {
	return ProtocolLogFileName;
}

/**
 * Return the current debug level.
 * @return  current debug level
 */

int get_debuglvl() {
	return _debuglevel;
}

/**
 * Return the log filename.
 * @return The log filename
 */

char*
get_logfile() {
	return LogFileName;
}

/**
 * Return the error handle, that writes the messages
 *              with the new line.
 * @return The error handle
 */

I2ErrHandle get_errhandle() {
	return _errorhandler_nl;
}

/**
 * Logs the message with the given level.
 * @param lvl  level of the message
 * @param format format of the message
 *            ... - the additional arguments
 */

void log_print(int lvl, const char* format, ...)
{
	va_list ap;

	if (lvl > _debuglevel) {
		return;
	}

	va_start(ap, format);
	I2ErrLogVT(_errorhandler,-1,0,format,ap);
	va_end(ap);
}

/**
 * Log the message with the given level. New line character
 *              is appended to the error stream.
 * @param lvl     level of the message
 * @param format  format of the message
 *            ... - the additional arguments
 */

void log_println(int lvl, const char* format, ...)
{
	va_list ap;

	if (lvl > _debuglevel) {
		return;
	}

	va_start(ap, format);
	I2ErrLogVT(_errorhandler_nl,-1,0,format,ap);
	va_end(ap);
}

/**
 * Method to replace certain characters in received messages with ones that are quoted, so that
 * the content is explicitly legible.
 *
 * @param line string containing characters to be replaced
 * @param line_size length of the string to be replaced
 * @param output_buf output buffer
 * @param output_buf_size size of buffer to write output to
 * @return number or characters written to the output buffer
 * */

int quote_delimiters(char *line, int line_size, char *output_buf,
		int output_buf_size) {
	static quoted[4][2] = { { '\n', 'n' }, { '"', '"' }, { '\0', '0' }, { '\\',
			'\\' }, };
	char quote_char = '\\';

	int i, j, k;
	int match;

	for (i = j = 0; i < line_size && j < output_buf_size - 1; i++) {
		// find any matching characters among the quoted
		int match = 0;
		for (k = 0; k < 4; k++) {
			if (line[i] == quoted[k][0]) {
				output_buf[j] = quote_char;
				output_buf[j + 1] = quoted[k][1];
				j += 2;
				match = 1;
				break;
			}
		}

		if (match == 0) {
			output_buf[j] = line[i];
			j++;
		}
	}

	output_buf[j] = '\0'; // make sure it's null-terminated
	log_println(8, "****Received=%s; len=%d; dest=%d; MSG=%s", line, line_size,
			output_buf_size, output_buf);

	return j - 1;
}


/**
 * Log in a single key-value pair as a particular event
 *
 * In future, based on need, this may be expanded to log
 * in a list of key-value pairs
 * @param key string key
 * @param value string value associated with this key
 */
void protolog_printgeneric(const char* key, const char* value) {
	FILE * fp;
	char isotime[64];

	char logmessage[4096]; /* 4096 is just a random default buffer size for the protocol message
	 Ideally, twice the messsage size will suffice */

	if (!enableprotologging) {
		log_println(5, "Protocol logging is not enabled");
		return;
	}

	// make delimiters in message payload explicit
	quote_delimiters(value, strlen(value), logmessage, sizeof(logmessage));

	fp = fopen(get_protologfile(), "a");
	if (fp == NULL) {
		printf(
				"--Unable to open proto file while trying to record key-vale: %s:%s \n",
				key, value);
		log_println(0,
				"--Unable to open proto file while trying to record msg: %s \n",
				key, value);
	} else {
		fprintf(fp, " event=\"%s\", name=\"%s\", time=\"%s\"\n", key, value,
				get_ISOtime(isotime, sizeof(isotime)));
		printf("%s = \"%s\" \n", key, logmessage);
		fclose(fp);
	}
}

/**
 * Logs a protocol message specifically indicating the start/end or other status of tests.
 *
 * @param lvl Level of the message
 * @param testid enumerator indicating name of the test @see TEST_ID
 * @param pid PID of process
 * @param teststatus enumerator indicating test status. @see TEST_STATUS_INT
 *
 */
void protolog_status(int pid, enum TEST_ID testid,
		enum TEST_STATUS_INT teststatus) {
	FILE * fp;
	va_list ap;
	char protomessage[256];
	char currenttestarr[TEST_NAME_DESC_SIZE];
	char currentstatusarr[TEST_STATUS_DESC_SIZE];
	char isotime[64];
	char *currenttestname = "";
	char *teststatusdesc = "";

	//get descriptive strings for test name and status
	currenttestname = get_testnamedesc(testid, currenttestarr);
	teststatusdesc = get_teststatusdesc(teststatus, currentstatusarr);

	if (!enableprotologging) {
		log_println(5, "Protocol logging is not enabled");
		return;
	}

	fp = fopen(get_protologfile(), "a");
	if (fp == NULL) {
		printf(
				"--Unable to open protocol log file while trying to record test status message: %s for the %s test \n",
				teststatusdesc, currenttestname);
		log_println(
				0,
				"--Unable to open protocol log file while trying to record test status message: %s for the %s test \n",
				teststatusdesc, currenttestname);
	} else {
		sprintf(protomessage,
				" event=\"%s\", name=\"%s\", pid=\"%d\", time=\"%s\"\n",
				teststatusdesc, currenttestname, pid,
				get_ISOtime(isotime, sizeof(isotime)));
		printf("%s: <-- %d - %s - %s --> \n ", protomessage, pid,
				teststatusdesc, currenttestname);
		fprintf(fp, "%s", protomessage);
		fclose(fp);
	}
}

/**
 * Logs a protocol message specifically indicating the start/end or other status of processes.
 * This method is different from the protolog_status in that this logs in status/progress of generic
 * processes.
 * @param pid PID of process
 * @param testidarg enumerator indicating name of the test @see TEST_ID
 * @param procidarg enumerator indicating name of the test @see PROCESS_TYPE_INT
 * @param teststatusarg enumerator indicating test status. @see TEST_STATUS_INT
 *
 */
void protolog_procstatus(int pid, enum TEST_ID testidarg,
		enum PROCESS_TYPE_INT procidarg, enum PROCESS_STATUS_INT teststatusarg) {
	FILE * fp;
	char protomessage[256];
	char isotime[64];
	char currentprocarr[TEST_NAME_DESC_SIZE]; // size suffices to describe process name name too
	char currentstatusarr[PROCESS_STATUS_DESC_SIZE];
	char currenttestarr[TEST_NAME_DESC_SIZE];

	char *currentprocname = "";
	char *procstatusdesc = "";
	char *currenttestname = "";

	//get descriptive strings for test name and status
	currenttestname = get_testnamedesc(testidarg, currenttestarr);
	currentprocname = get_processtypedesc(procidarg, currentprocarr);
	procstatusdesc = get_procstatusdesc(teststatusarg, currentstatusarr);

	if (!enableprotologging) {
		log_println(5, "Protocol logging is not enabled");
		return;
	}

	fp = fopen(get_protologfile(), "a");
	if (fp == NULL) {
		printf(
				"--Unable to open protocol log file while trying to record process status message: %s for the %s test \n",
				procstatusdesc, currentprocname);
		log_println(
				0,
				"--Unable to open protocol log file while trying to record process status message: %s for the %s test \n",
				procstatusdesc, currentprocname);
	} else {
		sprintf(
				protomessage,
				" event=\"%s\", name=\"%s\", test=\"%s\", pid=\"%d\", time=\"%s\"\n",
				procstatusdesc, currentprocname, currenttestname, pid,
				get_ISOtime(isotime, sizeof(isotime)));
		printf("%s: -- %d - %s - %s - %s -- \n ", protomessage, pid,
				currenttestname, procstatusdesc, currentprocname);
		fprintf(fp, "%s", protomessage);
		fclose(fp);
	}
}

/**
 * Enable protocol logging
 */
void enableprotocollogging() {
	enableprotologging = 1;
}

/** Log all send/receive protocol messages.
 *  This method currently is called only internally, and thus
 *  does not check for whether protocol logging is enabled
 * @param lvl Level of the message
 * @param *msgdirection Direction of msg (S->C, C->S)
 * @param type message type
 * @param *msg Actual message
 * @param len Message length
 * @param processid PID of process
 * @param ctlSocket socket over which message has been exchanged
 * */
void protolog_println(char *msgdirection, const int type, void* msg,
		const int len, const int processid, const int ctlSocket) {
	FILE * fp;
	//char currentdrcnarr[TEST_DIRN_DESC_SIZE];
	char msgtypedescarr[MSG_TYPE_DESC_SIZE];
	char *currenttestname, *currentmsgtype;
	char isotime[64];
	char logmessage[4096]; // message after replacing delimiter characters

	// get descriptive strings for test name and direction
	currenttestname = get_currenttestdesc();
	currentmsgtype = get_msgtypedesc(type, msgtypedescarr);

	// make delimiters in message payload explicit
	quote_delimiters(msg, len, logmessage, sizeof(logmessage));

	fp = fopen(get_protologfile(), "a");
	if (fp == NULL) {
		log_println(
				5,
				"Unable to open protocol log file '%s', continuing on without logging",
				get_protologfile());
	} else {

		fprintf(
				fp,
				" event=\"message\", direction=\"%s\", test=\"%s\", type=\"%s\", len=\"%d\", msg=\"%s\", pid=\"%d\", socket=\"%d\", time=\"%s\"\n",
				msgdirection, currenttestname, currentmsgtype, len, logmessage,
				processid, ctlSocket, get_ISOtime(isotime, sizeof(isotime)));
		printf(
				"direction = %s, test= %s, type=%s, len=%d, msg=%s, pid=%d, socket=%d, time=%s\n",
				msgdirection, currenttestname, currentmsgtype, len, logmessage,
				processid, ctlSocket, get_ISOtime(isotime, sizeof(isotime)));
		fclose(fp);
	}
}

/** Log "sent" protocol messages.
 * Picks up the "send" direction and calls the generic protocol log method.
 * @param lvl Level of the message
 * @param type message type
 * @param *msg Actual message
 * @param len Message length
 * @param processid PID of process
 * @param ctlSocket socket over which message has been exchanged
 * */
//void protolog_sendprintln (int lvl, const int type, void* msg, const int len, const int processid, const int ctlSocket) {
void protolog_sendprintln(const int type, void* msg, const int len,
		const int processid, const int ctlSocket) {
	char *currentDir;

	if (!enableprotologging) {
		log_println(0, "Protocol logging is not enabled");
		return;
	}
	currentDir = get_currentdirndesc();

	protolog_println(currentDir, type, msg, len, processid, ctlSocket);
}

/**
 * Log all received protocol messages.
 * Picks up the "receive" direction and calls the generic protocol log method.
 * @param lvl Level of the message
 * @param type message type
 * @param *msg Actual message
 * @param len Message length
 * @param processid PID of process
 * @param ctlSocket socket over which message has been exchanged
 * */
void protolog_rcvprintln(const int type, void* msg, const int len,
		const int processid, const int ctlSocket) {
	char *otherDir;
	if (!enableprotologging) {
		log_println(0, "Protocol logging is not enabled");
		return;
	}
	otherDir = get_otherdirndesc();
	//protolog_println(lvl, otherDir, type, msg, len, processid, ctlSocket);
	protolog_println(otherDir, type, msg, len, processid, ctlSocket);
}

/**
 * Set the timestamp to actual time.
 */
void set_timestamp() {
	struct timeval tv;

	gettimeofday(&tv, NULL);
	timestamp = tv.tv_sec;
	utimestamp = tv.tv_usec;

	/*  Changed function to use gettimeofday() need usec value for ISO8601 file names
	 *  RAC 5/6/09
	 *  timestamp = time(NULL);
	 */

}

/**
 * Return the previously recorded timestamp.
 * @return  timestamp
 */
time_t get_timestamp() {
	return timestamp;
}

/**
 * Return the previously recorded utimestamp.
 * @return The utimestamp
 */
long int get_utimestamp() {
	return utimestamp;
}

/**
 * Return a character string YYYY for the current year
 * Author: Rich Carlson - 6/29/09
 * @param Pointer to the string indicating the year
 */

void get_YYYY(char *year) {

	struct tm *result;
	time_t now;

	setenv("TZ", "UTC", 0);
	now = get_timestamp();
	result = gmtime(&now);

	sprintf(year, "%d", 1900 + result->tm_year);
}

/**
 * Return a character string MM for the current year
 * Author: Rich Carlson - 6/29/09
 * @param Pointer to the string indicating month
 */

void get_MM(char *month) {

	struct tm *result;
	time_t now;

	/* setenv("TZ", NULL, 0); */
	setenv("TZ", "UTC", 0);
	now = get_timestamp();
	result = gmtime(&now);

	if (1 + result->tm_mon < 10)
		sprintf(month, "0%d", 1 + result->tm_mon);
	else
		sprintf(month, "%d", 1 + result->tm_mon);
}

/**
 * Return a character string DD for the current year
 * Author: Rich Carlson - 6/29/09
 * @param Pointer to the string indicating day
 */

void get_DD(char *day) {

	struct tm *result;
	time_t now;

	setenv("TZ", "UTC", 0);
	now = get_timestamp();
	result = gmtime(&now);

	if (result->tm_mday < 10)
		sprintf(day, "0%d", result->tm_mday);
	else
		sprintf(day, "%d", result->tm_mday);
}

/** Return a character string in the ISO8601 time foramt
 * 		used to create snaplog and trace file names
 *
 * Author: Rich Carlson - 5/6/09
 * @param Pointer to the string indicating ISO time
 * @param character string with ISO time string.
 */

char *
get_ISOtime(char *isoTime, int isotimearrsize) {

	struct tm *result;
	time_t now;
	char tmpstr[16];

	setenv("TZ", "UTC", 0);
	now = get_timestamp();
	result = gmtime(&now);

	sprintf(isoTime, "%d", 1900 + result->tm_year);
	if (1 + result->tm_mon < 10)
		sprintf(tmpstr, "0%d", 1 + result->tm_mon);
	else
		sprintf(tmpstr, "%d", 1 + result->tm_mon);

	strlcat(isoTime, tmpstr, isotimearrsize);

	if (result->tm_mday < 10)
		sprintf(tmpstr, "0%d", result->tm_mday);
	else
		sprintf(tmpstr, "%d", result->tm_mday);

	strlcat(isoTime, tmpstr, isotimearrsize);

	if (result->tm_hour < 10)
		sprintf(tmpstr, "T0%d", result->tm_hour);
	else
		sprintf(tmpstr, "T%d", result->tm_hour);

	strlcat(isoTime, tmpstr, isotimearrsize);

	if (result->tm_min < 10)
		sprintf(tmpstr, ":0%d", result->tm_min);
	else
		sprintf(tmpstr, ":%d", result->tm_min);

	strlcat(isoTime, tmpstr, isotimearrsize);

	if (result->tm_sec < 10)
		sprintf(tmpstr, ":0%d", result->tm_sec);
	else
		sprintf(tmpstr, ":%d", result->tm_sec);

	strlcat(isoTime, tmpstr, isotimearrsize);

	sprintf(tmpstr, ".%ldZ", get_utimestamp() * 1000);

	strlcat(isoTime, tmpstr, isotimearrsize);
	return isoTime;
}

/**
 * Write meta data out to log file.  This file contains details and
 * names of the other log files.
 * @param compress integer flag indicating whether log file compression
 * 			is enabled
 * @param cputime integer flag indicating if cputime trace logging is on
 * @param snaplog integer flag indicating if snaplogging is enabled
 * @param tcpdump integer flag indicating if tcpdump trace logging is on
 *
 * RAC 7/7/09
 */

void writeMeta(int compress, int cputime, int snaplog, int tcpdump) {
	FILE * fp;
	char tmpstr[256], dir[128], tmp2str[256];
	char isoTime[64], filename[256];
	size_t tmpstrlen = sizeof(tmpstr);
	socklen_t len;
	DIR *dp;
	char metafilesuffix[256] = "meta";

	/* Get the clients domain name and same in metadata file
	 * changed to use getnameinfo 7/24/09
	 * RAC 7/7/09
	 */

	// get socketaddr size based on whether IPv6/IPV4 address was used
#ifdef AF_INET6
	if (meta.family == AF_INET6)
	len = sizeof(struct sockaddr_in6);
#endif
	if (meta.family == AF_INET)
		len = sizeof(struct sockaddr_in);

	// Look up the host name and service name information for given struct sockaddress and length
	if (getnameinfo((struct sockaddr *) &meta.c_addr, len, tmpstr, tmpstrlen,
			NULL, 0, NI_NAMEREQD)) {
		// No fully qualified domain name
		memcpy(meta.client_name, "No FQDN name", 12);
	} else {
		// copy client's hostname into meta structure
		log_println(2, "extracting hostname %s", tmpstr);
		memcpy(meta.client_name, tmpstr, strlen(tmpstr));
	}

	// reset tmpstr
	memset(tmpstr, 0, tmpstrlen);

	// Create metadata file
	create_client_logdir((struct sockaddr *) &meta.c_addr, len, tmpstr,
			sizeof(tmpstr), metafilesuffix, sizeof(metafilesuffix));

	log_println(6, "Should compress snaplog and tcpdump files compress=%d",
			compress);
	/* #ifdef HAVE_ZLIB_H */

	// If compression is enabled, compress files in the "log" directory
	if (compress == 1) {
		log_println(5,
				"Compression is enabled, compress all files in '%s' basedir",
				tmp2str);
		if (snaplog) { // if snaplog is enabled, compress those into .gz formats

			// Try compressing C->S test snaplogs
			memset(filename, 0, 256);
			sprintf(filename, "%s/%s", tmp2str, meta.c2s_snaplog);
			if (zlib_def(filename) != 0)
				log_println(5, "compression failed for file:%s: %s.", filename,
						tmp2str);
			else
				strlcat(meta.c2s_snaplog, ".gz", sizeof(meta.c2s_snaplog));

			// Try compressing S->C test snaplogs
			memset(filename, 0, 256);
			sprintf(filename, "%s/%s", tmp2str, meta.s2c_snaplog);
			if (zlib_def(filename) != 0)
				log_println(5, "compression failed for file :%s", filename);
			else
				strlcat(meta.s2c_snaplog, ".gz", sizeof(meta.s2c_snaplog));
		}

		// If tcpdump file writing is enabled, compress those
		if (tcpdump) {

			// Try compressing C->S test tcpdump.
			// The tcpdump file extension is as specified in the "meta" data-structure
			memset(filename, 0, 256);
			sprintf(filename, "%s/%s", tmp2str, meta.c2s_ndttrace);
			if (zlib_def(filename) != 0)
				log_println(5, "compression failed for tcpdump file %s =%s",
						filename, meta.c2s_ndttrace);
			else
				strlcat(meta.c2s_ndttrace, ".gz", sizeof(meta.c2s_ndttrace));

			// Try compressing S->C test tcpdumps
			memset(filename, 0, 256);
			sprintf(filename, "%s/%s", tmp2str, meta.s2c_ndttrace);
			if (zlib_def(filename) != 0)
				log_println(5, "compression failed for tcpdump file %s =%s",
						filename, meta.s2c_ndttrace);
			else
				strlcat(meta.s2c_ndttrace, ".gz", sizeof(meta.s2c_ndttrace));
		}

		// If writing "cputime" file is enabled, compress those log files too
		if (cputime) {
			memset(filename, 0, 256);
			sprintf(filename, "%s/%s", tmp2str, meta.CPU_time);
			if (zlib_def(filename) != 0)
				log_println(5, "compression failed");
			else
				strlcat(meta.CPU_time, ".gz", sizeof(meta.CPU_time));
		} else
			log_println(
					5,
					"Zlib compression disabled, log files will not be compressed");
	}
	/* #endif */

	// Try logging metadata into the metadata logfile
	fp = fopen(tmpstr, "w");
	if (fp == NULL) { // error, unable to open file in write mode
		log_println(
				1,
				"Unable to open metadata log file, continuing on without logging");
	} else {
		log_println(5, "Opened '%s' metadata log file", tmpstr);
		fprintf(fp, "Date/Time: %s\n", meta.date);
		fprintf(fp, "c2s_snaplog file: %s\n", meta.c2s_snaplog);
		fprintf(fp, "c2s_ndttrace file: %s\n", meta.c2s_ndttrace);
		fprintf(fp, "s2c_snaplog file: %s\n", meta.s2c_snaplog);
		fprintf(fp, "s2c_ndttrace file: %s\n", meta.s2c_ndttrace);
		fprintf(fp, "cputime file: %s\n", meta.CPU_time);
		fprintf(fp, "server IP address: %s\n", meta.server_ip);
		fprintf(fp, "server hostname: %s\n", meta.server_name);
		fprintf(fp, "server kernel version: %s\n", meta.server_os);
		fprintf(fp, "client IP address: %s\n", meta.client_ip);
		fprintf(fp, "client hostname: %s\n", meta.client_name);
		fprintf(fp, "client OS name: %s\n", meta.client_os);
		fprintf(fp, "client_browser name: %s\n", meta.client_browser);
		fprintf(fp, "Summary data: %s\n", meta.summary);
		if (meta.additional) {
			fprintf(fp, " * Additional data:\n");
			struct metaentry* entry = meta.additional;
			while (entry) {
				fprintf(fp, "%s: %s\n", entry->key, entry->value);
				entry = entry->next;
			}
		}
		fclose(fp);
	}
}


/** Create directories for snap/tcp trace log files, and meta files.
 *
 *
 * The log file location, and name themselves record the year, month, date,
 * time and client name.
 *
 * The "primary" location for this log file is obtained as a command line option
 * from the user, or is the default location of BASE_DIR+LOG_DIR of the NDT installation
 *
 * @param direnamedestarg        location to store final directory name
 * @param destnamearrsize        Size of dest name string
 * @param finalsuffix            string constant suffix (C2S/S2C IP:port.ndttrace etc)
 *
 *
 */
void create_named_logdir(char *dirnamedestarg, int destnamearrsize,
		char *finalsuffix) {

	char namebuf[256];
	size_t namebuflen = 255;
	char dir[128];
	DIR *dp;

	strlcpy(dirnamedestarg, DataDirName, destnamearrsize);
	if ((dp = opendir(dirnamedestarg)) == NULL && errno == ENOENT)
		mkdir(dirnamedestarg, 0755);
	closedir(dp);
	get_YYYY(dir);

	strlcat(dirnamedestarg, dir, destnamearrsize);
	if ((dp = opendir(dirnamedestarg)) == NULL && errno == ENOENT)
		mkdir(dirnamedestarg, 0755);
	closedir(dp);

	strlcat(dirnamedestarg, "/", destnamearrsize);
	get_MM(dir);

	strlcat(dirnamedestarg, dir, destnamearrsize);
	if ((dp = opendir(dirnamedestarg)) == NULL && errno == ENOENT)
		mkdir(dirnamedestarg, 0755);
	closedir(dp);

	strlcat(dirnamedestarg, "/", destnamearrsize);
	get_DD(dir);

	strlcat(dirnamedestarg, dir, destnamearrsize);
	if ((dp = opendir(dirnamedestarg)) == NULL && errno == ENOENT)
		mkdir(dirnamedestarg, 0755);
	closedir(dp);

	strlcat(dirnamedestarg, "/", destnamearrsize);
	sprintf(dir, "%s", finalsuffix);
	strlcat(dirnamedestarg, dir, destnamearrsize);
log_println(0,"end named_log_create %s", dirnamedestarg);
}

/** Create directories for snap/tcp trace log files, and meta files.
 * Gets the endpoint name and port details and then builds the logfile name.
 * Calls the create_named_logdir(..) method with the endpoint details.
 *
 * @param namebufarg             string containing ip address/name of client
 * @param socketaddrarg          string containing socket address
 * @param direnamedestarg        location to store final directory name
 * @param destnamearrsize        Size of dest name string
 * @param finalsuffix            string constant suffix indicating C2S/S2c etc
 * @param finalsuffixsize        string constant suffix indicating C2S/S2c etc
 */
void create_client_logdir(struct sockaddr *cliaddrarg, socklen_t clilenarg,
		char *dirnamedestarg, int destnamearrsize, char *finalsuffix,
		int finalsuffixsize) {
	char namebuf[256];
	size_t namebuflen = 255;
	char dir[128];
	DIR *dp;
	char isoTime[64];
	char *socketaddrport;

	I2Addr sockAddr = I2AddrBySAddr(get_errhandle(), cliaddrarg, clilenarg, 0,
			0);
	memset(namebuf, 0, 256);
	I2AddrNodeName(sockAddr, namebuf, &namebuflen);
	socketaddrport = I2AddrPort(sockAddr);

	sprintf(dir, "%s_%s:%d.%s", get_ISOtime(isoTime, sizeof(isoTime)), namebuf,
			socketaddrport, finalsuffix);
	strlcpy(finalsuffix, dir, finalsuffixsize);

	create_named_logdir(dirnamedestarg, destnamearrsize, finalsuffix);

	I2AddrFree(sockAddr);

}
