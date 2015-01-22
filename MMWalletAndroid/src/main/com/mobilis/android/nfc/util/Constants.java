package com.mobilis.android.nfc.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.DisplayMetrics;

import com.mobilis.android.nfc.domain.QuickLink;
import com.mobilis.android.nfc.model.TransactionHistoryRoot;
import com.mobilis.android.nfc.slidemenu.utils.Menus;

import java.util.ArrayList;

public final class Constants{
    
	private Constants() {}

    public static String currency;
	
	public static Menus menus;
	//public static int SHOW_SPLASH_SCREEN = 0 ;
	public static TransactionHistoryRoot transactionsRoot = new TransactionHistoryRoot();
	
//	public static boolean userLoggedInWallet = false;
	public static String itemDescription;
	public static Application MY_APP = null;
	public static Activity slidingContentActivity;

    public static boolean startedQPProcess;
    public static ArrayList<QuickLink> quickLinks = new ArrayList<QuickLink>();

    /**This property is to check if user has logged in through one of the login screens,
	 * if not then stop NFC screens from coming up if user scans tag while app is in offline mode
	 * */
	public static boolean merchantLoggedin;
	
	/**This property is used to prevent the user from going from Configuration Screen to Standard Login screen if they don't have TxlIds stored on the phone*/
	private static String LAST_TASK; 
	
	/** This property is used in POJOs that don't use ModelAbstact instance and needs access to ApplicationContext (MerchantServiceBuilder & NFCReadHelper)*/
	private static Context ApplicationContext;


    public static final int MAX_AMOUNT_LIMIT = 1000;

    public static boolean internetConnectionLost;
	
	public static boolean isMerchantLoggedin() {
		return merchantLoggedin;
	}
	public static void setMerchantLoggedin(boolean merchantLoggedin) {
		Constants.merchantLoggedin = merchantLoggedin;
	}
	public static Context getApplicationContext() {
		return ApplicationContext;
	}
	public static void setApplicationContext(Context applicationContext) {
		ApplicationContext = applicationContext;
	}
	public static String getLastTask() {
		return LAST_TASK;
	}

	public static void setLastTask(String lastTask) {
		LAST_TASK = lastTask;
	}
	
	public static String getAppVersionNumber(Context context){
		try {
			@SuppressWarnings("unused")
			String name = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			int code = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			return "1."+code;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

    public static int getScreenHeight(Activity activity){
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }


}
