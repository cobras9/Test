package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;

/**
 * Created by dee-bitros on 23/04/14.
 */
public class RedeemVoucher extends AbstractModel {

    private String strClaimCode;

    public RedeemVoucher(Activity activity){
        super(activity, activity);
    }

    @Override
    public String getRequestParameters() {

        StringBuffer buffer = new StringBuffer();
        buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_REMMITANCE_CLAIM), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_CHANNEL), getResString(R.string.REQ_SMARTPHONE), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMerchantId(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_PIN), ApplicationActivity.loginClientPin, false));
        buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_AMOUNT), getWorkingAmount(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_WORKING_CURRENCY), getWorkingCurrency(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_PAYMENTTYPE), "C2MR", false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLAIM_CODE), getClaimCode(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));

        return new String(buffer);
    }

    public String getTransactionId() {
        setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
        return getTxl().getTxlId();
    }

    @Override
    public void verifyPostTaskResults() {
        Intent intent = new Intent(INTENT.REDEEM_VOUCHER.toString());
        intent.putExtra(INTENT.EXTRA_RESPONSE.toString(), getServerResponse());
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        super.verifyTransferTXL();
    }

    public String getClaimCode() {
        return strClaimCode;
    }

    public void setClaimCode(String strClaimCode) {
        this.strClaimCode = strClaimCode;
    }

}
