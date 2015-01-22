package com.mobilis.android.nfc.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.mobilis.android.nfc.domain.INTENT;

/**
 * Created by ahmed on 17/06/14.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    final int MIN_RSSI = -100;
    final int MIN_GSM = 5;
    SignalStrengthListener signalStrengthListener;
    SignalStrengthRegainedListener signalStrengthRegainedListener;
    TelephonyManager telManager;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(Constants.internetConnectionLost && wifi.isAvailable() && wifi.isConnected())
        {
            Constants.internetConnectionLost = false;
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            int linkSpeed = wifiManager.getConnectionInfo().getRssi();
            String message = "";
            if(linkSpeed < MIN_RSSI)
                message = "Wifi connected. Signal is weak";
            else
                message = "Wifi connected.";
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(INTENT.INTERNET_REGAINED.toString()).
                    putExtra(INTENT.EXTRA_INTERNET.toString(), message));
        }
        else if (Constants.internetConnectionLost && wifi.isAvailable() && mobile.isConnected())
        {
            Constants.internetConnectionLost = false;
            telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            signalStrengthRegainedListener = new SignalStrengthRegainedListener(context);
            telManager.listen(signalStrengthRegainedListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        }
        else if (wifi.isAvailable() && wifi.isConnected()) {
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            int linkSpeed = wifiManager.getConnectionInfo().getRssi();
            if(linkSpeed < MIN_RSSI)
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(INTENT.INTERNET_WIFI_SIGNAL_WEAK.toString()));
        }
        else if (mobile.isAvailable() && mobile.isConnected()) {
            telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            signalStrengthListener = new SignalStrengthListener(context);
            telManager.listen(signalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
        else{
            Constants.internetConnectionLost = true;
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(INTENT.INTERNET_NO_SIGNAL.toString()));
        }
    }
    private class SignalStrengthListener extends PhoneStateListener{
        Context mContext;
        public SignalStrengthListener(Context context){
            mContext = context;
        }
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);
            if (telManager.getPhoneType()== TelephonyManager.PHONE_TYPE_CDMA)
            {
                int currentSignalStrength = signalStrength.getCdmaDbm();
                if(currentSignalStrength < MIN_GSM)
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(INTENT.INTERNET_GSM_SIGNAL.toString()));
                telManager.listen(signalStrengthListener, PhoneStateListener.LISTEN_NONE);
            }else{
                int asu = signalStrength.getGsmSignalStrength();
                if(asu < MIN_GSM)
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(INTENT.INTERNET_GSM_SIGNAL.toString()));
                telManager.listen(signalStrengthListener, PhoneStateListener.LISTEN_NONE);
            }
        }
    }

    private class SignalStrengthRegainedListener extends PhoneStateListener{
        Context mContext;
        String message = "";
        public SignalStrengthRegainedListener(Context context){
            mContext = context;
        }
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);
            if (telManager.getPhoneType()== TelephonyManager.PHONE_TYPE_CDMA)
            {
                int currentSignalStrength = signalStrength.getCdmaDbm();
                if(currentSignalStrength < MIN_GSM)
                    message = "Mobile data connected. Signal is weak.";
                else
                    message = "Mobile data connected.";
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(INTENT.INTERNET_REGAINED.toString()).
                        putExtra(INTENT.EXTRA_INTERNET.toString(), message));
                telManager.listen(signalStrengthRegainedListener, PhoneStateListener.LISTEN_NONE);
            }else{
                int asu = signalStrength.getGsmSignalStrength();
                if(asu < MIN_GSM)
                    message = "Mobile data connected. Signal is weak.";
                else
                    message = "Mobile data connected.";
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(INTENT.INTERNET_REGAINED.toString()).
                        putExtra(INTENT.EXTRA_INTERNET.toString(), message));
                telManager.listen(signalStrengthRegainedListener, PhoneStateListener.LISTEN_NONE);
            }
        }
    }
}
