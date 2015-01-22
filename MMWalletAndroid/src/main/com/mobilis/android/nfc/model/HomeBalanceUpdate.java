package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.util.Log;

import com.mobilis.android.nfc.R;

import java.util.HashMap;

public class HomeBalanceUpdate extends AbstractModel{

	public static HashMap<String, String> balanceData;
	
	public HomeBalanceUpdate(Activity activity){
		super(activity, activity);
 
	}
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_MESSAGE_TYPE), getContext().getResources().getString(R.string.REQ_MESSAGE_TYPE_GETBALANCE), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_BALANCE_TYPE), getContext().getResources().getString(R.string.REQ_CUSTOMER), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CUST_DATA), getCustomerData(), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_CLIENT_ID), getClientId(), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_CUSTOMER_ID), getClientId(), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_CLIENT_PIN), getClientPIN(), false));
		buffer.append(getFullParamString(getContext().getResources().getString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
		return new String(buffer);
	}
	
	private String getCustomerData(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_NFCTAGID) + getResString(R.string.EQUAL));
		buffer.append(getAndroidId(getActivity()));
		buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MOBMONPIN) + getResString(R.string.EQUAL));
		buffer.append(getClientPIN());
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}

	@Override
	public void verifyPostTaskResults() {
		Log.d("mobilisDemo", "verifyResults in BalanceUpdate is called");
		Log.d("mobilisDemo", "responseStatus: "+getResponseStatus());
//		getDisplayHandler().dismissProgressBar();
//		getDisplayHandler().setStatusAlertMessage(null);
//		if (getResponseStatus() == getResInt(R.string.STATUS_OK)){
//			if(balanceData != null)
//			{
//				Log.d(HomeBalanceUpdate.class.getSimpleName(), "BalanceUpdate.verifyPostTaskResults() clearing balanceData");
//				balanceData.clear();
//			}
//			else
//			{
//				Log.d(HomeBalanceUpdate.class.getSimpleName(), "BalanceUpdate.verifyPostTaskResults() balanceData is null no need to clear it");
//			}
//			balanceData = getBalanceHashMap();
//			HomeFragment.updateBalanceAmount(balanceData);
//
//		}
//		else
//		{
//			getDisplayHandler().getTransactionStatusTV().setText("ERROR");
//			getDisplayHandler().getTransactionIdTV().setText(getServerError());
//		}
	}
	private HashMap<String, String> getBalanceHashMap(){
		
		Log.d(HomeBalanceUpdate.class.getSimpleName(), "BalanceUpdate.getBalanceHashMap() server response is: "+getServerResponse());
		String balanceResponse[] = getServerResponse().split(",");
		HashMap<String, String> result = new HashMap<String, String>();
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
						result.put("balance"+counter, key);
//						result.put("value"+counter, "$"+value);
						result.put("value"+counter, value);
						counter++;
					}
				}
				 
			}
		}
		return result;
	}
	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_GETBALANCE)));
		return getTxl().getTxlId();
	}

}
