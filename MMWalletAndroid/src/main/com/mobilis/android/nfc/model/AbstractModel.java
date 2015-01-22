package com.mobilis.android.nfc.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.*;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.LoginActivity;
import com.mobilis.android.nfc.dao.DBService;
import com.mobilis.android.nfc.domain.TxlDomain;
import com.mobilis.android.nfc.network.BalanceHistoryTaskManager;
import com.mobilis.android.nfc.network.ConnTaskManager;
import com.mobilis.android.nfc.network.TaskManager;
import com.mobilis.android.nfc.util.Constants;
import com.mobilis.android.nfc.util.NFCForegroundUtil;
import com.mobilis.android.nfc.util.SecurePreferences;
import com.mobilis.android.nfc.util.SharedPreferencesException;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressLint("UseValueOf")
public abstract class AbstractModel{

	private static final String TAG = "ModelAbstract";
	protected DBService service;
	private TxlDomain txl;
	protected Context context;
	protected Context componentContext;
	protected Activity activity;
	protected String workingAmount;
	protected String NFCId;
	protected String serverResponse;
	protected String serverError;
	protected String IMEA;
	protected String clientId;
	protected String clientPIN;
	protected String clientOldPIN;
	protected String clientNewPIN;
	protected String MobMonPIN;
	protected String WorkingCurrency;
	protected String manualTxlId;
	protected String msisdn;
	private String txlWorkingAmount;
	private String txlWorkingCurrency;
	private String txlPaymentTrailId;
	private String txlErrorMessage;
	private int txlStatus;
	private SecurePreferences sharedPreference;
	
	private ImageView splash;
	
	protected boolean avoidNullValuesInContructor;
	protected boolean isMsisdnTransaction;
	
	@SuppressLint("ShowToast")
	public void showPinError(Context context){
		Toast.makeText(context, "Enter PIN to complete transaction", Toast.LENGTH_SHORT).show();
	}
	
	public boolean pinExists(String pin){
		return pin != null && !pin.isEmpty();
	}
	
