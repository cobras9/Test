package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.dao.DBService;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.network.ConnTaskManager;
import com.mobilis.android.nfc.slidemenu.utils.IDType;
import com.mobilis.android.nfc.slidemenu.utils.LoginResponseConstants;
import com.mobilis.android.nfc.util.SecurePreferences;

/**
 * Created by ahmed on 24/07/14.
 */
public class CustomerRegistration extends AbstractModel {

    private Customer customer;

    public CustomerRegistration(Activity activity) {
        setContext(activity);
        setActivity(activity);
        setService(DBService.getService(getContext()));
        setIMEA(getIMEIFromPhone(activity));
        setConnTaskManager(new ConnTaskManager(getContext(), this));
        setSharedPreference(new SecurePreferences(activity));
    }
    private String getCustomerData(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_CONTACT_PHONE) + getResString(R.string.EQUAL));
        buffer.append(customer.getMSISDN());
        buffer.append(getResString(R.string.COMMA)+getResString(R.string.REQ_GIVENNAME) +getResString(R.string.EQUAL));
        buffer.append(customer.getGivenName());
        buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_SURNAME) + getResString(R.string.EQUAL));
        buffer.append(customer.getSurName());
        buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_DOB) + getResString(R.string.EQUAL));
        buffer.append(customer.getDOB());
        buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_CUSTOMER_TYPE) + getResString(R.string.EQUAL));
        buffer.append(getCustomerType());
        if(customer.getEmailAddress()!= null && !customer.getEmailAddress().isEmpty()) {
            buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_EMAIL_ADDRESS) + getResString(R.string.EQUAL));
            buffer.append(customer.getEmailAddress());
        }
        buffer.append(getResString(R.string.CLOSE_BRACKET));
        return new String(buffer);
    }

    private String getCustomerAddress(){
        StringBuffer buffer = new StringBuffer();
        if(customer.getCustomerAddress().getAddress1() != null && !customer.getCustomerAddress().getAddress1().isEmpty())
            buffer.append(getFullParamString(getResString(R.string.REQ_ADDRESS_1), customer.getCustomerAddress().getAddress1(), false));
        if(customer.getCustomerAddress().getAddress2() != null && !customer.getCustomerAddress().getAddress2().isEmpty())
            buffer.append(getFullParamString(getResString(R.string.REQ_ADDRESS_2), customer.getCustomerAddress().getAddress2(), false));
        if(customer.getCustomerAddress().getCity() != null && !customer.getCustomerAddress().getCity().isEmpty())
            buffer.append(getFullParamString(getResString(R.string.REQ_CITY), customer.getCustomerAddress().getCity(), false));
        if(customer.getCustomerAddress().getState() != null && !customer.getCustomerAddress().getState().isEmpty())
            buffer.append(getFullParamString(getResString(R.string.REQ_STATE), customer.getCustomerAddress().getState(), false));
        if(customer.getCustomerAddress().getCountry() != null && !customer.getCustomerAddress().getCountry().isEmpty())
            buffer.append(getFullParamString(getResString(R.string.REQ_COUNTRY), customer.getCustomerAddress().getCountry(), false));
        return new String(buffer);
    }

    @Override
    public String getRequestParameters() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getFullParamString(getResString(R.string.REQ_APP), getAppVersionCode(getActivity()), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_OS), getActivity().getResources().getString(R.string.REQ_OS_ANDROID), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getActivity().getResources().getString(R.string.ATOMIC_CUSTOMER_CREATE), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), getTransactionId(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CUST_DATA), getCustomerData(), false));
        if(customer.getCustomerAddress() != null)
            buffer.append(getCustomerAddress());
        if(customer.isiDVerified()) {
            buffer.append(getFullParamString(getResString(R.string.REQ_ID_VERIFIED), "true", false));
            buffer.append(getFullParamString(getResString(R.string.REQ_ID_TYPE), customer.getIdType(), false));
            if(!customer.getIdType().equalsIgnoreCase(IDType.TYPE.PHOTO_ID.toString()))
                buffer.append(getFullParamString(getResString(R.string.REQ_ID_NUMBER), customer.getCustomerVerificationId(), false));
        }
        else
            buffer.append(getFullParamString(getResString(R.string.REQ_ID_VERIFIED), "false", false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMerchantId(), true));
        return new String(buffer);
    }

    @Override
    public void verifyPostTaskResults() {
        super.verifyTransferTXL();
        commitNewTxlId(getTxl());
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                new Intent(INTENT.CUSTOMER_REGISTRATION.toString())
                        .putExtra(INTENT.EXTRA_SERVER_RESPONSE.toString(), getServerResponse()));
    }
    private String getTransactionId() {
        setTxl(generateTXLId("CUSTOMER_CREATION"));
        return getTxl().getTxlId();
    }

    private String getCustomerType(){
        if(LoginResponseConstants.customerTypes == null|| LoginResponseConstants.customerTypes.size() == 0) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Customer Type was missing in Login Response.", Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }
        return customer.getCustomerType();
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

}
