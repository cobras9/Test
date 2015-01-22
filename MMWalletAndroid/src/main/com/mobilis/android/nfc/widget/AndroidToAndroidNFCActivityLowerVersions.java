package com.mobilis.android.nfc.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Handler;
import android.util.Log;

import com.mobilis.android.nfc.interfaces.A2ACallback;

import java.io.IOException;
import java.util.Locale;

@TargetApi(19)
public class AndroidToAndroidNFCActivityLowerVersions {

	private static NfcAdapter nfcAdapter;
	private static Activity activity;
	private static A2ACallback callback;

    private final static String TAG = AndroidToAndroidNFCActivityLowerVersions.class.getSimpleName();
	public AndroidToAndroidNFCActivityLowerVersions(Activity activity, A2ACallback callback) {
		setNfcAdapter(NfcAdapter.getDefaultAdapter(activity));
		setActivity(activity);
		setCallback(callback);
	}


	public static String onTagDiscovered(final Tag tag, Activity activity ) {
		AndroidToAndroidNFCActivityLowerVersions.activity =activity;

		Log.d(TAG, "Got it!!!");
        StringBuilder output = new StringBuilder();

        if(isNdefTag(tag))
		{
			String uid = bytesToHexString(tag.getId());
			Log.d(TAG, "This is Ndef Message! sending back ID: "+uid);
			return uid;
		}

		String type = "NfcB";
		if (NfcA.get(tag) != null)
		{
			type="NfcA";
		}
		Log.d(TAG, "Tag of type: "+type);
	    Log.d(TAG, "NFC Type is:"+type);
		String uid = bytesToHexString(tag.getId());
		Log.d(TAG, "This is Ndef Message! sending back ID: "+uid);
        if(uid.matches(".*[a-zA-Z]+.*"))
			return uid;

		byte[] SELECT = {
				(byte)0x00, // CLA = 00 (first interindustry command set)
				(byte)0xA4, /// INS = A4 (SELECT)
				(byte)0x04, // P1  = 04 (select file by DF name)
				(byte)0x0C, /// P2  = 0C (first or only file; no FCI)
				(byte)0x07, /// Lc  = 7  (data/AID has 7 bytes)
				// AID = A0000000041010
				(byte)0xA0, (byte)0x00, (byte)0x00, (byte)0x00,
				(byte)0x04, (byte)0x10, (byte)0x10
		};


		final IsoDep myCard = IsoDep.get(tag);

		try {
			Log.d(TAG, "connecting to tag");
			myCard.connect();
			connected = true;
			Log.d(TAG, "connected");
			if(myCard.isConnected()){

				Log.d(TAG, "sending select statement");
				byte[] resp = myCard.transceive(SELECT);
				Log.d(TAG, "respo array == null ? "+(resp == null));
				Log.d(TAG, "222 response length: "+resp.length);
                if(resp.length < 4)
                {
                    uid = bytesToHexString(tag.getId());
                    Log.d(TAG, "response length is less than 3 setting UID: "+uid);
                    getCallback().finishedA2ACommunication(uid.toUpperCase(Locale.US));
                    closeTagConnection(myCard);
                    return uid;
                }
				for (int i = 0; i < resp.length; i++) {
					Log.d(TAG, "resp Byte["+i+"} is: "+resp[i]);
				}
				byte[] hex = new byte[resp.length - 3];
				int counter = 0;
				for (int i = 0; i < resp.length; i++) {
					if(i > 0 && i < (resp.length-2))
					{
						hex[counter] =  resp[i];
						counter++;
					}
				}
				String string = bytesToHexString(hex);
                output.append(string);
				Log.d(TAG, "(LowerVersion) got androidId from other device: "+output);
				Log.d(TAG, "Device UID: "+output.toString().toUpperCase(Locale.US));
				putConnectedStatusBackToNormal();
			}
		}
		catch (IOException e) {
			Log.d(TAG, "Exception occured");
			e.printStackTrace();
		}
        finally {

            Log.d(TAG, "Finally clause is called");
            if(myCard != null && myCard.isConnected())
                try {
                    Log.d(TAG, "Finally clause..closing Tag connection");
                    myCard.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return output.toString().toUpperCase(Locale.US);


	}
    private static void closeTagConnection(IsoDep myCard) {
        Log.d(TAG, "closeTagConnection() is called in lower version");
        if(myCard != null && myCard.isConnected())
            try {
                Log.d(TAG, "closing Tag connection");
                myCard.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


	private static void putConnectedStatusBackToNormal() {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						connected = false;
					}
				}, 500);
			}
		});
	}


	private static void startTypeAChecker(final Tag tag) {
       	activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Log.d(TAG, "connected ?"+connected);
						
						if(!connected)
						{
							Log.d(TAG, "Couldn't connect..will send back UID");
							String uid = bytesToHexString(tag.getId());
							Log.d(TAG, "TAG UID: "+uid.toUpperCase(Locale.US));
						}
					}
				}, 500);
			}
		});
	}
	static boolean connected = false;
	private static String bytesToHexString(byte[] src) {
//		StringBuilder stringBuilder = new StringBuilder("0x");
//		if (src == null || src.length <= 0) {
//			return null;
//		}
//
//		char[] buffer = new char[2];
//		for (int i = 0; i < src.length; i++) {
//			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
//			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
//			stringBuilder.append(buffer);
//		}
//		return stringBuilder.toString().substring(2);
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }

        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        Log.d(TAG, "(LOWER VERSION) Android2Android bytesToHexString is returning: "+stringBuilder.toString().substring(2));
        return stringBuilder.toString().substring(2);
	}

	private static boolean isNdefTag(Tag myTag){
		Ndef ndefTag = Ndef.get(myTag);
		if(ndefTag == null)
			return false;
		NdefMessage ndefMesg = ndefTag.getCachedNdefMessage();
		
		if(ndefMesg == null)
			return false;
		else
			return true;
	}
	
	
	public NfcAdapter getNfcAdapter() {
		return nfcAdapter;
	}

	public void setNfcAdapter(NfcAdapter nfcAdapter) {
		this.nfcAdapter = nfcAdapter;
	}

	public Activity getActivity() {
		return activity;
	}
	public void setActivity(Activity activity) {
		this.activity = activity;
	}


	public static A2ACallback getCallback() {
		return callback;
	}


	public void setCallback(A2ACallback callback) {
		this.callback = callback;
	}
	


}
