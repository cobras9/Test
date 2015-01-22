package com.mobilis.android.nfc.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.model.MifareClassicTag;
import com.mobilis.android.nfc.util.MFCWritingResult;
import com.mobilis.android.nfc.util.MifareClassicReader;
import com.mobilis.android.nfc.util.NFCForegroundUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MifareClassicReaderActivity  extends Activity implements OnClickListener{
	
	private final String TAG = MifareClassicReaderActivity.class.getSimpleName();
	private MifareClassicReader mfcReader;
	private NFCForegroundUtil nfcForegrounfUtil;
	public final static int OPS_READ = 0;
	public final static int OPS_WRITE_INCREMENT = 1;
	public final static int OPS_WRITE_DECREMENT = 2;
	public static int opsType = OPS_READ;

	private MifareClassicTag tag;
	
	Button incrementButton;
	Button decrementButton;
	Button writeFirstTimeButton;
	Button restoreButton;
	EditText amountEditText;
	TextView balanceTextView;
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate is called");
		
		setContentView(R.layout.activity_mifareclassviewer);
		mapControllersToUI();
		
		mfcReader = new MifareClassicReader();
		nfcForegrounfUtil = new NFCForegroundUtil(this);
		
		getIntent().setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		setIntent(getIntent());
		// get an instance of MifareClassic upon arrival of new tag
		Tag intentTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		setTag(new MifareClassicTag());
		if(intentTag != null){
			if(!isSupportedMifareClassicTag(getIntent())){
				toast(this, "Tag type is not supported (not in MifareClassic row data format)");
				this.finish();
				return;
			}
			getTag().setMfc(MifareClassic.get(intentTag));
			byte[] tagId = intentTag.getId();
			String uid = bytesToHexString(tagId);
			
			if(getTag().getTagUId() == null || !(getTag().getTagUId().equalsIgnoreCase(uid))){	
				getTag().setTagUId(uid);
				getTag().setNewTag(true);
			}
			else 
				getTag().setNewTag(false);
		}
		resolveIntent(getIntent());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(nfcForegrounfUtil.getNfc() != null)
			enableNFC();			
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		opsType = OPS_READ;
		if(nfcForegrounfUtil.getNfc() != null)
			disableNFC();
	}
	
	@Override
    public void onNewIntent(Intent intent) {
		// instantiates one instance of this activity for multiple tag scans
		incrementButton.setEnabled(true);
		decrementButton.setEnabled(true);
		writeFirstTimeButton.setEnabled(true);
		amountEditText.setEnabled(true);
		
		// enable foreground
		if(nfcForegrounfUtil == null)
			nfcForegrounfUtil = new NFCForegroundUtil(this);
				
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		setIntent(intent);
		// get an instance of MifareClassic upon arrival of new tag
		Tag intentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		setTag(new MifareClassicTag());
		if(intentTag != null){
			if(!isSupportedMifareClassicTag(getIntent())){
				toast(this, "Tag type is not supported (not in MifareClassic row data format)");
				this.finish();
				return;
			}
			getTag().setMfc(MifareClassic.get(intentTag));
			byte[] tagId = intentTag.getId();
			String uid = bytesToHexString(tagId);
			
			if(getTag().getTagUId() == null || !(getTag().getTagUId().equalsIgnoreCase(uid))){	
				getTag().setTagUId(uid);
				getTag().setNewTag(true);
			}
			else 
				getTag().setNewTag(false);
		}
		// process new intent
        resolveIntent(intent);            
    }
	
	private boolean isSupportedMifareClassicTag(Intent intent){
		
		if(intent.getAction().equalsIgnoreCase(NfcAdapter.ACTION_TECH_DISCOVERED) || intent.getAction().equalsIgnoreCase(NfcAdapter.ACTION_TAG_DISCOVERED)){	
			Tag intentTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
			if(intentTag == null)
				return false;
			else 
				return true;
		
		}
		else
			return false;
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
	private void resolveIntent(Intent intent){
	
		if (!getPackageManager().hasSystemFeature("com.nxp.mifare")) {
			toast(this, "No MIFARE support!");
		    return;
		}
		switch (opsType) {
		case OPS_READ:
			readAppsValueBlock();
			break;
		case OPS_WRITE_INCREMENT:
			writeToTag(this, true);
			break;
		case OPS_WRITE_DECREMENT:
			writeToTag(this, false);	
			break;
		}
		
	}

	private void mapControllersToUI() {
		incrementButton = (Button) findViewById(R.id.Button_increment);
		decrementButton = (Button) findViewById(R.id.Button_decrement);
		writeFirstTimeButton = (Button) findViewById(R.id.Button_WriteFirstTime);
		restoreButton = (Button) findViewById(R.id.Button_Restore);
		amountEditText = (EditText) findViewById(R.id.EditText_Amount);
		balanceTextView = (TextView) findViewById(R.id.TextView_Balance);
		incrementButton.setOnClickListener(this);
		decrementButton.setOnClickListener(this);
		writeFirstTimeButton.setOnClickListener(this);
		restoreButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		InputMethodManager imm = null;
		switch (v.getId()) {
			case R.id.Button_increment:
				imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(amountEditText.getWindowToken(), 0);
				opsType = OPS_WRITE_INCREMENT;
				Log.d(TAG, "Changed opsType to "+opsType);
				writeToTag(v.getContext(), true);
				break;
			case R.id.Button_decrement:
				imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(amountEditText.getWindowToken(), 0);
				opsType = OPS_WRITE_DECREMENT;
				Log.d(TAG, "Changed opsType to "+opsType);
				writeToTag(v.getContext(), false);
				break;
			case R.id.Button_WriteFirstTime:
				imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(amountEditText.getWindowToken(), 0);
				writeToTagFirstTime(v.getContext());
				break;
			case R.id.Button_Restore:
				imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(amountEditText.getWindowToken(), 0);
				restoreTag(v.getContext());
				break;
		}
	}
	
	private void restoreTag(Context context){
		
		try{
			// Authenticate Sector 0 to read MAD
			if(getTag().isNewTag()){
				if(!mfcReader.connectAndAuthenticate(getTag().getMfc(), 0))
				{
					toast(context,"Writing error: Failed to authetincate MAD");
					return;
				}
				// Get Sectors indexes for our app BB88
				ArrayList<Integer> sectors = mfcReader.getApplicationPositionFromSector0(getTag().getMfc(), MifareClassicReader.AppID.MOBILIS_ID.getValue());
				if(sectors.size() < 1){
					toast(context, "No Entry for MMWallet app found on Tag");
					return;
				}
				getTag().setDataSectorIndex(sectors.get(0));
				if(sectors.size() > 1){
					getTag().setHasBackupSector(true);
					getTag().setBackupSectorIndex(sectors.get(1));
				}
			}
			if(!getTag().hasBackupSector())
			{
				toast(context, "Tag does not have backup address.");
				return;
			}
			Log.d(TAG, "will call restoreBlock method in helper class now");
			boolean ops = mfcReader.restoreBlock(getTag().getMfc(), getTag().getBackupSectorIndex(), getTag().getDataSectorIndex());
			if(ops){
				toast(context, "Data's been restored successfully");
				readAppsValueBlock();
			}
			else
				toast(context, "Error occured while restoring data");	
			
         }
		catch(TagLostException t){
			t.printStackTrace();
			toast(context,"Writing error: Tag has been removed before completing operation.");
		}
		catch(Exception e){
			e.printStackTrace();
			toast(context,"Place phone on tag and try again");
		}
	}
	
	private void writeToTag(Context context, boolean isIncrement) {
		
		if(amountEditText.getText().toString().length() == 0){
			toast(context, "Enter amount");
			return;
		}
		try{
			if(getTag().isNewTag()){
				// Authenticate Sector 0 to read MAD
				if(!mfcReader.connectAndAuthenticate(getTag().getMfc(), 0))
				{
					toast(context,"Writing error: Failed to authetincate MAD");
					return;
				}
				// Get Sectors indexes for our app BB88
				ArrayList<Integer> sectors = mfcReader.getApplicationPositionFromSector0(getTag().getMfc(), MifareClassicReader.AppID.MOBILIS_ID.getValue());
				if(sectors.size() < 1){
					toast(context, "No Entry for MMWallet app found on Tag");
					return;
				}
				getTag().setDataSectorIndex(sectors.get(0));
				if(sectors.size() > 1){
					getTag().setHasBackupSector(true);
					getTag().setBackupSectorIndex(sectors.get(1));
				}

			}
			// check Tag have backup address
			if(getTag().hasBackupSector()){
				// authenticate data sector
				if(!mfcReader.connectAndAuthenticate(getTag().getMfc(), getTag().getDataSectorIndex())){
					toast(context, "Writing error: Failed to authenticate data sector");
					return;
				}
				// verify backup address is in correct fromat in the data sector. Also retrieve amount in data sector
				MFCWritingResult result = mfcReader.verifyBackupAddress(getTag().getMfc(), getTag().getDataSectorIndex());
				if(!result.success){
					toast(context, (result.message.isEmpty() ? "Incorrect Backup address format in data block." : result.message));
					return;
				}
				// increment data sector
				Log.d(TAG, "result.originalBackupAddress retrieved from data block: "+result.originalBackupAddress);
				boolean ops = mfcReader.writeToBlock(getTag().getMfc(), getTag().getDataSectorIndex(), isIncrement, Integer.parseInt(amountEditText.getText().toString()));
				if(!ops){
					toast(context, "Writing error: Failed to write on data sector");
					return;
				}
				// authenticate backup sector
				if(!mfcReader.connectAndAuthenticate(getTag().getMfc(), getTag().getBackupSectorIndex())){
					toast(context, "Writing error: Failed to authenticate backup sector");
					return;
				}
				//write retrieved (old) amount from data block into backup block
				ops = mfcReader.writeToBlock(getTag().getMfc(),  getTag().getBackupSectorIndex(), isIncrement, result.oldValue);
				if(!ops){
					toast(context, "Writing error: Failed to write on backup sector");
					return;
				}
			}
			// update UI with new balance
			int amount = Integer.parseInt(amountEditText.getText().toString());
			if(isIncrement)
				getTag().setDataBalance(String.valueOf(Integer.parseInt(getTag().getDataBalance()) + amount));
			else
				getTag().setDataBalance(String.valueOf(Integer.parseInt(getTag().getDataBalance()) - amount));
			toast(context, "Successfully Written amount onto tag");
			balanceTextView.setText("Balance: "+formatBalance(String.valueOf(getTag().getDataBalance())));
			getTag().getMfc().close();
         }
		catch(TagLostException t){
			t.printStackTrace();
			toast(context,"Writing error: Tag has been removed before completing operation.");
		}
		catch(IOException e){
			e.printStackTrace();
			toast(context,"Place phone on tag and try again");
		}
	}

	private void writeToTagFirstTime(Context context) {
		if(amountEditText.getText().toString().length() == 0)
		{
			toast(context, "Enter amount");
			return;
		}
		try{
			// Authenticate Sector 0 to read MAD
			if(!mfcReader.connectAndAuthenticate(getTag().getMfc(), 0)){
				toast(context,"Writing error: Failed to authetincate MAD");
				return;
			}
			if(getTag().isNewTag()){
				// Get Sectors indexes for our app BB88
				ArrayList<Integer> sectors = mfcReader.getApplicationPositionFromSector0(getTag().getMfc(), MifareClassicReader.AppID.MOBILIS_ID.getValue());
				getTag().setDataSectorIndex(sectors.get(0));
				if(sectors.size() > 1)
				{
					getTag().setHasBackupSector(true);
					getTag().setBackupSectorIndex(sectors.get(1));
				}
				Log.d(TAG, "Backup address sector retrieved from MAD is: "+sectors.get(1));
			}
			
			// authenticate data sector
			if(!mfcReader.connectAndAuthenticate(getTag().getMfc(), getTag().getDataSectorIndex())){
				toast(context, "Writing error: Failed to authenticate App's data sector");
				return;
			}
			boolean ops = false;
			try {
				//write to data sector
				ops = mfcReader.writeFirstTimeToDataBlock(getTag().getMfc(), getTag().getDataSectorIndex(), 
								getTag().getBackupSectorIndex(),Integer.parseInt(amountEditText.getText().toString()));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
				
			if(!ops){
				toast(context, "Writing error: Failed to write on App's data sector");
				return;
			}
			if(!getTag().hasBackupSector()){
				getTag().getMfc().close();
				Log.w(TAG, "Successfully wrote first value onto Tag. W: Tag does not have backup address for data's sector");
				return;
			}
			// authenticate back up data sector
			if(!mfcReader.connectAndAuthenticate(getTag().getMfc(), getTag().getBackupSectorIndex())){
				toast(context, "Writing error: Failed to authenticate App's backup sector");
				return;
			}
			ops = false;
			try {
				//write to backup data sector value 0..this is first time write
				ops = mfcReader.writeFirstTimeToBackupBlock(getTag().getMfc(), getTag().getBackupSectorIndex(), 0);
			} 
			catch (NumberFormatException e) {
				e.printStackTrace();
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			if(!ops){
				toast(context, "Writing error: Failed to write on App's backup sector");
				return;
			}
			amountEditText.setText(amountEditText.getText().toString());
			toast(context, "Successfully wrote first value onto Tag");
			getTag().getMfc().close();
		}
		catch(TagLostException t){
			t.printStackTrace();
			toast(context,"Writing error: Tag has been removed before completing operation.");
		}
		catch(IOException e){
			e.printStackTrace();
			toast(context,"Place phone on tag and try again");
		}
	}

	private void readAppsValueBlock() {

		try{
			if(!mfcReader.connectAndAuthenticate(getTag().getMfc(), 0))
			{
				toast(this, "Reading error: Failed to authenticate MAD sector");
				return;
			}
			ArrayList<Integer> sectors = mfcReader.getApplicationPositionFromSector0(getTag().getMfc(), MifareClassicReader.AppID.MOBILIS_ID.getValue());
			if(sectors.size() <= 0){
				toast(this,"Card does not have MMWallet data.");
				incrementButton.setEnabled(false);
				decrementButton.setEnabled(false);
				writeFirstTimeButton.setEnabled(false);
				amountEditText.setEnabled(false);
				return;
			}
			getTag().setDataSectorIndex(sectors.get(0));
			if(sectors.size() > 1){
				getTag().setHasBackupSector(true);
				getTag().setBackupSectorIndex(sectors.get(1));
			}
			if(!mfcReader.connectAndAuthenticate(getTag().getMfc(), sectors.get(0))){
				toast(this, "Reading error: Failed to autheticate App's sector");
				return;
			}
			String balance = mfcReader.readBlockAfterAutheitcation(getTag().getMfc(), getTag().getDataSectorIndex());
			getTag().setDataBalance(balance);
			getTag().getMfc().close();
			balanceTextView.setText("Balance: "+formatBalance(balance));
		}
		catch(TagLostException t){
			t.printStackTrace();
			toast(this,"Reading error: Tag has been removed before completing operation.");
		}
		catch(IOException e){
			e.printStackTrace();
			toast(this,"Reading error: General IO Exception");
		}
	}

	
	public void disableNFC(){
		nfcForegrounfUtil.disableForeground();
		Log.d(TAG, "disabled NFC foreground");
	}
	
	public void enableNFC(){
		nfcForegrounfUtil.enableForeground();
		Log.d(TAG, "Enabled NFC foreground");
	}
	
	private void toast(Context context, String message){
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	private String formatBalance(String balance){
		double balanceAsDouble = Double.parseDouble(balance);
		balanceAsDouble = balanceAsDouble/100;
		DecimalFormat df = new DecimalFormat("0.00##");
		return df.format(balanceAsDouble);
	}

	public MifareClassicTag getTag() {
		return tag;
	}

	public void setTag(MifareClassicTag tag) {
		this.tag = tag;
	}

}
