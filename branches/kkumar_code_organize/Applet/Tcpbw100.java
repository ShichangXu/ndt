import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

/*
Copyright 2003 University of Chicago.  All rights reserved.
The Web100 Network Diagnostic Tool (NDT) is distributed subject to
the following license conditions:
SOFTWARE LICENSE AGREEMENT
Software: Web100 Network Diagnostic Tool (NDT)

1. The "Software", below, refers to the Web100 Network Diagnostic Tool (NDT)
(in either source code, or binary form and accompanying documentation). Each
licensee is addressed as "you" or "Licensee."

2. The copyright holder shown above hereby grants Licensee a royalty-free
nonexclusive license, subject to the limitations stated herein and U.S. Government
license rights.

3. You may modify and make a copy or copies of the Software for use within your
organization, if you meet the following conditions: 
    a. Copies in source code must include the copyright notice and this Software
    License Agreement.
    b. Copies in binary form must include the copyright notice and this Software
    License Agreement in the documentation and/or other materials provided with the copy.

4. You may make a copy, or modify a copy or copies of the Software or any
portion of it, thus forming a work based on the Software, and distribute copies
outside your organization, if you meet all of the following conditions: 
    a. Copies in source code must include the copyright notice and this
    Software License Agreement;
    b. Copies in binary form must include the copyright notice and this
    Software License Agreement in the documentation and/or other materials
    provided with the copy;
    c. Modified copies and works based on the Software must carry prominent
    notices stating that you changed specified portions of the Software.

5. Portions of the Software resulted from work developed under a U.S. Government
contract and are subject to the following license: the Government is granted
for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable
worldwide license in this computer software to reproduce, prepare derivative
works, and perform publicly and display publicly.

6. WARRANTY DISCLAIMER. THE SOFTWARE IS SUPPLIED "AS IS" WITHOUT WARRANTY
OF ANY KIND. THE COPYRIGHT HOLDER, THE UNITED STATES, THE UNITED STATES
DEPARTMENT OF ENERGY, AND THEIR EMPLOYEES: (1) DISCLAIM ANY WARRANTIES,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO ANY IMPLIED WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE OR NON-INFRINGEMENT,
(2) DO NOT ASSUME ANY LEGAL LIABILITY OR RESPONSIBILITY FOR THE ACCURACY,
COMPLETENESS, OR USEFULNESS OF THE SOFTWARE, (3) DO NOT REPRESENT THAT USE
OF THE SOFTWARE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS, (4) DO NOT WARRANT
THAT THE SOFTWARE WILL FUNCTION UNINTERRUPTED, THAT IT IS ERROR-FREE OR THAT
ANY ERRORS WILL BE CORRECTED.

7. LIMITATION OF LIABILITY. IN NO EVENT WILL THE COPYRIGHT HOLDER, THE
UNITED STATES, THE UNITED STATES DEPARTMENT OF ENERGY, OR THEIR EMPLOYEES:
BE LIABLE FOR ANY INDIRECT, INCIDENTAL, CONSEQUENTIAL, SPECIAL OR PUNITIVE
DAMAGES OF ANY KIND OR NATURE, INCLUDING BUT NOT LIMITED TO LOSS OF PROFITS
OR LOSS OF DATA, FOR ANY REASON WHATSOEVER, WHETHER SUCH LIABILITY IS ASSERTED
ON THE BASIS OF CONTRACT, TORT (INCLUDING NEGLIGENCE OR STRICT LIABILITY), OR
OTHERWISE, EVEN IF ANY OF SAID PARTIES HAS BEEN WARNED OF THE POSSIBILITY OF
SUCH LOSS OR DAMAGES.
The Software was developed at least in part by the University of Chicago,
as Operator of Argonne National Laboratory (http://miranda.ctd.anl.gov:7123/). 
 */




 /* 
  * Naming convention used: Hungarian, with the following details
 * _VarName: Instance variables 
 * __Varname: Static variables (instead of c_VarName to reduce length)
 * iVarName: Integer variable 
 * sVarName: String variable
 * bVarName: boolean variable
 * dVarName: double variable
 * _iaVarName: Integer "Array"  variable
 * ...and some other self descriptive examples are..
 * _rscBundleMessages : class scoped ResourceBundle Variable called "Messages"
 * _cmboboxIpV6 : Class scoped combo-box variable to indicate IpV6 choice..
 * 
 * Some variables which were called "pub_xxx" are declared to have "accessor" methods for use by
 * other clients. I have left this untouched. These are private variables. Though the type is
 * not evident from the declaration immediately, the "getter/setter" methods for them will immediately
 * indicate their types
 * 
 */
 
 /**
  * Main Applet class that creates UI, defines tests and interprets results
  * from the tests.
  * 
  * */
public class Tcpbw100 extends JApplet implements ActionListener    {


	/**
	 * Compiler generated UID that does not follow naming convention,
	 * and is not related to functionality itself
	 */
	private static final long serialVersionUID = -2030725073538492542L;
	
	JTextArea _txtDiagnosis, _txtStatistics;
	ResultsTextPane _resultsTxtPane;
	//String inresult, outresult; //comment out unused variables
	String _sErrMsg;
	JButton _buttonStartTest;
	//TODO: Could use just one button for dismiss and copy. For later release
	JButton _buttonDismiss, _buttonStatsDismiss; 
	JButton _buttonCopy, _buttonStatsCopy;
	JButton _buttonDetails;
	JButton _buttonStatistics;
	JButton _buttonMailTo;
	JButton _buttonOptions;
	JCheckBox _chkboxDefaultTest, _chkboxPreferIPv6;
	JSpinner _spinnerTestCount = new JSpinner();
	String[] _saDelays = { "immediate", "1min","5mins","10mins","30mins","2hours","12hours","1day" };
	JComboBox _cmboboxDelay;

	boolean _bRandomize;
	boolean _bFailed, _bCanCopy;
	//URL location; //unused, hence commenting out
	NewFrame _frameWeb100Vars, _frameDetailedStats, _frameOptions;
	//String s; Unused, commenting out
	double _dTime;
	int _iECNEnabled, _iNagleEnabled, MSSSent, MSSRcvd;
	int _iSACKEnabled, _iTimestampsEnabled, _iWinScaleRcvd, _iWinScaleSent;
	int _iFastRetran, _iAckPktsOut, _iSmoothedRTT, _iCurrentCwnd, _iMaxCwnd;
	int _iSndLimTimeRwin, _iSndLimTimeCwnd, _iSndLimTimeSender;
	int _iSndLimTransRwin, _iSndLimTransCwnd, _iSndLimTransSender, _iMaxSsthresh;
	int _iSumRTT, _iCountRTT, _iCurrentMSS, _iTimeouts, _iPktsRetrans;
	int _iSACKsRcvd, _iDupAcksIn, _iMaxRwinRcvd, _iMaxRwinSent;
	int _iDataPktsOut, _iRcvbuf, _iSndbuf, _iAckPktsIn, _iDataBytesOut;
	int _iPktsOut, _iCongestionSignals, _iRcvWinScale;
	//int _iPkts, _iLength=8192, _iCurrentRTO; 
	int _iPkts, _iLength = NDTConstants.PREDEFINED_BUFFER_SIZE, _iCurrentRTO; 
	int _iC2sData, _iC2sAck, _iS2cData, _iS2cAck;
	// added for mailto url
	protected URL _targetURL;

	String _sEmailText;
	double _dS2cspd, _dC2sspd, _dSc2sspd, _dSs2cspd;
	int _iSsndqueue;
	double _dSbytes;

	/**
	 * Added by Martin Sandsmark, UNINETT AS
	 * Internationalization
	 */
	private Locale _localeObj;
	private ResourceBundle _resBundDisplayMsgs;
	private String _sLang="en";
	private String _sCountry="US";
	//private static String lang="nb";
	//private static String country="NO";
	/***/

	//these variables are self-explanatory. Do not follow naming convention, but left that way
	int half_duplex, congestion, bad_cable, mismatch;
	double mylink;
	double loss, estimate, avgrtt, spd, waitsec, timesec, rttsec;
	double order, rwintime, sendtime, cwndtime, rwin, swin, cwin;
	double aspd;
	//end naming convention-not-followed variables

	boolean _bIsApplication = false;
	boolean _bTestInProgress = false;
	String sHostName = null;
	String _sTestResults, _sMidBoxTestResult;
	byte _yTests = NDTConstants.TEST_MID | NDTConstants.TEST_C2S | NDTConstants.TEST_S2C | 
			NDTConstants.TEST_SFW | NDTConstants.TEST_STATUS | NDTConstants.TEST_META;
	int _iC2sSFWResult = NDTConstants.SFW_NOTTESTED;
	int _iS2cSFWResult = NDTConstants.SFW_NOTTESTED;
	
	/*************************************************************************
	 * JavaScript access API extension
	 * Added by Seth Peery and Gregory Wilson, Virginia Tech
	 * October 28, 2009
	 * This section adds classwide variables, written to at runtime, 
	 * which are then exposed by public accessor methods that can be called
	 * from web applications using NDT as a back-end.  
	 */

	//These variables are accessed by the setter/getter methods. While they do not follow naming convention,
	//  are left this way
	//  pub_c2sspd is assigned the value of _dC2sspd (declared above). the pub_xxx version seems to be used 
	//for making public to javascript. No other details known
	private double pub_c2sspd = 0.0;
	private double pub_s2cspd = 0.0;
	private int pub_CurRwinRcvd = 0; // source variable does not exist
	private int pub_MaxRwinRcvd = 0;
	private int  pub_MinRTT = 0; // source variable does not exist
	private int  pub_MaxRTT = 0; // source variable does not exist
	private double pub_loss = 0.0;
	private double pub_avgrtt = 0.0;
	//TODO:Getter/setter commented out for both below. Why?
	private int pub_MinRTO = 0; // source variable does not exist 
	private int pub_MaxRTO = 0; // source variable does not exist
	private int pub_CurRTO = 0;
	// private String pub_CWNDpeaks = ""; // source variable does not exist
	private int pub_SACKsRcvd = 0;
	private String pub_osVer = "unknown"; 
	private String pub_javaVer = "unknown";
	private String pub_host = "unknown";
	private String pub_osName = "unknown"; 
	private String pub_osArch = "unknown";
	private int pub_mismatch = 0;
	private int pub_Bad_cable = 0;
	private int pub_congestion = 0;
	private double pub_cwndtime = 0.0;
	private double pub_pctRcvrLimited = 0.0;
	private String pub_AccessTech = "unknown";
	private String pub_natBox = "unknown";
	private int pub_DupAcksOut = 0;
	private Date pub_TimeStamp;
	private String pub_isReady = new String("no");
	private String pub_clientIP = "unknown";
	private int pub_jitter = 0; //unused. TODO: find out use
	private int pub_Timeouts = 0;
	private String pub_errmsg = "Test not run.";
	private String pub_diagnosis = "Test not run.";
	private String pub_statistics = "Test not run.";
	private String pub_status = "notStarted";
	private double pub_time = 0.0;
	private int pub_bytes = 0;
	private String _sIsAutoRun;
	private String _sUserAgent = null;

