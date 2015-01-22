package com.mobilis.android.nfc.interfaces;

/** Interface called when NFC Tag is scanned and read in AndroidToAndroid classes **/
public interface A2ACallback {

	public void finishedA2ACommunication(String androidId);
}
