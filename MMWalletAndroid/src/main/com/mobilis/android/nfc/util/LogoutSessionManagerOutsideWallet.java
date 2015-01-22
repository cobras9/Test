package com.mobilis.android.nfc.util;

import android.app.Activity;

import com.mobilis.android.nfc.activities.ApplicationActivity;

import java.util.Timer;

public class LogoutSessionManagerOutsideWallet {
	
	public static Timer logoutTimer ;
	public static LogoutTimerTaskOutsideWallet logoutTask;
	
	public static void restartLogoutTimer(Activity mwalletActivity, Activity outsideActivity) {
		if(mwalletActivity.getClass().getName().equalsIgnoreCase(ApplicationActivity.class.getName()))
			Constants.slidingContentActivity = mwalletActivity;
		stopLogoutTimer();
		logoutTimer = new Timer();
		logoutTask = new LogoutTimerTaskOutsideWallet(mwalletActivity, outsideActivity);
		logoutTimer.schedule(logoutTask, LogoutTimerTask.LOGOUT_DELAY_TIME);
	}	
	public static void stopLogoutTimer(){
		if(logoutTimer != null)
		{
			logoutTimer.cancel();
			logoutTimer = null;
		}
		if(logoutTask != null){
			logoutTask.cancel();
			logoutTask = null;
		}
	}
	
}
