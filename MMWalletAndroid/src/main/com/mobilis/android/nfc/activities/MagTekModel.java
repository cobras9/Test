package com.mobilis.android.nfc.activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.magtek.mobile.android.scra.ConfigParam;
import com.magtek.mobile.android.scra.MTSCRAException;
import com.magtek.mobile.android.scra.MagTekSCRA;
import com.magtek.mobile.android.scra.ProcessMessageResponse;
import com.magtek.mobile.android.scra.SCRAConfiguration;
import com.magtek.mobile.android.scra.SCRAConfigurationDeviceInfo;
import com.magtek.mobile.android.scra.SCRAConfigurationReaderType;
import com.magtek.mobile.android.scra.StatusCode;
import com.mobilis.android.nfc.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;

//import com.magtek.mobile.android.scra.ArrayOfConfigParam;

public class MagTekModel {

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int STATUS_IDLE = 1;
	public static final int STATUS_PROCESSCARD = 2;
//	private static final int MESSAGE_UPDATE_GUI = 6;
	public static final String CONFIGWS_URL = "https://deviceconfig.magensa.net/service.asmx";//Production URL

	 static final int CONFIGWS_READERTYPE = 0;
	 static final String CONFIGWS_USERNAME = "magtek";
	 static final String CONFIGWS_PASSWORD = "p@ssword";
	
		
	 AudioManager mAudioMgr;	
	
	 String mStringDebugData;
	public static final String DEVICE_NAME = "device_name";
	public static final String CONFIG_FILE = "MTSCRADevConfig.cfg";
//	public static final String TOAST = "toast";
	public static final String PARTIAL_AUTH_INDICATOR = "1";
	static final boolean mShowTitle = false;
	// Intent request codes
	 static final int REQUEST_CONNECT_DEVICE = 1;
	
	public MagTekSCRA mMTSCRA;
	//private int miDeviceType=MagTekSCRA.DEVICE_TYPE_NONE;
	Handler mSCRADataHandler = new Handler(new SCRAHandlerCallback());

	int mIntCurrentDeviceStatus;
	// =============================================================================================================
    //private Boolean mBooleanBTConnect;

	 boolean mbAudioConnected;

	 long mLongTimerInterval;

	 int mIntCurrentStatus;

	 int mIntCurrentVolume;
	
	// private String mRegisterScorePCodeResponse;
	// =============================================================================================================
	// private SensorManager mSensorMgr;
	// =============================================================================================================
	Handler GUIUpdateTimerHandler;

	String mStringLocalConfig;
	
	final Handler mUIProcessCardHandler = new Handler();
	Activity mActivity;
	
	public MagTekModel(Activity activity){
		mActivity = activity;
	}
	
