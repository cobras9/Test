package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;

public class CashOutVouchers extends AbstractModel {

	private String givenName;
	private String surName;
	private String recepientMSISDN;
	
	public CashOutVouchers(Activity activity){
		super(activity, activity);
	}
	
	private String getRecepientData(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_GIVENNAME) + getResString(R.string.EQUAL));
		buffer.append(getGivenName());
		buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_SURNAME) + getResString(R.string.EQUAL));
		buffer.append(getSurName());
		buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MSISDN) + getResString(R.string.EQUAL));
		buffer.append(getRecepientMSISDN());
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}

    private String getCustomerData(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_MSISDN) + getResString(R.string.EQUAL));
        buffer.append(getMerchantId());
        buffer.append(getResString(R.string.CLOSE_BRACKET));
        return new String(buffer);
    }
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_REMMITANCE_SEND), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMerchantId(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_PIN), ApplicationActivity.loginClientPin, false));
		buffer.append(getFullParamString(getResString(R.string.REQ_RECEPIENT_DATA), getRecepientData(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_AMOUNT), getWorkingAmount(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_CURRENCY), getWorkingCurrency(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENT_TYPE), getResString(R.string.PAYMENT_TYPE_SENDTOPUP), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CUST_DATA), getCustomerData(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
		return new String(buffer);
	}

	@Override
	public void verifyPostTaskResults() {

        Intent intent = new Intent(INTENT.CASH_OUT_VOUCHER.toString());
        intent.putExtra(INTENT.EXTRA_RESPONSE.toString(), getServerResponse());
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
//		super.verifyTransferTXL();
	}
	
	
	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
		return getTxl().getTxlId();
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getSurName() {
		return surName;
	}

	public void setSurName(String surName) {
		this.surName = surName;
	}

	public String getRecepientMSISDN() {
		return recepientMSISDN;
	}

	public void setRecepientMSISDN(String recepientMSISDN) {
		this.recepientMSISDN = recepientMSISDN;
	}


}
