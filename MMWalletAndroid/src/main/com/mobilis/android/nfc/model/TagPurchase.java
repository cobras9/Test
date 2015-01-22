package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.TxlDomain;
import com.mobilis.android.nfc.util.Constants;
import com.mobilis.android.nfc.util.SecurePreferences;
import com.mobilis.android.nfc.util.SpecialTxl;

import java.text.DateFormat;
import java.util.Date;

public class TagPurchase extends AbstractModel implements SpecialTxl{

	private String NFCId;
	private String itemDescription;
	private String clientPin;
	private String workingAmount;
	private String workingCurrency;
	private Activity activity;
	private Context context;
	private TxlDomain txl;
	private ProgressDialog progressDialog;
	private Dialog displayDialog;
//	private ItemsManager itemsManager;
	private String checksum;
	
	public TagPurchase(Context context, Activity activity){
		super(context, activity);
		setContext(context);
		setActivity(activity);
		setWorkingCurrency(Constants.currency);
//		setItemsManager(new ItemsManager());
	}

	private String getTagData(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_DESCRIPTION) + getResString(R.string.EQUAL));
		buffer.append(getItemDescription());

		if(getNFCId() != null)
		{
			buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_NFCTAGID) + getResString(R.string.EQUAL));
			buffer.append(getNFCId());
		}
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}
	
	private String getCustomerSearchData(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_IMSI) + getResString(R.string.EQUAL));
		buffer.append(getAndroidId(getActivity()));
		buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MOBMONPIN) + getResString(R.string.EQUAL));
		buffer.append(getClientPin());
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}

	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_CUSTOMERTRANSACTION), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(context), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getClientId().trim(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TAG), getTagData(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_AMOUNT), getWorkingAmount(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_CURRENCY), getWorkingCurrency(), false));
		if(checksum != null)
			buffer.append(getFullParamString(getResString(R.string.REQ_CHECKSUM), getChecksum(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENT_TYPE), getResString(R.string.PAYMENT_TYPE_TAGVIEWER), false));	
		buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
		return buffer.toString();
	}

	@Override
	public void verifyPostTaskResults() {
		Log.d("mobilisDemo", "inside verifyPostTaskResults right now");
		Log.d("mobilisDemo", "server response is: "+getRequestStatus());
		
//		TextView statutsTV = (TextView) getDisplayHandler().getStatusAlert().findViewById(R.id.transactionStatus);
//		TextView bodyTv = (TextView) getDisplayHandler().getStatusAlert().findViewById(R.id.transactionId);

		SecurePreferences preferences = new SecurePreferences(getContext());
		String myDeviceClientId = preferences.getString(SecurePreferences.KEY_LOGIN_CLIENT_ID, null);
		String quickBalance = preferences.getString(SecurePreferences.KEY_WIDGET_QUICKBALANCE, null);

		if(getRequestStatus() == -69){}
//			statutsTV.setText("No response from server");
		else if(getResponseStatus() == 0)
		{
//			statutsTV.setText("SUCCESSFUL");
//			statutsTV.setTextColor(Color.GREEN);

		}
		else if(getResponseStatus() == 666){
//			TextView titleTV = (TextView)getDisplayHandler().getStatusAlert().findViewById(R.id.transactionLabel);
//			titleTV.setText("");
//			getDisplayHandler().getTransactionStatusTV().setText("INTERNAL ERROR");
//			getDisplayHandler().getTransactionStatusTV().setTextSize(16);
//			getDisplayHandler().getTransactionIdTV().setText("SharedPreference error 10301");
//			getDisplayHandler().getStatusAlert().show();
		}
		else
		{
//			statutsTV.setText("FAILED");
//			statutsTV.setTextColor(Color.RED);
//			bodyTv.setText(getServerError());
		}
//		getDisplayHandler().getStatusAlert().show();
//		getDisplayHandler().getStatusAlert().setOnDismissListener(new OnDismissListener() {
//			public void onDismiss(DialogInterface dialog) {
//				getActivity().finish();
//			}
//		});
		
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
			Log.v("ModelAbstract.generateTXLId(): ", "lastTxl is not null and will create new one now");
			txlId = constructNewTxlId(lastTxl.getTxlId());
			Log.v("ModelAbstract.generateTXLId(): ", "new txl is: "+txlId);
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
		Log.v("TagViewer", "inside constructNewTxlId -- the olf txlId is: "+ lastTransaction);
		int counter = Integer.parseInt(lastTransaction);
		counter++;
		StringBuffer buffer = new StringBuffer(String.valueOf(counter));
		for (int i = buffer.length(); i < 10; i++) {
			buffer.insert(0, getResString(R.string.ZERO_STRING));
		}
		Log.v("TagViewer", "inside constructNewTxlId -- the new txlId is: "+ buffer);
		return new String(buffer);
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}

	public String getClientPin() {
		return clientPin;
	}

	public void setClientPin(String clientPin) {
		this.clientPin = clientPin;
	}

	public String getWorkingAmount() {
		return workingAmount;
	}

	public void setWorkingAmount(String workingAmount) {
		this.workingAmount = workingAmount;
	}

	public String getWorkingCurrency() {
		return workingCurrency;
	}

	public void setWorkingCurrency(String workingCurrency) {
		this.workingCurrency = workingCurrency;
	}
	
	public String getResString(int res){
		return getContext().getResources().getString(res);
	}
	public int getResInt(int res){
		return Integer.parseInt(getContext().getResources().getString(res));
	}

	public String getNFCId() {
		return NFCId;
	}

	public void setNFCId(String nFCId) {
		NFCId = nFCId;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public TxlDomain getTxl() {
		return txl;
	}

	public void setTxl(TxlDomain txl) {
		this.txl = txl;
	}

	public ProgressDialog getProgressDialog() {
		return progressDialog;
	}

	public void setProgressDialog(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

//	public ItemsManager getItemsManager() {
//		return itemsManager;
//	}
//
//	public void setItemsManager(ItemsManager itemsManager) {
//		this.itemsManager = itemsManager;
//	}

	public Dialog getDisplayDialog() {
		return displayDialog;
	}

	public void setDisplayDialog(Dialog displayDialog) {
		this.displayDialog = displayDialog;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

}
