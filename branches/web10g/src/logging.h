/**
 * This file contains the function declarations of the logging
 * system.
 *
 * Jakub S�awi�ski 2006-06-14
 * jeremian@poczta.fm
 */

#ifndef _JS_LOGGING_H
#define _JS_LOGGING_H

#include <I2util/util.h>
#include <dirent.h>
#include <sys/stat.h>
#include <sys/types.h>
#include "ndtptestconstants.h"
#include "runningtest.h" // protocol validation
#define LOGFILE "web100srv.log"   /* Name of log file */
#define PROTOLOGFILE "web100srvprotocol.log"   /* Name of protocol validation log file */
#define PROTOLOGPREFIX "web100srvprotocol_" /* prefix for protocol validation log file */
#define PROTOLOGSUFFIX ".log" /* suffix for protocol validation log file */
#define FILENAME_SIZE 256	// This could have been in utils.h, but will be used immediately here
void log_init(char* progname, int debuglvl);
void set_debuglvl(int debuglvl);
void set_logfile(char* filename);
int get_debuglvl();
char* get_logfile();

I2ErrHandle get_errhandle();
void log_print(int lvl, const char* format, ...);
void log_println(int lvl, const char* format, ...);
void log_free(void);
void set_timestamp();
time_t get_timestamp();
long int get_utimestamp();
char * get_ISOtime(char * isoTime, int isoTimeArrSize);
char *get_currenttime(char *isoTime, int isotimearrsize);
void get_YYYY(char * year);
void get_MM(char * month);
void get_DD(char * day);
char * DataDirName;

int zlib_def(char *src_fn);

/**
 * Format used to exchange meta test data between client->server.
 * */
struct metaentry {
	char key[64]; // key name
	char value[256]; // value associated with this meta key
	struct metaentry* next; // pointer to next link
};

/**
 * Used to save results of meta tests.
 * These values (most) are thes logged in the
 *  meta data file created  for every session
 * */
struct metadata {
	char c2s_snaplog[FILENAME_SIZE]; // C->S test Snaplog file name, changed to 256 to avoid truncation
	char c2s_ndttrace[FILENAME_SIZE]; // C->S NDT trace file name, changed to 256 to avoid truncation
	char s2c_snaplog[FILENAME_SIZE]; // S->C test Snaplog file name, changed to 256 to avoid truncation
	char s2c_ndttrace[FILENAME_SIZE]; // S->C NDT trace file name, changed to 256 to avoid truncation
	char CPU_time[FILENAME_SIZE]; // CPU time file
	char summary[256]; // Summary data
	char date[32]; // Date and,
	char time[16]; // time
	char client_ip[64]; // Client IP Address
	struct sockaddr_storage c_addr; // client socket details, not logged
	char client_name[64]; // client's host-name
	char client_os[32]; // client OS name
	char client_browser[32]; // client's browser name
	int ctl_port; // ctl port
	char server_ip[64]; // server IP address
	char server_name[64]; // server's host-name
	char server_os[32]; // server os name
	int family; // IP family
	struct metaentry* additional; // all other additional data
};

void set_protologdir(char* dirname);
void set_protologfile(char* client_ip, char *protologfileparam);
char*
get_protologfile(int socketNum, char *protologfilename) ;
char* get_protologdir();
void enableprotocollogging();
char *createprotologfilename(char* client_ip, char* textappendarg);
void create_named_logdir(char *dirnamedestarg, int destnamearrsize,
		char *finalsuffix);
void create_client_logdir(struct sockaddr *cliaddrarg, socklen_t clilenarg,
		char *dirnamedestarg, int destnamearrsize, char *finalsuffix,
		int finalsuffixsize);
void log_linkspeed(int index);

void protolog_printgeneric(const char* key, const char* val,int socketnum);
void protolog_status(int pid, enum TEST_ID testid,
		enum TEST_STATUS_INT teststatus,int socketnum);
void protolog_sendprintln(const int type, void* msg, const int len,
		const int processid, const int ctlSocket);
void protolog_rcvprintln(const int type, void* msg, const int len,
		const int processid, const int ctlSocket);
void protolog_procstatus(int pid, enum TEST_ID testidarg,
		enum PROCESS_TYPE_INT procidarg, enum PROCESS_STATUS_INT teststatusarg, int socketnum);
void protolog_procstatuslog(int pid, enum TEST_ID testidarg,
		enum PROCESS_TYPE_INT procidarg, enum PROCESS_STATUS_INT teststatusarg, int socketnum);

struct metadata meta;
#endif
