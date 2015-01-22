package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.util.Financial;

public class BillPayment extends AbstractModel implements Financial {

	private String payeeRef;
	private String payeeId;
	
	public BillPayment( Activity activity){
		super(activity, activity);
	}
	
	private String getCustomerSearchData(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_NFCTAGID) + getResString(R.string.EQUAL));
		buffer.append(getAndroidId(getActivity()));
		buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MOBMONPIN) + getResString(R.string.EQUAL));
		buffer.append(ApplicationActivity.loginClientPin);
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}
	
	@Override
	public String getRequestParameters() {
	
		StringBuffer buffer = new StringBuffer();
		buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_TRANSFER), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CHANNEL), getResString(R.string.MPOS), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), ApplicationActivity.loginClientId, false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_PIN), ApplicationActivity.loginClientPin, false));
        /** This is removed because no NFC Tag could be scanned **/
//		buffer.append(getFullParamString(getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_AMOUNT), getWorkingAmount(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_CURRENCY), getWorkingCurrency(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENTTYPE), "OUTTXF", false));
		buffer.append(getFullParamString(getResString(R.string.REQ_BILLPAYEE_REFERENCE), getPayeeRef(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_ORIGINCODE), "VFMM", false));
		buffer.append(getFullParamString(getResString(R.string.REQ_BILLPAYEE_ID), getPayeeId(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
		
		return new String(buffer);
	}

	@Override
	public void verifyPostTaskResults() {
        Intent intent = new Intent(INTENT.CABLE_TV.toString());
        intent.putExtra(INTENT.EXTRA_RESPONSE.toString(), getServerResponse());
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
	}
	
	
	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
		return getTxl().getTxlId();
	}

	public String getPayeeRef() {
		return payeeRef;
	}

	public void setPayeeRef(String payeeRef) {
		this.payeeRef = payeeRef;
	}

	public String getPayeeId() {
		return payeeId;
	}

	public void setPayeeId(String payeeId) {
		this.payeeId = payeeId;
	}

}
