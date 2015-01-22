package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.fragments.BuyElectronicVoucherFragment;
import com.mobilis.android.nfc.fragments.ElectronicVoucherFragment;

public class VoucherExchangeQuotation extends AbstractModel{

    private String denomination;
    private String destinationCode;
    private String quantity;

	public VoucherExchangeQuotation(Activity activity) {
		super(activity, activity);
	}
 
	private String getCustomerData(){
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
        buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
        buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_EXCHANGE_QUOTATION), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENT_TYPE), getResString(R.string.PAYMENT_TYPE_EVD), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMerchantId(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_AMOUNT), getWorkingAmount(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_CURRENCY), getWorkingCurrency(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_DESTINATION_CURRENCY), getWorkingCurrency(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_SOURCE_CURRENCY), getWorkingCurrency(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_DENOMINATION), getDenomination(), false));
        if(ElectronicVoucherFragment.isCashInOperation || BuyElectronicVoucherFragment.isCashInOperation) {
            buffer.append(getFullParamString(getResString(R.string.REQ_QUANTITY), getQuantity(), false));
            buffer.append(getFullParamString(getResString(R.string.REQ_CUST_DATA), getCustomerData(), false));
        }
        buffer.append(getFullParamString(getResString(R.string.REQ_DESTINATION_CODE), getDestinationCode(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
		return new String(buffer);
	}

	public String getTransactionId() {
		setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
		return getTxl().getTxlId();
	}

	@Override
	public void verifyPostTaskResults() {
        Intent intent = new Intent(INTENT.EXCHANGE_QUOTATION.toString());
        if(getResponseStatus() == getResInt(R.string.STATUS_OK)){
            String name = getName(getServerResponse());
            String fee = getMerchantFees(getServerResponse());
            String totalAmount = getTotalAmount(getServerResponse());
            intent.putExtra(INTENT.EXTRA_NAME.toString(),name);
            intent.putExtra(INTENT.EXTRA_MERCHANT_FEES.toString(),fee);
            intent.putExtra(INTENT.EXTRA_TOTAL_AMOUNT.toString(),totalAmount);
        }
        else{
            intent.putExtra(INTENT.EXTRA_NAME.toString(),"");
            intent.putExtra(INTENT.EXTRA_MERCHANT_FEES.toString(),"");
            intent.putExtra(INTENT.EXTRA_TOTAL_AMOUNT.toString(),"");
        }
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

    private String getTotalAmount(String response){
        String[] temp = response.split("ReceivedAmount=");
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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
