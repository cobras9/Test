package com.mobilis.android.nfc.server.messages;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.model.AbstractModel;

import java.util.Calendar;

/**
 * Created by ahmed on 10/07/14.
 */
public class LoginRequest {

    AbstractModel model;

    public LoginRequest(AbstractModel model){
        this.model = model;
    }

    public String getServerMessage(String merchantPin){
        Calendar cal = Calendar.getInstance();
        StringBuffer buffer = new StringBuffer();
        buffer.append(model.addDateTime());
        buffer.append(model.getFullParamString(model.getResString(R.string.REQ_APP), model.getAppVersionCode(model.getActivity()), false));
        buffer.append(model.getFullParamString(model.getResString(R.string.REQ_OS), model.getActivity().getResources().getString(R.string.REQ_OS_ANDROID), false));
        buffer.append(model.getFullParamString(model.getResString(R.string.REQ_MESSAGE_TYPE), model.getActivity().getResources().getString(R.string.REQ_MESSAGE_TYPE_MLOGIN), false));
        buffer.append(model.getFullParamString(model.getResString(R.string.REQ_TERMINAL_ID), model.getIMEIFromPhone(model.getActivity()), false));
        buffer.append(model.getFullParamString(model.getResString(R.string.REQ_CLIENT_PIN), merchantPin, false));
        buffer.append(model.getFullParamString(model.getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), false));
        buffer.append(model.getFullParamString(model.getResString(R.string.REQ_CLIENT_ID), model.getMerchantId(), false));
        buffer.append(model.getFullParamString(model.getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(merchantPin), true));
        buffer.append(model.getFullParamString(model.getResString(R.string.REQ_CUST_SEARCH_DATA), getCustomerSearchData(merchantPin), true));

        return new String(buffer);
    }

    private String getCustomerSearchData(String pin){
        StringBuffer buffer = new StringBuffer();
        buffer.append(model.getResString(R.string.OPEN_BRACKET) + model.getResString(R.string.REQ_IMSI) + model.getResString(R.string.EQUAL));
        buffer.append(model.getAndroidId(model.getActivity()));
        buffer.append(model.getResString(R.string.COMMA) + model.getResString(R.string.REQ_MOBMONPIN) + model.getResString(R.string.EQUAL));
        buffer.append(pin);
        buffer.append(model.getResString(R.string.CLOSE_BRACKET));
        return new String(buffer);
    }

    private String getTransactionId() {
        model.setTxl(model.generateTXLId("LOGIN"));
        return model.getTxl().getTxlId();
    }
}
