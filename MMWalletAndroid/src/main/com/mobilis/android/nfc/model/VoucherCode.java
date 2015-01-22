package com.mobilis.android.nfc.model;

import java.util.ArrayList;
import java.util.List;

public class VoucherCode {


    private List<String> availableQuantity = new ArrayList<String>();
    private String issuer;
    private List<String> denomination = new ArrayList<String>();


    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getDestinationCode() {
        return issuer;
    }
    public List<String> getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(List<String> availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public List<String> getDenomination() {
        return denomination;
    }

    public void setDenomination(List<String> denomination) {
        this.denomination = denomination;
    }


}
