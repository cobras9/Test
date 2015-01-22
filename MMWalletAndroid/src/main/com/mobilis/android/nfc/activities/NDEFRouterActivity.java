package com.mobilis.android.nfc.activities;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.util.Constants;
import com.mobilis.android.nfc.util.NFCForegroundUtil;
import com.mobilis.android.nfc.util.NFCReadHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class NDEFRouterActivity extends Activity{
	
	protected NFCForegroundUtil nfcForegroundUtil = null;
	private final String TAG = NDEFRouterActivity.class.getSimpleName();
	String fromClientId;
	String fromClientPin;
	String workingAmount;
	String fromTerminalId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate() is called with intent action: "+getIntent().getAction());
	    if(nfcForegroundUtil == null)
	    	nfcForegroundUtil = new NFCForegroundUtil(this);
		
		if(isNdefDiscoveredIntent())
	    {
	    	handleNdefIntent(getIntent());
	    }
		else if (isMifareClassicIntent()) {
			handleMifareClassicIntent();
		}
		
	}
	
	@Override
	public void onNewIntent(Intent intent) {

		Log.d(TAG, "onNewIntent() is called with intent action: "+getIntent().getAction());
		
		if(isNdefDiscoveredIntent())
			handleNdefIntent(intent);
		else if(isMifareClassicIntent()){
			handleMifareClassicIntent();
		}
	}
	
	private boolean isNdefDiscoveredIntent() {
		if(getIntent().getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED) || getIntent().getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED))
		{
			Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Log.d(TAG, "android.nfc.tech.MifareClassic.class.getSimpleName(): "+android.nfc.tech.MifareClassic.class.getSimpleName());
			Log.d(TAG, "tag.getClass().getSimpleName(): "+tag.getClass().getSimpleName());
			if(tag.getClass().getSimpleName().equalsIgnoreCase(android.nfc.tech.MifareClassic.class.getSimpleName()))
				return false; 
		}
		return getIntent().getAction().equalsIgnoreCase("android.nfc.action.NDEF_DISCOVERED");
	}
	private boolean isMifareClassicIntent() {
		
		return getIntent().getAction().equalsIgnoreCase("android.nfc.action.TECH_DISCOVERED");
	}
	
	private void handleMifareClassicIntent(){
		Log.d("ahmed", "Yes its Tech_descovered intent");
		if(!getPackageManager().hasSystemFeature("com.nxp.mifare")){
			Toast.makeText(this, "No MIFARE support!", Toast.LENGTH_LONG).show();
			return;
		}
	
	}
	@SuppressWarnings("unused")
	private void handleNdefIntent(Intent intent) {
		
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if(Ndef.get(tag) == null){
			Toast.makeText(this, "NDEF message is empty", Toast.LENGTH_LONG).show();
			return;
		}
		NFCReadHelper helper = new NFCReadHelper();	
		ArrayList<String> records = helper.readNDEFContent(Ndef.get(tag));
			
		Intent nextScreen;
		// get all items from all records in items ArrayList
		ArrayList<String> items = extractItemsFromNdefRecords(records);
		if(isRegistrationData(items)) // (NDEF) is coming from registration tag
		{
			nextScreen = new Intent(this, RegistrationActivity.class);
			startActivity(nextScreen);
			this.finish();
			return;
		}

		// (NDEF) is coming from nfc tag for purchase
		String NFCId , price, desc, clientId ;

		NFCId = items.get(0);		
		desc = items.get(2);
		clientId = items.get(3);
		double cost = Double.parseDouble(items.get(1));
		cost = cost/100;
		DecimalFormat df = new DecimalFormat("0.00##");
		price = df.format(cost);
		
		Constants.itemDescription = items.get(2).toString();
		nextScreen = new Intent(this, TagPurchaseActivity.class);					
		nextScreen = putExtraData(nextScreen, getResString(R.string.EXTRA_DATA_NFCID), NFCId);
		nextScreen = putExtraData(nextScreen, getResString(R.string.EXTRA_DATA_DESC), Constants.itemDescription);
		nextScreen = putExtraData(nextScreen, getResString(R.string.EXTRA_DATA_PRICE), price);
		nextScreen = putExtraData(nextScreen, getResString(R.string.EXTRA_DATA_EMBDEDDED_CLIENTID), clientId);
		startActivity(nextScreen);
		finish();
		intent = null;
		tag = null;	
	}

	private ArrayList<String> extractItemsFromNdefRecords(ArrayList<String> records) {
		ArrayList<String> items = new ArrayList<String>();
		for (String record : records) {
			String[] temp = record.split(",");
			for (String item : temp) {
				items.add(item);
			} 
		}
		return items;
	}

	private boolean isRegistrationData(ArrayList<String> items) {
		return items.size() == 2;
	}
	
	private void extractAndAssignBeamData(ArrayList<String> items){
		for (String string : items) {
			Log.d(TAG, "BeamData string is: "+string);
			String[] data = string.split(",");
			for (String dataItem : data) {
				if(dataItem.startsWith("workingAmount")){
					String[] amount = dataItem.split(":");
					workingAmount = amount[1];
				}
				else if(dataItem.startsWith("fromClientId")){
					String[] clientId = dataItem.split(":");
					fromClientId = clientId[1];
				}
				else if(dataItem.startsWith("fromClientPin")){
					String[] clientPin = dataItem.split(":");
					fromClientPin = clientPin[1];
				}
				else if(dataItem.startsWith("fromTerminalId")){
					String[] terminalId = dataItem.split(":");
					fromTerminalId = terminalId[1];
				}
			}
		}
	}

	public Intent putExtraData(Intent nextIntent, String key, String value) {
		return nextIntent.putExtra(key, value);
	}
	
	public String getResString(int res){
		return getResources().getString(res);
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    nfcForegroundUtil.disableForeground();
	}   
	
	@Override
	public void onResume() {
	    super.onResume();
	    nfcForegroundUtil.enableForeground();
	}
	
	
}
