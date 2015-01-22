package com.mobilis.android.nfc.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.AccountBalance;
import com.mobilis.android.nfc.domain.INTENT;

import java.util.TimerTask;

public class LoginBalance extends AbstractModel{

//	public static HashMap<String, String> balanceData;
	private TimerTask updateAlpha;
	private TextView loadingLabelTV;
	private EditText merchantPinET;
	
	public LoginBalance(Activity activity){
		super(activity, activity);

	}

	@Override
	public String getRequestParameters() {

    	StringBuffer buffer = new StringBuffer();
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_MESSAGE_TYPE), getContext().getResources().getString(R.string.REQ_MESSAGE_TYPE_GETBALANCE), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_BALANCE_TYPE), getContext().getResources().getString(R.string.REQ_CUSTOMER), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CUST_DATA), getCustomerData(), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_CLIENT_ID), ApplicationActivity.loginClientId, false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_CUSTOMER_ID), ApplicationActivity.loginClientId, false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_CLIENT_PIN), ApplicationActivity.loginClientPin, false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
        Log.d(LoginBalance.class.getSimpleName(),"Sending request: "+buffer.toString());
        return new String(buffer);
	}
	
	private String getCustomerData(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_NFCTAGID) + getResString(R.string.EQUAL));
		buffer.append(getAndroidId(getActivity()));
		buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MOBMONPIN) + getResString(R.string.EQUAL));
		buffer.append(ApplicationActivity.loginClientPin);
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}

	@Override
	public void verifyPostTaskResults() {
        Log.d("RUA", "LoginBalance verifyPostTaskResults() is called");
        Log.d("RUA", "getResponseStatus(): "+getResponseStatus());

        if (getResponseStatus() == getResInt(R.string.STATUS_OK)){
            commitNewTxlId(getTxl());
            final AccountBalance accountBalance = getBalanceHashMap();

            Log.d("RUA", "sending GOT_BALANCE intent now");
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.GOT_BALANCE.toString()).putExtra("ACCOUNT_BALANCE", accountBalance));
//            LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent("LOGIN_BALANCE").putExtra("SERVER_RESP", getServerResponse()).putExtra("ACCOUNT_BALANCE", accountBalance));
        }
	}
	private AccountBalance getBalanceHashMap(){
        AccountBalance accountBalance = new AccountBalance();
		String balanceResponse[] = getServerResponse().split(",");
		int counter = 0;
		for (String item : balanceResponse) {
			String[] itemDetails = item.split("=");
			if(itemDetails[0].equalsIgnoreCase("DspData"))
			{
				String[] balances = itemDetails[1].split(",");
				for (String balance : balances) {
					String[] dspData = balance.split("\\s+");
					String key  = null;
					String value = null;
                    if(dspData.length <= 1)
                        return accountBalance;
					if(dspData.length > 2)
					{
						key = dspData[0].replace("(", "") +" "+dspData[1];
						value = dspData[2].replace(")", "");//
					}
					else{
						key = dspData[0].replace("(", "");
						value = dspData[1].replace(")", "");//
					}
					
					if(value != null && !value.isEmpty())
					{
                        accountBalance.setDescription(key);
                        accountBalance.setValue(value);
						counter++;
					}
				}
			}
		}
        return accountBalance;
	}
	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_GETBALANCE)));
		return getTxl().getTxlId();
	}
	
	@SuppressLint("NewApi")
	private void resetUIComponents() {
		
		getUpdateAlpha().cancel();
		getMerchantPinET().setEnabled(true);
		getLoadingLabelTV().setVisibility(View.INVISIBLE);
	}

	public TimerTask getUpdateAlpha() {
		return updateAlpha;
	}

	public void setUpdateAlpha(TimerTask updateAlpha) {
		this.updateAlpha = updateAlpha;
	}

	public EditText getMerchantPinET() {
		return merchantPinET;
	}

	public void setMerchantPinET(EditText merchantPinET) {
		this.merchantPinET = merchantPinET;
	}

	public TextView getLoadingLabelTV() {
		return loadingLabelTV;
	}

	public void setLoadingLabelTV(TextView loadingLabelTV) {
		this.loadingLabelTV = loadingLabelTV;
	}

}
