package com.mobilis.android.nfc.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.mobilis.android.nfc.util.SecurePreferences;
 
public class BalanceUpdateService extends Service {
    
        final String TAG = BalanceUpdateService.class.getSimpleName();
        // command strings to send to service
        public static final String UPDATE = "update";
    
		@SuppressWarnings("deprecation")
		@Override
        public void onStart(Intent intent, int startId) {
			Log.d(TAG, "BalanceUpdateService onStart() is called");
			
//			RemoteViews remoteView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_view);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
  
			SecurePreferences preferences = new SecurePreferences(getApplicationContext());
			String quickBalance = preferences.getString(SecurePreferences.KEY_WIDGET_QUICKBALANCE, null);
			if(quickBalance != null && quickBalance.equalsIgnoreCase("true"))
			{
//				remoteView.setViewVisibility(R.id.widget_Layout2, View.VISIBLE);
//				remoteView.setViewVisibility(R.id.widget_balance_textview, View.VISIBLE);
//				remoteView.setTextViewText(R.id.widgetRefreshTV, preferences.getString("LastUpdatedTime", null));
//				remoteView.setTextViewText(R.id.widget_textview, preferences.getString("WidgetTextView", null));
//				remoteView.setTextViewText(R.id.widget_balance_textview, preferences.getString("WidgetBalance", null));
			}
			else
			{
//				remoteView.setTextViewText(R.id.widget_textview, "Quick blance disabled.");
//				remoteView.setViewVisibility(R.id.widget_balance_textview, View.GONE);
//				remoteView.setViewVisibility(R.id.widget_Layout2, View.INVISIBLE);
			}
	
//			appWidgetManager.updateAppWidget(new ComponentName( getApplicationContext(), MMWalletBalanceWidget.class), remoteView);
			super.onStart(intent, startId);
        }
 
        @Override
        public IBinder onBind(Intent arg0) {
                return null;
        }
}
