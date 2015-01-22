package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.interfaces.HideServerResponse;

/**
 * Created by dee-bitros on 15/04/14.
 */
public class EVDCodes extends AbstractModel implements HideServerResponse {

    private static final String LOG_TAG = EVDCodes.class.getSimpleName();

    private String owner = null;

    public EVDCodes(Activity activity){
        super(activity, activity);
    }

//    DISPLAYDATA=(MTNEVD|100|22,MTNEVD|200|5,AitelEVD|500|45)
    @Override
    public String getRequestParameters() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_EVOUCHERGET), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getOwner(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), true));
        return new String(buffer);
    }

    @Override
    public void verifyPostTaskResults() {
        super.verifyTransferTXL();
        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(INTENT.CODE_EVD.toString()).
                putExtra(INTENT.EXTRA_SERVER_RESPONSE.toString(), getServerResponse()));
    }

    public String getTransactionId() {
        setTxl(generateTXLId(getResString(R.string.DB_TXL_PAYMENT)));
        return getTxl().getTxlId();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}
