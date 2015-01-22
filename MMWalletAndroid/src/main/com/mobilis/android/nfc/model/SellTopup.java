package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.mobilis.android.nfc.R;

public class SellTopup extends AbstractModel{

	public SellTopup(Context context, Activity activity) {
		super(context, activity);
	}
	
	private String getCustomerSearchData(){
		StringBuffer buffer = new StringBuffer();
		if(isMsisdnTransaction())
		{
			Log.d("mobilisDemo", "yes is MSISDN Transaction ");
			buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_MSISDN) + getResString(R.string.EQUAL));
			buffer.append(getMsisdn());
		}
		else{
			Log.d("mobilisDemo", "NFC transaction ");
			buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_NFCTAGID) + getResString(R.string.EQUAL));
			buffer.append(getNFCId());
		}
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_MERCHANTTRANSACTION), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getClientId(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_PIN), getClientPIN(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_AMOUNT), getWorkingAmount(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_CURRENCY), getWorkingCurrency(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENT_TYPE), getResString(R.string.PAYMENT_TYPE_SELLTOPUP), false));		
		buffer.append(getFullParamString(getResString(R.string.REQ_DATE), getDate(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TIME), getTime(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
		return new String(buffer);
	}

	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
		return getTxl().getTxlId();
	}

	@Override
	public void verifyPostTaskResults() {
		super.verifyTransferTXL();
	}

}
