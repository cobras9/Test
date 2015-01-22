package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;


public class ChangePIN extends AbstractModel{

    private String enteredCustomerId;

	public ChangePIN(Activity activity){
		super(activity, activity);
	}
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
        buffer.append(super.addDateTime());
		buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_CHANGEPIN), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
        String merchantId = getEnteredCustomerId();
        if(merchantId != null && !merchantId.isEmpty())
		    buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), merchantId, false));
        else
            buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMerchantId(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_PIN), getClientOldPIN(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_NEWPIN), getClientNewPIN(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
		return new String(buffer);
	}

	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_CHANGEPIN)));
		return getTxl().getTxlId();
	} 

	@Override
	public void verifyPostTaskResults() {
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.CHANGE_PIN.toString()).putExtra(INTENT.EXTRA_RESPONSE.toString(), getServerResponse()));
    }

    public String getEnteredCustomerId() {
        return enteredCustomerId;
    }

    public void setEnteredCustomerId(String enteredCustomerId) {
        this.enteredCustomerId = enteredCustomerId;
    }

}
