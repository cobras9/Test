package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.fragments.ElectronicVoucherFragment;

public class ElectronicVoucher extends AbstractModel {

    private String denomination;
    private String destinationCode;
    private String issuer;
    private String quantity;
    private String creditCardNumber;
    private String creditCardExpiry;
    private String owner;

    public ElectronicVoucher(Activity activity) {
		super(activity, activity);
	}
 
	private String getCustomerSearchData(){
        StringBuffer buffer = new StringBuffer();
        if(nfcScanned) {
            buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_NFCTAGID) + getResString(R.string.EQUAL));
            buffer.append(getNFCId());
//            buffer.append(getAndroidId(getActivity()));
        }
        else {
            buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_MSISDN) + getResString(R.string.EQUAL));
            buffer.append(getMsisdn());
        }
        if (getCreditCardNumber() != null) {
            buffer.append("," + getFullParamString(getResString(R.string.REQ_CREDIT_CARD_NUMBER), getCreditCardNumber(), false));
        }

        if (getCreditCardExpiry() != null) {
            buffer.append(getFullParamString(getResString(R.string.REQ_CREDIT_CARD_EXPIRY), getCreditCardExpiry(), true));
        }

        buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
        buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_EVD), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getOwner(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_PIN), ApplicationActivity.loginClientPin, false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_AMOUNT), getWorkingAmount(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_CURRENCY), getWorkingCurrency(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_ISSUER), getIssuer(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_DENOMINATION), getDenomination(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(), false));
        if(ElectronicVoucherFragment.isCashInOperation)
            buffer.append(getFullParamString(getResString(R.string.REQ_QUANTITY), getQuantity(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));

		return new String(buffer);
	}

	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
		return getTxl().getTxlId();
	}

	@Override
	public void verifyPostTaskResults() {
        Intent intent = new Intent(INTENT.ELECTRONIC_VOUCHER.toString());
        intent.putExtra(INTENT.EXTRA_RESPONSE.toString(), getServerResponse());
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
	}

    public String getDestinationCode() {
        return destinationCode;
    }

    public void setDestinationCode(String destinationCode) {
        this.destinationCode = destinationCode;
    }

    private String getName(String response){
        String[] temp = response.split("RecipientName=");
        if(temp.length <= 1)
            return "";
        String[] data = temp[1].split(",");
        String name = data[0];
        return name;
    }

    private String getMerchantFees(String response){
        String[] temp = response.split("MerchantFee=");
        if(temp.length <= 1)
            return "";
        String[] data = temp[1].split(",");
        String fee = data[0];
        return fee;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCreditCardExpiry() {
        return creditCardExpiry;
    }

    public void setCreditCardExpiry(String creditCardExpiry) {
        this.creditCardExpiry = creditCardExpiry;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}
