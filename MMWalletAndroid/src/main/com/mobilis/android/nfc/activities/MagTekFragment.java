/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobilis.android.nfc.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.magtek.mobile.android.scra.ConfigParam;
import com.magtek.mobile.android.scra.MTSCRAException;
import com.magtek.mobile.android.scra.MagTekSCRA;
import com.magtek.mobile.android.scra.ProcessMessageResponse;
import com.magtek.mobile.android.scra.SCRAConfiguration;
import com.magtek.mobile.android.scra.SCRAConfigurationDeviceInfo;
import com.magtek.mobile.android.scra.SCRAConfigurationReaderType;
import com.magtek.mobile.android.scra.StatusCode;
import com.mobilis.android.nfc.domain.INTENT;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

//import com.magtek.mobile.android.scra.ArrayOfConfigParam;

/**
 * This is the main Activity that displays the current chat session.
 */
public class MagTekFragment extends ApplicationActivity.PlaceholderFragment{

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

    private static final int CONFIGWS_READERTYPE = 0;
    private static final String CONFIGWS_USERNAME = "magtek";
    private static final String CONFIGWS_PASSWORD = "p@ssword";


    private AudioManager mAudioMgr;


    public static final String DEVICE_NAME = "device_name";
    public static final String CONFIG_FILE = "MTSCRADevConfig.cfg";
    //	public static final String TOAST = "toast";
    public static final String PARTIAL_AUTH_INDICATOR = "1";
    private static final boolean mShowTitle = false;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;

    private MagTekSCRA mMTSCRA;
    //private int miDeviceType=MagTekSCRA.DEVICE_TYPE_NONE;
    private Handler mSCRADataHandler = new Handler(new SCRAHandlerCallback());
    final headSetBroadCastReceiver mHeadsetReceiver = new headSetBroadCastReceiver();
    final NoisyAudioStreamReceiver mNoisyAudioStreamReceiver = new NoisyAudioStreamReceiver();

    // Layout Views
//    private TextView mTitleLeftTextView;
//    private TextView mAppStatusTextView;
//    private EditText mCardDataEditText;
//    private TextView mInfoTextView;
    //	private int miReadCount=0;
//    private String mStringDebugData;
//    private CheckBox mGetConfigFromWeb;
//
//    private ImageButton mClearImageButton;
//    private ImageButton mSubmitImageButton;
    String mStringLocalConfig;

    private int mIntCurrentDeviceStatus;


    private RelativeLayout mTitleLayout;

    // =============================================================================================================
    //private Boolean mBooleanBTConnect;

    private boolean mbAudioConnected;

    private long mLongTimerInterval;

    private int mIntCurrentStatus;

    private int mIntCurrentVolume;

    private String mStringAudioConfigResult;


    // private String mRegisterScorePCodeResponse;
    // =============================================================================================================
    // private SensorManager mSensorMgr;
    // =============================================================================================================
    Handler GUIUpdateTimerHandler;

    final Handler mUIProcessCardHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the window layout
//        if (mShowTitle) {
//            getActivity().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//        }// if(mShowTitle)
//        else {
//            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        }
//        setContentView(R.layout.main);
//
//        if (mShowTitle)
//        {
//            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
//                    R.layout.custom_title);
//        }// if(mShowTitle)

//        // Set up the custom title
//        if (mShowTitle)
//        {
//            mTitleLeftTextView = (TextView) findViewById(R.id.title_left_text);
//            mTitleLeftTextView.setText(R.string.app_title);
//            mTitleLayout = (RelativeLayout) findViewById(R.id.relative_layout_title);
//            mTitleLayout.setVisibility(View.INVISIBLE);
//        }// if(mShowTitle)
//
//        mAppStatusTextView = (TextView) findViewById(R.id.textview_app_status);
//        mInfoTextView = (TextView) findViewById(R.id.textview_info);
//        mCardDataEditText= (EditText) findViewById(R.id.edittext_carddata);
//        mGetConfigFromWeb=(CheckBox) findViewById(R.id.checkbox_getconfig);
//        mClearImageButton = (ImageButton) findViewById(R.id.imagebutton_clear);
//        mSubmitImageButton= (ImageButton) findViewById(R.id.imagebutton_submit);

        mMTSCRA = new MagTekSCRA(mSCRADataHandler);
        mAudioMgr = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
//        setStatus(R.string.status_default, Color.RED);
        InitializeData();

