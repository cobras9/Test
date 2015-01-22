package com.mobilis.android.nfc.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.interfaces.A2ACallback;
import com.mobilis.android.nfc.util.NFCForegroundUtil;
import com.mobilis.android.nfc.widget.AndroidToAndroidNFCActivity;
import com.mobilis.android.nfc.widget.AndroidToAndroidNFCActivityLowerVersions;

/**
 * Created by ahmed on 12/06/14.
 */
//public abstract class NFCFragment extends ApplicationActivity.PlaceholderFragment implements A2ACallback {
public abstract class NFCFragment extends ApplicationActivity.PlaceholderFragment implements A2ACallback {
//
    protected static AndroidToAndroidNFCActivity androidToAndroid;
    protected NFCForegroundUtil nfcForegroundUtil = null;
    protected AndroidToAndroidNFCActivityLowerVersions androidLowerVersion;

    public abstract void finishedA2ACommunication(String scannedId);

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        setUpNFC();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpNFC();
//        setUpNFC();
//        if(nfcForegroundUtil != null)
//            nfcForegroundUtil.enableForeground();

    }

    protected void enableNFCScan(){
        if(nfcForegroundUtil != null)
            nfcForegroundUtil.enableForeground();
    }
    protected void disableNFCScan(){
        if(nfcForegroundUtil != null) {
            nfcForegroundUtil.disableForeground();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
//        if(nfcForegroundUtil != null)
//            nfcForegroundUtil.disableForeground();
    }
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        setUpNFC();
//        if(nfcForegroundUtil != null)
//            nfcForegroundUtil.enableForeground();
//    }

//    @Override
//    public void onDetach() {
//        if(nfcForegroundUtil != null)
//            nfcForegroundUtil.disableForeground();
//        super.onDetach();
//    }

    protected void setUpNFC(){
        if(NFCForegroundUtil.hasNFCFeature(getActivity())){
            int buildVersion = Build.VERSION.SDK_INT;
            // Check if NFC Feature is enabled
            if(nfcForegroundUtil == null)
                nfcForegroundUtil = new NFCForegroundUtil(getActivity());
            if (!isNFCEnabled())
            {
                Toast.makeText(getActivity(), getString(R.string.TOAST_NFC_DISABLED_MSG), Toast.LENGTH_LONG).show();
                if (buildVersion>= 16)
                    startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                else
                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
            }
//            else {
//                nfcForegroundUtil.enableForeground();
//            }
            androidLowerVersion = new AndroidToAndroidNFCActivityLowerVersions(getActivity(), this);
//            if(buildVersion > 18){
//                Log.d("A7mad", "creating new androidToAndroid");
//                // This will support NFC scan read for devices with SDK build >= 18 -- less than 18 is supported in ApplicationActivity.onNewIntent()
//                androidToAndroid = new AndroidToAndroidNFCActivity(getActivity(), this);
//                androidToAndroid.enableReadMode();
//            }
//            else
//                androidLowerVersion = new AndroidToAndroidNFCActivityLowerVersions(getActivity(), this);
        }
    }

    public static boolean supportsNFC(NFCForegroundUtil nfcForegroundUtil) {
        return (nfcForegroundUtil.getNfc() != null);
    }



    public boolean isNFCEnabled() {
        return nfcForegroundUtil.getNfc().isEnabled();
    }


}
