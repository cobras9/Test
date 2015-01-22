package com.mobilis.android.nfc.slidemenu.utils;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.util.BitsMapper;

public class CustomerType {


    private boolean customerTypeAvailable;
    private final CustomerType.TYPE customerType;
    public CustomerType(CustomerType.TYPE customerType){
        this.customerType = customerType;
    }
    public CustomerType.TYPE getCustomerType(){
        return customerType;
    }

	public enum TYPE{
        SUBSCRIBER("SUBSCRIBER", BitsMapper.getContext().getResources().getString(R.string.LABEL_SUBSCRIBER)),
        AGENT("AGENT", BitsMapper.getContext().getResources().getString(R.string.LABEL_AGENT)),
        SUPER_AGENT("SUPERAGENT", BitsMapper.getContext().getResources().getString(R.string.LABEL_SUPERAGENT)),
        MERCHANT("MERCHANT", BitsMapper.getContext().getResources().getString(R.string.LABEL_MERCHANT)),
        MM_STAFF("MM STAFF", BitsMapper.getContext().getResources().getString(R.string.LABEL_MMSTAFF));

        private final String value;
        private final String label;

        private TYPE(String value, String label){
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return this.label;
        }
    }

    public boolean isCustomerTypeAvailable() {
        return customerType == null;
    }

}