	public void startLoadingAnimation(Dialog dialog){
		RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setDuration(2000);
		
		TextView title = (TextView) dialog.findViewById(R.id.ndefLoadingLabel);
		title.setTextSize(18f);
		title.setVisibility(View.INVISIBLE);
		
		TextView textView = (TextView) dialog.findViewById(R.id.EnterPinTextV);
		textView.setAlpha(0);
		
		EditText editText = (EditText) dialog.findViewById(R.id.ndefPinET);
		editText.setAlpha(0);
		
		Button button = (Button) dialog.findViewById(R.id.ndefButton);
		button.setAlpha(0);
		
		setSplash((ImageView) dialog.findViewById(R.id.loadingIV));
		getSplash().setVisibility(View.VISIBLE);
		getSplash().startAnimation(anim);
		
	}
	public void stopLoadingAnimation(Dialog dialog){
		TextView textView = (TextView) dialog.findViewById(R.id.ndefLoadingLabel);
		textView.setVisibility(View.GONE);
		
		TextView descTV = (TextView) dialog.findViewById(R.id.productName);
		descTV.setVisibility(View.GONE);
		
		TextView priceTV = (TextView) dialog.findViewById(R.id.productPrice);
		priceTV.setVisibility(View.GONE);
		
		getSplash().setAnimation(null);
		getSplash().setVisibility(View.INVISIBLE);
		
	}
    // ahmedsss
	public void saveConnectionDetails() throws SharedPreferencesException{
        String appVersionCode = String.valueOf(getAppVersionCode(getActivity()));
        String ip = LoginActivity.mainSecurePreferences.getString(appVersionCode+SecurePreferences.KEY_SERVER_IP, null);
        String port = LoginActivity.mainSecurePreferences.getString(appVersionCode+SecurePreferences.KEY_SERVER_PORT, null);
        if(ip == null)
        {
            LoginActivity.mainSecurePreferences.edit().putString(appVersionCode+SecurePreferences.KEY_SERVER_IP, getResString(R.string.SERVER_IP));
            LoginActivity.mainSecurePreferences.edit().commit();
        }
        if(port == null){
            LoginActivity.mainSecurePreferences.edit().putString(appVersionCode+SecurePreferences.KEY_SERVER_PORT, getResString(R.string.SERVER_PORT));
            LoginActivity.mainSecurePreferences.edit().commit();
        }
	}
    public static void hideKeyboard(Activity activity)
    {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }
    public static void showKeyboard(Activity activity)
    {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    public static void hideKeyboard(Activity activity, EditText editText)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

    }
    public static void showKeyboard(Activity activity, EditText editText)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInputFromInputMethod(editText.getWindowToken(), 0);
    }

    public static String maskPin(String request){
        StringBuffer buffer = new StringBuffer();
        String[] items = request.split(",");
        for (int i = 0 ; i < items.length; i++) {
            String pin = items[i];
            if(pin.toUpperCase(Locale.US).contains("PIN"))
            {
                String[] temp = pin.split("=");
                if(temp.length > 1)
                    pin = temp[0]+"=****";
            }
            if(i == (items.length-1))
                buffer.append(pin);
            else
                buffer.append(pin+",");
        }

        return buffer.toString();
    }
    public void saveMerchantId(String merchantId){
        LoginActivity.mainSecurePreferences.edit().putString(SecurePreferences.KEY_MERCHANT_ID, merchantId);
        LoginActivity.mainSecurePreferences.edit().commit();
    }

    public static String getMerchantId(){
        return LoginActivity.mainSecurePreferences.getString(SecurePreferences.KEY_MERCHANT_ID, null);
    }

    public static String getMerchantId(AbstractModel model){
        return model.getSharedPreference().getString(SecurePreferences.KEY_MERCHANT_ID, null);
    }

    public void savePort(String port){
        String appVersionCode = String.valueOf(getAppVersionCode(getActivity()));
        LoginActivity.mainSecurePreferences.edit().putString(appVersionCode+SecurePreferences.KEY_SERVER_PORT, port);
        LoginActivity.mainSecurePreferences.edit().commit();
    }
    public void saveIPAddress(String ip){
        String appVersionCode = String.valueOf(getAppVersionCode(getActivity()));
        LoginActivity.mainSecurePreferences.edit().putString(appVersionCode+SecurePreferences.KEY_SERVER_IP, ip);
        LoginActivity.mainSecurePreferences.edit().commit();

    }
    public void saveDefaultCurrency(String currency){
        String appVersionCode = String.valueOf(getAppVersionCode(getActivity()));
        LoginActivity.mainSecurePreferences.edit().putString(appVersionCode+SecurePreferences.KEY_DEFAULT_CURRENCY, currency);
        LoginActivity.mainSecurePreferences.edit().commit();

    }
    public String getIPAddress(){
        String appVersionCode = String.valueOf(getAppVersionCode(getActivity()));
        return LoginActivity.mainSecurePreferences.getString(appVersionCode+SecurePreferences.KEY_SERVER_IP, "");
    }
    public String getPort(){
        String appVersionCode = String.valueOf(getAppVersionCode(getActivity()));
        return LoginActivity.mainSecurePreferences.getString(appVersionCode+SecurePreferences.KEY_SERVER_PORT, "");
    }

    public String getDefaultCurrency(){
        String appVersionCode = String.valueOf(getAppVersionCode(getActivity()));
        return LoginActivity.mainSecurePreferences.getString(appVersionCode+SecurePreferences.KEY_DEFAULT_CURRENCY, null);
    }

	public String getManualTxlId() {
		return manualTxlId;
	}

	public void setManualTxlId(String manualTxlId) {
		this.manualTxlId = manualTxlId;
	}
	
	public String getResString(int res){
		return getContext().getResources().getString(res);
	}
	public int getResInt(int res){
		return Integer.parseInt(getContext().getResources().getString(res));
	}

    public String getAppVersionCode(Context context){
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, "Error occurred while getting version code", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return activity.getResources().getString(R.string.app_name)+pInfo.versionName;
    }

	protected Integer responseStatus ;
	protected Integer requestStatus ;
	protected Integer lastTxnStatus ;
	protected boolean backButtonPressed;
	protected boolean taskFinished;
	protected TaskManager taskManager;
	protected NFCForegroundUtil nfcForegroundUtil = null;
	protected boolean nfcScanned;
	protected TextView resultLabel;
	protected TextView balanceTopupLabel;
	protected boolean getTxlOnDevice;
	
	public void playAudio(){
	    //set up MediaPlayer    
//	    MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.beep);
//	    mp.setOnCompletionListener(new OnCompletionListener() {
//			public void onCompletion(MediaPlayer mp) {
//				mp.release();
//
//			}
//		});
//        mp.start();
	}

	public AbstractModel(){}
	
	public AbstractModel(Context context, Activity activity){
		initTXLModelObject(context, activity);
	}
	
	public AbstractModel(Context context, Activity activity, boolean avoidNullValuesInContructor){
		this.avoidNullValuesInContructor = avoidNullValuesInContructor;
		initTXLModelObject(context, activity);
	}
	
	public abstract String getRequestParameters();
	public abstract void verifyPostTaskResults();
	
	public TxlDomain generateTXLId(String txlType){
		TxlDomain lastTxl = getDBService().getLastLogin();
		String txlId = new String();
		String dateCreated = getCurrentDate();
		
		if (lastTxl.getTxlId() != null) {
			Log.v("ModelAbstract.generateTXLId(): ", "lastTxl is not null and will create new one now");
			txlId = constructNewTxlId(lastTxl.getTxlId());
			Log.v("ModelAbstract.generateTXLId(): ", "new txl is: "+txlId);
		}
		else{
			txlId  = new String(getResString(R.string.TRANSACTIONID_BASE));
		}
		TxlDomain newTxl = new TxlDomain();
		newTxl.setTxlId(txlId);
		newTxl.setDateCreated(dateCreated);
		newTxl.setTxlType(txlType);
		return newTxl;
	}

    public String addDateTime() {
        StringBuffer sb = new StringBuffer();

        sb.append(getFullParamString(getResString(R.string.REQ_DATE), getDate(), false));
        sb.append(getFullParamString(getResString(R.string.REQ_TIME), getTime(), false));

        return sb.toString();
    }
	
	private String constructNewTxlId(String lastTransaction){
		Log.v(TAG, "inside constructNewTxlId -- the olf txlId is: "+ lastTransaction);
		int counter = Integer.parseInt(lastTransaction);
		counter++;
		StringBuffer buffer = new StringBuffer(String.valueOf(counter));
		for (int i = buffer.length(); i < 10; i++) {
			buffer.insert(0, getResString(R.string.ZERO_STRING));
		}
		Log.v(TAG, "inside constructNewTxlId -- the new txlId is: "+ buffer);
		return new String(buffer);
	}
	
	public void commitNewTxlId(TxlDomain txl){
		Log.v(TAG, "BEfore commiting the txl, its ID is: "+txl.getTxlId());
		getDBService().commitTxl(txl);
	}
	
