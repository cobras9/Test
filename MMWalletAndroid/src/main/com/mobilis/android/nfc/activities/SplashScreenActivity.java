package com.mobilis.android.nfc.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Window;

import com.mobilis.android.nfc.R;

public class SplashScreenActivity extends Activity{

	/**
	 * Simple Dialog used to show the splash screen
	 */
	protected Dialog mSplashDialog;
	private BroadcastReceiver broadcastReceiver;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.splashscreen);
    
	    Handler handler = new Handler();
	    handler.postDelayed(new Runnable() {	
	    	public void run() {
	    		Intent nextScreen = new Intent(getApplicationContext(), LoginActivity.class);
	    		startActivity(nextScreen);
	    		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	    		finish();
	    	}
	    }, 500);
	    
	    broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equalsIgnoreCase("FINISHED_LOGING_PROCESS"))
				{
					finish();
				}
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("FINISHED_LOGING_PROCESS"));

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
	}

}
