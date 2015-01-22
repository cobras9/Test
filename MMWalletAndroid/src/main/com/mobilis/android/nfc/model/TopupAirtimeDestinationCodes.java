package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.interfaces.HideServerResponse;

public class TopupAirtimeDestinationCodes extends AbstractModel implements HideServerResponse {
	
	public TopupAirtimeDestinationCodes(Activity activity){
		super(activity, activity);
	}
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_TRANSFERCODESGET), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_TYPE), getResString(R.string.REQ_C2CA), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
		return new String(buffer);
	}

	@Override
	public void verifyPostTaskResults() {
		super.verifyTransferTXL();
		LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(INTENT.CODE_AIRTIME_TOP_UP.toString()).
                putExtra(INTENT.EXTRA_SERVER_RESPONSE.toString(), getServerResponse()));
	}
	
	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
		return getTxl().getTxlId();
	}


}
