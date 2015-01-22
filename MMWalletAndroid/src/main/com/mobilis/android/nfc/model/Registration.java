package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.domain.TxlDomain;
import com.mobilis.android.nfc.util.SpecialTxl;

import java.text.DateFormat;
import java.util.Date;

public class Registration extends AbstractModel implements SpecialTxl{

	private String clientPin;
	private ProgressDialog progressDialog;
	private Dialog displayDialog;
	
	public Registration(Activity activity){
		super(activity, activity, true);
	}
	
	private String getCustomerSearchData(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_IMSI) + getResString(R.string.EQUAL));
		buffer.append(getAndroidId(getActivity()));
		buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MOBMONPIN) + getResString(R.string.EQUAL));
		buffer.append(getClientPin());
		buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MSISDN) + getResString(R.string.EQUAL));
		buffer.append(getMsisdn());
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_REGISTERTAG), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(context), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMsisdn(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TAGTYPE), getResString(R.string.TAGTYPE_PRIMARY), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(), true));	
		
		return buffer.toString();
	}

	@Override
	public void verifyPostTaskResults() {
	    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.REGISTRATION_RESULT.toString()).putExtra(INTENT.EXTRA_RESPONSE.toString(), getServerResponse()));
	}
	
	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
		return getTxl().getTxlId();
	}
	
	public String getFullParamString(String key, String value, boolean lastParam){
		StringBuffer buffer = new StringBuffer();
     	if(lastParam)
     		buffer.append(key+getResString(R.string.EQUAL)+value);
     	else
     		buffer.append(key+getResString(R.string.EQUAL)+value+getResString(R.string.COMMA));
     	return new String(buffer);
     	
    }
	
	public TxlDomain generateTXLId(String txlType){
		TxlDomain lastTxl = getDBService().getLastLogin();
		String txlId = new String();
		String dateCreated = DateFormat.getDateTimeInstance().format(new Date());
		
		if (lastTxl.getTxlId() != null) {
			txlId = constructNewTxlId(lastTxl.getTxlId());
		}
		else{
			txlId  = new String(getResString(R.string.TRANSACTIONID_BASE));
		}
		TxlDomain newTxl = new TxlDomain();
		newTxl.setTxlId(txlId);
		newTxl.setDateCreated(dateCreated);
		newTxl.setTxlType(txlType);
		return newTxl;
	}
	
	private String constructNewTxlId(String lastTransaction){
		int counter = Integer.parseInt(lastTransaction);
		counter++;
		StringBuffer buffer = new StringBuffer(String.valueOf(counter));
		for (int i = buffer.length(); i < 10; i++) {
			buffer.insert(0, getResString(R.string.ZERO_STRING));
		}
		return new String(buffer);
	}

	public String getClientPin() {
		return clientPin;
	}

	public void setClientPin(String clientPin) {
		this.clientPin = clientPin;
	}

	public ProgressDialog getProgressDialog() {
		return progressDialog;
	}

	public void setProgressDialog(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

	public Dialog getDisplayDialog() {
		return displayDialog;
	}

	public void setDisplayDialog(Dialog displayDialog) {
		this.displayDialog = displayDialog;
	}

}
