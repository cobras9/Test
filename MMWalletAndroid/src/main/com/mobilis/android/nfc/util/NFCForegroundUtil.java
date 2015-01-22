package com.mobilis.android.nfc.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.util.Log;

@SuppressLint("NewApi")
@TargetApi(10)
public class NFCForegroundUtil extends Activity{
	
	private NfcAdapter nfc;
	private Activity activity;
	private IntentFilter intentFiltersArray[];
	private PendingIntent intent;
	private String techListsArray[][];

	
	public NFCForegroundUtil(Activity activity) {
		super();
		
	    this.activity = activity; 
	    if(nfc == null)
			nfc = NfcAdapter.getDefaultAdapter(activity.getApplicationContext());

        // check NFC is supported in device
        if(nfc == null)
            return;
	    intent = PendingIntent.getActivity(activity, 0, new Intent(activity,
	            activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

	    IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
	    IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
	    IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

	    try {
	        ndef.addDataType("*/*");
	    } catch (MalformedMimeTypeException e) {
	        throw new RuntimeException("Unable to speciy */* Mime Type", e);
	    }
	    intentFiltersArray = new IntentFilter[] { ndef, tech, tag };

	    techListsArray = new String[][] { new String[] {Ndef.class.getName(), NdefFormatable.class.getName(),
	    												NfcA.class.getName() , NfcB.class.getName() ,
	    												NfcF.class.getName() , NfcV.class.getName(), 
	    												MifareUltralight.class.getName() , MifareClassic.class.getName() }};
	    //techListsArray = new String[][] { new String[] { NfcA.class.getName(), NfcB.class.getName() }, new String[] {NfcV.class.getName()} };
	}

	public void enableForeground()
	{
	    Log.d("NFCForegroundUtil", "Foreground NFC dispatch enabled");
	    nfc.enableForegroundDispatch(activity, intent, intentFiltersArray, techListsArray);     
	}

    public void enableForeground(Activity activity)
    {
        Log.d("NFCForegroundUtil", "Foreground NFC dispatch enabled");
        nfc.enableForegroundDispatch(activity, intent, intentFiltersArray, techListsArray);
    }

	public void disableForeground()
	{
	    Log.d("NFCForegroundUtil", "Foreground NFC dispatch disabled");
	    nfc.disableForegroundDispatch(activity);
	}

	public NfcAdapter getNfc() {
	    return nfc;
	}


    public static boolean hasNFCFeature(Context context){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
    }
}
