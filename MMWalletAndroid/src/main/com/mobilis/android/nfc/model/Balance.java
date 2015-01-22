package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.mobilis.android.nfc.R;

public class Balance extends AbstractModel{

	private Dialog displayDialog;
	
	public Balance(Context context, Activity activity){
		super(context, activity);
	}
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
        buffer.append(super.addDateTime());
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_MESSAGE_TYPE), getContext().getResources().getString(R.string.REQ_MESSAGE_TYPE_GETBALANCE), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_CLIENT_ID), getClientId(), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_CUSTOMER_ID), getClientId(), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_CLIENT_PIN), getClientPIN(), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
		return new String(buffer);
	}

	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_GETBALANCE)));
		return getTxl().getTxlId();
	}

	@Override
	public void verifyPostTaskResults() {
		
		if(getRequestStatus() == getResInt(R.string.STATUS_SOCKET_EXCEPTION))
		{
//			getDisplayHandler().getTransactionStatusTV().setText("ERROR");
//			getDisplayHandler().getTransactionIdTV().setText("Network dropped before verifying transaction");
		}
		
		else if(getRequestStatus() == getResInt(R.string.STATUS_INITIALVALUE))
		{
//			getDisplayHandler().getTransactionStatusTV().setText("ERROR");
//			getDisplayHandler().getTransactionIdTV().setText("Can't establish connection with server");
		}
		
		else if (getResponseStatus() == getResInt(R.string.STATUS_OK)){
			commitNewTxlId(getTxl());
//			getDisplayHandler().getTransactionIdTV().setText(getContext().getResources().getString(R.string.STRING_ZERO_SPACE));
		}
		
		else if(getResponseStatus() == getResInt(R.string.STATUS_WRONG_PIN_ACCOUTN))
		{
//			getDisplayHandler().getTransactionStatusTV().setText("ERROR");
//			getDisplayHandler().getTransactionIdTV().setText(getContext().getResources().getString(R.string.TOAST_TXL_FAILED)+getServerError());
		}
		else
		{
//			getDisplayHandler().getTransactionStatusTV().setText("ERROR");
//			getDisplayHandler().getTransactionIdTV().setText(getContext().getResources().getString(R.string.TOAST_TXL_EXCEPTION));
		}
		
	}
	public Dialog getDisplayDialog() {
		return displayDialog;
	}
	public void setDisplayDialog(Dialog progressDialog) {
		this.displayDialog = progressDialog;
	}
	

}