	/** public static void main for invoking as an Application
	 * */
	public static void main(String[] args)
	{
		JFrame frame = new JFrame("ANL/Internet2 NDT (applet)");
		if (args.length != 1) {
			System.out.println("Usage: java -jar Tcpbw100.jar " + "HOST");
			System.exit(0);
		}
		final Tcpbw100 applet = new Tcpbw100();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				applet.destroy();
				System.exit(0);
			}
		});
		applet._bIsApplication = true;
		applet.sHostName = args[0];
		frame.getContentPane().add(applet);
		frame.setSize(700, 320);
		applet.init();
		applet.start();
		frame.setVisible(true);
	}

	
	/*
	 * Accessor methods for public variables
	 **/

	public String get_c2sspd()
	{
		// Expressed as MiB using base 10
		return Double.toString((pub_c2sspd));  
	}

	public String get_s2cspd()
	{
		// Expressed as MiB using base 10
		return Double.toString(pub_s2cspd);
	}

	public String get_CurRwinRcvd()
	{
		return Integer.toString(pub_CurRwinRcvd);
	}

	public String get_MaxRwinRcvd()
	{
		return Integer.toString(pub_MaxRwinRcvd);
	}

	public String get_Ping()
	{
		return Integer.toString(pub_MinRTT);
	} 	

	public String get_MaxRTT()
	{
		return Integer.toString(pub_MaxRTT);
	}  

	public String get_loss()
	{
		return Double.toString(pub_loss);
	}

	public String get_avgrtt()
	{
		return Double.toString(pub_avgrtt); 
	}

	/* public String get_MinRTO()
  {
    return pub_MinRTO;
  }*/

	/* public String get_MaxRTO()
  {
    return pub_MaxRTO;
  }*/

	public String get_CurRTO()
	{
		return Integer.toString(pub_CurRTO);
	}

	/*
  public String get_CWNDpeaks()
  {
    return pub_CWNDpeaks;
  } */

	public String get_SACKsRcvd()
	{
		return Integer.toString(pub_SACKsRcvd);
	}  

	public String get_osVer()
	{
		return pub_osVer;
	}

	public String get_javaVer()
	{
		return pub_javaVer;
	}

	public String get_host()
	{
		return pub_host;
	}

	public String get_osName()
	{
		return pub_osName;
	}

	public String get_osArch()
	{
		return pub_osArch;
	}

	public String get_mismatch()
	{
		String result;
		if (pub_mismatch==0) {
			result = "no";
		} else {
			result = "yes";
		}
		return result;
	}

	public String get_Bad_cable()
	{
		String result;
		if (pub_Bad_cable ==1) {
			result = "yes";
		} else {
			result = "no";
		}
		return result;
	}

	public String get_congestion()
	{
		String result;
		if (pub_congestion == 1) {
			result = "yes";
		} else {
			result = "no";
		}
		return result;
	}

	public String get_cwndtime()
	{
		return Double.toString(pub_cwndtime);
	}

	public String get_AccessTech()
	{
		return pub_AccessTech;
	}

	public String get_rcvrLimiting()
	{
		return Double.toString(pub_pctRcvrLimited);	
	}

	public String get_optimalRcvrBuffer()
	{
		return Integer.toString(pub_MaxRwinRcvd*1024);
	}

	public String get_clientIP()
	{
		return pub_clientIP;
	}

	public String get_natStatus()
	{
		return pub_natBox;
	}

	public String get_DupAcksOut()
	{
		return Integer.toString(pub_DupAcksOut);
	}

	public String get_TimeStamp()
	{
		String result = "unknown";
		if (pub_TimeStamp != null) {
			result =  pub_TimeStamp.toString();	
		}
		return result;
	}

	//commenting out unused method, but not removing in case of future use
	/* 
	public String isReady()
	{

		// if ((pub_isReady == null) || (pub_isReady.equals(""))) {
		//	 pub_isReady = "no";
		// }
		// String result = "foo";

		//if (failed) {
		//    pub_isReady = "failed1";
		//}
		//result = pub_isReady;
		//   return result; 
		return pub_isReady;
	}
	*/
	
	public String get_jitter()
	{
		return Integer.toString((pub_MaxRTT - pub_MinRTT));
	}

	public String get_WaitSec()
	{
		return Integer.toString((pub_CurRTO * pub_Timeouts)/1000);	
	}

	public String get_errmsg() 
	{
		//String result = "Test not run";
		//result = pub_errmsg;
		//return result;
		return pub_errmsg;
	}

	public String get_diagnosis()
	{
		return pub_diagnosis;
	} 

	public String get_statistics()
	{
		return pub_statistics;
	} 

	public String get_status()
	{
		return pub_status;
	} 

	public String get_instSpeed()
	{
		return Double.toString((8.0 * pub_bytes) /  (System.currentTimeMillis() - pub_time));
	}

	/** Set UserAgent String containing browser details.
	 * @return String UserAgent details set locally
	 * @see UserAgentTools 
	 * */
	public String getUserAgent() {
		return _sUserAgent;
	}

	/** Set UserAgent String.
	 * @param paramStrUserAgent UserAgent String to be set locally
	 * @see UserAgentTools 
	 * */
	public void setUserAgent(String paramStrUserAgent) {
		this._sUserAgent = paramStrUserAgent;
	}

    /**
     * Get Client->Server fire-wall test results.
     * @return integer indicating C->S test results
     * */
	public int getC2sSFWTestResults() {
		return this._iC2sSFWResult;
	}

    /**
     * Set Client->Server fire-wall test results.
     * @param iParamC2SRes integer indicating C->S test results
     * */
	public void setC2sSFWTestResults(int iParamC2SRes) {
		this._iC2sSFWResult = iParamC2SRes;
	}

	  /**
     * Get Server->Client fire-wall test results.
     * @return integer indicating C->S test results
     * */
	public int getS2cSFWTestResults() {
		return this._iS2cSFWResult;
	}

	/**
     * Set server->Client fire-wall test results.
     * @param iParamS2CRes integer indicating C->S test results
     * */
	public void setS2cSFWTestResults(int iParamS2CRes) {
		this._iS2cSFWResult = iParamS2CRes;
	}
	
	/*
    End of accessor methods
	 **/	
	
	/** Class to start tests in a thread.
	 *  Starts by disabling all buttons, and invokes the dottcp() method.
	 *  This thread is stopped when the number of tests that was configured to be
	 *  run have all completed, or if the user stops it by interrupting from the GUI.
	 *  Once the tests have been run, the buttons are enabled so that results can be 
	 *  viewed in detail. 
	 *  */
	class TestWorker implements Runnable {
		public void run() {
			if (!_bTestInProgress) {
				int testNo = 1;
				int testsNum = ((Integer)_spinnerTestCount.getValue()).intValue();
				_bTestInProgress = true;
				createDiagnoseWindow();
				createStatsWindow();
				_buttonStartTest.setEnabled(false);
				_buttonDetails.setEnabled(false);
				_buttonStatistics.setEnabled(false);
				_buttonMailTo.setEnabled(false);
				_buttonOptions.setEnabled(false);
				_spinnerTestCount.setEnabled(false);

				//StatusPanel sPanel = new StatusPanel(testsNum);
				// re-arch. Replaced above by the line below
				String sTempEnable = getParameter("enableMultipleTests");

				// create status panel based on whether multiple tests are enabled
				// If not, then the progress bar displays just the specific test (middlebox, C2S, firewall etc)
				// If yes, then the progress bar also shows the progress on the number of tests
				
				StatusPanel sPanel = new StatusPanel(testsNum, sTempEnable);
				getContentPane().add(BorderLayout.NORTH, sPanel);
				getContentPane().validate();
				getContentPane().repaint();

				try {
					while (true) {
						if (sPanel.wantToStop()) {
							break;
						}
						if (testsNum == 0) {
							_resultsTxtPane.append("\n** " + _resBundDisplayMsgs.getString("startingTest") + " " + testNo + " **\n");
						}
						else {
							_resultsTxtPane.append("\n** " + _resBundDisplayMsgs.getString("startingTest") + " " + testNo + " " + _resBundDisplayMsgs.getString("of") + " " + testsNum + " **\n");
						}
						dottcp(sPanel);
						// If test count scheduled is complete, quit
						if (testNo == testsNum) {
							break;
						}
						// If user stops the test, quit
						if (sPanel.wantToStop()) {
							break;
						}
						sPanel.setText("");
						sPanel.endTest();
						// increment test count
						testNo += 1;

						// This iteration of tests is now complete. Enable all buttons and output
						// so that user can view details of results
						_buttonDetails.setEnabled(true);
						_buttonStatistics.setEnabled(true);
						_buttonMailTo.setEnabled(true);
						_buttonOptions.setEnabled(true);
						_txtStatistics.append("\n** " + _resBundDisplayMsgs.getString("test") + " " + testNo + " **\n");
						_txtDiagnosis.append("\n** " + _resBundDisplayMsgs.getString("test") + " " + testNo + " **\n");

						// Now, sleep for some time based on user's choice before running the next
						// iteration of the test suite
						try {
							switch (_cmboboxDelay.getSelectedIndex()) {
							case 1:
								_resultsTxtPane.append("\n** " + _resBundDisplayMsgs.getString("sleep1m") + " **\n");
								Thread.sleep(1000 * 60);
								break;
							case 2:
								_resultsTxtPane.append("\n** " + _resBundDisplayMsgs.getString("sleep5m") + " **\n");
								Thread.sleep(1000 * 60 * 5);
								break;
							case 3:
								_resultsTxtPane.append("\n** " + _resBundDisplayMsgs.getString("sleep10m") + " **\n");
								Thread.sleep(1000 * 60 * 10);
								break;
							case 4:
								_resultsTxtPane.append("\n** " + _resBundDisplayMsgs.getString("sleep30m") + " **\n");
								Thread.sleep(1000 * 60 * 30);
								break;
							case 5:
								_resultsTxtPane.append("\n** " + _resBundDisplayMsgs.getString("sleep2h") + " **\n");
								Thread.sleep(1000 * 60 * 120);
								break;
							case 6:
								_resultsTxtPane.append("\n** " + _resBundDisplayMsgs.getString("sleep12h") + " **\n");
								Thread.sleep(1000 * 60 * 720);
								break;
							case 7:
								_resultsTxtPane.append("\n** " + _resBundDisplayMsgs.getString("sleep1d") + " **\n");
								Thread.sleep(1000 * 60 * 1440);
								break;
							}
						}
						catch (InterruptedException e) {
							// do nothing.
						}
					}
				} catch(IOException e) {
					e.printStackTrace();
					_bFailed=true;
					_sErrMsg = _resBundDisplayMsgs.getString("serverBusy30s") + "\n";
				}

				// If test failed due to any reason, mark failure reason too
				if (_bFailed) {
					_resultsTxtPane.append(_sErrMsg);

					pub_isReady = "failed";
					pub_errmsg = _sErrMsg;
				}

				// Enable all buttons. Continue activities to mark status as complete
				_buttonDetails.setEnabled(true);
				_buttonStatistics.setEnabled(true);
				_buttonMailTo.setEnabled(true);
				_buttonOptions.setEnabled(true);
				_spinnerTestCount.setEnabled(true);
				showStatus(_resBundDisplayMsgs.getString("done2"));
				_resultsTxtPane.append("\n" + _resBundDisplayMsgs.getString("clickStart2") + "\n");
				_buttonStartTest.setEnabled(true);
				_bTestInProgress = false;
				getContentPane().remove(sPanel);
				getContentPane().validate();
				getContentPane().repaint();
			}
		}
	} //end inner class

	
	/** 
	 * "Remote Control" function - invoke NDT' runtest() method from the API
	 */
	public void run_test()
	{
		// The Java security model considers calling a method that opens a socket
		// from JavaScript to be a privileged action.  By using 
		// java.security.privilegedAction here, we can grant JavaScript the 
		// same expanded privileges as the signed applet to open a socket.
		AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				pub_errmsg = "Test in progress.";
				runtest();
				return null;
			}
		});
	}


	/**
	 * Initialize the base NDT window Applet init() method
	 */
	public void init() {
		if (getParameter("country") != null)  {
			_sCountry = getParameter("country");
		}
		if (getParameter("language") != null) {
			_sLang = getParameter("language");
		}

		try {
			_localeObj = new Locale(_sLang, _sCountry);
			_resBundDisplayMsgs = ResourceBundle.getBundle("Tcpbw100_msgs",
					_localeObj);

			// Replaced method call to initialize _resBundDisplayMsgs for access
			// by class
			// NDTConstants.initConstants(locale);
			NDTConstants.initConstants(_sLang, _sCountry);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Error while loading language files:\n" + e.getMessage());
			e.printStackTrace();
		}

		// call method to create a window
		createMainWindow();

		// Autorun functionality
		_sIsAutoRun = getParameter("autoRun");
		if ((_sIsAutoRun != null) && _sIsAutoRun.equals("true")) {
			pub_errmsg = "Test in progress.";
			runtest();
		}

	}

	/** Initialize the "main" window.
	 * The main window is composed of
	 * 1. The results pane, which describes the process and displays their results
	 * 2. The buttons pane, which houses all the buttons for various options
	 * //TODO :Can we move this to SwingUtilies? Plan for later release - it does'nt help
		//with understanding the functionality itself
		 * 
		 * */	
	private void createMainWindow () {
		// set content manager 
		getContentPane().setLayout(new BorderLayout());

		// start with status set to "Ready" to perform tests
		showStatus(_resBundDisplayMsgs.getString("ready"));

		// initialize 
		_bFailed = false ;
		_bRandomize = false; //Seems unused after this. Retaining either way
		_bCanCopy = false;

		// Results panel
		_resultsTxtPane = new ResultsTextPane();
		_resultsTxtPane.append( NDTConstants.NDT_TITLE_STR + NDTConstants.VERSION + "\n");
		_resultsTxtPane.setEditable(false);
		getContentPane().add(new JScrollPane(_resultsTxtPane));
		_resultsTxtPane.append(_resBundDisplayMsgs.getString("clickStart") + "\n");

		// Panel too add all buttons
		Panel buttonsPanel = new Panel();

		// Add "start" button
		_buttonStartTest = new JButton(_resBundDisplayMsgs.getString("start"));
		_buttonStartTest.addActionListener(this);
		buttonsPanel.add(_buttonStartTest);

		// Add "statistics" button
		_buttonStatistics = new JButton(_resBundDisplayMsgs.getString("statistics"));
		_buttonStatistics.addActionListener(this);
		if (getParameter("disableStatistics") == null) {
			buttonsPanel.add(_buttonStatistics);
		}
		_buttonStatistics.setEnabled(false);

		// Add "Details" button
		_buttonDetails = new JButton(_resBundDisplayMsgs.getString("moreDetails"));
		_buttonDetails.addActionListener(this);
		if (getParameter("disableDetails") == null) {
			buttonsPanel.add(_buttonDetails);
		}
		_buttonDetails.setEnabled(false);

		// Add "Report problem" button 
		_buttonMailTo = new JButton(_resBundDisplayMsgs.getString("reportProblem"));
		_buttonMailTo.addActionListener(this);
		if (getParameter("disableMailto") == null) {
			buttonsPanel.add(_buttonMailTo);
		}
		_buttonMailTo.setEnabled(false);

		// Add "Options" button
		_buttonOptions = new JButton(_resBundDisplayMsgs.getString("options") + "...");
		_buttonOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_buttonOptions.setEnabled(false);
				// call the Options-window create code
				createOptionsWindow();
				_buttonOptions.setEnabled(true);
			}
		});
		// If disableOptions is not set, then add button
		if (getParameter("disableOptions") == null) {
			buttonsPanel.add(_buttonOptions);
		}

		// add buttons panel to the main window
		getContentPane().add(BorderLayout.SOUTH, buttonsPanel);

		// "Options" panel components
		// 1. Is IPv6 preferred?	
		_chkboxPreferIPv6 = new JCheckBox(_resBundDisplayMsgs.getString("preferIPv6"));
		_chkboxPreferIPv6.setSelected(true);
		// 2. Conduct default tests?
		_chkboxDefaultTest = new JCheckBox(_resBundDisplayMsgs.getString("defaultTests"));
		_chkboxDefaultTest.setSelected(true);
		_chkboxDefaultTest.setEnabled(false);
		// 3. configure number of tests
		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setMinimum(new Integer(0));
		model.setValue(new Integer(1));
		_spinnerTestCount.setModel(model);
		_spinnerTestCount.setPreferredSize(new Dimension(60, 20));
		_cmboboxDelay = new JComboBox();
		for (int i = 0; i < _saDelays.length; i++) {
			_cmboboxDelay.addItem(_resBundDisplayMsgs.getString(_saDelays[i]));
		}
		_cmboboxDelay.setSelectedIndex(0);

	} // createDiagnoseWindow() ends

	
	/** Create the "More details" window.
	 */	
	public void createDiagnoseWindow() {
		showStatus(_resBundDisplayMsgs.getString("getWeb100Var"));

		// create new frame 
		if (_frameWeb100Vars == null) {
			_frameWeb100Vars = new NewFrame();
		}
		
		// Get title for this window
		_frameWeb100Vars.setTitle(_resBundDisplayMsgs.getString("web100Var"));
		Panel buttons = new Panel();
		_frameWeb100Vars.getContentPane().add("South", buttons);

		// Add "close" button
		_buttonDismiss = new JButton(_resBundDisplayMsgs.getString("close"));
		_buttonDismiss.addActionListener(this);

		// Add "copy" button
		_buttonCopy = new JButton(_resBundDisplayMsgs.getString("copy"));
		_buttonCopy.addActionListener(this);

		// Create Text area for displaying results, add "Heading" 
		_txtDiagnosis = new JTextArea(_resBundDisplayMsgs.getString("web100KernelVar") + ":\n", 15,30);
		_txtDiagnosis.setEditable(true);
		_buttonDismiss.setEnabled(true);
		_buttonCopy.setEnabled(_bCanCopy);

		// Now place all the buttons
		buttons.add("West", _buttonDismiss);
		buttons.add("East", _buttonCopy);
		_frameWeb100Vars.getContentPane().add(new JScrollPane(_txtDiagnosis));
		_frameWeb100Vars.pack();
	}  // createDiagnoseWindow() ends


	/** Create the "Statistics" window.
	*/	
	public void createStatsWindow() {
		showStatus(_resBundDisplayMsgs.getString("printDetailedStats"));

		// create new frame
		if (_frameDetailedStats  == null) {
			_frameDetailedStats = new NewFrame();
		}
		_frameDetailedStats.setTitle(_resBundDisplayMsgs.getString("detailedStats"));
		
		// create panel for buttons
		Panel buttons = new Panel();
		_frameDetailedStats.getContentPane().add("South", buttons);

		// Button for "close"
		_buttonStatsDismiss = new JButton(_resBundDisplayMsgs.getString("close"));
		_buttonStatsDismiss.addActionListener(this);

		// Button for "copy"
		_buttonStatsCopy = new JButton(_resBundDisplayMsgs.getString("copy"));
		_buttonStatsCopy.addActionListener(this);

		// Text area for Statistics, add "heading"
		_txtStatistics = new JTextArea(_resBundDisplayMsgs.getString("web100Stats") + ":\n", 25,70);
		_txtStatistics.setEditable(true);
		_buttonStatsDismiss.setEnabled(true);
		_buttonStatsCopy.setEnabled(_bCanCopy);
		
		// Place all components
		buttons.add("West", _buttonStatsDismiss);
		buttons.add("East", _buttonStatsCopy);
		_frameDetailedStats.getContentPane().add(new JScrollPane(_txtStatistics));
		_frameDetailedStats.pack();
	}  // createStatsWindow()


	/** Create the "Options" window.
	 * The options displayed to the user are:
	 * <ul>
	 *<li>  Would you like to Perform default tests? </li>
	 *<li>  Would you prefer IPV6? </li>
	 * <li> Select number of times the test-suite is to be run </li>
	 * </ul>
	  */	
	public void createOptionsWindow() {
		showStatus(_resBundDisplayMsgs.getString("showOptions"));

		if (_frameOptions == null) {
			_frameOptions = new NewFrame();
			_frameOptions.setTitle(_resBundDisplayMsgs.getString("options"));

			// main panel
			JPanel optionsPanel = new JPanel();
			optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

			// Panel for displaying the "default tests" option
			JPanel testsPanel = new JPanel();
			testsPanel.setBorder(BorderFactory.createTitledBorder(_resBundDisplayMsgs.getString("performedTests")));
			testsPanel.add(_chkboxDefaultTest);
			optionsPanel.add(testsPanel);

			// Panel for displaying the IPV6 option
			JPanel protocolPanel = new JPanel();
			protocolPanel.setBorder(BorderFactory.createTitledBorder(_resBundDisplayMsgs.getString("ipProtocol")));
			protocolPanel.add(_chkboxPreferIPv6);
			optionsPanel.add(protocolPanel);

			
			// If user has enabled multiple tests, then build the panel, and add it
			// to the parent( options ) panel
			
			if (getParameter("enableMultipleTests") != null) {
				JPanel generalPanel = new JPanel();
				generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));
				generalPanel.setBorder(BorderFactory.createTitledBorder(_resBundDisplayMsgs.getString("general")));
				JPanel tmpPanel = new JPanel();
				tmpPanel.add(new JLabel(_resBundDisplayMsgs.getString("numberOfTests") + ":"));
				tmpPanel.add(_spinnerTestCount);
				generalPanel.add(tmpPanel);
				tmpPanel = new JPanel();
				tmpPanel.add(new JLabel(_resBundDisplayMsgs.getString("delayBetweenTests") + ":"));
				tmpPanel.add(_cmboboxDelay);
				generalPanel.add(tmpPanel);

				optionsPanel.add(generalPanel);
			}

			// Add options to the parent frame
			_frameOptions.getContentPane().add(optionsPanel);    

			// create and add panel containing buttons
			Panel buttonsPanel = new Panel();
			_frameOptions.getContentPane().add("South", buttonsPanel);

			JButton okButton= new JButton(_resBundDisplayMsgs.getString("ok"));
			okButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					_frameOptions.toBack();
					_frameOptions.dispose();
				}

			});

			// place buttons
			buttonsPanel.add("West", okButton);

			_frameOptions.pack();
		}
		_frameOptions.setResizable(false);
		_frameOptions.setVisible(true);
	}


	/** 
	 * Run the Thread that calls the "dottcp" method to run tests.
	 * This method is called by the Applet's init method if user selected an
	 * "autorun" option,  is run individually if user presses the "start button",
	 * or, is internally invoked by the API call.
	 */
	synchronized public void runtest() {
		pub_status = "notStarted";
		new Thread(new TestWorker()).start();
	}

	

	/** Action handler method called when an associated action is performed
	 * @param paramEventObj Event object that prompted the call
	 * */
	public void actionPerformed(ActionEvent paramEventObj) {
		Object source = paramEventObj.getSource();
		// System.err.println("Processing WINDOW event #" +event.getID());
		// System.err.println("Processing event " + source);

		// Start the test
		if (source == _buttonStartTest) {
			if(_frameWeb100Vars != null) {
				_frameWeb100Vars.toBack();
				_frameWeb100Vars.dispose();
				_frameWeb100Vars = null;
			}

			if(_frameDetailedStats != null) {
				_frameDetailedStats.toBack();
				_frameDetailedStats.dispose();
				_frameDetailedStats = null;
			}

			pub_errmsg = "Test in progress.";
			runtest();
		}
		// show details of tests since that button was clicked
		else if (source == _buttonDetails) {
			_buttonDetails.setEnabled(false);
			_frameWeb100Vars.setResizable(true);
			_frameWeb100Vars.setVisible(true);
			_buttonDetails.setEnabled(true);
		}
		// "More Details" Web100 variables window to be closed
		else if (source == _buttonDismiss) {
			_frameWeb100Vars.toBack();
			_frameWeb100Vars.dispose();
		}
		// "statistics" window to be closed
		else if (source == _buttonStatsDismiss) {
			_frameDetailedStats.toBack();
			_frameDetailedStats.dispose();
		}
		// "More details" copy button functionality
		else if (source == _buttonCopy) {
			try {
				Clipboard clipbd = getToolkit().getSystemClipboard();
				_bCanCopy = true;
				String s = _txtDiagnosis.getText();
				StringSelection ss = new StringSelection(s);
				clipbd.setContents(ss, ss);
				_txtDiagnosis.selectAll();
			} catch (SecurityException e) {
				_bCanCopy = false;
			}
		}
		// "Statistics" copy button functionality
		else if (source == _buttonStatsCopy) {
			Clipboard clipbd = getToolkit().getSystemClipboard();
			String sTemp = _txtStatistics.getText();
			StringSelection ssTemp = new StringSelection(sTemp);
			clipbd.setContents(ssTemp, ssTemp);
			_txtStatistics.selectAll();
		}
		// Show "statistics" window
		else if (source == _buttonStatistics) {
			_buttonStatistics.setEnabled(false);
			_frameDetailedStats.setResizable(true);
			_frameDetailedStats.setVisible(true);
			_buttonStatistics.setEnabled(true);
		}
		// mail to functionality
		else if (source == _buttonMailTo) {
			// int i; //did'nt need it
			// char key; comment out. seems unused
			// String to[], from[], comments[]; //commented out unused variables
			String sName, sHost;

			_buttonMailTo.setEnabled(false);
			// invoke mailto: function
			showStatus(_resBundDisplayMsgs.getString("invokingMailtoFunction") + "...");

			_resultsTxtPane.append(_resBundDisplayMsgs.getString("generatingReport") + "\n");
			try {
				// user 
				if ((sName = getParameter(NDTConstants.TARGET1)) == null) {
					throw new IllegalArgumentException("U parameter Required:");
				}
				// host name
				if ((sHost = getParameter(NDTConstants.TARGET2)) == null) {
					throw new IllegalArgumentException("H parameter Required:");
				}

				String theURL = "mailto:" + sName + "@" + sHost;
				String subject = getParameter("subject"); //get subject

				if (subject == null) {
					subject = _resBundDisplayMsgs.getString("troubleReportFrom") + " " + getCodeBase().getHost();
				}
				theURL += "?subject=" + subject;
				theURL += "&body=" + _resBundDisplayMsgs.getString("comments") + ":%0A%0A" + _sEmailText + " " + _resBundDisplayMsgs.getString("endOfEmail") + "\n%0A";
				// System.out.println("Message body is '" + emailText + "'\n");
				_targetURL = new URL(theURL);

			} catch (MalformedURLException rsi) {
				throw new IllegalArgumentException("Can't create mailto: URL" + rsi.getMessage());
			}

			getAppletContext().showDocument(_targetURL);
		} //end mail-to functionality
	}  // actionPerformed()

	/** Display current status in Applet window.
	 * @param msg String value of status
	 * */
	public void showStatus(String msg) {
		if (!_bIsApplication) {
			super.showStatus(msg);
		}
	}


	/** MiddleBox testing is a throughput test from the Server to the 
	 * Client to check for duplex mismatch conditions. It determines 
	 * if routers or switches in the path may be making changes to 
	 * some TCP parameters.
	 *
	 * @param paramProtoObj Protocol Object used to exchange messages
	 * @return boolean value indicating test failure status
	 * 	   true if test was not completed
	 *     false if test was completed
	 *     
	 * @throws IOException when sending/receiving messages from server fails
	 * @see Protocol#recv_msg(Message msgParam)
	 * @see Protocol#send_msg(byte bParamType, byte[] baParamTab)
	 *    These methods indicate more information about IOException
	 *     */

	public boolean test_mid(Protocol paramProtoObj) throws IOException {

		//byte buff[] = new byte[8192];	
		byte buff[] = new byte[NDTConstants.MIDDLEBOX_PREDEFINED_MSS];

		Message msg = new Message();
		
		if ((_yTests & NDTConstants.TEST_MID) == NDTConstants.TEST_MID) {
			
			// now look for middleboxes (firewalls, NATs, and other boxes that
			// muck with TCP's end-to-end principles
			
			showStatus(_resBundDisplayMsgs.getString("middleboxTest"));
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("checkingMiddleboxes") + "  ");
			_txtStatistics.append(_resBundDisplayMsgs.getString("checkingMiddleboxes") + "  ");
			_sEmailText = _resBundDisplayMsgs.getString("checkingMiddleboxes") + "  ";
			pub_status = "checkingMiddleboxes";

			// If reading socket was not successful, then declare middlebox test as a failure
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) {
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}

			// Initially, expecting a TEST_PREPARE message
			if (msg.getType() != MessageType.TEST_PREPARE) {
				_sErrMsg = _resBundDisplayMsgs.getString("mboxWrongMessage") + "\n";
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}
			
			// get port number that server wants client to bind to for this test
			int midport = Integer.parseInt(new String(msg.getBody()));

			// connect to server using port obtained above
			Socket midSrvrSockObj = null;
			try {
				midSrvrSockObj = new Socket(sHostName, midport);
			} catch (UnknownHostException e) {
				System.err.println("Don't know about host: " + sHostName);
				_sErrMsg = _resBundDisplayMsgs.getString("unknownServer") + "\n" ;
				return true;
			} catch (IOException e) {
				System.err.println("Couldn't perform middlebox testing to: " + sHostName);
				_sErrMsg = _resBundDisplayMsgs.getString("middleboxFail") + "\n" ;
				return true;
			}

			InputStream srvin2 = midSrvrSockObj.getInputStream();
			OutputStream srvout2 = midSrvrSockObj.getOutputStream();

			// int largewin = 128*1024; //Unused variable, commenting out until needed

			//Time out the socket after 6.5 seconds
			midSrvrSockObj.setSoTimeout(6500);
			int bytes = 0;
			int inlth;
			_dTime = System.currentTimeMillis();
			pub_TimeStamp = new Date();

			// Read server packets into the pre-initialized buffer.
			// Record number of packets read as reading completes
			
			try {  
				while ((inlth=srvin2.read(buff,0,buff.length)) > 0) {
					bytes += inlth;
					pub_bytes = bytes;
					// If more than 5.5 seconds have passed by, stop reading socket input
					if ((System.currentTimeMillis() - _dTime) > 5500) {
						break;
					}
				}
			} 
			catch (IOException e) {
				System.err.println("Couldn't complete middlebox testing to: " + sHostName +
						" since the socket read was not succesful");
				_sErrMsg = _resBundDisplayMsgs.getString("middleboxFail") + "\n" ;
			} 
			

			// Record test duration seconds
			_dTime =  System.currentTimeMillis() - _dTime;
			double tempSpeedInBits =  (NDTConstants.EIGHT * bytes)/_dTime;
			double tempTimeInSecs = _dTime/NDTConstants.KILO;
			System.out.println(bytes + " bytes " + tempSpeedInBits + " kb/s " + tempTimeInSecs + " secs");
			// Calculate throughput in Kbps 
			_dS2cspd =  tempSpeedInBits / NDTConstants.KILO; //((8.0 * bytes) / 1000) / _dTime;

			// Test is complete. Now, get results from server 
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { //msg not received correctly
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + 
						Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}

			// Results are sent from server in the form of a TEST_MSG object
			if (msg.getType() != MessageType.TEST_MSG) { //If not TEST_MSG, then test results not obtained
				_sErrMsg = _resBundDisplayMsgs.getString("mboxWrongMessage") + "\n";
				// get error code
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}

			// Get Test results 
			_sMidBoxTestResult = new String(msg.getBody());

			// client now sends throughput as calculated above
			String tmpstr4 = Double.toString(_dS2cspd * NDTConstants.KILO); //was  Double.toString(_dS2cspd* 1000);
			System.out.println("Sending '" + tmpstr4 + "' back to server");
			paramProtoObj.send_msg(MessageType.TEST_MSG, tmpstr4.getBytes());

			// Append server address as seen by the client
			// to the Test Results obtained from server
			try {
				_sMidBoxTestResult += midSrvrSockObj.getInetAddress() + ";";
			} catch (SecurityException e) {
				System.err.println("Unable to obtain Servers IP addresses: using " + sHostName);
				_sErrMsg = "getInetAddress() called failed\n" ;
				_sMidBoxTestResult += sHostName + ";";
				_resultsTxtPane.append(_resBundDisplayMsgs.getString("lookupError") + "\n");
			}

			// Append local address to the Test results obtained from server
			System.err.println("calling in2Socket.getLocalAddress()");
			try {
				_sMidBoxTestResult += midSrvrSockObj.getLocalAddress() + ";";
			} catch (SecurityException e) {
				System.err.println("Unable to obtain local IP address: using 127.0.0.1");
				_sErrMsg = "getLocalAddress() call failed\n" ;
				_sMidBoxTestResult += NDTConstants.LOOPBACK_ADDRS_STRING + ";";
			}

			// wrap up test set up
			srvin2.close();
			srvout2.close();
			midSrvrSockObj.close();

			// Expect TEST_FINALIZE message from server
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) {
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + 
						Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}

			if (msg.getType() != MessageType.TEST_FINALIZE) { //report unexpected message reception
				_sErrMsg = _resBundDisplayMsgs.getString("mboxWrongMessage");
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}

			// Report status as "complete"
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("done") + "\n");
			_txtStatistics.append(_resBundDisplayMsgs.getString("done") + "\n");
			_sEmailText += _resBundDisplayMsgs.getString("done") + "\n%0A";
		}
		return false;
	}

	/** Fire-wall tests aiming to find out if one exists between Client and server. 
	 * Tests are performed in both directions.
	 * @param protocolObj Protocol Object used for message exchange
	 * @return boolean, 
	 * 	   true if test was not completed,
	 *     false if test was completed.
	 * @throws IOException when sending/receiving messages from server fails. 
	 * 			See the @see methods for more information on causes for Exception.
	 * @see Protocol#recv_msg(Message msgParam)
	 * @see Protocol#send_msg(byte bParamType, byte[] baParamTab)
	 *    
	 *    */
	public boolean test_sfw(Protocol protocolObj) throws IOException   {
		Message msg = new Message();
		// start test
		if ((_yTests & NDTConstants.TEST_SFW) == NDTConstants.TEST_SFW) {
			// display status of test as "started"
			showStatus(_resBundDisplayMsgs.getString("sfwTest"));
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("checkingFirewalls") + "  ");
			_txtStatistics.append(_resBundDisplayMsgs.getString("checkingFirewalls") + "  ");
			_sEmailText = _resBundDisplayMsgs.getString("checkingFirewalls") + "  ";
			pub_status = "checkingFirewalls";

			// Message received in error?
			if (protocolObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) {
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}

			// TEST_PREPARE is the first message expected from the server
			if (msg.getType() != MessageType.TEST_PREPARE) { //oops, unexpected message received
				_sErrMsg = _resBundDisplayMsgs.getString("sfwWrongMessage") + "\n";
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}

			// Get message body from server packet
			String sMsgBody = new String(msg.getBody());

			// This message body contains ephemeral port # and testTime separated by a 
			// single space
		
			int iSrvPort, iTestTime;
			try {
				int k = sMsgBody.indexOf(" ");
				iSrvPort = Integer.parseInt(sMsgBody.substring(0,k));
				iTestTime = Integer.parseInt(sMsgBody.substring(k+1));
			}
			catch (Exception e) {
				_sErrMsg = _resBundDisplayMsgs.getString("sfwWrongMessage") + "\n";
				return true;
			}

			System.out.println("SFW: port=" + iSrvPort);
			System.out.println("SFW: testTime=" + iTestTime);

			// Client creates a server socket to send TEST_MSG
			ServerSocket srvSocket;
			try {
				SecurityManager security = System.getSecurityManager();
				if (security != null) {
					System.out
							.println("Asking security manager for listen permissions...");
					security.checkListen(0);
				}
				// SOCKET_FREE_PORT_INDICATOR = 0 to use any free port
				srvSocket = new ServerSocket(
						NDTConstants.SOCKET_FREE_PORT_INDICATOR);
			} catch (Exception e) {
				e.printStackTrace();
				_sErrMsg = _resBundDisplayMsgs.getString("sfwSocketFail")
						+ "\n";
				return true;
			}

			System.out.println("SFW: oport=" + srvSocket.getLocalPort());
			// Send TEST_MSG
			protocolObj.send_msg(MessageType.TEST_MSG, Integer.toString(srvSocket.getLocalPort()).getBytes());

			// Expect a TEST_START message from the server
			if (protocolObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { // oops, error receiving message
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}
			// Only TEST_START message expected at this point
			if (msg.getType() != MessageType.TEST_START) {
				_sErrMsg = _resBundDisplayMsgs.getString("sfwWrongMessage");
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}     

			// re-arch new code
			// OsfwWorker osfwTest = new OsfwWorker(srvSocket, testTime);
			// Listen for server sending out a test for the S->C direction , and update test results
			OsfwWorker osfwTest = new OsfwWorker(srvSocket, iTestTime, this);
			new Thread(osfwTest).start();

			// Now, run Test from client for the C->S direction SFW test
			// trying to connect to ephemeral port number sent by server
			Socket sfwSocket = new Socket();
			try {
				// create socket to ephemeral port. testTime now specified in mS
				sfwSocket.connect(new InetSocketAddress(sHostName, iSrvPort), iTestTime * NDTConstants.KILO);

				Protocol sfwCtl = new Protocol(sfwSocket);

				// send a simple string message over this socket
				sfwCtl.send_msg(MessageType.TEST_MSG, new String(NDTConstants.SFW_PREDEFINED_TEST_MESSAGE).getBytes());
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			// Server expected to respond back with a TEST_MSG too
			if (protocolObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { //oops, error reading Protocol
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}

			// Only TEST_MSG type expected at this point
			if (msg.getType() != MessageType.TEST_MSG) {
				_sErrMsg = _resBundDisplayMsgs.getString("sfwWrongMessage") + "\n";
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}

			// This is an integer with value 0/1/2/3 indicating status of a 
			// firewall's presence
			_iC2sSFWResult = Integer.parseInt(new String(msg.getBody()));

			// Sleep for some time
			osfwTest.finalize();

			// Server closes the SFW test session by sending TEST_FINALIZE
			if (protocolObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { //oops, error reading message
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}

			// ONLY TEST_FINALIZE type of message expected here
			if (msg.getType() != MessageType.TEST_FINALIZE) {
				_sErrMsg = _resBundDisplayMsgs.getString("sfwWrongMessage") + "\n";
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}

			//Conclude by updatng status as "complete" on GUI window
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("done") + "\n");
			_txtStatistics.append(_resBundDisplayMsgs.getString("done") + "\n");
			_sEmailText += _resBundDisplayMsgs.getString("done") + "\n%0A";
		}

		// completed the SFW test, hence return false
		return false;
	}

	/**  Client to server throughput test. This test performs 10 seconds 
	 *   memory-to-memory data transfer to test achievable network bandwidth.
	 *  @param paramProtoObj Protocol Object used to exchange messages
	 *	@return boolean, 
	 *		true if test was not completed,
	 *		false if test was completed.
	 * @throws IOException when sending/receiving messages from server fails
	 * @see Protocol#recv_msg(Message msgParam)
	 * @see Protocol#send_msg(byte bParamType, byte[] baParamTab)
	 *    
	 */
	public boolean test_c2s(Protocol paramProtoObj) throws IOException   {

		// byte buff2[] = new byte[8192];
		// Initialise for 64 Kb
		byte yabuff2Write[] = new byte[64* NDTConstants.KILO_BYTES]; 
		Message msg = new Message();
		// start C2S throughput tests
		if ((_yTests & NDTConstants.TEST_C2S) == NDTConstants.TEST_C2S) {
			showStatus(_resBundDisplayMsgs.getString("outboundTest"));
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("runningOutboundTest") + " ");
			_txtStatistics.append(_resBundDisplayMsgs.getString("runningOutboundTest") + " ");
			_sEmailText += _resBundDisplayMsgs.getString("runningOutboundTest") + " ";
			pub_status = "runningOutboundTest";

			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { // msg receive/read error
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}
			// Initial message expected from server is a TEST_PREPARE
			if (msg.getType() != MessageType.TEST_PREPARE) { //any other msg is error indicator
				_sErrMsg = _resBundDisplayMsgs.getString("outboundWrongMessage") + "\n";
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}
			// Server sends port number to bind to in the TEST_PREPARE
			int iC2sport = Integer.parseInt(new String(msg.getBody()));

			// client connects to this port
			final Socket outSocket;
			try {
				outSocket = new Socket(sHostName, iC2sport);
			} catch (UnknownHostException e) {
				System.err.println("Don't know about host: " + sHostName);
				_sErrMsg = _resBundDisplayMsgs.getString("unknownServer") + "\n" ;
				return true;
			} catch (IOException e) {
				System.err.println("Couldn't get 2nd connection to: " + sHostName);
				_sErrMsg = _resBundDisplayMsgs.getString("serverBusy15s") + "\n";
				return true;
			}

			// Get server IP address from the outSocket.
			pub_host = outSocket.getInetAddress().getHostAddress().toString();

			// Get output Stream from socket to write data into
			final OutputStream outStream = outSocket.getOutputStream();

			// wait here for signal from server application 
			// This signal tells the client to start pumping out data
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { //error reading/receiving message
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + 
						Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}
			// Expect a TEST_START message from server now. Any other message type is an error
			if (msg.getType() != MessageType.TEST_START) {
				_sErrMsg = _resBundDisplayMsgs.getString("outboundWrongMessage") + "\n";
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}

			// Fill buffer upto NDTConstants.PREDEFNED_BUFFER_SIZE packets
			byte c = '0';
			int i;
			for (i=0; i<_iLength; i++) {
				if (c == 'z') {
					c = '0';
				}
				yabuff2Write[i] = c++;
			}
			System.err.println("Send buffer size =" + i);

			_iPkts = 0;
			_dTime = System.currentTimeMillis();
			pub_time = _dTime;

			// sleep for 10 s
			new Thread() {

				public void run() {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						System.out.println(e);
					}
					try {
						outStream.close();
						outSocket.close();
					} catch (IOException e) {
						System.out.println(e);
					}
				}
			}.start();

			// While the 10 s timer ticks, write buffer data into server socket
			while (true) {
				// System.err.println("Send pkt = " + pkts + "; at " + System.currentTimeMillis());
				try {
					outStream.write(yabuff2Write, 0, yabuff2Write.length);
				}
				catch (SocketException e) {
					System.out.println(e);
					break;
				}
				// catch (InterruptedIOException iioe) {
				catch (IOException ioe) {
					System.out.println("Client socket timed out");
					break;
				}
				_iPkts++;
				// number of bytes sent = (num of iterations) X (buffer size)
				pub_bytes = (_iPkts * _iLength);
			}

			_dTime =  System.currentTimeMillis() - _dTime;
			System.err.println(_dTime + "sec test completed" +","+yabuff2Write.length); //actually ms, not s
			if (_dTime == 0) {
				_dTime = 1;
			}
			System.out.println((8.0 * _iPkts * yabuff2Write.length) / _dTime + " kb/s outbound");

			// Calculate C2S throughput in kbps
			_dC2sspd = (( NDTConstants.EIGHT * _iPkts * yabuff2Write.length) / NDTConstants.KILO) / _dTime;

			// The client has stopped streaming data, and the server is now expected 
			// to send a TEST_MSG message with the throughout it calculated 
			// So, its time now to receive the throughput (c2sspd) from the server
			
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { //error reading/receiving data
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}
			if (msg.getType() != MessageType.TEST_MSG) { //if not TEST_MSG, wrong , unexpected
				_sErrMsg = _resBundDisplayMsgs.getString("outboundWrongMessage");
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}
			// Get throughput as calculated by server
			String tmpstr3 = new String(msg.getBody());
			_dSc2sspd = Double.parseDouble(tmpstr3) / NDTConstants.KILO;

			// Print results in the most convenient units (kbps or Mbps)
			if (_dSc2sspd < 1.0) {
				_resultsTxtPane.append(prtdbl(_dSc2sspd*NDTConstants.KILO) + "kb/s\n");
				_txtStatistics.append(prtdbl(_dSc2sspd*NDTConstants.KILO) + "kb/s\n");
				_sEmailText += prtdbl(_dSc2sspd*NDTConstants.KILO) + "kb/s\n%0A";
			} 
			else {
				_resultsTxtPane.append(prtdbl(_dSc2sspd) + "Mb/s\n");
				_txtStatistics.append(prtdbl(_dSc2sspd) + "Mb/s\n");
				_sEmailText += prtdbl(_dSc2sspd) + "Mb/s\n%0A";
			}

			// Expose upload speed to JavaScript clients
			pub_c2sspd = _dSc2sspd;

			// Server should close test session with a TEST_FINALIZE message
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { // read/receive error
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}
			if (msg.getType() != MessageType.TEST_FINALIZE) { // all other types unexpected, erroneous
				_sErrMsg = _resBundDisplayMsgs.getString("outboundWrongMessage");
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true; // true indicates test incomplete
			}
		}
		// false indicates test's completion
		return false;
	}

	/** S2C throughput test to measure network bandwidth from server to client.
	 * @param paramProtoObj Protocol Object used to exchange messages
	 * @param paramSocketObj Socket Object to write/read NDTProtocol control messages
	 * @return boolean, 
	 *		true if test was not completed,
	 *		false if test was completed.
	 * @throws IOException when sending/receiving messages from server fails
	 * 	@see Protocol#recv_msg(Message msgParam)
	 * 	@see Protocol#send_msg(byte bParamType, byte[] baParamTab)
	 *   
	 * */
	public boolean test_s2c(Protocol paramProtoObj, Socket paramSocketObj) throws IOException   {
		//byte buff[] = new byte[8192];
		byte buff[] = new byte[NDTConstants.PREDEFINED_BUFFER_SIZE];
		Message msg = new Message();
		// start S2C tests
		if ((_yTests & NDTConstants.TEST_S2C) == NDTConstants.TEST_S2C) {
			showStatus(_resBundDisplayMsgs.getString("inboundTest"));
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("runningInboundTest") + " ");
			_txtStatistics.append(_resBundDisplayMsgs.getString("runningInboundTest") + " ");
			_sEmailText += _resBundDisplayMsgs.getString("runningInboundTest") + " ";
			pub_status = "runningInboundTest";

			// Server sends TEST_PREPARE with port to bind to as message body
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { // read/receive error
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}
			if (msg.getType() != MessageType.TEST_PREPARE) { // no other message type expected
				_sErrMsg = _resBundDisplayMsgs.getString("inboundWrongMessage") + "\n";
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}
			// get port to bind to for S2C tests
			int iS2cport = Integer.parseInt(new String(msg.getBody()));

			// Create socket and bind to port as instructed by server
			Socket inSocket;
			try {
				inSocket = new Socket(sHostName, iS2cport);
			} 
			catch (UnknownHostException e) {
				System.err.println("Don't know about host: " + sHostName);
				_sErrMsg = "unknown server\n" ;
				return true;
			} 
			catch (IOException e) {
				System.err.println("Couldn't get 3rd connection to: " + sHostName);
				_sErrMsg = "Server Failed while receiving data\n" ;
				return true;
			}

			// Get input stream to read bytes from socket
			InputStream srvin = inSocket.getInputStream();
			int iBitCount = 0;
			int inlth;

			// wait here for signal from server application 

			// server now sends a TEST_START message 
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { //erroneous read/receive
				_sErrMsg = _resBundDisplayMsgs.getString("unknownServer") + 
						Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}

			if (msg.getType() != MessageType.TEST_START) { // no other type of message expected
				_sErrMsg = _resBundDisplayMsgs.getString("serverFail") + "\n";
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + 
							Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}

			// Set socket timeout to 15 seconds
			inSocket.setSoTimeout(15000);
			_dTime = System.currentTimeMillis();
			pub_time = _dTime;

			// read data sent by server
			try {  
				while ((inlth=srvin.read(buff,0,buff.length)) > 0) {
					iBitCount += inlth; //increment bit count
					pub_bytes = iBitCount;
					if ((System.currentTimeMillis() - _dTime) > 14500) {
						break;
					}
				}
			} 
			catch (IOException ioExcep) {
				//new addition to handle Exception
				System.err.println("Couldn't perform s2c testing to: " + sHostName +
						":" + ioExcep);
				_sErrMsg = "Server Failed while reading socket data\n" ;
				return true;
			}

			// get time duration during which bytes were received
			_dTime = System.currentTimeMillis() - _dTime;
			System.out.println(iBitCount + " bytes " + 
					(NDTConstants.EIGHT * iBitCount)/_dTime + " kb/s " + _dTime/NDTConstants.KILO + " secs");

			// Calculate throughput
			_dS2cspd = ((NDTConstants.EIGHT * iBitCount) / NDTConstants.KILO) / _dTime;

			// Once the "send" window is complete, server sends TEST_MSG 
			// message with throughout as calculated at its end, unsent data queue size, 
			// and total sent byte count , separated by spaces
			// receive the s2cspd from the server
			
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { // error in read/receive of msg
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + 
						Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}
			// Only message of type TEST_MSG expected from server at this point
			if (msg.getType() != MessageType.TEST_MSG) {
				_sErrMsg = _resBundDisplayMsgs.getString("inboundWrongMessage") + "\n";
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}
			// get data from payload
			try {
				String tmpstr3 = new String(msg.getBody());
				int k1 = tmpstr3.indexOf(" ");
				int k2 = tmpstr3.substring(k1+1).indexOf(" ");
				_dSs2cspd = Double.parseDouble(tmpstr3.substring(0, k1)) / NDTConstants.KILO;
				_iSsndqueue = Integer.parseInt(tmpstr3.substring(k1+1).substring(0, k2));
				_dSbytes = Double.parseDouble(tmpstr3.substring(k1+1).substring(k2+1));
			}
			catch (Exception e) {
				e.printStackTrace();
				_sErrMsg = _resBundDisplayMsgs.getString("inboundWrongMessage") + "\n";
				return true;
			}

			// Represent throughput using optimal units (kbps / mbps)
			if (_dS2cspd < 1.0) {
				_resultsTxtPane.append(prtdbl(_dS2cspd*NDTConstants.KILO) + "kb/s\n");
				_txtStatistics.append(prtdbl(_dS2cspd*NDTConstants.KILO) + "kb/s\n");
				_sEmailText += prtdbl(_dS2cspd*NDTConstants.KILO) + "kb/s\n%0A";
			} else {
				_resultsTxtPane.append(prtdbl(_dS2cspd) + "Mb/s\n");
				_txtStatistics.append(prtdbl(_dS2cspd) + "Mb/s\n");
				_sEmailText += prtdbl(_dS2cspd) + "Mb/s\n%0A";
			}

			// Expose download speed to JavaScript clients
			pub_s2cspd = _dS2cspd;
			pub_status = "done";

			// Perform wrap-up activities for test
			srvin.close();
			inSocket.close();

			// Client has to send its throughput to server inside a TEST_MSG message
			buff = Double.toString(_dS2cspd*NDTConstants.KILO).getBytes();
			String tmpstr4 = new String(buff, 0, buff.length);
			System.out.println("Sending '" + tmpstr4 + "' back to server");
			paramProtoObj.send_msg(MessageType.TEST_MSG, buff);

			// get web100 variables from server
			_sTestResults = "";
			int i = 0;

			// Try setting a 5 second timer here to break out if the read fails.
			paramSocketObj.setSoTimeout(5000);
			try {  
				for (;;) {
					if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { // msg could not be read/received correctly
						_sErrMsg = _resBundDisplayMsgs.getString("protocolError") +
								Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
						return true;
					}
					if (msg.getType() == MessageType.TEST_FINALIZE) {
						// All web100 variables have been sent
						break;
					}
					// Only a message of TEST_MSG type containing the Web100 variables is expected.
					// Every other type of message is indicative of errors
					//
					if (msg.getType() != MessageType.TEST_MSG) {
						_sErrMsg = _resBundDisplayMsgs.getString("inboundWrongMessage") + "\n";
						if (msg.getType() == MessageType.MSG_ERROR) {
							_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
						}
						return true;
					}
					// Get all web100 variables as name-value string pairs
					_sTestResults += new String(msg.getBody());
					i++;
				} // end for
			} catch (IOException ioExcep) {
				// new addition to handle Exception
				_sErrMsg = _resBundDisplayMsgs.getString("s2cThroughputFailed") + "\n";
				System.err.println("Couldn't perform s2c testing to: " + sHostName +
						":" + ioExcep);
				_sErrMsg += "Server Failed while reading socket data\n" ;
				return true;
			}
		}
		pub_status = "done";
		return false; // false indicating no stoppage was encountered
	}

	/**
	 * The META test allows the Client to send an additional information to the Server
	 * that basically gets included along with the overall set of test results. 
	 *  
	 * @param paramProtoObj Protocol Object used to exchange protocol messages
	 * @return boolean, 
	 * 	   true if test was completed,
	 *     false if test is incomplete.
	 * @throws IOException when sending/receiving messages from server fails
	 * @see Protocol#recv_msg(Message msgParam)
	 * @see Protocol#send_msg(byte bParamType, byte[] baParamTab)
	 *    These methods indicate more information about IOException
	 * */
	public boolean test_meta(Protocol paramProtoObj) throws IOException
	{
		Message msg = new Message();
		// Start META tests
		if ((_yTests & NDTConstants.TEST_META) == NDTConstants.TEST_META) {
			showStatus(_resBundDisplayMsgs.getString("metaTest"));
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("sendingMetaInformation") + " ");
			_txtStatistics.append(_resBundDisplayMsgs.getString("sendingMetaInformation") + " ");
			_sEmailText += _resBundDisplayMsgs.getString("sendingMetaInformation") + " ";
			pub_status = "sendingMetaInformation";

			// Server starts with a TEST_PREPARE message. 
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { //error, message not received correctly
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}
			if (msg.getType() != MessageType.TEST_PREPARE) { 
				// only TEST_PREPARE message expected at this point
				_sErrMsg = _resBundDisplayMsgs.getString("metaWrongMessage") + "\n";
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}

			// Server now send a TEST_START message 
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { //error, message not read/received correctly
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}

			// Only TEST_START message expected here. Everything else is unacceptable
			if (msg.getType() != MessageType.TEST_START) {
				_sErrMsg = _resBundDisplayMsgs.getString("metaWrongMessage") + "\n";

				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}

			// As a response to the Server's TEST_START message, client responds with TEST_MSG. 
			// These messages may be used, as below, to send configuration data name-value pairs.
			// Note that there are length constraints to keys- values: 64/256 characters respectively
			System.err.println ("USERAGENT " + getUserAgent());
			paramProtoObj.send_msg(MessageType.TEST_MSG, (NDTConstants.META_CLIENT_OS + ":" + System.getProperty("os.name")).getBytes());
			paramProtoObj.send_msg(MessageType.TEST_MSG, (NDTConstants.META_BROWSER_OS + ":" + UserAgentTools.getBrowser(getUserAgent())[2]).getBytes());
			paramProtoObj.send_msg(MessageType.TEST_MSG, (NDTConstants.META_CLIENT_KERNEL_VERSION + ":" + System.getProperty("os.version")).getBytes());
			paramProtoObj.send_msg(MessageType.TEST_MSG, (NDTConstants.META_CLIENT_VERSION + ":" + NDTConstants.VERSION).getBytes());

			// Client can send any number of such meta data in a TEST_MSG format, and signal
			// the end of the transmission using an empty TEST_MSG
			paramProtoObj.send_msg(MessageType.TEST_MSG, new byte[0]);

			// The server now closes the META test session by sending a TEST_FINALIZE message
			if (paramProtoObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { //error, message cannot be read/received properly
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				return true;
			}
			if (msg.getType() != MessageType.TEST_FINALIZE) { // Only this message type is expected
				_sErrMsg = _resBundDisplayMsgs.getString("metaWrongMessage");
				if (msg.getType() == MessageType.MSG_ERROR) {
					_sErrMsg += "ERROR MSG: " + Integer.parseInt(new String(msg.getBody()), 16) + "\n";
				}
				return true;
			}
			// Display status as "complete"
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("done") + "\n");
			_txtStatistics.append(_resBundDisplayMsgs.getString("done") + "\n");
			_sEmailText += _resBundDisplayMsgs.getString("done") + "\n%0A";
		}

		//completed tests
		pub_status = "done";
		//status is false indicating test-failure=false
		return false;
	}

	/** Method to run tests and interpret the results sent by the server
	 * @param sPanel StatusPanel object to describe status of tests
	 * @throws IOException when sending/receiving messages from server fails
	 * @see Protocol#recv_msg(Message msgParam)
	 * @see Protocol#send_msg(byte bParamType, byte[] baParamTab)
	 *    
	 */
	public void dottcp(StatusPanel sPanel) throws IOException {
		Socket ctlSocket = null;
		if (!_bIsApplication) {

			//
			// Enable NDT to test against a web100srv instance on a remote server.
			// Instead of using the getCodeBase().getHost() value for the testing server,
			// which assumes this applet is being served from the web100srv server,
			// use a parameter provided in the APPLET tag.
			// Note that for this to work the applet must be signed because you are
			// potentially accessing a server outside the source domain.
			//
			sHostName = getParameter("testingServer");
			
			// fall back to the old behavior if the APPLET tag is not set
			if (sHostName == null) {
				sHostName = getCodeBase().getHost();
			}

			pub_host = sHostName;
		}

		// The default control port used for the NDT tests session. NDT server listens 
		// to this port
		int ctlport = NDTConstants.CONTROL_PORT_DEFAULT;

		// Commenting these 2 variables - seem unused
		// double wait2;
		// int sbuf, rbuf;
		int i, wait;
		int iServerWaitFlag=0; // flag indicating whether a wait message was already received once

		// Assign false to test result status initially
		_bFailed = false;

		try {

			// RAC Debug message
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("connectingTo") + " '" +
					sHostName + "' [" + InetAddress.getByName(sHostName) + "] " + 
					_resBundDisplayMsgs.getString("toRunTest") + "\n");
			// If IPv6 is preferred by Applet user, set property for any further use
			if (_chkboxPreferIPv6.isSelected()) {
				try {
					System.setProperty("java.net._chkboxPreferIPv6Addresses", "true");
				}
				catch (SecurityException e) {
					System.err.println("Couldn't set system property. Check your security settings.");
				}
			}
			_chkboxPreferIPv6.setEnabled(false);
			// create socket to host specified by user and the default port
			ctlSocket = new Socket(sHostName, ctlport);
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + sHostName);
			_sErrMsg = _resBundDisplayMsgs.getString("unknownServer") + "\n" ;
			_bFailed = true;
			return;
		} catch (IOException e) {
			System.err.println("Couldn't get the connection to: " + sHostName + " " +ctlport);
			_sErrMsg = _resBundDisplayMsgs.getString("serverNotRunning") + " (" + 
					sHostName + ":" + ctlport + ")\n" ;
			_bFailed = true;
			return;
		}


		Protocol protocolObj = new Protocol(ctlSocket);
		Message msg = new Message();

		// The beginning of the protocol

		// Determine, and indicate to client about Inet6/4 address being used
		if (ctlSocket.getInetAddress() instanceof Inet6Address) {
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("connected") + " " + 
					sHostName + _resBundDisplayMsgs.getString("usingIpv6") + "\n");
		}
		else {
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("connected") + " " + 
					sHostName + _resBundDisplayMsgs.getString("usingIpv4") + "\n");
		}

		// write our test suite request by sending a login message
		// _yTests indicates the requested test-suite
		 
		protocolObj.send_msg(MessageType.MSG_LOGIN, _yTests);
		
		// read the specially crafted data that kicks off the old clients 
		if (protocolObj.readn(msg, 13) != 13) {
			_sErrMsg = _resBundDisplayMsgs.getString("unsupportedClient") + "\n";
			_bFailed = true;
			return;
		}

		for (;;) {
			
			// If SRV_QUEUE message sent by NDT server does not indicate that the test 
			// session starts now, return
			if (protocolObj.recv_msg(msg) != NDTConstants.SRV_QUEUE_TEST_STARTS_NOW) {
				_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + 
						Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
				_bFailed = true;
				return;
			}

			// SRV_QUEUE messages are only sent to queued clients with a message body that
			// indicates one of a few statuses, as will be hanlded individually below. 
			// Any other type of message at this stage is incorrect			
			if (msg.getType() != MessageType.SRV_QUEUE) {
				_sErrMsg = _resBundDisplayMsgs.getString("loggingWrongMessage") + "\n";
				_bFailed = true;
				return;
			}	

			// Get wait flag value
			String tmpstr3 = new String(msg.getBody());
			wait = Integer.parseInt(tmpstr3);
			System.out.println("wait flag received = " + wait);


			if (wait == 0) { // SRV_QUEUE message received indicating "ready to start tests" status,
							//	proceed to running tests 		 
				break;
			}

			
			if (wait == NDTConstants.SRV_QUEUE_SERVER_BUSY) {
				if (iServerWaitFlag == 0) { // First message from server, indicating server is busy. Quit
					_sErrMsg = _resBundDisplayMsgs.getString("serverBusy") + "\n";
					_bFailed = true;
					return;
				} else {    // Server fault, quit without further ado
					_sErrMsg = _resBundDisplayMsgs.getString("serverFault") + "\n";
					_bFailed = true;
					return;
				}
			}

			
			//server busy, wait 60 s for previous test to finish
			if ( wait == NDTConstants.SRV_QUEUE_SERVER_BUSY_60s ) {
				_sErrMsg = _resBundDisplayMsgs.getString("serverBusy60s") + "\n";
				_bFailed = true;
				return;
			}

			if (wait == NDTConstants.SRV_QUEUE_HEARTBEAT) {  // signal from the server to see if the client is still alive
				
				// Client has to respond with a "MSG_WAITING" to such heart-beat messages from server
				protocolObj.send_msg(MessageType.MSG_WAITING, _yTests);
				continue;
			}

			// Each test should take less than 30 seconds, so tell them 45 sec * number of 
			// tests-suites waiting in the queue. Note that server sends a number equal to the number of clients ==
			// number of minutes to wait before starting tests (i.e wait = number of minutes to wait = number of queued clients)
			wait = (wait * 45);
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("otherClient") + wait + _resBundDisplayMsgs.getString("seconds") +".\n");
			iServerWaitFlag = 1; // mark variable as ==first message from server already encountered
		} //end waiting 

		_frameWeb100Vars.toBack();
		_frameDetailedStats.toBack();

		// Tests can be started. Read server response again.
		// The server must send a message to verify version, and this is
		// a MSG_LOGIN type of message
		//
		if (protocolObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) { // it not read correctly, it is protocol error
			_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
			_bFailed = true;
			return;
		}
		if (msg.getType() != MessageType.MSG_LOGIN) { // Only this type of message is expected at this stage
			// ..every other message type is "wrong"
			_sErrMsg = _resBundDisplayMsgs.getString("versionWrongMessage") + "\n";
			_bFailed = true;
			return;
		}

		// Version compatibility between server-client must be verified
		String vVersion = new String(msg.getBody());
		if (!vVersion.startsWith("v")) {
			_sErrMsg = _resBundDisplayMsgs.getString("incompatibleVersion");
			_bFailed = true;
			return;
		}
		System.out.println("Server version: " + vVersion.substring(1));

		// Read server message again. Server must send a message to negotiate the test suite, and this is
		// a MSG_LOGIN type of message which indicates the same set of tests as requested by the client earlier 
		if (protocolObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) {
			_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
			_bFailed = true;
			return;
		}
		if (msg.getType() != MessageType.MSG_LOGIN) { // Only tests-negotiation message expected at this stage.
			// ..Every other message type is "wrong"
			_sErrMsg = _resBundDisplayMsgs.getString("testsuiteWrongMessage") + "\n";
			_bFailed = true;
			return;
		}

		// get ids of tests to be run now
		StringTokenizer tokenizer = new StringTokenizer(new String(msg.getBody()), " ");

		// Run all tests requested, based on the ID. In each case, if tests cannot be successfully run,
		// indicate reason
		while (tokenizer.hasMoreTokens()) {
			if (sPanel.wantToStop()) { // user has indicated decision to stop tests from GUI
				protocolObj.send_msg(MessageType.MSG_ERROR, "Manually stopped by the user".getBytes());
				protocolObj.close();
				ctlSocket.close();
				_sErrMsg = "\n" + _resBundDisplayMsgs.getString("stopped") + "\n";
				_bFailed = true;
				return;
			}
			int testId = Integer.parseInt(tokenizer.nextToken());
			switch (testId) {
			case NDTConstants.TEST_MID:
				sPanel.setText(_resBundDisplayMsgs.getString("middlebox"));
				if (test_mid(protocolObj)) {
					_resultsTxtPane.append(_sErrMsg);
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("middleboxFail2") + "\n");
					_yTests &= (~NDTConstants.TEST_MID);
				}
				break;
			case NDTConstants.TEST_SFW:
				sPanel.setText(_resBundDisplayMsgs.getString("simpleFirewall"));
				if (test_sfw(protocolObj)) {
					_resultsTxtPane.append(_sErrMsg);
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("sfwFail") + "\n");
					_yTests &= (~NDTConstants.TEST_SFW);
				}
				break;
			case NDTConstants.TEST_C2S:
				sPanel.setText(_resBundDisplayMsgs.getString("c2sThroughput"));
				if (test_c2s(protocolObj)) {
					_resultsTxtPane.append(_sErrMsg);
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("c2sThroughputFailed") + "\n");
					_yTests &= (~NDTConstants.TEST_C2S);
				}
				break;
			case NDTConstants.TEST_S2C:
				sPanel.setText(_resBundDisplayMsgs.getString("s2cThroughput"));
				if (test_s2c(protocolObj, ctlSocket)) {
					_resultsTxtPane.append(_sErrMsg);
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("s2cThroughputFailed") + "\n");
					_yTests &= (~NDTConstants.TEST_S2C);
				}
				break;
			case NDTConstants.TEST_META:
				sPanel.setText(_resBundDisplayMsgs.getString("meta"));
				if (test_meta(protocolObj)) {
					_resultsTxtPane.append(_sErrMsg);
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("metaFailed") + "\n");
					_yTests &= (~NDTConstants.TEST_META);
				}
				break;
			default:
				_sErrMsg = _resBundDisplayMsgs.getString("unknownID") + "\n";
				_bFailed = true;
				return;
			}
		}

		if (sPanel.wantToStop()) { // user has indicated decision to stop tests from GUI
			protocolObj.send_msg(MessageType.MSG_ERROR, "Manually stopped by the user".getBytes());
			protocolObj.close();
			ctlSocket.close();
			_sErrMsg = _resBundDisplayMsgs.getString("stopped") + "\n";
			_bFailed = true;
			return;
		}

		sPanel.setText(_resBundDisplayMsgs.getString("receiving"));

		// Get results of tests
		i = 0;
		try {  
			for (;;) {
				if (protocolObj.recv_msg(msg) != NDTConstants.PROTOCOL_MSG_READ_SUCCESS) {
					_sErrMsg = _resBundDisplayMsgs.getString("protocolError") + Integer.parseInt(new String(msg.getBody()), 16) + " instead\n";
					_bFailed = true;
					return;
				}

				// results obtained. "Log out" message received now
				if (msg.getType() == MessageType.MSG_LOGOUT) {
					break;
				}

				// get results in the form of a human-readable string
				if (msg.getType() != MessageType.MSG_RESULTS) {
					_sErrMsg = _resBundDisplayMsgs.getString("resultsWrongMessage") + "\n";
					_bFailed = true;
					return;
				}
				_sTestResults += new String(msg.getBody());
				i++;
			}
		} catch (IOException e) {
			System.err.println("Couldn't complete tests while waiting for MSG_LOGOUT/MSG_RESULTS for " 
					+ sHostName );
			_sErrMsg = "Couldn't complete tests while waiting for MSG_LOGOUT/MSG_RESULTS for " 
					+ sHostName + ")\n" ;
			_bFailed = true;
			return;
		}

		// Timed-out while waiting for results
		
		if (i == 0) {
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("resultsTimeout") + "\n");
		}
		System.err.println("Calling InetAddress.getLocalHost() twice");
		try {
			_txtDiagnosis.append(_resBundDisplayMsgs.getString("client") + ": " + InetAddress.getLocalHost() + "\n");
		} catch (SecurityException e) {
			_txtDiagnosis.append(_resBundDisplayMsgs.getString("client") + ": 127.0.0.1\n");
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("unableToObtainIP") + "\n");
			System.err.println("Unable to obtain local IP address: using 127.0.0.1");
		}

		try {
			_sEmailText += _resBundDisplayMsgs.getString("client") + ": " + InetAddress.getLocalHost() + "\n%0A";
		} catch (SecurityException e) {
			_sEmailText += _resBundDisplayMsgs.getString("client") + ": " + "127.0.0.1" + "\n%0A";
		}

		// Final cleanup steps after completion of tests
		protocolObj.close();
		ctlSocket.close();
		// call testResults method
		try {
			testResults(_sTestResults);
		}
		catch (Exception ex) {
			_resultsTxtPane.append(_resBundDisplayMsgs.getString("resultsParseError")  + "\n");
			_resultsTxtPane.append(ex + "\n");
		}
		// interpret middlebox test results
		if ((_yTests & NDTConstants.TEST_MID) == NDTConstants.TEST_MID) {
			middleboxResults(_sMidBoxTestResult);
		}

		pub_isReady="yes";
		pub_errmsg ="All tests completed OK.";
		pub_status = "done";
	}



	/**
	 * Method that interprets test results.
	 * This routine extracts the key-value pairs of results of various categories
	 * and assigns these to the correct variables. 
	 * 
	 *  These values are then interpreted to make decisions about various measurement
	 *  items and written to the main results, statistics, web100 or mail-to panels. 
	 * 
	 * @param sTestResParam String containing all results
	 *
	 * */
	public void testResults(String sTestResParam) {
		StringTokenizer tokens;
		int i=0;
		String sSysvar, sStrval;
		int iSysval, iZero=0; //iBwdelay, iMinwin;  // commented out unused variables
		double dSysval2, j;
		String sOsName, sOsArch, sOsVer, sJavaVer, sJavaVendor, sClient;

		// extract key-value pair results
		
		tokens = new StringTokenizer(sTestResParam);
		sSysvar = null;
		sStrval = null;
		while (tokens.hasMoreTokens()) {
			if (++i % 2 == 1) {
				sSysvar = tokens.nextToken();
			} else {
				sStrval = tokens.nextToken();
				_txtDiagnosis.append(sSysvar + " " + sStrval + "\n");
				_sEmailText += sSysvar + " " + sStrval + "\n%0A";
				if (sStrval.indexOf(".") == -1) { // no decimal point, hence
													// integer
					iSysval = Integer.parseInt(sStrval);
					// save value into a key value expected by us
					save_int_values(sSysvar, iSysval);
				} else { // if not integer, save as double
					dSysval2 = Double.valueOf(sStrval).doubleValue();
					save_dbl_values(sSysvar, dSysval2);
				}
			}
		}

		// Grab some client details from the Applet environment
		sOsName = System.getProperty("os.name");
		pub_osName = sOsName;

		sOsArch = System.getProperty("os.arch");
		pub_osArch = sOsArch;

		sOsVer = System.getProperty("os.version");
		pub_osVer = sOsVer;

		sJavaVer = System.getProperty("java.version");
		pub_javaVer = sJavaVer;

		sJavaVendor = System.getProperty("java.vendor");

		if (sOsArch.startsWith("x86") == true) {
			sClient = _resBundDisplayMsgs.getString("pc");
		}
		else {
			sClient = _resBundDisplayMsgs.getString("workstation");
		}

		// Calculate some variables and determine path conditions
		// Note: calculations now done in server and the results are shipped
		//    back to the client for printing.

		if (_iCountRTT>0) {
			
			// Now write some _resBundDisplayMsgs to the screen
			// Access speed/technology details added to the result main panel
			// and mailing text. Link speed is also assigned.
			if (_iC2sData < NDTConstants.DATA_RATE_ETHERNET) {  //< 3
				if (_iC2sData < NDTConstants.DATA_RATE_RTT) { // <0) {
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("unableToDetectBottleneck") + "\n");
					_sEmailText += "Server unable to determine bottleneck link type.\n%0A";
					pub_AccessTech = "Connection type unknown";

				} 
				else {
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("your") + " " + sClient + " " + _resBundDisplayMsgs.getString("connectedTo") + " ");
					_sEmailText += _resBundDisplayMsgs.getString("your") + " " + sClient + " " + _resBundDisplayMsgs.getString("connectedTo") + " ";
					if (_iC2sData == NDTConstants.DATA_RATE_DIAL_UP) { //== 1) {
						_resultsTxtPane.append(_resBundDisplayMsgs.getString("dialup") + "\n");
						_sEmailText += _resBundDisplayMsgs.getString("dialup") +  "\n%0A";
						mylink = .064; // 64 kbps speed
						pub_AccessTech = "Dial-up Modem";
					} 
					else {
						_resultsTxtPane.append(_resBundDisplayMsgs.getString("cabledsl") + "\n");
						_sEmailText += _resBundDisplayMsgs.getString("cabledsl") +  "\n%0A";
						mylink = 3;
						pub_AccessTech = "Cable/DSL modem";
					}
				}
			} 
			else {
				_resultsTxtPane.append(_resBundDisplayMsgs.getString("theSlowestLink") + " ");
				_sEmailText += _resBundDisplayMsgs.getString("theSlowestLink") + " ";
				switch (_iC2sData) {				
				case NDTConstants.DATA_RATE_ETHERNET:
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("10mbps") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("10mbps") + "\n%0A";
					mylink = 10;
					pub_AccessTech = "10 Mbps Ethernet";
					break;				
				case NDTConstants.DATA_RATE_T3:
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("45mbps") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("45mbps") + "\n%0A";
					mylink = 45;
					pub_AccessTech = "45 Mbps T3/DS3 subnet";
					break;				
				case NDTConstants.DATA_RATE_FAST_ETHERNET:
					_resultsTxtPane.append("100 Mbps ");
					_sEmailText += "100 Mbps ";
					mylink = 100;
					pub_AccessTech = "100 Mbps Ethernet";

					if (half_duplex == 0) {
						_resultsTxtPane.append(_resBundDisplayMsgs.getString("fullDuplex") + "\n");
						_sEmailText += _resBundDisplayMsgs.getString("fullDuplex") + "\n%0A";
					} 
					else {
						_resultsTxtPane.append(_resBundDisplayMsgs.getString("halfDuplex") + "\n");
						_sEmailText += _resBundDisplayMsgs.getString("halfDuplex") + "\n%0A";
					}
					break;				
				case NDTConstants.DATA_RATE_OC_12:
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("622mbps") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("622mbps") + "\n%0A";
					mylink = 622;
					pub_AccessTech = "622 Mbps OC-12";
					break;				
				case NDTConstants.DATA_RATE_GIGABIT_ETHERNET:
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("1gbps") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("1gbps") + "\n%0A";
					mylink = 1000;
					pub_AccessTech = "1.0 Gbps Gigabit Ethernet";
					break;				
				case NDTConstants.DATA_RATE_OC_48:
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("2.4gbps") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("2.4gbps") + "\n%0A";
					mylink = 2400;
					pub_AccessTech = "2.4 Gbps OC-48";
					break;				
				case NDTConstants.DATA_RATE_10G_ETHERNET:
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("10gbps") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("10gbps") + "\n%0A";
					mylink = 10000;
					pub_AccessTech = "10 Gigabit Ethernet/OC-192";
					break;	
				default: //default block indicating no match
					System.err.println("No _iC2sData option match");
					break;
				}				
			}

			//duplex mismatch 
			switch (mismatch) {
			case 1:
				_resultsTxtPane.append(_resBundDisplayMsgs.getString("oldDuplexMismatch") + "\n");
				_sEmailText += _resBundDisplayMsgs.getString("oldDuplexMismatch") + "\n%0A";
				break;
			case 2:
				_resultsTxtPane.append(_resBundDisplayMsgs.getString("duplexFullHalf") + "\n");
				_sEmailText += _resBundDisplayMsgs.getString("duplexFullHalf") + "\n%0A";
				break;
			case 3:
				_resultsTxtPane.append(_resBundDisplayMsgs.getString("duplexHalfFull") + "\n");
				_sEmailText += _resBundDisplayMsgs.getString("duplexHalfFull") + "\n%0A";
				break;
			case 4:
				_resultsTxtPane.append(_resBundDisplayMsgs.getString("possibleDuplexFullHalf") + "\n");
				_sEmailText += _resBundDisplayMsgs.getString("possibleDuplexFullHalf") + "\n%0A";
				break;
			case 5:
				_resultsTxtPane.append(_resBundDisplayMsgs.getString("possibleDuplexHalfFull") + "\n");
				_sEmailText += _resBundDisplayMsgs.getString("possibleDuplexHalfFull") + "\n%0A";
				break;
			case 7:
				_resultsTxtPane.append(_resBundDisplayMsgs.getString("possibleDuplexHalfFullWarning") + "\n");
				_sEmailText += _resBundDisplayMsgs.getString("possibleDuplexHalfFullWarning") + "\n%0A";
				break;
			case 0:
				if (bad_cable == 1) {
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("excessiveErrors ") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("excessiveErrors") + "\n%0A";
				}
				if (congestion == 1) {
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("otherTraffic") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("otherTraffic") + "\n%0A";
				}
				if (((2*rwin)/rttsec) < mylink) {
					j = (float)((mylink * avgrtt) * 1000) / 8 / 1024;
					if (j > (float)_iMaxRwinRcvd) {
						_resultsTxtPane.append(_resBundDisplayMsgs.getString("receiveBufferShouldBe") + " " + prtdbl(j) + _resBundDisplayMsgs.getString("toMaximizeThroughput") + " \n");
						_sEmailText += _resBundDisplayMsgs.getString("receiveBufferShouldBe") + " " + prtdbl(j) + _resBundDisplayMsgs.getString("toMaximizeThroughput") + "\n%0A";
					}
				}
				break;
			default: //default for indication of no match for mismatch variable
				break;

			}	

			//C2S throughput test: Packet queuing 
			if ((_yTests & NDTConstants.TEST_C2S) == NDTConstants.TEST_C2S) {
				if (_dSc2sspd < (_dC2sspd  * (1.0 - NDTConstants.VIEW_DIFF))) {
					// TODO:  distinguish the host buffering from the middleboxes buffering
					JLabel info = new JLabel(_resBundDisplayMsgs.getString("information"));
					info.addMouseListener(new MouseAdapter()
					{
						public void mouseClicked(MouseEvent e) {
							showBufferedBytesInfo();
						}
					});
					info.setForeground(Color.BLUE);
					info.setCursor(new Cursor(Cursor.HAND_CURSOR));
					info.setAlignmentY((float) 0.8);
					_resultsTxtPane.insertComponent(info);
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("c2sPacketQueuingDetected") + "\n");
				}
			}

			//S2C throughput test: Packet queuing 
			if ((_yTests & NDTConstants.TEST_S2C) == NDTConstants.TEST_S2C) {
				if (_dS2cspd < (_dSs2cspd  * (1.0 - NDTConstants.VIEW_DIFF))) {
					// TODO:  distinguish the host buffering from the middleboxes buffering
					JLabel info = new JLabel(_resBundDisplayMsgs.getString("information"));
					info.addMouseListener(new MouseAdapter()  {
						public void mouseClicked(MouseEvent e) {
							showBufferedBytesInfo();
						}

					});
					info.setForeground(Color.BLUE);
					info.setCursor(new Cursor(Cursor.HAND_CURSOR));
					info.setAlignmentY((float) 0.8);
					_resultsTxtPane.insertComponent(info);
					_resultsTxtPane.append(_resBundDisplayMsgs.getString("s2cPacketQueuingDetected") + "\n");
				}
			} //end s2C test based packet queuing results

			//Add client information obtained earlier
			_txtStatistics.append("\n\t------  " + _resBundDisplayMsgs.getString("clientInfo") + "------\n");
			_txtStatistics.append(_resBundDisplayMsgs.getString("osData") + " " + _resBundDisplayMsgs.getString("name") + " = " + 
					sOsName + ", " + _resBundDisplayMsgs.getString("architecture") + " = " + sOsArch);
			_txtStatistics.append(", " + _resBundDisplayMsgs.getString("version") + " = " + sOsVer + "\n");
			_txtStatistics.append(_resBundDisplayMsgs.getString("javaData") + ": " +  _resBundDisplayMsgs.getString("vendor") + " = " +
					sJavaVendor + ", " + _resBundDisplayMsgs.getString("version") + " = " + sJavaVer + "\n");
			// statistics.append(" java.class.version=" + System.getProperty("java.class.version") + "\n");

			_txtStatistics.append("\n\t------  " + _resBundDisplayMsgs.getString("web100Details") + "  ------\n");
			

			/*Now add data to the statistics pane about access speed/technology
			Slightly different from the earlier switch (that added data about this to the
			Results pane) in that negative values are checked for too.*/
			switch (_iC2sData) {
			//case -2:
			case NDTConstants.DATA_RATE_INSUFFICIENT_DATA:
				_txtStatistics.append(_resBundDisplayMsgs.getString("insufficient") + "\n");
				break;
			//case -1:
			case NDTConstants.DATA_RATE_SYSTEM_FAULT:
				_txtStatistics.append(_resBundDisplayMsgs.getString("ipcFail") + "\n");	
				break;
			//case 0:
			case NDTConstants.DATA_RATE_RTT:
				_txtStatistics.append(_resBundDisplayMsgs.getString("rttFail") + "\n");
				break;
			//case 1:
			case NDTConstants.DATA_RATE_DIAL_UP:
				_txtStatistics.append(_resBundDisplayMsgs.getString("foundDialup") + "\n");
				break;
			//case 2:
			case NDTConstants.DATA_RATE_T1:
				_txtStatistics.append(_resBundDisplayMsgs.getString("foundDsl") + "\n");
				break;
			//case 3:
			case NDTConstants.DATA_RATE_ETHERNET:
				_txtStatistics.append(_resBundDisplayMsgs.getString("found10mbps") + "\n");
				break;
			//case 4:
			case NDTConstants.DATA_RATE_T3:
				_txtStatistics.append(_resBundDisplayMsgs.getString("found45mbps") + "\n");
				break;
			//case 5:
			case NDTConstants.DATA_RATE_FAST_ETHERNET:
				_txtStatistics.append(_resBundDisplayMsgs.getString("found100mbps") + "\n");
				break;
			//case 6:
			case NDTConstants.DATA_RATE_OC_12:
				_txtStatistics.append(_resBundDisplayMsgs.getString("found622mbps") + "\n");
				break;
			//case 7:
			case NDTConstants.DATA_RATE_GIGABIT_ETHERNET:
				_txtStatistics.append(_resBundDisplayMsgs.getString("found1gbps") + "\n");		
				break;
			//case 8:
			case NDTConstants.DATA_RATE_OC_48:
				_txtStatistics.append(_resBundDisplayMsgs.getString("found2.4gbps") + "\n");		
				break;
			//case 9:
			case NDTConstants.DATA_RATE_10G_ETHERNET:
				_txtStatistics.append(_resBundDisplayMsgs.getString("found10gbps") + "\n");
				break;
			}

			//Add decisions  about duplex mode, congestion, duplex mismatch to Statistics pane
			if (half_duplex == 0)
				_txtStatistics.append(_resBundDisplayMsgs.getString("linkFullDpx") + "\n");
			else
				_txtStatistics.append(_resBundDisplayMsgs.getString("linkHalfDpx") + "\n");

			if (congestion == 0)
				_txtStatistics.append(_resBundDisplayMsgs.getString("congestNo") + "\n");
			else
				_txtStatistics.append(_resBundDisplayMsgs.getString("congestYes") + "\n");

			if (bad_cable == 0)
				_txtStatistics.append(_resBundDisplayMsgs.getString("cablesOk") + "\n");
			else
				_txtStatistics.append(_resBundDisplayMsgs.getString("cablesNok") + "\n");

			if (mismatch == NDTConstants.DUPLEX_OK_INDICATOR)
				_txtStatistics.append(_resBundDisplayMsgs.getString("duplexOk") + "\n");
			else if (mismatch == NDTConstants.DUPLEX_NOK_INDICATOR) { 
				_txtStatistics.append(_resBundDisplayMsgs.getString("duplexNok") + " ");
				_sEmailText += _resBundDisplayMsgs.getString("duplexNok") + " ";
			}
			else if (mismatch == NDTConstants.DUPLEX_SWITCH_FULL_HOST_HALF) { 
				_txtStatistics.append(_resBundDisplayMsgs.getString("duplexFullHalf") + "\n");
				_sEmailText += _resBundDisplayMsgs.getString("duplexFullHalf") + "\n%0A ";
			}
			else if (mismatch == NDTConstants.DUPLEX_SWITCH_HALF_HOST_FULL ) {
				_txtStatistics.append(_resBundDisplayMsgs.getString("duplexHalfFull") + "\n");
				_sEmailText += _resBundDisplayMsgs.getString("duplexHalfFull") + "\n%0A ";
			}

			_txtStatistics.append("\n" + _resBundDisplayMsgs.getString("web100rtt") + " =  " + prtdbl(avgrtt) + " " + "ms" + "; ");
			_sEmailText += "\n%0A" +  _resBundDisplayMsgs.getString("web100rtt") + " = " + prtdbl(avgrtt) + " " + "ms" + "; ";

			_txtStatistics.append(_resBundDisplayMsgs.getString("packetsize") + " = " + _iCurrentMSS + " " + 
					_resBundDisplayMsgs.getString("bytes") + "; " + _resBundDisplayMsgs.getString("and") + " \n");
			_sEmailText += _resBundDisplayMsgs.getString("packetsize") + " = " + _iCurrentMSS + " " + 
					_resBundDisplayMsgs.getString("bytes") + "; " + _resBundDisplayMsgs.getString("and") + " \n%0A";

			//check packet retransmissions count, and update Statistics pane and email text data
			if (_iPktsRetrans > 0) { //packet retransmissions found
				_txtStatistics.append(_iPktsRetrans + " " + _resBundDisplayMsgs.getString("pktsRetrans"));
				_txtStatistics.append(", " + _iDupAcksIn + " " + _resBundDisplayMsgs.getString("dupAcksIn"));
				_txtStatistics.append(", " + _resBundDisplayMsgs.getString("and") + " " + _iSACKsRcvd + " " + _resBundDisplayMsgs.getString("sackReceived") + "\n");
				_sEmailText += _iPktsRetrans + " " + _resBundDisplayMsgs.getString("pktsRetrans");
				_sEmailText += ", " + _iDupAcksIn + " " + _resBundDisplayMsgs.getString("dupAcksIn");
				_sEmailText += ", " + _resBundDisplayMsgs.getString("and") + " " + _iSACKsRcvd + " " + _resBundDisplayMsgs.getString("sackReceived") + "\n%0A";
				if (_iTimeouts > 0) {
					_txtStatistics.append(_resBundDisplayMsgs.getString("connStalled") + " " + _iTimeouts + " " + _resBundDisplayMsgs.getString("timesPktLoss") + "\n");
				}

				_txtStatistics.append(_resBundDisplayMsgs.getString("connIdle") + " " + prtdbl(waitsec) + " " + _resBundDisplayMsgs.getString("seconds") + " (" + prtdbl((waitsec/timesec)*100) + _resBundDisplayMsgs.getString("pctOfTime") + ")\n");
				_sEmailText += _resBundDisplayMsgs.getString("connStalled") + " " + _iTimeouts + " " + _resBundDisplayMsgs.getString("timesPktLoss") + "\n%0A";
				_sEmailText += _resBundDisplayMsgs.getString("connIdle") + " " + prtdbl(waitsec) + " " + _resBundDisplayMsgs.getString("seconds") + " (" + prtdbl((waitsec/timesec)*100) + _resBundDisplayMsgs.getString("pctOfTime") + ")\n%0A";
			} 
			else if (_iDupAcksIn > 0) { //No packets loss, but packets arrived out-of-order
				_txtStatistics.append(_resBundDisplayMsgs.getString("noPktLoss1") + " - ");
				_txtStatistics.append(_resBundDisplayMsgs.getString("ooOrder") + " " + prtdbl(order*100) + _resBundDisplayMsgs.getString("pctOfTime") + "\n");
				_sEmailText += _resBundDisplayMsgs.getString("noPktLoss1") + " - ";
				_sEmailText += _resBundDisplayMsgs.getString("ooOrder") + " " + prtdbl(order*100) + _resBundDisplayMsgs.getString("pctOfTime") + "\n%0A";
			} 
			else { //no packets transmissions found
				_txtStatistics.append(_resBundDisplayMsgs.getString("noPktLoss2") + ".\n");
				_sEmailText += _resBundDisplayMsgs.getString("noPktLoss2") + ".\n%0A";
			}

			//Add Packet queuing details found during C2S throughput test to the statistics pane
			if ((_yTests & NDTConstants.TEST_C2S) == NDTConstants.TEST_C2S) {
				if (_dC2sspd > _dSc2sspd) {
					if (_dSc2sspd < (_dC2sspd  * (1.0 - NDTConstants.VIEW_DIFF))) {
						_txtStatistics.append(_resBundDisplayMsgs.getString("c2s") + " " + _resBundDisplayMsgs.getString("qSeen") + ": " +
								prtdbl(100 * (_dC2sspd - _dSc2sspd) / _dC2sspd) + "%\n");
					}
					else {
						_txtStatistics.append(_resBundDisplayMsgs.getString("c2s") + " " + _resBundDisplayMsgs.getString("qSeen") + ": " +
								prtdbl(100 * (_dC2sspd - _dSc2sspd) / _dC2sspd) + "%\n");
					}
				}
			}

			//Add Packet queuing details found during S2C throughput test to the statistics pane
			if ((_yTests & NDTConstants.TEST_S2C) == NDTConstants.TEST_S2C) {
				if (_dSs2cspd > _dS2cspd) {
					if (_dSs2cspd < (_dSs2cspd  * (1.0 - NDTConstants.VIEW_DIFF))) {
						_txtStatistics.append(_resBundDisplayMsgs.getString("s2c") + " " + _resBundDisplayMsgs.getString("qSeen") + ": " + 
								prtdbl(100 * (_dSs2cspd - _dS2cspd) / _dSs2cspd) + "%\n");
					}
					else {
						_txtStatistics.append(_resBundDisplayMsgs.getString("s2c") + " " + _resBundDisplayMsgs.getString("qSeen") + ": " + 
								prtdbl(100 * (_dSs2cspd - _dS2cspd) / _dSs2cspd) + "%\n");
					}
				}
			}

			// Add connection details to statistics pane and email text
			
			// Is the connection receiver limited?
			if (rwintime > .015) {
				_txtStatistics.append(_resBundDisplayMsgs.getString("thisConnIs") + " " + _resBundDisplayMsgs.getString("limitRx") + " " + 
						prtdbl(rwintime*100) + _resBundDisplayMsgs.getString("pctOfTime") + ".\n");
				_sEmailText += _resBundDisplayMsgs.getString("thisConnIs") + " " + _resBundDisplayMsgs.getString("limitRx") + " " + 
						prtdbl(rwintime*100) + _resBundDisplayMsgs.getString("pctOfTime") + ".\n%0A";
				pub_pctRcvrLimited = rwintime*100;

				// I think there is a bug here, it sometimes tells you to increase the buffer
				// size, but the new size is smaller than the current - (older comment left as is)

				if (((2*rwin)/rttsec) < mylink) {
					_txtStatistics.append("  " + _resBundDisplayMsgs.getString("incrRxBuf") + " (" + prtdbl(_iMaxRwinRcvd/1024) + " KB) " + _resBundDisplayMsgs.getString("willImprove") + "\n");
				}
			}

			//Is the connection sender limited?
			if (sendtime > .015) {
				_txtStatistics.append(_resBundDisplayMsgs.getString("thisConnIs") + " " + _resBundDisplayMsgs.getString("limitTx") + " " + 
						prtdbl(sendtime*100) + _resBundDisplayMsgs.getString("pctOfTime") + ".\n");
				_sEmailText += _resBundDisplayMsgs.getString("thisConnIs") + " " + _resBundDisplayMsgs.getString("limitTx") + " " + 
						prtdbl(sendtime*100) + _resBundDisplayMsgs.getString("pctOfTime") + ".\n%0A";
				if ((2*(swin/rttsec)) < mylink) {
					_txtStatistics.append("  " + _resBundDisplayMsgs.getString("incrTxBuf") + " (" + prtdbl(_iSndbuf/2048) + " KB) " + _resBundDisplayMsgs.getString("willImprove") + "\n");
				}
			}

			//Is the connection network limited?
			if (cwndtime > .005) {
				_txtStatistics.append(_resBundDisplayMsgs.getString("thisConnIs") + " " + _resBundDisplayMsgs.getString("limitNet") + " " +
						prtdbl(cwndtime*100) + _resBundDisplayMsgs.getString("pctOfTime") + ".\n");
				_sEmailText += _resBundDisplayMsgs.getString("thisConnIs") + " " + _resBundDisplayMsgs.getString("limitNet") + " " + 
						prtdbl(cwndtime*100) + _resBundDisplayMsgs.getString("pctOfTime") + ".\n%0A";
				// if (cwndtime > .15)
				//	statistics.append("  Contact your local network administrator to report a network problem\n");
				// if (order > .15)
				//	statistics.append("  Contact your local network admin and report excessive packet reordering\n");
			}

			// Is the loss excessive?
			if ((spd < 4) && (loss > .01)) {
				_txtStatistics.append(_resBundDisplayMsgs.getString("excLoss") + "\n");
			}

			// Update statistics on TCP negotiated optional Performance Settings
			_txtStatistics.append("\n" + _resBundDisplayMsgs.getString("web100tcpOpts") + " \n");
			_txtStatistics.append("RFC 2018 Selective Acknowledgment: ");
			if(_iSACKEnabled == iZero)
				_txtStatistics.append(_resBundDisplayMsgs.getString("off") + "\n");
			else
				_txtStatistics.append(_resBundDisplayMsgs.getString("on") + "\n");

			_txtStatistics.append("RFC 896 Nagle Algorithm: ");
			if(_iNagleEnabled == iZero)
				_txtStatistics.append(_resBundDisplayMsgs.getString("off") + "\n");
			else
				_txtStatistics.append(_resBundDisplayMsgs.getString("on") + "\n");

			_txtStatistics.append("RFC 3168 Explicit Congestion Notification: ");
			if(_iECNEnabled == iZero)
				_txtStatistics.append(_resBundDisplayMsgs.getString("off") + "\n");
			else
				_txtStatistics.append(_resBundDisplayMsgs.getString("on") + "\n");

			_txtStatistics.append("RFC 1323 Time Stamping: ");
			if(_iTimestampsEnabled == 0)
				_txtStatistics.append(_resBundDisplayMsgs.getString("off") + "\n");
			else
				_txtStatistics.append(_resBundDisplayMsgs.getString("on") + "\n");

			_txtStatistics.append("RFC 1323 Window Scaling: ");
			if (_iMaxRwinRcvd < 65535)
				_iWinScaleRcvd = 0;
			if((_iWinScaleRcvd == 0) || (_iWinScaleRcvd > 20))
				_txtStatistics.append(_resBundDisplayMsgs.getString("off") + "\n");
			else
				_txtStatistics.append (_resBundDisplayMsgs.getString("on") + "; " + _resBundDisplayMsgs.getString("scalingFactors") + " -  " + _resBundDisplayMsgs.getString("server") + "=" + _iWinScaleRcvd + ", " + _resBundDisplayMsgs.getString("client") + "=" + _iWinScaleSent + "\n");

			_txtStatistics.append("\n");
			// End tcp negotiated performance settings

			// SFW test results
			if ((_yTests & NDTConstants.TEST_SFW) == NDTConstants.TEST_SFW) {
				// Results in the direction of Client to server
				// switch (_iC2sSFWResult) {
				int iSFWResC2S = this.getC2sSFWTestResults();

				switch (iSFWResC2S) {
				// Update Statistics  and email-text based on results 
				case NDTConstants.SFW_NOFIREWALL:
					_txtStatistics.append(_resBundDisplayMsgs.getString("server") + " '" +
							sHostName + "' " + _resBundDisplayMsgs.getString("firewallNo") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("server") + " '" + 
							sHostName + "' " + _resBundDisplayMsgs.getString("firewallNo") + "\n%0A";
					break;
				case NDTConstants.SFW_POSSIBLE:
					_txtStatistics.append(_resBundDisplayMsgs.getString("server") + " '" + 
							sHostName + "' " + _resBundDisplayMsgs.getString("firewallYes") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("server") + " '" + 
							sHostName + "' " + _resBundDisplayMsgs.getString("firewallYes") + "\n%0A";
					break;
				case NDTConstants.SFW_UNKNOWN:
				case NDTConstants.SFW_NOTTESTED:
					break;
				}
				// Results in the direction of Server to client
				//s witch (_iS2cSFWResult) {
				int iSFWResS2C = this.getS2cSFWTestResults();
				switch (iSFWResS2C) {
				//Update Statistics  and email-text based on results
				case NDTConstants.SFW_NOFIREWALL:
					_txtStatistics.append(_resBundDisplayMsgs.getString("client2") + " " + _resBundDisplayMsgs.getString("firewallNo") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("client2") + " " + _resBundDisplayMsgs.getString("firewallNo") + "\n%0A";
					break;
				case NDTConstants.SFW_POSSIBLE:
					_txtStatistics.append(_resBundDisplayMsgs.getString("client2") + " " + _resBundDisplayMsgs.getString("firewallYes") + "\n");
					_sEmailText += _resBundDisplayMsgs.getString("client2") + " " + _resBundDisplayMsgs.getString("firewallYes") + "\n%0A";
					break;
				case NDTConstants.SFW_UNKNOWN:
				case NDTConstants.SFW_NOTTESTED:
					break;
				}
			}

			//			diagnosis.append("\nEstimate = " + prtdbl(estimate) + " based on packet size = "
			//				+ (CurrentMSS*8/1024) + "kbits, RTT = " + prtdbl(avgrtt) + "msec, " + "and loss = " + loss + "\n");
			_txtDiagnosis.append("\n");

			// Output relevant to the "More Details" tab, related to factors influencing throughput
			
			// Theoretical network limit
			_txtDiagnosis.append(_resBundDisplayMsgs.getString("theoreticalLimit") + " " + prtdbl(estimate) + " " + "Mbps\n");
			_sEmailText += _resBundDisplayMsgs.getString("theoreticalLimit") + " " + prtdbl(estimate) + " Mbps\n%0A";

			// NDT server buffer imposed limit
			_txtDiagnosis.append(_resBundDisplayMsgs.getString("ndtServerHas") + " " + prtdbl(_iSndbuf/2048) + " " + _resBundDisplayMsgs.getString("kbyteBufferLimits") + " " + prtdbl(swin/rttsec) + " Mbps\n");
			_sEmailText += _resBundDisplayMsgs.getString("ndtServerHas") + " " + prtdbl(_iSndbuf/2048) + " " + _resBundDisplayMsgs.getString("kbyteBufferLimits") + " " + prtdbl(swin/rttsec) + " Mbps\n%0A";

			// PC buffer imposed throughput limit
			_txtDiagnosis.append(_resBundDisplayMsgs.getString("yourPcHas") + " " + prtdbl(_iMaxRwinRcvd/1024) + " " + _resBundDisplayMsgs.getString("kbyteBufferLimits") + " " + prtdbl(rwin/rttsec) + " Mbps\n");
			_sEmailText += _resBundDisplayMsgs.getString("yourPcHas") + " " + prtdbl(_iMaxRwinRcvd/1024) + " " + _resBundDisplayMsgs.getString("kbyteBufferLimits") + " " + prtdbl(rwin/rttsec) + " Mbps\n%0A";

			// Network based flow control limit imposed throughput limit
			_txtDiagnosis.append(_resBundDisplayMsgs.getString("flowControlLimits") + " " +	prtdbl(cwin/rttsec) + " Mbps\n");
			_sEmailText += _resBundDisplayMsgs.getString("flowControlLimits") + " " +	prtdbl(cwin/rttsec) + " Mbps\n%0A";

			// Client, Server data reports on link capacity
			_txtDiagnosis.append("\n" + _resBundDisplayMsgs.getString("clientDataReports") + " '" + prttxt(_iC2sData) + "', " + _resBundDisplayMsgs.getString("clientAcksReport") + " '" + prttxt(_iC2sAck) + "'\n" + _resBundDisplayMsgs.getString("serverDataReports") + " '" + prttxt(_iS2cData) + "', " + _resBundDisplayMsgs.getString("serverAcksReport") + " '" + prttxt(_iS2cAck) + "'\n");
			pub_diagnosis = _txtDiagnosis.getText();

		} //end if (CountRTT >0)

	}  // end testResults()



	/** This routine decodes the middlebox test results.  The data is returned
	 * from the server is a specific order.  This routine pulls the string apart
	 * and puts the values into the proper variable.  It then compares the values
	 * to known values and writes out the specific results.
	 *
	 * server data is first
	 * order is Server IP; Client IP; CurrentMSS; WinScaleRcvd; WinScaleSent;
	 * Client then adds
	 * Server IP; Client IP.
	 * @param sMidBoxTestResParam String Middlebox results
	 */

	public void middleboxResults(String sMidBoxTestResParam) {
		StringTokenizer tokens;
		int k;

		// results.append("Mbox stats: ");
		tokens = new StringTokenizer(sMidBoxTestResParam, ";");
		String sServerIp = tokens.nextToken();
		String sClientIp = tokens.nextToken();

		// Fix for JS API not reporting NAT'd IPs correctly
		// Assign client and server IP addresses for JA API
		// based on public, not local IP.  
		pub_clientIP = sClientIp;


		// results.append("ssip=" + ssip + " scip=" + scip + "\n");

		// String mss = tokens.nextToken();
		// String winsrecv = tokens.nextToken();
		// String winssent = tokens.nextToken();
		int iMss = Integer.parseInt(tokens.nextToken());
		int iWinsRecv = Integer.parseInt(tokens.nextToken()); //unused, but retaining
		int iWinsSent = Integer.parseInt(tokens.nextToken()); //unused, but retaining

		// Get Client reported server IP
		String sClientSideServerIp = tokens.nextToken();
		k = sClientSideServerIp.indexOf("/");
		sClientSideServerIp = sClientSideServerIp.substring(k+1);

		// get client side IP
		String sClientSideClientIp = tokens.nextToken();
		k = sClientSideServerIp.indexOf("/");
		sClientSideServerIp = sClientSideServerIp.substring(k+1);

		// results.append("csip=" + csip + " ccip=" + ccip + "\n");
		// results.append("mss=" + mss + " winsrecv=" + winsrecv + " winssent=" +
		// 	winssent + "\n");

		// MSS = 1456 = Ethernet MTU = 1500 - 24 -20 (bytes of IP header) = 1456, thus preserved
		//if (iMss == 1456)
		if (iMss == NDTConstants.ETHERNET_MTU_SIZE)
			_txtStatistics.append(_resBundDisplayMsgs.getString("packetSizePreserved") + "\n");
		else
			_txtStatistics.append(_resBundDisplayMsgs.getString("middleboxModifyingMss") + "\n");

		/*
		// if ((winsrecv == -1) && (winssent == -1))
		//     statistics.append("Window scaling option is preserved End-to-End\n");
		// else
		//     statistics.append("Information: Network Middlebox is modifying Window scaling option\n");
		*/
		
		// server IP has been preserved end-to-end without changes
		
		boolean preserved = false;
		try {
			preserved = 
					InetAddress.getByName(sServerIp).equals(InetAddress.getByName(sClientSideServerIp));
		}
		catch (UnknownHostException e) {
			preserved = sServerIp.equals(sClientSideServerIp);
		}
		if (preserved) {
			_txtStatistics.append(_resBundDisplayMsgs.getString("serverIpPreserved") + "\n");
			pub_natBox = "no";
		}
		else {
			pub_natBox = "yes";
			_txtStatistics.append(_resBundDisplayMsgs.getString("serverIpModified") + "\n");
			_txtStatistics.append("\t" + 
					_resBundDisplayMsgs.getString("serverSays") +
					" [" + sServerIp + "], " + 
					_resBundDisplayMsgs.getString("clientSays") +
					" [" + sClientSideServerIp + "]\n");
		}

		// Client side IP was never obtained
		if (sClientSideClientIp.equals(NDTConstants.LOOPBACK_ADDRS_STRING)) {
			_txtStatistics.append(_resBundDisplayMsgs.getString("clientIpNotFound") + "\n");
		}
		else { // try to find if client IP was changed
			try {
				preserved = 
						InetAddress.getByName(sClientIp).equals(InetAddress.getByName(sClientSideClientIp));
			}
			catch (UnknownHostException e) {
				preserved = sClientIp.equals(sClientSideClientIp);
			}
			catch (SecurityException e ) {
				preserved = sClientIp.equals(sClientSideClientIp);
			}

			if (preserved)
				_txtStatistics.append(_resBundDisplayMsgs.getString("clientIpPreserved") + "\n");
			else {
				_txtStatistics.append(_resBundDisplayMsgs.getString("clientIpModified") + "\n");
				_txtStatistics.append("\t" + _resBundDisplayMsgs.getString("serverSays") +
						" [" + sClientIp + "], " + _resBundDisplayMsgs.getString("clientSays") +" [" + 
						sClientSideClientIp + "]\n");
			}
		}
		pub_statistics = _txtStatistics.getText();
	} // middleboxResults()


	/** Pop up a window to display some information about TCP packet queuing.
	 * */
	public void showBufferedBytesInfo() {
		JOptionPane.showMessageDialog(null, _resBundDisplayMsgs.getString("packetQueuingInfo"), _resBundDisplayMsgs.getString("packetQueuing"),
				JOptionPane.INFORMATION_MESSAGE);
	}


	/** Utility method to print double value upto the hundreth place. 
	 * @param paramDblToFormat Double numbers to format
	 * @return String value of double number */	
	public String prtdbl(double paramDblToFormat) {
		String str = null;
		int i;

		if (paramDblToFormat == 0) {
			return ("0");
		}
		str = Double.toString(paramDblToFormat);
		i = str.indexOf(".");
		i = i + 3;
		if (i > str.length()) {
			i = i - 1;
		}
		if (i > str.length()) {
			i = i - 1;
		}
		return (str.substring(0, i));
	} // prtdbl()


	/** Utility method to print Text values for data speed related keys.
	 * @param paramIntVal integer parameter for which we find text value
	 * @return String Textual name for input parameter
	 */
	public String prttxt(int paramIntVal) {
		String strNameTxt = null;

		switch (paramIntVal) {
		case (NDTConstants.DATA_RATE_SYSTEM_FAULT):
			strNameTxt = _resBundDisplayMsgs.getString(NDTConstants.SYSTEM_FAULT_STR); //"systemFault"
		break;
		case NDTConstants.DATA_RATE_RTT:
			strNameTxt = _resBundDisplayMsgs.getString(NDTConstants.RTT_STR); //"rtt"
			break;
		case NDTConstants.DATA_RATE_DIAL_UP:
			strNameTxt = _resBundDisplayMsgs.getString(NDTConstants.DIALUP_STR); //"dialup2"
			break;
		case NDTConstants.DATA_RATE_T1:
			strNameTxt = NDTConstants.T1_STR; //"T1"
			break;
		case NDTConstants.DATA_RATE_ETHERNET:
			strNameTxt = NDTConstants.ETHERNET_STR; //"Ethernet";
			break;
		case NDTConstants.DATA_RATE_T3:
			strNameTxt = NDTConstants.T3_STR; //"T3";
			break;
		case NDTConstants.DATA_RATE_FAST_ETHERNET:
			strNameTxt = NDTConstants.FAST_ETHERNET; //"FastE";
			break;
		case NDTConstants.DATA_RATE_OC_12:
			strNameTxt = NDTConstants.OC_12_STR; //"OC-12";
			break;
		case NDTConstants.DATA_RATE_GIGABIT_ETHERNET:
			strNameTxt = NDTConstants.GIGABIT_ETHERNET_STR; //"GigE";
			break;
		case NDTConstants.DATA_RATE_OC_48:
			strNameTxt = NDTConstants.OC_48_STR; //"OC-48";
			break;
		case NDTConstants.DATA_RATE_10G_ETHERNET:
			strNameTxt = NDTConstants.TENGIGABIT_ETHERNET_STR; //"10 Gig";
			break;
		} //end switch
		return(strNameTxt);
	} // prttxt()



	/* This routine saves the specific value into the variable of the
	 * same name.  There should probably be an easier way to do this.
	 */
	
	/** Method to save double values of various "keys" from the 
	 * the test results string into corresponding double datatypes.
	 * @param sSysvarParam key name string
	 * @param dSysvalParam Value for this key name
	 */
	public void save_dbl_values(String sSysvarParam, double dSysvalParam) {
		if(sSysvarParam.equals("bw:")) 
			estimate = dSysvalParam;
		else if(sSysvarParam.equals("loss:")) { 
			loss = dSysvalParam;
			pub_loss = loss;
		}
		else if(sSysvarParam.equals("avgrtt:")) {
			avgrtt = dSysvalParam;
			pub_avgrtt = avgrtt;
		}
		else if(sSysvarParam.equals("waitsec:")) 
			waitsec = dSysvalParam;
		else if(sSysvarParam.equals("timesec:")) 
			timesec = dSysvalParam;
		else if(sSysvarParam.equals("order:")) 
			order = dSysvalParam;
		else if(sSysvarParam.equals("rwintime:")) 
			rwintime = dSysvalParam;
		else if(sSysvarParam.equals("sendtime:")) 
			sendtime = dSysvalParam;
		else if(sSysvarParam.equals("cwndtime:")) {
			cwndtime = dSysvalParam;
			pub_cwndtime=cwndtime;
		}
		else if(sSysvarParam.equals("rttsec:")) 
			rttsec = dSysvalParam;
		else if(sSysvarParam.equals("rwin:")) 
			rwin = dSysvalParam;
		else if(sSysvarParam.equals("swin:")) 
			swin = dSysvalParam;
		else if(sSysvarParam.equals("cwin:")) 
			cwin = dSysvalParam;
		else if(sSysvarParam.equals("spd:")) 
			spd = dSysvalParam;
		else if(sSysvarParam.equals("aspd:")) 
			aspd = dSysvalParam;
	} // save_dbl_values()


	/** Method to save integer values of various "keys" from the 
	 * the test results string into corresponding integer datatypes.
	 * @param sSysvarParam String key name
	 * @param iSysvalParam Value for this key name
	 * */

	public void save_int_values(String sSysvarParam, int iSysvalParam) {
		/*  Values saved for interpretation:
		 *	SumRTT 
		 *	CountRTT
		 *	CurrentMSS
		 *	Timeouts
		 *	PktsRetrans
		 *	SACKsRcvd
		 *	DupAcksIn
		 *	MaxRwinRcvd
		 *	MaxRwinSent
		 *	Sndbuf
		 *	Rcvbuf
		 *	DataPktsOut
		 *	SndLimTimeRwin
		 *	SndLimTimeCwnd
		 *	SndLimTimeSender
		 */   
		if(sSysvarParam.equals("MSSSent:")) 
			MSSSent = iSysvalParam;
		else if(sSysvarParam.equals("MSSRcvd:")) 
			MSSRcvd = iSysvalParam;
		else if(sSysvarParam.equals("ECNEnabled:")) 
			_iECNEnabled = iSysvalParam;
		else if(sSysvarParam.equals("NagleEnabled:")) 
			_iNagleEnabled = iSysvalParam;
		else if(sSysvarParam.equals("SACKEnabled:")) 
			_iSACKEnabled = iSysvalParam;
		else if(sSysvarParam.equals("TimestampsEnabled:")) 
			_iTimestampsEnabled = iSysvalParam;
		else if(sSysvarParam.equals("WinScaleRcvd:")) 
			_iWinScaleRcvd = iSysvalParam;
		else if(sSysvarParam.equals("WinScaleSent:")) 
			_iWinScaleSent = iSysvalParam;
		else if(sSysvarParam.equals("SumRTT:")) 
			_iSumRTT = iSysvalParam;
		else if(sSysvarParam.equals("CountRTT:")) 
			_iCountRTT = iSysvalParam;
		else if(sSysvarParam.equals("CurMSS:"))
			_iCurrentMSS = iSysvalParam;
		else if(sSysvarParam.equals("Timeouts:")) 
			_iTimeouts = iSysvalParam;
		else if(sSysvarParam.equals("PktsRetrans:")) 
			_iPktsRetrans = iSysvalParam;
		else if(sSysvarParam.equals("SACKsRcvd:")) {
			_iSACKsRcvd = iSysvalParam;
			pub_SACKsRcvd = _iSACKsRcvd;
		}
		else if(sSysvarParam.equals("DupAcksIn:")) 
			_iDupAcksIn = iSysvalParam;
		else if(sSysvarParam.equals("MaxRwinRcvd:")) {
			_iMaxRwinRcvd = iSysvalParam;
			pub_MaxRwinRcvd=_iMaxRwinRcvd;
		}
		else if(sSysvarParam.equals("MaxRwinSent:")) 
			_iMaxRwinSent = iSysvalParam;
		else if(sSysvarParam.equals("Sndbuf:")) 
			_iSndbuf = iSysvalParam;
		else if(sSysvarParam.equals("X_Rcvbuf:")) 
			_iRcvbuf = iSysvalParam;
		else if(sSysvarParam.equals("DataPktsOut:")) 
			_iDataPktsOut = iSysvalParam;
		else if(sSysvarParam.equals("FastRetran:")) 
			_iFastRetran = iSysvalParam;
		else if(sSysvarParam.equals("AckPktsOut:")) 
			_iAckPktsOut = iSysvalParam;
		else if(sSysvarParam.equals("SmoothedRTT:")) 
			_iSmoothedRTT = iSysvalParam;
		else if(sSysvarParam.equals("CurCwnd:")) 
			_iCurrentCwnd = iSysvalParam;
		else if(sSysvarParam.equals("MaxCwnd:")) 
			_iMaxCwnd = iSysvalParam;
		else if(sSysvarParam.equals("SndLimTimeRwin:")) 
			_iSndLimTimeRwin = iSysvalParam;
		else if(sSysvarParam.equals("SndLimTimeCwnd:")) 
			_iSndLimTimeCwnd = iSysvalParam;
		else if(sSysvarParam.equals("SndLimTimeSender:")) 
			_iSndLimTimeSender = iSysvalParam;
		else if(sSysvarParam.equals("DataBytesOut:")) 
			_iDataBytesOut = iSysvalParam;
		else if(sSysvarParam.equals("AckPktsIn:")) 
			_iAckPktsIn = iSysvalParam;
		else if(sSysvarParam.equals("SndLimTransRwin:"))
			_iSndLimTransRwin = iSysvalParam;
		else if(sSysvarParam.equals("SndLimTransCwnd:"))
			_iSndLimTransCwnd = iSysvalParam;
		else if(sSysvarParam.equals("SndLimTransSender:"))
			_iSndLimTransSender = iSysvalParam;
		else if(sSysvarParam.equals("MaxSsthresh:"))
			_iMaxSsthresh = iSysvalParam;
		else if(sSysvarParam.equals("CurRTO:")) {
			_iCurrentRTO = iSysvalParam;
			pub_CurRTO = _iCurrentRTO;
		}
		else if(sSysvarParam.equals("MaxRTO:")) {
			pub_MaxRTO = iSysvalParam;
		}
		else if(sSysvarParam.equals("MinRTO:")) {
			pub_MinRTO = iSysvalParam;
		}
		else if(sSysvarParam.equals("MinRTT:")) {
			pub_MinRTT = iSysvalParam;
		}
		else if(sSysvarParam.equals("MaxRTT:")) {
			pub_MaxRTT = iSysvalParam;
		}
		else if(sSysvarParam.equals("CurRwinRcvd:")) {
			pub_CurRwinRcvd = iSysvalParam;
		}		
		else if(sSysvarParam.equals("Timeouts:")) {
			pub_Timeouts = iSysvalParam;
		}	
		else if(sSysvarParam.equals("c2sData:"))
			_iC2sData = iSysvalParam;
		else if(sSysvarParam.equals("c2sAck:"))
			_iC2sAck = iSysvalParam;
		else if(sSysvarParam.equals("s2cData:"))
			_iS2cData = iSysvalParam;
		else if(sSysvarParam.equals("s2cAck:"))
			_iS2cAck = iSysvalParam;
		else if(sSysvarParam.equals("PktsOut:"))
			_iPktsOut = iSysvalParam;
		else if(sSysvarParam.equals("mismatch:")) {
			mismatch = iSysvalParam;
			pub_mismatch=mismatch;
		}
		else if(sSysvarParam.equals("congestion:")) {
			congestion = iSysvalParam;
			pub_congestion = congestion;
		}
		else if(sSysvarParam.equals("bad_cable:")) {
			bad_cable = iSysvalParam;
			pub_Bad_cable = bad_cable;
		}
		else if(sSysvarParam.equals("half_duplex:"))
			half_duplex = iSysvalParam;
		else if(sSysvarParam.equals("CongestionSignals:"))
			_iCongestionSignals = iSysvalParam;
		else if(sSysvarParam.equals("RcvWinScale:")) {
			if (_iRcvWinScale > 15)
				_iRcvWinScale = 0;
			else
				_iRcvWinScale = iSysvalParam;
		}
	}  // save_int_values()

	/** Utility method to get parameter value.
	 * @param paramStrName Key String whose value has to be found 
	 * @return String Value of key requested for */
	public String getParameter(String paramStrName) {
		if (!_bIsApplication) {
			return super.getParameter(paramStrName);
		}
		return null;
	}

} // class: Tcpbw100