	String getConfigurationLocal()
	{
		String strXMLConfig="";
		try
		{
			strXMLConfig = ReadSettings(mActivity.getApplicationContext(),CONFIG_FILE);
			if(strXMLConfig==null)strXMLConfig="";
		}
		catch (Exception ex)
		{
		}
		
		return strXMLConfig;
	
	}
	void setConfigurationLocal(String lpstrConfig)
	{
		try
		{
			WriteSettings(mActivity.getApplicationContext(),lpstrConfig,CONFIG_FILE);
		}
		catch (Exception ex)
		{
			
		}
		
	}
	void dumpWebConfigResponse(ProcessMessageResponse lpMessageResponse)
	{
		String strDisplay="";
		try
		{
            
				if(lpMessageResponse!=null)
				{
					if(lpMessageResponse.Payload!=null)
					{
						if(lpMessageResponse.Payload.StatusCode!= null)
						{
							if(lpMessageResponse.Payload.StatusCode.Number==0)
							{
								if(lpMessageResponse.Payload.SCRAConfigurations.size() > 0)
								{
									for (int i=0; i < lpMessageResponse.Payload.SCRAConfigurations.size();i++)
									{
										SCRAConfiguration tConfig = (SCRAConfiguration) lpMessageResponse.Payload.SCRAConfigurations.elementAt(i);
										strDisplay="********* Config:" + Integer.toString(i+1) + "***********\n";
										
										strDisplay+="DeviceInfo:Model:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_MODEL) + "\n";
										strDisplay+="DeviceInfo:Device:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_DEVICE) + "\n";
										strDisplay+="DeviceInfo:Firmware:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_FIRMWARE) + "\n";
										strDisplay+="DeviceInfo.Platform:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_PLATFORM) + "\n";
										strDisplay+="DeviceInfo:Product:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_PRODUCT) + "\n";
										strDisplay+="DeviceInfo:Release:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_RELEASE) + "\n";
										strDisplay+="DeviceInfo:SDK:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_SDK) + "\n";
										strDisplay+="DeviceInfo:Status:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_STATUS)+ "\n";
										//Status = 0 Unknown
										//Status = 1 Tested and Passed 
										//Status = 2 Tested and Failed 
										strDisplay+="ReaderType.Name:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_NAME) + "\n";
										strDisplay+="ReaderType.Type:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_TYPE) + "\n";
										strDisplay+="ReaderType.Version:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_VERSION) + "\n";
										strDisplay+="ReaderType.SDK:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_SDK) + "\n";
										strDisplay+="StatusCode.Description:" + tConfig.StatusCode.Description + "\n";
										strDisplay+="StatusCode.Number:" + tConfig.StatusCode.Number + "\n";
										strDisplay+="StatusCode.Version:" + tConfig.StatusCode.Version + "\n";
										for (int j=0; j < tConfig.ConfigParams.size();j++)
										{
											strDisplay+="ConfigParam.Name:" + ((ConfigParam)tConfig.ConfigParams.elementAt(j)).Name + "\n";
											strDisplay+="ConfigParam.Type:" + ((ConfigParam)tConfig.ConfigParams.elementAt(j)).Type + "\n";
											strDisplay+="ConfigParam.Value:" + ((ConfigParam)tConfig.ConfigParams.elementAt(j)).Value + "\n";
										}//for (int j=0; j < tConfig.ConfigParams.size();j++)
										strDisplay+="*********  Config:" + Integer.toString(i+1) + "***********\n";
										debugMsg(strDisplay);
									}//for (int i=0; i < lpMessageResponse.Payload.SCRAConfigurations.size();i++)
									//debugMsg(strDisplay);
								}//if(lpMessageResponse.Payload.SCRAConfigurations.size() > 0)
								
							}//if(lpMessageResponse.Payload.StatusCode.Number==0)
							strDisplay= "Payload.StatusCode.Version:" + lpMessageResponse.Payload.StatusCode.getProperty(StatusCode.PROP_VERSION) + "\n";
							strDisplay+="Payload.StatusCode.Number:" + lpMessageResponse.Payload.StatusCode.getProperty(StatusCode.PROP_NUMBER) + "\n";
							strDisplay+="Payload.StatusCode.Description:" + lpMessageResponse.Payload.StatusCode.getProperty(StatusCode.PROP_DESCRIPTION) + "\n";
							debugMsg(strDisplay);
						}//if(lpMessageResponse.Payload.StatusCode!= null)
							
					}//if(lpMessageResponse.Payload!=null)
				}//if(lpMessageResponse!=null)
				else
				{
					debugMsg("Configuration Not Found");
				}
			
		}
		catch(Exception ex)
		{
			debugMsg("Exception:" + ex.getMessage());
		}
		
	}
	
	void dumpWebConfigResponse(String lpstrXML)
	{
		debugMsg(lpstrXML);
		
	}

	public void openDevice()
	{
		try
		{
			if(mMTSCRA.getDeviceType()==MagTekSCRA.DEVICE_TYPE_AUDIO)
			{
				setupAudioParameters();
				
			}
			mMTSCRA.openDevice();
			
		}
		catch(MTSCRAException ex)
		{
			
		}
	}


	public void ClearCardDataBuffer() {
		mMTSCRA.clearBuffers();

	}
	
