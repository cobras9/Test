package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.dao.DBService;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.network.ConnTaskManager;
import com.mobilis.android.nfc.util.SecurePreferences;

import static com.mobilis.android.nfc.util.Constants.setMerchantLoggedin;

/**
 * Created by ahmed on 24/07/14.
 */
public class CustomerUpdate extends AbstractModel {

    private Customer customer;

    public CustomerUpdate(Activity activity) {
        setMerchantLoggedin(false);
        setContext(activity);
        setActivity(activity);
        setService(DBService.getService(getContext()));
        setIMEA(getIMEIFromPhone(activity));
        setConnTaskManager(new ConnTaskManager(getContext(), this));
        setSharedPreference(new SecurePreferences(activity));
    }
    private String getCustomerData(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_MSISDN) + getResString(R.string.EQUAL));
        buffer.append(customer.getMSISDN());
        buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_GIVENNAME) + getResString(R.string.EQUAL));
        buffer.append(customer.getGivenName());
        buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_SURNAME) + getResString(R.string.EQUAL));
        buffer.append(customer.getSurName());
        buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_DOB) + getResString(R.string.EQUAL));
        buffer.append(customer.getDOB());
        buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_CUSTOMER_ID) + getResString(R.string.EQUAL));
        buffer.append(customer.getCustomerId());
        buffer.append(getResString(R.string.CLOSE_BRACKET));
        return new String(buffer);
    }
    @Override
    public String getRequestParameters() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getFullParamString(getResString(R.string.REQ_APP), getAppVersionCode(getActivity()), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_OS), getActivity().getResources().getString(R.string.REQ_OS_ANDROID), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getActivity().getResources().getString(R.string.REQ_MESSAGE_TYPE_CUSTOMER_ADDITIONAL_DATA), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CUST_DATA), getCustomerData(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMerchantId(), true));
        return new String(buffer);
    }

    @Override
    public void verifyPostTaskResults() {
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                new Intent(INTENT.CUSTOMER_UPDATE.toString())
                        .putExtra(INTENT.EXTRA_SERVER_RESPONSE.toString(), getServerResponse()));
    }
    private String getTransactionId() {
        setTxl(generateTXLId("CUSTOMER_CREATION"));
        return getTxl().getTxlId();
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

}
