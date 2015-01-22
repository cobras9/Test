package com.mobilis.android.nfc.util;

import android.annotation.SuppressLint;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;

@SuppressLint("DefaultLocale")
public class MifareClassicReader {

	private final String TAG = MifareClassicReader.class.getSimpleName();
	public boolean connectAndAuthenticate(MifareClassic mfc, int sector) throws IOException{
		if(!mfc.isConnected())
			mfc.connect();
		Log.d(TAG, "connected to tag and now will authenticate sector "+sector);
		boolean auth = mfc.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT);
		return auth;
	}
	
	
	public ArrayList<Integer> getApplicationPositionFromSector0(MifareClassic mfc, String appId) throws IOException{
		
		byte[] data;
		if(!mfc.isConnected())
			throw new IOException("NFC Tag is not connected");	
		data = mfc.readBlock(1); 
        String block1 = getHexString(data);
    
        data = mfc.readBlock(2);    
        String block2 = getHexString(data);
		return getSectorsIndexes(new String(block1+block2), appId);
	}
	
	public String readBlockAfterAutheitcation(MifareClassic mfc, int sectorIndex) throws IOException{
		
		byte[] data;
		String result = null;
		if(!mfc.isConnected())
			throw new IOException("NFC Tag is not connected");
		
		int blockIndex = mfc.sectorToBlock(sectorIndex) ;
		data = mfc.readBlock(blockIndex);
		String hex = getHexString(data);
			
		byte[] asBytes = hexStringToByteArray( hex.substring(0, 8));
        reverseByteArrasInPlace(asBytes);
        ByteBuffer bb = ByteBuffer.wrap(asBytes);
        int i = bb.getInt();
        result = "" + i;
       
        return result;
	}
	public boolean writeFirstTimeToDataBlock(MifareClassic mfc, int dataSectorIndex, int backupSectorIndex, int amount) throws Exception{
		/** First will write to data block*/
		int blockIndex = mfc.sectorToBlock(dataSectorIndex) ;
		
		byte[] bytes = ByteBuffer.allocate(4).putInt(backupSectorIndex).array();
		byte originalAddress = 0;
		for (int i=0; i < bytes.length; i++) {
			if(i == (bytes.length-1))
				originalAddress = bytes[i];
		}       
		byte reversedbyte = (byte) (~originalAddress & 0xFF);
		Log.d(TAG,String.format("Original is :0x%x ", originalAddress));
		Log.d(TAG,String.format("reversed is :0x%x ", reversedbyte));
		Log.d(TAG, "Backup Block index: "+blockIndex);
		
		byte myByte = (byte) 255;	
		byte[] dataValueWithBackupAddress = {0, 0, 0, 0, myByte, myByte, myByte, myByte, 0, 0, 0, 0, originalAddress, reversedbyte, originalAddress, reversedbyte };
		mfc.writeBlock(blockIndex, dataValueWithBackupAddress);
		Log.d(TAG, "Amount to be written: "+amount);
		mfc.increment(blockIndex, amount);
		mfc.transfer(blockIndex);
		
		return true;
	}
	
	public boolean writeFirstTimeToBackupBlock(MifareClassic mfc, int sectorIndex, int amount) throws Exception{
		
		/** First will write to data block*/
		int blockIndex = mfc.sectorToBlock(sectorIndex) ;
		Log.d(TAG, "Block index: "+blockIndex);
			
		byte myByte = (byte) 255;	
		byte[] dataValue = {0, 0, 0, 0, myByte, myByte, myByte, myByte, 0, 0, 0, 0, 0, myByte, 0, myByte };		
		Log.d(TAG, "Will write zero value with backup address");
		mfc.writeBlock(blockIndex, dataValue);
		//mfc.transfer(backupBlockIndex);
		Log.d(TAG, "Will write value now");
		mfc.increment(blockIndex, amount);
		mfc.transfer(blockIndex);
		
		return true;
	}
	
	public boolean restoreBlock(MifareClassic mfc, int fromBackupSectorIndex, int toDataSectorIndex) throws Exception{
		Log.d(TAG, "restoreBlock is called");
		int backupBlockIndex = mfc.sectorToBlock(fromBackupSectorIndex) ;
		int dataBlockIndex = mfc.sectorToBlock(toDataSectorIndex) ;
		Log.d(TAG, "backup Block index: "+backupBlockIndex);
		Log.d(TAG, "data Block index: "+dataBlockIndex);

		connectAndAuthenticate(mfc, fromBackupSectorIndex);
        String amount = readBlockAfterAutheitcation(mfc, fromBackupSectorIndex);
        Log.d(TAG, "amount retrieved from backup block: "+amount);
        
        connectAndAuthenticate(mfc, toDataSectorIndex);
        writeFirstTimeToDataBlock(mfc, toDataSectorIndex, fromBackupSectorIndex, Integer.parseInt(amount));
      
		return true;
	}
	
	public MFCWritingResult verifyBackupAddress(MifareClassic mfc, int sectorIndex) throws IOException{
		MFCWritingResult result = new MFCWritingResult();
		byte[] data;
		if(!mfc.isConnected())
			throw new IOException("NFC Tag is not connected");
		
		int blockIndex = mfc.sectorToBlock(sectorIndex) ;
		data = mfc.readBlock(blockIndex);
		
		String hex = getHexString(data);
		
		String or1 = hex.substring(hex.length()-2);
		String b1 = hex.substring(hex.length()-4, hex.length()-2);
		
		String orginalFirstHex = invertString(lookupHex(or1.substring(0,1).toUpperCase()));
		String originalSecondHex = invertString(lookupHex(or1.substring(1,2).toUpperCase()));
		String invertedFirstHex = lookupHex(b1.substring(0,1));
		String invertedSecondHex = lookupHex(b1.substring(1,2));
		
		if((orginalFirstHex+originalSecondHex).equalsIgnoreCase(invertedFirstHex+invertedSecondHex)){
			// convert backup address from hex to int then return it
			byte[] asBytes = hexStringToByteArray( hex.substring(0, 8));
	        reverseByteArrasInPlace(asBytes);
	        ByteBuffer bb = ByteBuffer.wrap(asBytes);
	        result.oldValue = bb.getInt();
			result.success = true;
			result.originalBackupAddress = Integer.parseInt(hex.substring(hex.length()-8, hex.length()-6));
		}
		else
		{
			result.success = false;
			result.message = "Corrupt Backup Address Format";
		}
		
    
		return result;
		
	}
	public boolean writeToBlock(MifareClassic mfc, int sectorIndex, boolean isIncrementOperation, int value){
		MFCWritingResult result = new MFCWritingResult();
		result.success = true;
		//byte myByte = (byte) 255;		
		//	byte[] zeroValue = {0, 0, 0, 0, myByte, myByte, myByte, myByte, 0, 0, 0, 0, 0, myByte, 0, myByte };
		try {
			int blockIndex = mfc.sectorToBlock(sectorIndex) ;
			Log.d(TAG, "value to write is: "+value);
			if(isIncrementOperation)
				mfc.increment(blockIndex, value);
			else
				mfc.decrement(blockIndex, value);
			mfc.transfer(blockIndex);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;

	}
	private String getHexString(byte[] bytes) {
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}
	
	private ArrayList<Integer> getSectorsIndexes(String block1And2, String targetAppId){
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		char[] charArray = block1And2.toCharArray();
		int index = 1;
		int sectorIndex = 0;
		
		StringBuffer buffer = new StringBuffer();
		String clusterCode = new String();
		String appId = new String();
		for (int i = 0; i < charArray.length; i++) {
			buffer.append(charArray[i]);
			
			if(index == 2)
			{
				appId = buffer.toString();
				buffer = new StringBuffer();
			}
			if(index == 4)
			{
				clusterCode = buffer.toString();
				if(new String(clusterCode+appId).equalsIgnoreCase(targetAppId))
					result.add(sectorIndex);
				buffer = new StringBuffer();
				index = 1;
				sectorIndex++;
			}
			else
				index++;
		}	
		return result;
	}
		
	public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                     + Character.digit(s.charAt(i+1), 16));
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return data;
    }
	
	/**
     * Reverse a byte Array (e.g. Little Endian -> Big Endian).
     * Hmpf! Java has no Array.reverse(). And I don't want to use
     * Commons.Lang (ArrayUtils) form Apache....
     * @param array The array to reverse (in-place).
     */
    public void reverseByteArrasInPlace(byte[] array) {
        for(int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }
	
	public enum AppID {
		
		MOBILIS_ID("88FF");
		
		private final String value;
		private AppID(final String value){
			this.value = value;
		}
		
		public String getValue(){
			return this.value;
		}
	}
	
	public String lookupHex(String hex) {
		System.out.println("lookup: "+hex);
         hexMap.put("0", "0000");
         hexMap.put("1", "0001");
         hexMap.put("2", "0010");
         hexMap.put("3", "0011");
         hexMap.put("4", "0100");
         hexMap.put("5", "0101");
         hexMap.put("6", "0110");
         hexMap.put("7", "0111");
         hexMap.put("8", "1000");
         hexMap.put("9", "1001");
         hexMap.put("A", "1010");
         hexMap.put("B", "1011");
         hexMap.put("C", "1100");
         hexMap.put("D", "1101");
         hexMap.put("E", "1110");
         hexMap.put("F", "1111");
         
         return hexMap.get(hex);
    }
	
	private String invertString(String hex){
		System.out.println("invertString: "+hex);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < hex.length(); i++) {
			if(hex.substring(i,(i+1)).equalsIgnoreCase("0"))
				buffer.append("1");
			else
				buffer.append("0");
		}
		System.out.println("inverted String: "+buffer.toString());
		return buffer.toString();
	}
	
	static Hashtable<String, String> hexMap =  new Hashtable<String, String>();
	static {	 	
		 hexMap.put("0", "0000");
         hexMap.put("1", "0001");
         hexMap.put("2", "0010");
         hexMap.put("3", "0011");
         hexMap.put("4", "0100");
         hexMap.put("5", "0101");
         hexMap.put("6", "0110");
         hexMap.put("7", "0111");
         hexMap.put("8", "1000");
         hexMap.put("9", "1001");
         hexMap.put("A", "1010");
         hexMap.put("B", "1011");
         hexMap.put("C", "1100");
         hexMap.put("D", "1101");
         hexMap.put("E", "1110");
         hexMap.put("F", "1111");
	}
}
