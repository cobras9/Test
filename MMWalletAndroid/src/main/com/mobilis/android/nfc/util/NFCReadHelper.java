package com.mobilis.android.nfc.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.nfc.tech.TagTechnology;
import android.util.Log;

import com.mobilis.android.nfc.R;

import java.io.IOException;
import java.util.ArrayList;

import static com.mobilis.android.nfc.util.Constants.getApplicationContext;

@TargetApi(10)
public class NFCReadHelper {

	/** Reading NFCTag starts with this method*/
	public StringBuffer readTagMessage(Tag tag, Intent intent) {

		Log.d("mobilisDemo", "readTagMessage(Tag tag, Intent intent) is called");
		StringBuffer buffer = new StringBuffer();
		TagTechnology tagT = readTag(tag);
		try {
			tagT.connect();
			buffer.append(getTagUID(tag, null, null, null));			
		}
		catch (Exception e)
		{
			buffer.append("Error reading Tag");
		    Log.e("ReadHelper.class", "IOException while reading NFCTag UID.", e);
		} finally 
		{
			if (tagT != null) {
				try {
					tagT.close();
		        }
		        catch (IOException e) {
		            Log.e("ReadHelper.class", "Error closing tag...", e);
		        }
		    }
		}
		return removeCR(new String(buffer));
		
	}

	/**Method to read the tag instance from NFCTag*/
	private TagTechnology readTag(Tag tag) {
		TagTechnology tagT;
		if (Ndef.get(tag) != null)
		{
			tagT = Ndef.get(tag);
//			readNDEFContent(Ndef.get(tag));
		
		}
		else if (MifareUltralight.get(tag) != null)
			tagT = MifareUltralight.get(tag);
		else if (MifareClassic.get(tag) != null)
			tagT = MifareClassic.get(tag);
		else if (NdefFormatable.get(tag) != null)
			tagT = NdefFormatable.get(tag);
		else if (NfcA.get(tag) != null)
			tagT = NfcA.get(tag);
		else if (NfcB.get(tag) != null)
			tagT = NfcB.get(tag);
		else if (NfcF.get(tag) != null)
			tagT = NfcF.get(tag);
		else if (NfcV.get(tag) != null)
			tagT = NfcV.get(tag);
		else
			tagT = IsoDep.get(tag);
		return tagT;
	}
	

	/** extracts NFCTag UID */
	private StringBuffer getTagUID(Tag tag, TagTechnology tagT,	NdefMessage[] ndefMsgs, NdefRecord[] ndefRecords) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(bytesToHexString(tag.getId()));
		
