package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.util.Financial;

import java.util.Locale;

public class MakePaymentLocal extends AbstractModel implements Financial{

    private boolean destinationProvided;
    private String destination;


    private String pin;

    public MakePaymentLocal(Activity activity) {
        super(activity, activity);
    }

    private String getCustomerSearchData(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_NFCTAGID) + getResString(R.string.EQUAL));
        buffer.append(getAndroidId(getActivity()));
        buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MOBMONPIN) + getResString(R.string.EQUAL));
//        if(getActivity().getString(R.string.app_name).toUpperCase(Locale.US).contains(getActivity().getString(R.string.AIRTEL)) || getActivity().getString(R.string.app_name).toUpperCase().contains("NFC"))
        if(getPin() != null)
            buffer.append(getPin());
        else
            buffer.append(ApplicationActivity.loginClientPin);
        buffer.append(getResString(R.string.CLOSE_BRACKET));
        return new String(buffer);
    }

    private String getRecepientData(){
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
        buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_TRANSFER), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CHANNEL), getResString(R.string.MPOS), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMerchantId(), false));
        if(getActivity().getString(R.string.app_name).toUpperCase(Locale.US).contains(getActivity().getString(R.string.AIRTEL)) || getActivity().getString(R.string.app_name).toUpperCase().equalsIgnoreCase("NFC"))
            buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_PIN), getPin(), false));
        else
            buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_PIN), ApplicationActivity.loginClientPin, false));
        buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_AMOUNT), getWorkingAmount(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_CURRENCY), getWorkingCurrency(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENT_TYPE), getResString(R.string.PAYMENT_TYPE_OUTTXF), false));
        if(nfcScanned) {
            buffer.append(getFullParamString(getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(), false));
            setNfcScanned(false);
        }
        buffer.append(getFullParamString(getResString(R.string.REQ_ORIGINCODE), getResString(R.string.REQ_ORIGINCODE_VALUE), false));
        if(isDestinationProvided())
            buffer.append(getFullParamString(getResString(R.string.REQ_DESTINATION), getDestination(), false));
        else
            buffer.append(getFullParamString(getResString(R.string.REQ_RECEPIENT_DATA), getRecepientData(), false));

        buffer.append(getFullParamString(getResString(R.string.REQ_DESTINATION_CODE), getResString(R.string.REQ_DESTINATIONCODE_VFMM_VALUE), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));

        return new String(buffer);
    }

    public String getTransactionId() {
        setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
        return getTxl().getTxlId();
    }

    @Override
    public void verifyPostTaskResults() {
        Intent intent = new Intent(INTENT.SEND_MONEY_LOCAL.toString());
        intent.putExtra(INTENT.EXTRA_RESPONSE.toString(), getServerResponse());
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        super.verifyTransferTXL();
    }


    public boolean isDestinationProvided() {
        return destinationProvided;
    }

    public void setDestinationProvided(boolean destinationCodeProvided) {
        this.destinationProvided = destinationCodeProvided;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }


    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