	void setAudioConfigManual()throws MTSCRAException
	{
    	String model = android.os.Build.MODEL.toUpperCase(Locale.US);
		try
		{
	    	if(model.contains("DROID RAZR") || model.toUpperCase(Locale.US).contains("XT910"))
	        {
				  debugMsg("Found Setting for :"  + model); 
				   mMTSCRA.setConfigurationParams("INPUT_SAMPLE_RATE_IN_HZ=48000,");
	        }
	        else if ((model.equals("DROID PRO"))||
	        		 (model.equals("MB508"))||
	        		 (model.equals("DROIDX"))||
	        		 (model.equals("DROID2"))||
	        		 (model.equals("MB525")))
	        {
				  debugMsg("Found Setting for :"  + model); 
				  mMTSCRA.setConfigurationParams("INPUT_SAMPLE_RATE_IN_HZ=32000,");
	        }    	
	        else if ((model.equals("GT-I9300"))||//S3 GSM Unlocked
	        		 (model.equals("SPH-L710"))||//S3 Sprint
	        		 (model.equals("SGH-T999"))||//S3 T-Mobile
	        		 (model.equals("SCH-I535"))||//S3 Verizon
	        		 (model.equals("SCH-R530"))||//S3 US Cellular
	        		 (model.equals("SAMSUNG-SGH-I747"))||// S3 AT&T
	        		 (model.equals("M532"))||//Fujitsu
	        		 (model.equals("GT-N7100"))||//Notes 2 
	        		 (model.equals("GT-N7105"))||//Notes 2 
	        		 (model.equals("SAMSUNG-SGH-I317"))||// Notes 2
	        		 (model.equals("SCH-I605"))||// Notes 2
	        		 (model.equals("SCH-R950"))||// Notes 2
	        		 (model.equals("SGH-T889"))||// Notes 2
	        		 (model.equals("SPH-L900"))||// Notes 2
	        		 (model.equals("GT-P3113")))//Galaxy Tab 2, 7.0
	        		
	        {
				  debugMsg("Found Setting for :"  + model); 
	        	  mMTSCRA.setConfigurationParams("INPUT_AUDIO_SOURCE=VRECOG,");
	        }
	        else if ((model.equals("XT907")))
	        {
				  debugMsg("Found Setting for :"  + model); 
				  mMTSCRA.setConfigurationParams("INPUT_WAVE_FORM=0,");
	        }    	
	    	
	        else
	        {
				  debugMsg("Found Setting for :"  + model); 
	        	  mMTSCRA.setConfigurationParams("INPUT_AUDIO_SOURCE=VRECOG,");
	        }
			
		}
		catch(MTSCRAException ex)
		{
			debugMsg("Exception:" + ex.getMessage());
			throw new MTSCRAException(ex.getMessage());
		}
		
	}
	void setupAudioParameters()throws MTSCRAException
    {
		mStringLocalConfig="";
		
		try
		{
            
			//Option 1
			/*
			if (mGetConfigFromWeb.isChecked())
			{
			  debugMsg("Retrieve Configuration From Web....");
			  mMTSCRA.setConfiguration(CONFIGWS_READERTYPE,null,CONFIGWS_URL,10000);//Call Web Service to retrieve XML
			  return;

			}
			*/
			

			//Option 2
			/*
			if (mGetConfigFromWeb.isChecked())
			{
			  debugMsg("Retrieve Configuration From Web....");
			  
			  ProcessMessageResponse pResponse = mMTSCRA.getConfigurationResponse(CONFIGWS_USERNAME,CONFIGWS_PASSWORD,CONFIGWS_READERTYPE,null,CONFIGWS_URL,10000);
			  if(pResponse!=null)
			  {
				  dumpWebConfigResponse(pResponse);
				  mMTSCRA.setConfigurationResponse(pResponse); 
			  }
			  return;
			}
			*/
			
			
			//Option 3
			
			String strXMLConfig="";
			if (strXMLConfig.length() <= 0)
			{
				debugMsg("Setting Configuration Locally From XML....");
				dumpWebConfigResponse(strXMLConfig);
				mMTSCRA.setConfigurationXML(strXMLConfig);//Convert XML to Response Object
				mStringLocalConfig=strXMLConfig;
				return;
			}
			
		}
		catch(MTSCRAException ex)
		{
			debugMsg("Exception:" + ex.getMessage());
			//throw new MTSCRAException(ex.getMessage());
		}
		
		//web configuration failed use local
		//The code below is only needed if configuration needs to be set manually
		//for some reason
		debugMsg("Setting Configuration Manually....");
		try
		{
			setAudioConfigManual();
			return;
			
		}
		catch(MTSCRAException ex)
		{
			debugMsg("Exception:" + ex.getMessage());
			throw new MTSCRAException(ex.getMessage());
		}
    }

	private Handler StatusTextUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.string.app_name:
				if(mMTSCRA.isDeviceConnected())
				{

				//	mAppStatusTextView.setText(R.string.title_connected);
				}
				else
				{

					//mAppStatusTextView.setText(R.string.title_not_connected);
				}
				break;
			default:

