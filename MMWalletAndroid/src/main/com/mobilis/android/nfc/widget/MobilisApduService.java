package com.mobilis.android.nfc.widget;

import android.annotation.TargetApi;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import com.mobilis.android.nfc.model.AbstractModel;

@TargetApi(19)
public class MobilisApduService extends HostApduService {

	@Override
	public void onDeactivated(int reason) {

	} 

	@Override
	public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
		Log.d("AhmedReader", "processCommandApdu is called!!");
//		StringBuffer hex = new StringBuffer();
//		for (int i = 0; i < commandApdu.length; i++) {
//			hex.append(String.format("%02X",commandApdu[i]));
//		}
//
//		int n = hex.toString().length();
//		StringBuilder ascii = new StringBuilder(n / 2);
//		for (int i = 0; i < n; i += 2) {
//			char a = hex.toString().charAt(i);
//			char b = hex.toString().charAt(i + 1);
//			ascii.append((char) ((hexToInt(a) << 4) | hexToInt(b)));
//		}
//
//		Log.d(MobilisApduService.class.getSimpleName(), "Hex: "+hex.toString());
		
		String androidId = AbstractModel.getAndroidId(this);
		Log.d("androidid", "AndroidId : "+androidId);
		
		byte[] idAsByteArray = null;

        idAsByteArray = HexStringToByteArray(androidId);//androidId.getBytes(Charset.forName("UTF-8"));

        int length = idAsByteArray.length;
        byte lengthAsByte = (byte) (length & (0xff));

        int responseArrayLength = length + 3;
        byte[] response = new byte[responseArrayLength];

        // setting length byte
        response[0] = lengthAsByte;


        int index = 1;
		
		// setting android Id bytes
		for (int i = 0; i < idAsByteArray.length; i++) {
			response[index] = idAsByteArray[i];
			index++;
		}
		
		//setting last two bytes
		response[response.length-2] = (byte) (144  & (0xff));
		response[response.length-1] = (byte) 0x00;
		
		for (int i = 0; i < response.length; i++) {
			Log.d("androidid","byte"+i+": "+response[i]);
		}
		
		
		return response;
		//return null;
	}
    public static byte[] HexStringToByteArray(String s) {
        byte data[] = new byte[s.length()/2];
        for(int i=0;i < s.length();i+=2) {
            data[i/2] = (Integer.decode("0x"+s.charAt(i)+s.charAt(i+1))).byteValue();
        }
        return data;
    }
//    public static byte[] hexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i+1), 16));
//        }
//        return data;
//    }
	private int hexToInt(char ch) {
		  if ('a' <= ch && ch <= 'f') { return ch - 'a' + 10; }
		  if ('A' <= ch && ch <= 'F') { return ch - 'A' + 10; }
		  if ('0' <= ch && ch <= '9') { return ch - '0'; }
		  throw new IllegalArgumentException(String.valueOf(ch));
		}

}
