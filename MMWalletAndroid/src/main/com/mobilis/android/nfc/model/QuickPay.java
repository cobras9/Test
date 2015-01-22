package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.domain.TxlDomain;
import com.mobilis.android.nfc.util.Financial;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QuickPay extends AbstractModel implements Financial{


    private String destination;
    private String destinationCode;
    private String paymentType;
    private boolean destinationProvided;
    private boolean isC2MPTransaction;
    private boolean destinationCodeProvided;
    private String pin;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyMMddhhmmss");

    public QuickPay(Activity activity) {
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

    private String getCustomerSearchDataOnlyNFC(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_NFCTAGID) + getResString(R.string.EQUAL));
        buffer.append(getNFCId());
        if(getPin() != null)
        {
            buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MOBMONPIN) + getResString(R.string.EQUAL));
            buffer.append(getPin());
        }
        buffer.append(getResString(R.string.CLOSE_BRACKET));
        return new String(buffer);
    }

    private String getRecipientData(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_NFCTAGID) + getResString(R.string.EQUAL));
        buffer.append(getNFCId());
        buffer.append(getResString(R.string.CLOSE_BRACKET));
        return new String(buffer);
    }

	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
        buffer.append(super.addDateTime());
        if(isC2MPTransaction())
        {
            buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_CUSTOMERTRANSACTION), false));
            buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENT_TYPE), getResString(R.string.PAYMENT_TYPE_C2MP), false));
            buffer.append(getFullParamString(getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchDataOnlyNFC(), false));
        }else{
		    buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_TRANSFER), false));
            if(nfcScanned) {
                buffer.append(getFullParamString(getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(), false));
                setNfcScanned(false);
            }
            buffer.append(getFullParamString(getResString(R.string.REQ_ORIGINCODE), getResString(R.string.REQ_ORIGINCODE_VALUE), false));
            if(isDestinationCodeProvided())
                buffer.append(getFullParamString(getResString(R.string.REQ_DESTINATION_CODE), getDestinationCode(), false));
            else
                buffer.append(getFullParamString(getResString(R.string.REQ_DESTINATION_CODE), getResString(R.string.REQ_DESTINATIONCODE_VFMM_VALUE), false));

            if(getPaymentType() != null && !getPaymentType().isEmpty())
                buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENT_TYPE), getPaymentType(), false));
            else
                buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENT_TYPE), getResString(R.string.PAYMENT_TYPE_OUTTXF), false));

            if(isDestinationProvided())
                buffer.append(getFullParamString(getResString(R.string.REQ_DESTINATION), getDestination(), false));
            else
                buffer.append(getFullParamString(getResString(R.string.REQ_RECEPIENT_DATA), getRecipientData(), false));
        }
        buffer.append(getFullParamString(getResString(R.string.REQ_CHANNEL), getResString(R.string.MPOS), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMerchantId(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_PIN), ApplicationActivity.loginClientPin, false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_AMOUNT), getWorkingAmount(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_CURRENCY), getWorkingCurrency(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
		return new String(buffer);
	}

	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
		return getTxl().getTxlId();
	}

    public TxlDomain generateTXLId(String txlType){
        StringBuffer txlId = new StringBuffer(sdf.format(new Date()));
//        txlId.append(getIMEIFromPhone(context));
        String dateCreated = DateFormat.getDateTimeInstance().format(new Date());


        TxlDomain newTxl = new TxlDomain();
        newTxl.setTxlId(txlId.toString());
        newTxl.setDateCreated(dateCreated);
        newTxl.setTxlType(txlType);
        return newTxl;
    }

	@Override
	public void verifyPostTaskResults() {
        Intent intent = new Intent(INTENT.QUICK_PAY.toString());
        intent.putExtra(INTENT.EXTRA_RESPONSE.toString(), getServerResponse());
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
	}

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
    public String getDestinationCode() {
        return destinationCode;
    }

    public void setDestinationCode(String destinationCode) {
        this.destinationCode = destinationCode;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }


    public boolean isDestinationProvided() {
        return destinationProvided;
    }

    public void setDestinationProvided(boolean destinationProvided) {
        this.destinationProvided = destinationProvided;
    }

    public boolean isC2MPTransaction() {
        return isC2MPTransaction;
    }

    public void setC2MPTransaction(boolean isC2MPTransaction) {
        this.isC2MPTransaction = isC2MPTransaction;
    }

    public boolean isDestinationCodeProvided() {
        return destinationCodeProvided;
    }

    public void setDestinationCodeProvided(boolean destinationCodeProvided) {
        this.destinationCodeProvided = destinationCodeProvided;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