		return buffer;
	}
	
	/** converts UID byte array to String of Hex*/
	private String bytesToHexString(byte[] src) {
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
		Log.d("mobilisDemo", "stringBuilder.toString().substring(2): "+stringBuilder.toString().substring(2));
		return stringBuilder.toString().substring(2);
	}
	   
	/** removes the special character at the end of the extracted UID*/
	private StringBuffer removeCR(String input){
		Log.d("mobilisDemo", "removeCR() is called for: "+input);
		Log.d("mobilisDemo", "special char is: "+getApplicationContext().getResources().getString(R.string.NEWLINE_ENDLINE_CHAR));
		if(input.contains(getApplicationContext().getResources().getString(R.string.NEWLINE_ENDLINE_CHAR)))
		{
			Log.d("mobilisDemo", "yes there is special char at end");
			return new StringBuffer(input.replaceAll(getApplicationContext().getResources().getString(R.string.NEWLINE_ENDLINE_CHAR), getApplicationContext().getResources().getString(R.string.STRING_ZERO_SPACE)));
		}
		else
		{
			Log.d("mobilisDemo", "no there is no special character at end");
			return new StringBuffer(input);
		}
	}
	
	private String hexToAscii(String hex){
		if(hex != null)
		{
			StringBuilder sb = new StringBuilder();
			StringBuilder temp = new StringBuilder();
			//49204c6f7665204a617661 split into two characters 49, 20, 4c...
			for( int i=0; i<hex.length()-1; i+=2 ){
				//grab the hex in pairs
				String output = hex.substring(i, (i + 2));
				//convert hex to decimal
				int decimal = Integer.parseInt(output, 16);
				//convert the decimal to character
				sb.append((char)decimal);
				temp.append(decimal);
			}
			return sb.toString();
		}
		else 
			return hex;
	}

	@SuppressWarnings("unused")
	@SuppressLint({ "NewApi", "DefaultLocale" })
	public ArrayList<String> readNDEFContent(Ndef ndefTag){
		Log.d("mobilisDemo", "readNDEFContent() is called");
		ArrayList<String> result = new ArrayList<String>();
		
		byte[] tagId = ndefTag.getTag().getId();
		Log.d("mobilisDemo", "byte[] id: "+tagId.toString());
		String uid = bytesToHexString(tagId);
		Log.d("mobilisDemo", "uid: "+uid);
		
		int size = ndefTag.getMaxSize();         // tag size
		boolean writable = ndefTag.isWritable(); // is tag writable?
		String type = ndefTag.getType();         // tag type

		// get NDEF message details
		NdefMessage ndefMesg = ndefTag.getCachedNdefMessage();
		NdefRecord[] ndefRecords = ndefMesg.getRecords();
		
		int len = ndefRecords.length;
		Log.d("mobilisDemo", "Ndef records length:"+len);
		String[] recTypes = new String[len];     // will contain the NDEF record types
		for (int i = 0; i < len; i++) {
		  recTypes[i] = new String(ndefRecords[i].getType());
		  if(ndefRecords[i].getPayload() != null)
		  {
			  Log.d("mobilisDemo", "ndefRecords[i].getPayload(): "+ndefRecords[i].getPayload());
			  String payloadData = hexToAscii(bytesToHexString(ndefRecords[i].getPayload()));
			  Log.d("mobilisDemo", "payload Data is: "+payloadData);
			  result.add(uid.toUpperCase()+","+payloadData+"\n");
		  }
		}
		return result;
		
		
	}
/**	@SuppressLint("NewApi")
	public String readNDEFContent(Ndef ndefTag){
		int size = ndefTag.getMaxSize();         // tag size
		boolean writable = ndefTag.isWritable(); // is tag writable?
		String type = ndefTag.getType();         // tag type

		StringBuffer buffer = new StringBuffer();
		
		buffer.append("ndefTag maxSize: "+size+"\n");
		buffer.append("is ndefTag writable? "+writable+"\n");
		buffer.append("ndef Tag Type: "+type+"\n");
		// get NDEF message details
		NdefMessage ndefMesg = ndefTag.getCachedNdefMessage();
		NdefRecord[] ndefRecords = ndefMesg.getRecords();
		buffer.append("number of Bytes in Ndef Message: "+ndefMesg.getByteArrayLength()+"\n");
		buffer.append("number of Ndefc Records: "+ndefRecords.length+"\n");
		int len = ndefRecords.length;
		String[] recTypes = new String[len];     // will contain the NDEF record types
		buffer.append("******printing ndefRecords now ******"+"\n");
		for (int i = 0; i < len; i++) {
		  recTypes[i] = new String(ndefRecords[i].getType());
		  buffer.append("id["+i+"]: "+hexToAscii(bytesToHexString(ndefRecords[i].getId()))+"\n");
		  buffer.append("type["+i+"]: "+hexToAscii(bytesToHexString(ndefRecords[i].getType()))+"\n");
		  buffer.append("payloadData["+i+"]: "+hexToAscii(bytesToHexString(ndefRecords[i].getPayload()))+"\n");
		  buffer.append("Tnf["+i+"]: "+ndefRecords[i].getTnf()+"\n");
		}
		buffer.append("******END******"+"\n");
		return buffer.toString();
		
		
	}**/


}