				//mAppStatusTextView.setText(msg.what);
				break;
			}
			mLongTimerInterval = 0;

		}
	};
	private Handler StatusColorUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//mAppStatusTextView.setBackgroundColor(msg.what);
			mLongTimerInterval = 0;
		}
	};
	
    void ShowSoftKeyboard (EditText lpEditText)
    {
		InputMethodManager objInputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
		// only will trigger it if no physical keyboard is open
		objInputManager.showSoftInput(lpEditText, InputMethodManager.SHOW_IMPLICIT);					

    }
    void HideSoftKeyboard (EditText lpEditText)
    {
		//Hide Keyboard
		InputMethodManager objInputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
		objInputManager.hideSoftInputFromWindow(lpEditText.getWindowToken(), 0);
    	
    }
	void InitializeData() 
	{
	    mMTSCRA.clearBuffers();
		mLongTimerInterval = 0;
//		miReadCount=0;
		mbAudioConnected=false;
		mIntCurrentVolume=0;
		mIntCurrentStatus = STATUS_IDLE;
		mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_DISCONNECTED;
		
		mStringDebugData ="";
		
	}
	void debugMsg(String lpstrMessage)
	{
		Log.i("MagTekSCRA.Demo:",lpstrMessage);
		
	}
	void clearScreen() 
	{
		//mCardDataEditText.setText("");
	}
	void clearAll() 
	{
	//	ClearCardDataBuffer();
	//	ClearScreen();
		mIntCurrentStatus = STATUS_IDLE;
//		miReadCount = 0;
		mStringDebugData="";
		displayInfo();

	}
	void displayInfo()
	{
		//ActivityManager tActivityManager =(ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		//MemoryInfo tMemoryInfo = new ActivityManager.MemoryInfo();
		//tActivityManager.getMemoryInfo(tMemoryInfo);		
		//String strLog = "SwipeCount=" + miReadCount + ",Memory=" + tMemoryInfo.availMem;
		String strVersion = "";
		
		try
		{
			PackageInfo pInfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
			strVersion =  pInfo.versionName;
			
		}
		catch(Exception ex)
		{
			
		}
		String strLog = "App.Version=" +strVersion + ",SDK.Version=" + mMTSCRA.getSDKVersion(); 
		//debugMsg(strLog);
		//-TODO
		//mInfoTextView.setText(strLog);
		//tMemoryInfo=null;
		//tActivityManager=null;
		
	}
	private void maxVolume()
	{
		mAudioMgr.setStreamVolume(AudioManager.STREAM_MUSIC,mAudioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC),AudioManager.FLAG_SHOW_UI);	
			    	
	                
	}
	private void minVolume()
	{
		mAudioMgr.setStreamVolume(AudioManager.STREAM_MUSIC,mIntCurrentVolume, AudioManager.FLAG_SHOW_UI);
		
	}
	
	
	public static String ReadSettings(Context context, String file) throws IOException {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		String data = null;
		fis = context.openFileInput(file);
		isr = new InputStreamReader(fis);
		char[] inputBuffer = new char[fis.available()];
		isr.read(inputBuffer);
		data = new String(inputBuffer);
		isr.close();
		fis.close();
		return data;
	}
	
	public static void WriteSettings(Context context, String data, String file) throws IOException {
		FileOutputStream fos= null;
		OutputStreamWriter osw = null;
		fos= context.openFileOutput(file,Context.MODE_PRIVATE);
		osw = new OutputStreamWriter(fos);
		osw.write(data);
		osw.close();
		fos.close();
	}
	private class SCRAHandlerCallback implements Callback {
        public boolean handleMessage(Message msg) 
        {
        	
        	try
        	{
            	switch (msg.what) 
            	{
    			case MagTekSCRA.DEVICE_MESSAGE_STATE_CHANGE:
    				switch (msg.arg1) {
    				case MagTekSCRA.DEVICE_STATE_CONNECTED:
    					mIntCurrentStatus = STATUS_IDLE;
    					mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_CONNECTED;    					
    					maxVolume();
    					setStatus(R.string.title_connected, Color.GREEN);
    					break;
    				case MagTekSCRA.DEVICE_STATE_CONNECTING:
    					mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_CONNECTING;
    					setStatus(R.string.title_connecting, Color.YELLOW);
    					break;
    				case MagTekSCRA.DEVICE_STATE_DISCONNECTED:
    					mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_DISCONNECTED;
    					setStatus(R.string.title_not_connected, Color.RED);
    					minVolume();
    					break;
    				}
    				break;
    			case MagTekSCRA.DEVICE_MESSAGE_DATA_START:
    	        	if (msg.obj != null) 
    	        	{
    	        		debugMsg("Transfer started");
    	        		//mCardDataEditText.setText("Card Swiped...");
    	                return true;
    	            }
    				break;  
    			case MagTekSCRA.DEVICE_MESSAGE_DATA_CHANGE:
    	        	if (msg.obj != null) 
    	        	{
    	        		debugMsg("Transfer ended");
//    	        		miReadCount++;
    	        		displayInfo();
    	        		displayResponseData();
    	        		msg.obj=null;
    	        		if(mStringLocalConfig.length() > 0)
    	        		{
    						setConfigurationLocal(mStringLocalConfig);//optional but can be useful to retrieve from locally and get it from server only certain times
    						mStringLocalConfig="";
    	        		}

    	                return true;
    	            }
    				break;  
    			case MagTekSCRA.DEVICE_MESSAGE_DATA_ERROR:
	        	//	mCardDataEditText.setText("Card Swipe Error... Please Swipe Again.\n");
	                return true;
    			default:
    	        	if (msg.obj != null) 
    	        	{
    	                return true;
    	            }
    				break;
            	};
        		
        	}
        	catch(Exception ex)
        	{
        		
        	}
        	
            return false;
        	
        	
        }
    }
    final String TAG = MagTekModel.class.getSimpleName();

    void displayResponseData()
    {
    	
		String strDisplay="";

		
		String strResponse =  mMTSCRA.getResponseData();
		if(strResponse!=null)
		{
			strDisplay =  strDisplay + "Response.Length=" +strResponse.length()+ "\n";		
		}
		
		strDisplay =  strDisplay + "EncryptionStatus=" + mMTSCRA.getEncryptionStatus() + "\n";
		strDisplay =  strDisplay + "SDK.Version=" + mMTSCRA.getSDKVersion() + "\n";
		strDisplay =  strDisplay + "Reader.Type=" + mMTSCRA.getDeviceType() + "\n";
		strDisplay =  strDisplay + "Track.Status=" + mMTSCRA.getTrackDecodeStatus() + "\n";
		strDisplay =  strDisplay + "KSN=" + mMTSCRA.getKSN()+ "\n";
		strDisplay =  strDisplay + "Track1.Masked=" + mMTSCRA.getTrack1Masked() + "\n";
		strDisplay =  strDisplay + "Track2.Masked=" + mMTSCRA.getTrack2Masked() + "\n";
		strDisplay =  strDisplay + "Track3.Masked=" + mMTSCRA.getTrack3Masked() + "\n";
		strDisplay =  strDisplay + "Track1.Encrypted=" + mMTSCRA.getTrack1() + "\n";
		strDisplay =  strDisplay + "Track2.Encrypted=" + mMTSCRA.getTrack2() + "\n";
		strDisplay =  strDisplay + "Track3.Encrypted=" + mMTSCRA.getTrack3() + "\n";  
		strDisplay =  strDisplay + "MagnePrint.Encrypted=" + mMTSCRA.getMagnePrint() + "\n";  
		strDisplay =  strDisplay + "MagnePrint.Status=" + mMTSCRA.getMagnePrintStatus() + "\n";  
		strDisplay =  strDisplay + "Card.IIN=" + mMTSCRA.getCardIIN() + "\n";
		strDisplay =  strDisplay + "Card.Name=" + mMTSCRA.getCardName() + "\n";
		strDisplay =  strDisplay + "Card.Last4=" + mMTSCRA.getCardLast4() + "\n";    	        	
		strDisplay =  strDisplay + "Card.ExpDate=" + mMTSCRA.getCardExpDate() + "\n";
		strDisplay =  strDisplay + "Card.SvcCode=" + mMTSCRA.getCardServiceCode() + "\n";
		strDisplay =  strDisplay + "Card.PANLength=" + mMTSCRA.getCardPANLength() + "\n";    
		strDisplay =  strDisplay + "Device.Serial=" + mMTSCRA.getDeviceSerial()+ "\n"; 
		strDisplay =  strDisplay  + "SessionID=" + mMTSCRA.getSessionID() + "\n";
		

		Log.d(TAG, "Encryption Status: "+mMTSCRA.getEncryptionStatus());
		Log.d(TAG, "device(Reader) type: "+mMTSCRA.getDeviceType());
		Log.d(TAG, "Track Decode Status: "+mMTSCRA.getTrackDecodeStatus());
		Log.d(TAG, "KSN: "+mMTSCRA.getKSN());
		Log.d(TAG, "Track1 Masked: "+mMTSCRA.getTrack1Masked());
		Log.d(TAG, "Track2 Masked: "+mMTSCRA.getTrack2Masked());
		Log.d(TAG, "Track3 Masked: "+mMTSCRA.getTrack3Masked());
		Log.d(TAG, "Track1: "+mMTSCRA.getTrack1());
		Log.d(TAG, "Track2: "+mMTSCRA.getTrack2());
		Log.d(TAG, "Track3: "+mMTSCRA.getTrack3());
		Log.d(TAG, "MagnePrint.Encrypted: "+mMTSCRA.getMagnePrint());
		Log.d(TAG, "MagnePrint.status: "+mMTSCRA.getMagnePrintStatus());
		Log.d(TAG, "Card.IIN=" + mMTSCRA.getCardIIN());
		Log.d(TAG, "Card.Name=" + mMTSCRA.getCardName());
		Log.d(TAG, "Card.Last4=" + mMTSCRA.getCardLast4());    	        	
		Log.d(TAG, "Card.ExpDate=" + mMTSCRA.getCardExpDate());
		Log.d(TAG, "Card.ServiceCode=" + mMTSCRA.getCardServiceCode());
		Log.d(TAG, "Card.PANLength=" + mMTSCRA.getCardPANLength());    
		Log.d(TAG, "Device.Serial=" + mMTSCRA.getDeviceSerial()); 
		Log.d(TAG, "SessionID=" + mMTSCRA.getSessionID());
		
		switch(mMTSCRA.getDeviceType())
		{
		case MagTekSCRA.DEVICE_TYPE_AUDIO:
			strDisplay =  strDisplay  + "Card.Status=" + mMTSCRA.getCardStatus() + "\n";
			strDisplay =  strDisplay  + "Firmware.Partnumber=" + mMTSCRA.getFirmware()+ "\n";
			strDisplay =  strDisplay  + "MagTek.SN=" + mMTSCRA.getMagTekDeviceSerial()+ "\n";
			strDisplay =  strDisplay  + "TLV.Version=" + mMTSCRA.getTLVVersion()+ "\n";
			strDisplay =  strDisplay  + "HashCode=" + mMTSCRA.getHashCode()+ "\n";
			String tstrTkStatus = mMTSCRA.getTrackDecodeStatus();
			String tstrTk1Status="01";
			String tstrTk2Status="01";
			String tstrTk3Status="01";
			
			if(tstrTkStatus.length() >=6)
			{
				tstrTk1Status=tstrTkStatus.substring(0,2);
				tstrTk2Status=tstrTkStatus.substring(2,4);
				tstrTk3Status=tstrTkStatus.substring(4,6);
				debugMsg("Track1.Status=" + tstrTk1Status );
				debugMsg("Track2.Status=" + tstrTk2Status );
				debugMsg("Track3.Status=" + tstrTk3Status );
				if ((!tstrTk1Status.equalsIgnoreCase("01"))&&
					(!tstrTk2Status.equalsIgnoreCase("01"))&&	
					(!tstrTk3Status.equalsIgnoreCase("01")))
				{
					closeDevice();
				}
			}
			else
			{
				closeDevice();
			}
			break;
		case MagTekSCRA.DEVICE_TYPE_BLUETOOTH:
			strDisplay =  strDisplay  + "CardDataCRC=" + mMTSCRA.getCardDataCRC() + "\n";
			
			break;
		default:
			break;
		
		};
		if(strResponse!=null)
		{
			strDisplay =  strDisplay + "Response.Raw=" + strResponse + "\n";		
		}
		
		mStringDebugData = strDisplay;
		//mCardDataEditText.setText(strDisplay);
    	
    }   
	
	public void closeDevice()
	{
		mMTSCRA.closeDevice();
	}
	
	public void setStatus(int lpiStatus, int lpiColor) 
	{
		StatusTextUpdateHandler.sendEmptyMessage(lpiStatus);
		StatusColorUpdateHandler.sendEmptyMessage(lpiColor);
	}
	
	
	
}
