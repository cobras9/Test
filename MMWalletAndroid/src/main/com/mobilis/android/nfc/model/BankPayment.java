package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Context;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.util.Financial;

public class BankPayment extends AbstractModel implements Financial{

	public BankPayment(Context context, Activity activity) {
		super(context, activity);
	}
 
	private String getCustomerSearchData(){
		StringBuffer buffer = new StringBuffer();
		if(isMsisdnTransaction())
		{
			buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_MSISDN) + getResString(R.string.EQUAL));
			buffer.append(getMsisdn());
		}
		else{
			buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_NFCTAGID) + getResString(R.string.EQUAL));
			buffer.append(getNFCId());
		}
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_TRANSFER), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), ApplicationActivity.loginClientId, false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_PIN), ApplicationActivity.loginClientPin, false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_AMOUNT), getWorkingAmount(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_CURRENCY), getWorkingCurrency(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENT_TYPE), getResString(R.string.PAYMENT_TYPE_BANK), false));
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
