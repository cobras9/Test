package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ProgressBar;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.domain.TxlDomain;
import com.mobilis.android.nfc.util.BitsMapper;
import com.mobilis.android.nfc.util.SpecialTxl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TagRegistration extends AbstractModel implements SpecialTxl{

	private String clientPin;
	private ProgressDialog progressDialog;
    private ProgressBar progressBar;
	private Dialog displayDialog;
    private String tagType;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyMMddhhmmss");

	public TagRegistration(Activity activity){
		super(activity, activity);
	}
	
	private String getCustomerSearchData(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_IMSI) + getResString(R.string.EQUAL));
		buffer.append(getNFCId());
        if(getClientPin() == null || getClientPin().isEmpty()) {
            //don't add them
        }
        else{
            buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MOBMONPIN) + getResString(R.string.EQUAL));
            buffer.append(getClientPin());
        }
        buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MSISDN) + getResString(R.string.EQUAL));
		buffer.append(getMsisdn());
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
        buffer.append(super.addDateTime());
		buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_REGISTERTAG), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(context), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMerchantId(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TAGTYPE), getTagType(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(), true));	
		
		return buffer.toString();
	}

	@Override
	public void verifyPostTaskResults() {
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.TAG_REGISTRATION.toString()).putExtra(INTENT.EXTRA_RESPONSE.toString(), getServerResponse()));
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
//		TxlDomain lastTxl = getDBService().getLastLogin();
		StringBuffer txlId = new StringBuffer(sdf.format(new Date()));
//        txlId.append(getIMEIFromPhone(context));
		String dateCreated = DateFormat.getDateTimeInstance().format(new Date());


		TxlDomain newTxl = new TxlDomain();
		newTxl.setTxlId(txlId.toString());
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

    public String getClientPin() {
		return clientPin;
	}

	public void setClientPin(String clientPin) {
		this.clientPin = clientPin;
	}

	public ProgressDialog getProgressDialog() {
		return progressDialog;
	}

	public void setProgressDialog(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

	public Dialog getDisplayDialog() {
		return displayDialog;
	}

	public void setDisplayDialog(Dialog displayDialog) {
		this.displayDialog = displayDialog;
	}

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public static enum TAG_TYPE{
        PRIMARY("PRIMARY", BitsMapper.getContext().getResources().getString(R.string.TAGTYPE_PRIMARY)),
        SECONDARY("SECONDARY", BitsMapper.getContext().getResources().getString(R.string.TAGTYPE_SECONDARY)),
        REPLACEMENT("REPLACE", BitsMapper.getContext().getResources().getString(R.string.TAGTYPE_REPLACE));

        private final String type;
        private final String label;
        private TAG_TYPE(String type, String label){

            this.label = label;
            this.type = type;
        }
        public String toString(){
            return this.type;
        }

        public String getLabel() {
            return this.label;
        }
    }
}