        mIntCurrentVolume = mAudioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);


//        mClearImageButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v)
//            {
//                clearAll();
//            }
//        });
//
//        mSubmitImageButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v)
//            {
//                debugMsg("Android.Model=" + android.os.Build.MODEL);
//                debugMsg("Android.Device=" + android.os.Build.DEVICE);
//                debugMsg("Android.Product=" + android.os.Build.PRODUCT);
//
//                String mStringBody="";
//                try
//                {
//                    String strVersion = "";
//                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//                    strVersion =  pInfo.versionName;
//                    mStringBody = "App.Version=" + strVersion + "\nSDK.Version=" + mMTSCRA.getSDKVersion() + "\n");
//
//                }
//                catch(Exception ex)
//                {
//
//                }
//                mStringBody += "Android.Model=" + android.os.Build.MODEL + "\n");
//                mStringBody += "Android.Device=" + android.os.Build.DEVICE + "\n");
//                mStringBody += "Android.Product=" + android.os.Build.PRODUCT + "\n");
//                mStringBody += mStringDebugData;
//                mCardDataEditText.setText(mStringBody);
//
//				/*
//				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//				String[] recipients = new String[]{"softeng@magtek.com"};
//                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
//                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "MagTek Audio Debug Info");
//                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, mStringBody);
//                emailIntent.setType("text/plain");
//                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
//                */
//            }
//        });


        Timer tTimer = new Timer();

        TimerTask tTimerTask = new TimerTask() {
            public void run() {
                if (mMTSCRA.isDeviceConnected()) {
                    if (mLongTimerInterval >= 2) {
                        if (mMTSCRA.isDeviceConnected())
                        {
                            if (mIntCurrentStatus == STATUS_IDLE)
                            {
//                                setStatus(R.string.status_default, Color.GREEN);
                            }
                        }// if(mDeviceStatus==BluetoothChatService.STATE_CONNECTED)
                        else
                        {
//                            setStatus(R.string.status_default, Color.RED);
                        }
                        mLongTimerInterval = 0;
                    }// if(mTimerInterval >= 2)

                }// if(mDeviceStatus==BluetoothChatService.STATE_CONNECTED)
                else
                {

                    if ((mIntCurrentStatus == STATUS_IDLE)&&(mIntCurrentDeviceStatus == MagTekSCRA.DEVICE_STATE_DISCONNECTED))
                    {
//                        setStatus(R.string.status_default, Color.RED);
                    }
                }
                mLongTimerInterval++;
            }
        };
        tTimer.scheduleAtFixedRate(tTimerTask, 0, 1000);
        displayInfo();


    }
    String getConfigurationLocal()
    {
        String strXMLConfig="";
        try
        {
            strXMLConfig = ReadSettings(getActivity().getApplicationContext(),CONFIG_FILE);
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
            WriteSettings(getActivity().getApplicationContext(),lpstrConfig,CONFIG_FILE);
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

                                    Log.d(TAG,"DeviceInfo:Model:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_MODEL) + "\n");
                                    Log.d(TAG,"DeviceInfo:Device:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_DEVICE) + "\n");
                                    Log.d(TAG,"DeviceInfo:Firmware:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_FIRMWARE) + "\n");
                                    Log.d(TAG,"DeviceInfo.Platform:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_PLATFORM) + "\n");
                                    Log.d(TAG,"DeviceInfo:Product:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_PRODUCT) + "\n");
                                    Log.d(TAG,"DeviceInfo:Release:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_RELEASE) + "\n");
                                    Log.d(TAG,"DeviceInfo:SDK:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_SDK) + "\n");
                                    Log.d(TAG,"DeviceInfo:Status:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_STATUS)+ "\n");
                                    //Status = 0 Unknown
                                    //Status = 1 Tested and Passed
                                    //Status = 2 Tested and Failed
                                    Log.d(TAG,"ReaderType.Name:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_NAME) + "\n");
                                    Log.d(TAG,"ReaderType.Type:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_TYPE) + "\n");
                                    Log.d(TAG,"ReaderType.Version:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_VERSION) + "\n");
                                    Log.d(TAG,"ReaderType.SDK:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_SDK) + "\n");
                                    Log.d(TAG,"StatusCode.Description:" + tConfig.StatusCode.Description + "\n");
                                    Log.d(TAG,"StatusCode.Number:" + tConfig.StatusCode.Number + "\n");
                                    Log.d(TAG,"StatusCode.Version:" + tConfig.StatusCode.Version + "\n");
                                    for (int j=0; j < tConfig.ConfigParams.size();j++)
                                    {
                                        Log.d(TAG,"ConfigParam.Name:" + ((ConfigParam)tConfig.ConfigParams.elementAt(j)).Name + "\n");
                                        Log.d(TAG,"ConfigParam.Type:" + ((ConfigParam)tConfig.ConfigParams.elementAt(j)).Type + "\n");
                                        Log.d(TAG,"ConfigParam.Value:" + ((ConfigParam)tConfig.ConfigParams.elementAt(j)).Value + "\n");
                                    }//for (int j=0; j < tConfig.ConfigParams.size();j++)
                                    Log.d(TAG,"*********  Config:" + Integer.toString(i+1) + "***********\n");
                                    debugMsg(strDisplay);
                                }//for (int i=0; i < lpMessageResponse.Payload.SCRAConfigurations.size();i++)
                                //debugMsg(strDisplay);
                            }//if(lpMessageResponse.Payload.SCRAConfigurations.size() > 0)

                        }//if(lpMessageResponse.Payload.StatusCode.Number==0)
                        Log.d(TAG,"Payload.StatusCode.Version:" + lpMessageResponse.Payload.StatusCode.getProperty(StatusCode.PROP_VERSION) + "\n");
                        Log.d(TAG,"Payload.StatusCode.Number:" + lpMessageResponse.Payload.StatusCode.getProperty(StatusCode.PROP_NUMBER) + "\n");
                        Log.d(TAG,"Payload.StatusCode.Description:" + lpMessageResponse.Payload.StatusCode.getProperty(StatusCode.PROP_DESCRIPTION) + "\n");
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

    void setAudioConfigManual()throws MTSCRAException
    {
        String model = android.os.Build.MODEL.toUpperCase();
        try
        {
            if(model.contains("DROID RAZR") || model.toUpperCase().contains("XT910"))
            {
                debugMsg("Found Setting for :"  + model);
                mMTSCRA.setConfigurationParams("INPUT_SAMPLE_RATE_IN_HZ=48000,");
                setStatusMessage("Found Setting for :"  + model + ":INPUT_SAMPLE_RATE_IN_HZ=48000");
            }
            else if ((model.equals("DROID PRO"))||
                    (model.equals("MB508"))||
                    (model.equals("DROIDX"))||
                    (model.equals("DROID2"))||
                    (model.equals("MB525")))
            {
                debugMsg("Found Setting for :"  + model);
                setStatusMessage("Found Setting for :"  + model + ":INPUT_SAMPLE_RATE_IN_HZ=32000");
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
                    (model.equals("SAMSUNG-SGH-I337"))||// S4
                    (model.equals("GT-P3113")))//Galaxy Tab 2, 7.0

            {
                setStatusMessage("Found Setting for :"  + model + ":INPUT_AUDIO_SOURCE=VRECOG");
                debugMsg("Found Setting for :"  + model);
                mMTSCRA.setConfigurationParams("INPUT_AUDIO_SOURCE=VRECOG,");
            }
            else if ((model.equals("XT907")))
            {
                debugMsg("Found Setting for :"  + model);
                setStatusMessage("Found Setting for :"  + model + ":INPUT_WAVE_FORM=0");
                mMTSCRA.setConfigurationParams("INPUT_WAVE_FORM=0,");
            }
            else
            {
                setStatusMessage("Using Default Settings For :"  + model);
            }
        }
        catch(MTSCRAException ex)
        {
            debugMsg("Exception:" + ex.getMessage());
            throw new MTSCRAException(ex.getMessage());
        }

    }
    String setupAudioParameters()throws MTSCRAException
    {
        mStringLocalConfig="";
        String strResult="OK";

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

            setStatusMessage("Setting up Audio");

            //Option 3

            String strXMLConfig="";
//            if (!mGetConfigFromWeb.isChecked())
//            {
//                strXMLConfig = getConfigurationLocal();//retrieve saved configuration. This is optional but useful if the web service connection
//                //is not available or sluggish for some reason. It is important to provide a way to
//                //sync the local configuration to server configuration to keep the local phone config updated
//            }


            if (strXMLConfig.length() <= 0)
            {
//                if (mGetConfigFromWeb.isChecked())
//                {
//                    debugMsg("Retrieve Configuration From Web....");
//                    setStatusMessage("Retrieve Configuration From Web");
//                    SCRAConfigurationDeviceInfo pDeviceInfo = new SCRAConfigurationDeviceInfo();
//                    pDeviceInfo.setProperty(SCRAConfigurationDeviceInfo.PROP_PLATFORM,"Android");
//                    pDeviceInfo.setProperty(SCRAConfigurationDeviceInfo.PROP_MODEL,android.os.Build.MODEL.toUpperCase());
//                    //pDeviceInfo.setProperty(SCRAConfigurationDeviceInfo.PROP_MODEL,"SPH-L720");
//                    strXMLConfig = mMTSCRA.getConfigurationXML(CONFIGWS_USERNAME,CONFIGWS_PASSWORD,CONFIGWS_READERTYPE,pDeviceInfo,CONFIGWS_URL,10000);//Call Web Service to retrieve XML
//                    if (strXMLConfig.length() > 0)
//                    {
//                        setStatusMessage("Configuration Received From Server\n******************************\n" + strXMLConfig + "\n******************************\n");
//                        ProcessMessageResponse pResponse = mMTSCRA.getConfigurationResponse(strXMLConfig);
//                        if(pResponse!=null)
//                        {
//                            dumpWebConfigResponse(pResponse);
//                            debugMsg("Setting Configuration From Response....");
//                            mMTSCRA.setConfigurationResponse(pResponse);
//                        }
//                        mStringLocalConfig=strXMLConfig;
//                        setStatusMessage("SDK Configuration Was Set Successful.\nPlease Swipe A Card....\n");
//                        return strResult;
//                    }//if (strXMLConfig.length() > 0)
//                    else
//                    {
//                        setStatusMessage("No Configuration Received, Using Default");
//                        strResult="Error:" + "No Configuration Received, Using Default";
//                        return strResult;
//
//                    }
//                }
//                else
//                {
                    setAudioConfigManual();
//                }
            }
            else
            {
                debugMsg("Setting Configuration Locally From XML....");
                setStatusMessage("Configuration Saved Locally\n******************************\n" + strXMLConfig + "\n******************************\n");
                dumpWebConfigResponse(strXMLConfig);
                mMTSCRA.setConfigurationXML(strXMLConfig);//Convert XML to Response Object
                mStringLocalConfig=strXMLConfig;
                return strResult;
            }

        }
        catch(MTSCRAException ex)
        {
            debugMsg("Exception:" + ex.getMessage());
            strResult = "Error:" +  ex.getMessage();
            setStatusMessage("Failed Retrieving Configuration From Server:" + strResult);
            //throw new MTSCRAException(ex.getMessage());
        }
        return strResult;
    }
    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.

    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        getActivity().registerReceiver(mHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        getActivity().registerReceiver(mNoisyAudioStreamReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity
        // returns.
    }


    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        getActivity().unregisterReceiver(mHeadsetReceiver);
        getActivity().unregisterReceiver(mNoisyAudioStreamReceiver);
        if (mMTSCRA != null)
            closeDevice();

    }
    private void openDevice()
    {
        Log.d(TAG,"openDevice() is called");
//        if(mMTSCRA.getDeviceType()==MagTekSCRA.DEVICE_TYPE_AUDIO)
//        {
            Thread tSetupAudioParams = new Thread() {
                public void run()
                {
                    try
                    {
                        mStringAudioConfigResult = setupAudioParameters();
                    }
                    catch(Exception ex)
                    {
                        mStringAudioConfigResult = "Error:" + ex.getMessage();
                    }
                    mUIProcessCardHandler.post(mUISetupAudioParamsResults);
                }
            };
            tSetupAudioParams.start();

//        }
//        else
//        {
//            mMTSCRA.openDevice();
//        }
    }
    final  Runnable mUISetupAudioParamsResults = new Runnable() {
        public void run() {
            try
            {
                if(!mStringAudioConfigResult.equalsIgnoreCase("OK"))
                {
                    //web configuration failed use local
                    //The code below is only needed if configuration needs to be set manually
                    //for some reason
                    debugMsg("Setting Configuration Manually....");
                    try
                    {
                        setAudioConfigManual();

                    }
                    catch(MTSCRAException ex)
                    {
                        debugMsg("Exception:" + ex.getMessage());
                        throw new MTSCRAException(ex.getMessage());
                    }

                }
                mMTSCRA.openDevice();
            } catch (Exception ex) {

            }

        }
    };
    private void closeDevice()
    {
        mMTSCRA.closeDevice();
    }

    private void ClearCardDataBuffer() {
        mMTSCRA.clearBuffers();

    }


    private void ClearScreen()
    {
//        mCardDataEditText.setText("");
    }
    private void setStatus(int lpiStatus, int lpiColor)
    {
//        StatusTextUpdateHandler.sendEmptyMessage(lpiStatus);
        StatusColorUpdateHandler.sendEmptyMessage(lpiColor);
    }

    final String TAG = MagTekFragment.class.getSimpleName();




    private void displayResponseData()
    {

        String strDisplay="";


        String strResponse =  mMTSCRA.getResponseData();
        if(strResponse!=null)
        {
            Log.d(TAG,"Response.Length=" +strResponse.length()+ "\n");
        }
        Log.d(TAG,"EncryptionStatus=" + mMTSCRA.getEncryptionStatus() + "\n");
        Log.d(TAG,"SDK.Version=" + mMTSCRA.getSDKVersion() + "\n");
        Log.d(TAG,"Reader.Type=" + mMTSCRA.getDeviceType() + "\n");
        Log.d(TAG,"Track.Status=" + mMTSCRA.getTrackDecodeStatus() + "\n");
        Log.d(TAG,"KSN=" + mMTSCRA.getKSN()+ "\n");
        Log.d(TAG,"Track1.Masked=" + mMTSCRA.getTrack1Masked() + "\n");
        Log.d(TAG,"Track2.Masked=" + mMTSCRA.getTrack2Masked() + "\n");
        Log.d(TAG,"Track3.Masked=" + mMTSCRA.getTrack3Masked() + "\n");
        Log.d(TAG,"Track1.Encrypted=" + mMTSCRA.getTrack1() + "\n");
        Log.d(TAG,"Track2.Encrypted=" + mMTSCRA.getTrack2() + "\n");
        Log.d(TAG,"Track3.Encrypted=" + mMTSCRA.getTrack3() + "\n");
        Log.d(TAG,"MagnePrint.Encrypted=" + mMTSCRA.getMagnePrint() + "\n");
        Log.d(TAG,"MagnePrint.Status=" + mMTSCRA.getMagnePrintStatus() + "\n");
        Log.d(TAG,"Card.IIN=" + mMTSCRA.getCardIIN() + "\n");
        Log.d(TAG,"Card.Name=" + mMTSCRA.getCardName() + "\n");
        Log.d(TAG,"Card.Last4=" + mMTSCRA.getCardLast4() + "\n");
        Log.d(TAG,"Card.ExpDate=" + mMTSCRA.getCardExpDate() + "\n");
        Log.d(TAG,"Card.SvcCode=" + mMTSCRA.getCardServiceCode() + "\n");
        Log.d(TAG,"Card.PANLength=" + mMTSCRA.getCardPANLength() + "\n");
        Log.d(TAG,"Device.Serial=" + mMTSCRA.getDeviceSerial()+ "\n");
        Log.d(TAG,"SessionID=" + mMTSCRA.getSessionID() + "\n");

        switch(mMTSCRA.getDeviceType())
        {
            case MagTekSCRA.DEVICE_TYPE_AUDIO:
                Log.d(TAG,"Card.Status=" + mMTSCRA.getCardStatus() + "\n");
                Log.d(TAG,"Firmware.Partnumber=" + mMTSCRA.getFirmware()+ "\n");
                Log.d(TAG,"MagTek.SN=" + mMTSCRA.getMagTekDeviceSerial()+ "\n");
                Log.d(TAG,"TLV.Version=" + mMTSCRA.getTLVVersion()+ "\n");
                Log.d(TAG,"HashCode=" + mMTSCRA.getHashCode()+ "\n");
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
                Log.d(TAG,"CardDataCRC=" + mMTSCRA.getCardDataCRC() + "\n");

                break;
            default:
                break;

        };
        if(strResponse!=null)
        {
            Log.d(TAG,"Response.Raw=" + strResponse + "\n");
        }

//        mStringDebugData = strDisplay;
//        mCardDataEditText.setText(strDisplay);

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE: {
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK)
                {
//                    String address = data.getExtras().getString(
//                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    mMTSCRA.setDeviceType(MagTekSCRA.DEVICE_TYPE_BLUETOOTH);
                    // if you know the address, you can directly specify here
                    // in that case you would not need a UI to show the list
                    // of BT devices
//                    mMTSCRA.setDeviceID(address);
                    openDevice();


                }
            }
            break;
        };
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.option_menu, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.scan:
//                return true;
//            case R.id.bluetooth:
//                // Launch the DeviceListActivity to see devices and do scan
//                if(!mMTSCRA.isDeviceConnected())
//                {
//                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
//                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
//                }
//                return true;
//            case R.id.audio:
//                clearScreen();
//                if(!mMTSCRA.isDeviceConnected())
//                {
//                    if(mbAudioConnected)
//                    {
//                        mMTSCRA.setDeviceType(MagTekSCRA.DEVICE_TYPE_AUDIO);
//                        openDevice();
//                    }
//                }
//
//                return true;
//            case R.id.disconn:
//                clearScreen();
//                if(mMTSCRA.isDeviceConnected())
//                {
//                    closeDevice();
//                }
//
//                return true;
//
//            case R.id.exit:
//                // Ensure this device is discoverable by others
//                // ensureDiscoverable();
//                if (mMTSCRA != null)
//                    closeDevice();
//
//                setResult(Activity.RESULT_OK);
//                this.finish();
//                return true;
//        }
//        return false;
//    }

//    private Handler StatusTextUpdateHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case R.string.status_default:
//                    if(mMTSCRA.isDeviceConnected())
//                    {
//                        mAppStatusTextView.setText(R.string.title_connected);
//                    }
//                    else
//                    {
//                        mAppStatusTextView.setText(R.string.title_not_connected);
//                    }
//                    break;
//                default:
//                    mAppStatusTextView.setText(msg.what);
//                    break;
//            }
//            mLongTimerInterval = 0;
//
//        }
//    };
    private Handler StatusColorUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            mAppStatusTextView.setBackgroundColor(msg.what);
            mLongTimerInterval = 0;
        }
    };

    void ShowSoftKeyboard (EditText lpEditText)
    {
        InputMethodManager objInputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        // only will trigger it if no physical keyboard is open
        objInputManager.showSoftInput(lpEditText, InputMethodManager.SHOW_IMPLICIT);

    }
    void HideSoftKeyboard (EditText lpEditText)
    {
        //Hide Keyboard
        InputMethodManager objInputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        objInputManager.hideSoftInputFromWindow(lpEditText.getWindowToken(), 0);

    }
    private void InitializeData()
    {
        mMTSCRA.clearBuffers();
        mLongTimerInterval = 0;
//		miReadCount=0;
        mbAudioConnected=false;
        mIntCurrentVolume=0;
        mIntCurrentStatus = STATUS_IDLE;
        mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_DISCONNECTED;

//        mStringDebugData ="";
        mStringAudioConfigResult="";

    }
    private void debugMsg(String lpstrMessage)
    {
        Log.i("MagTekSCRA.Demo:",lpstrMessage);

    }
    private void clearScreen()
    {
//        mCardDataEditText.setText("");
    }
    private void clearAll()
    {
        ClearCardDataBuffer();
        ClearScreen();
        mIntCurrentStatus = STATUS_IDLE;
//		miReadCount = 0;
//        mStringDebugData="";
        displayInfo();

    }
    private void displayInfo()
    {
        //ActivityManager tActivityManager =(ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        //MemoryInfo tMemoryInfo = new ActivityManager.MemoryInfo();
        //tActivityManager.getMemoryInfo(tMemoryInfo);
        //String strLog = "SwipeCount=" + miReadCount + ",Memory=" + tMemoryInfo.availMem;
        String strVersion = "";

        try
        {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            strVersion =  pInfo.versionName;

        }
        catch(Exception ex)
        {

        }
        String strLog = "App.Version=" +strVersion + ",SDK.Version=" + mMTSCRA.getSDKVersion();
        //debugMsg(strLog);
//        mInfoTextView.setText(strLog);
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
    private class SCRAHandlerCallback implements Callback {
        public boolean handleMessage(Message msg)
        {

            try
            {
                Log.d(TAG,"msg.what: "+msg.what);
                Log.d(TAG,"msg.what == MagTekSCRA.DEVICE_MESSAGE_STATE_CHANGE ? "+(msg.what == MagTekSCRA.DEVICE_MESSAGE_STATE_CHANGE));
                switch (msg.what)
                {
                     case MagTekSCRA.DEVICE_MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case MagTekSCRA.DEVICE_STATE_CONNECTED:
                                Log.d(TAG,"msg.arg1: "+"DEVICE_STATE_CONNECTED");

                                mIntCurrentStatus = STATUS_IDLE;
                                mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_CONNECTED;
                                maxVolume();
                                Toast.makeText(getActivity(), "Device is connected",Toast.LENGTH_LONG).show();
//                                setStatus(R.string.title_connected, Color.GREEN);
                                break;
                            case MagTekSCRA.DEVICE_STATE_CONNECTING:
                                Log.d(TAG,"msg.arg1: "+"DEVICE_STATE_CONNECTING");

                                mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_CONNECTING;
//                                setStatus(R.string.title_connecting, Color.YELLOW);
                                break;
                            case MagTekSCRA.DEVICE_STATE_DISCONNECTED:
                                Log.d(TAG,"msg.arg1: "+"DEVICE_STATE_DISCONNECTED");

                                mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_DISCONNECTED;
//                                setStatus(R.string.title_not_connected, Color.RED);
                                minVolume();
                                break;
                            default:
                                Log.d(TAG,"msg.arg1: "+msg.arg1);

                                break;
                        }
                        break;
                    case MagTekSCRA.DEVICE_MESSAGE_DATA_START:
                        if (msg.obj != null)
                        {
                            Log.d(TAG,"Card swiped...");
                            debugMsg("Transfer started");
//                            mCardDataEditText.setText("Card Swiped...");
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
                            String data =  mMTSCRA.getTrack1Masked();
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.NFC_SCANNED.toString())
                            .putExtra(INTENT.EXTRA_NFC_ID.toString(),data));
                            return true;
                        }
                        break;
                    case MagTekSCRA.DEVICE_MESSAGE_DATA_ERROR:

                        Log.d(TAG,"Card Swipe Error... Please Swipe Again.\n");
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

    public class NoisyAudioStreamReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
    		/* If the device is unplugged, this will immediately detect that action,
    		 * and close the device.
    		 */
            if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
            {
                mbAudioConnected=false;
                if(mMTSCRA.getDeviceType()==MagTekSCRA.DEVICE_TYPE_AUDIO)
                {
                    if(mMTSCRA.isDeviceConnected())
                    {
                        closeDevice();
                        clearScreen();
                    }
                }
            }
        }
    }

    public class headSetBroadCastReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {

            // TODO Auto-generated method stub

            try
            {
                String action = intent.getAction();
                //Log.i("Broadcast Receiver", action);
                if( (action.compareTo(Intent.ACTION_HEADSET_PLUG))  == 0)   //if the action match a headset one
                {
                    int headSetState = intent.getIntExtra("state", 0);      //get the headset state property
                    int hasMicrophone = intent.getIntExtra("microphone", 0);//get the headset microphone property
                    //mCardDataEditText.setText("Headset.Detected=" + headSetState + ",Microphone.Detected=" + hasMicrophone);

                    if( (headSetState == 1) && (hasMicrophone == 1))        //headset was unplugged & has no microphone
                    {
                        mbAudioConnected=true;
                        openDevice();
                    }
                    else
                    {
                        mbAudioConnected=false;
                        if(mMTSCRA.getDeviceType()==MagTekSCRA.DEVICE_TYPE_AUDIO)
                        {
                            if(mMTSCRA.isDeviceConnected())
                            {
                                closeDevice();
                                clearScreen();
                            }
                        }

                    }

                }

            }
            catch(Exception ex)
            {

            }

        }

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
    private Handler StatusMessageTextUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
            Bundle tBundle = msg.getData();
            String tData = tBundle.getString("XXDataXX");
//            mCardDataEditText.setText(tData);
        }
    };

    private void setStatusMessage(String lpstrStatus)
    {
//        String tTemp = mCardDataEditText.getText().toString();
//        if(tTemp!=null)
//        {
//            tTemp+=lpstrStatus + "\n");
//            Message msg = new Message();
//            Bundle tBundle = new Bundle();
//            tBundle.putString("XXDataXX",tTemp);
//            msg.setData(tBundle);
//            StatusMessageTextUpdateHandler.sendMessage(msg);
//        }
    }

}
