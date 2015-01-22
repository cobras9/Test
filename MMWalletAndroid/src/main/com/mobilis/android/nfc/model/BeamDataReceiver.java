package com.mobilis.android.nfc.model;

import android.app.Activity;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.util.Constants;
import com.mobilis.android.nfc.util.SecurePreferences;
import com.mobilis.android.nfc.util.SharedPreferencesException;

public class BeamDataReceiver extends AbstractModel{

	String workingAmount;
	String toClientId;
	String fromClientPin;
	String fromClientId;
	String fromTerminalId;
	
	public BeamDataReceiver(Activity activity, String fromClientId, String fromClientPin, String fromTerminalId, String toClientId, String workingAmount){
		super(activity, activity);
		this.fromClientId = fromClientId;
		this.fromClientPin = fromClientPin;
		this.fromTerminalId = fromTerminalId;
		this.toClientId = toClientId;
		this.workingAmount = workingAmount; 
	}
	
	private String getCustomerSearchData(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_MSISDN) + getResString(R.string.EQUAL));
		buffer.append(toClientId);
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_MERCHANTTRANSACTION), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), fromTerminalId, false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), fromClientId, false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_PIN), fromClientPin, false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_AMOUNT), workingAmount, false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_CURRENCY), Constants.currency, false));
		buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENT_TYPE), getResString(R.string.PAYMENT_TYPE_DEPOSIT), false));	
		buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
		return new String(buffer);
	}
	
	@Override
	public void verifyPostTaskResults() {
		super.verifyTransferTXL();
	}

	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
		return getTxl().getTxlId();
	}
			
	public void saveConnectionDetails() throws SharedPreferencesException{
		getSharedPreference().edit().putString(SecurePreferences.KEY_SERVER_IP, getResString(R.string.SERVER_IP));
		getSharedPreference().edit().commit();
		getSharedPreference().edit().putString(SecurePreferences.KEY_SERVER_PORT, getResString(R.string.SERVER_PORT));
		getSharedPreference().edit().commit();
		if(getSharedPreference().getString(SecurePreferences.KEY_SERVER_IP, null) == null || getSharedPreference().getString(SecurePreferences.KEY_SERVER_IP, null).isEmpty())
			throw new SharedPreferencesException();
	}

}