//	public void updateConfigurationSettings(String serverIP, String serverPort){
//
//
//        Log.d("ahmed", "updating IP address to "+fixUpServerIP(serverIP));
//		Log.d("ahmed", "updating port to "+serverPort);
//
//        if(serverIP != null)
//            LoginActivity.mainSecurePreferences.edit().putString(SecurePreferences.KEY_SERVER_IP, fixUpServerIP(serverIP));
//        if(serverPort != null)
//            LoginActivity.mainSecurePreferences.edit().putString(SecurePreferences.KEY_SERVER_PORT, serverPort);
//        LoginActivity.mainSecurePreferences.edit().commit();
//	}
//
	
	public void verifyTransferTXL(){


		setMsisdnTransaction(false);

	}

	
	private void initTXLModelObject(Context context, Activity activity){
		setContext(context);    
		setActivity(activity);
		setSharedPreference(new SecurePreferences(getContext()));
		setNfcForegroundUtil(new NFCForegroundUtil(activity));
		if(this instanceof LastTransactions){
			setConnTaskManager(new BalanceHistoryTaskManager(context, this));
		}
		else
			setConnTaskManager(new ConnTaskManager(context, this));
		setIMEA(getIMEIFromPhone(context));
		setTaskFinished(true);
		setService(DBService.getService(getContext()));
	}
	
	public static String getIMEIFromPhone(Context context) {
        if (hasTelephony(context)) {
//        return "352701060137731"; // This is for Airtel Test
		    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if(telephonyManager.getDeviceId() == null)
            {
                return telephonyManager.getSubscriberId().toString();
            }
//        return "35372606614014900"
            return telephonyManager.getDeviceId().toString();
        } else {
            return Settings.System.getString(context.getContentResolver(),Secure.ANDROID_ID);
        }
	}

    private static boolean hasTelephony(Context mContext)
    {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null)
            return false;

        PackageManager pm = mContext.getPackageManager();

        if (pm == null)
            return false;

        boolean retval = false;
        try
        {
            Class<?> [] parameters = new Class[1];
            parameters[0] = String.class;
            Method method = pm.getClass().getMethod("hasSystemFeature", parameters);
            Object [] parm = new Object[1];
            parm[0] = "android.hardware.telephony";
            Object retValue = method.invoke(pm, parm);
            if (retValue instanceof Boolean)
                retval = ((Boolean) retValue).booleanValue();
            else
                retval = false;
        }
        catch (Exception e)
        {
            retval = false;
        }

        return retval;
    }


    @SuppressLint("SimpleDateFormat")
	public String getDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Date date = new Date();
		return sdf.format(date).toString();
	}
	
	@SuppressLint("SimpleDateFormat")
	public String getTime(){
		SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		return tf.format(date).toString();
	}
	

	public String getFullParamString(String key, String value, boolean lastParam){
		StringBuffer buffer = new StringBuffer();
     	if(lastParam)
     		buffer.append(key+getResString(R.string.EQUAL)+value);
     	else
     		buffer.append(key+getResString(R.string.EQUAL)+value+getResString(R.string.COMMA));
     	return new String(buffer);
     	
    }

    public static String getDateAndTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return sdf.format(new Date());
    }


    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

	public void setNfcForegroundUtil(NFCForegroundUtil nfcForegroundUtil) {
		this.nfcForegroundUtil = nfcForegroundUtil;
	}
	
	public NFCForegroundUtil getNfcForegroundUtil() {
		return nfcForegroundUtil;
	}
	
	public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}


    public static String getMessageFromServerResponse(String response){
        String[] temp = response.split("Message=");
        if(temp.length > 1){
            String[] message = temp[1].split(",");
            return message[0];
        }
        else{
            return "OK";
        }
    }

    public static void saveMerchantIdIfNecessary(AbstractModel model) {
        if(model.getMerchantId() == null || model.getMerchantId().isEmpty())
            model.saveMerchantId(getCustomerIdFromServerResponse(model.getServerResponse()));
    }

    public static String getCustomerIdFromServerResponse(String resp){
        String customerId = null;
        String[] temp = resp.split(",");
        for (String string : temp) {
            if(string.startsWith("CustomerId=")){
                customerId = string.replace("CustomerId=", "");
                break;
            }
        }
        return customerId;
    }

    public static int getStatusFromServerResponse(String response){
        String[] temp = response.split("Status=");
        if(temp.length > 1){
            String[] message = temp[1].split(",");
            return Integer.parseInt(message[0]);
        }
        else{
            return -1;
        }
    }

	public String getCurrentDate() {
		return DateFormat.getDateTimeInstance().format(new Date());
	}
	
	public Context getContext() {
		return context;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
	public String getServerResponse() {
		return serverResponse;
	}
	
	public void setServerResponse(String serverResponse) {
		this.serverResponse = serverResponse;
	}
	
	public String getServerError() {
		return serverError;
	}
	
	public void setServerError(String serverError) {
		this.serverError = serverError;
	}
	
	public void setIMEA(String IMEA) {
		this.IMEA = IMEA;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getClientPIN() {
		return clientPIN;
	}
	
	public Integer getResponseStatus() {
		return responseStatus;
	}
	public void setResponseStatus(Integer responseStatus) {
		this.responseStatus = responseStatus;
	}
	public Integer getRequestStatus() {
		return requestStatus;
	}
	public void setRequestStatus(Integer requestStatus) {
		this.requestStatus = requestStatus;
	}
	
	public TaskManager getConnTaskManager() {
		return taskManager;
	}

	public void setConnTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public String getNFCId() {
		return NFCId;
	}
	public void setNFCId(String nFCId) {
		NFCId = nFCId;
	}
	
	public String getWorkingAmount() {
		return workingAmount;
	}
	public void setWorkingAmount(String workingAmount) {
		this.workingAmount = workingAmount;
	}


	public String getWorkingCurrency() {
        String currency = getDefaultCurrency();
        if(currency == null)
            currency = getActivity().getString(R.string.DEFAULT_CURRENCY);
        Constants.currency = currency;
		return Constants.currency;
	}

	public void setNfcScanned(boolean nfcScanned) {
		this.nfcScanned = nfcScanned;
	}
    public boolean isNfcScanned(){
        return nfcScanned;
    }

	public String getClientNewPIN() {
		return clientNewPIN;
	}

	public void setClientNewPIN(String clientNewPIN) {
		this.clientNewPIN = clientNewPIN;
	}

	public String getClientOldPIN() {
		return clientOldPIN;
	}

	public void setClientOldPIN(String clientOldPIN) {
		this.clientOldPIN = clientOldPIN;
	}

	public void setTaskFinished(boolean taskFinished) {
		this.taskFinished = taskFinished;
	}

	public TxlDomain getTxl() {
		return txl;
	}

	public void setTxl(TxlDomain txl) {
		this.txl = txl;
	}

	public DBService getDBService() {
		return service;
	}

	public void setService(DBService service) {
		this.service = service;
	}


	public boolean isMsisdnTransaction() {
		return isMsisdnTransaction;
	}

	public void setMsisdnTransaction(boolean isMsisdnTransaction) {
		this.isMsisdnTransaction = isMsisdnTransaction;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public SecurePreferences getSharedPreference() {
		return sharedPreference;
	}
	public void setSharedPreference(SecurePreferences sharedPreference) {
		this.sharedPreference = sharedPreference;
	}
	public ImageView getSplash() {
		return splash;
	}
	public void setSplash(ImageView splash) {
		this.splash = splash;
	}
	
	public static String getAndroidId(Context context){
//		String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID).toUpperCase(Locale.US)+"AS";
//        String androidId = "ASSOTELAHMED";//+Secure.getString(context.getContentResolver(), Secure.ANDROID_ID).toUpperCase(Locale.US);

        String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID).toUpperCase(Locale.US);
        return androidId;
//        androidId = "B74A208FFE82A8E0AC"+"AA";

//        return "E5362301F85FBCD6";// This is for Airtel Test
	}

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
	
}
