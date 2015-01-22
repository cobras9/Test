package com.mobilis.android.nfc.domain;

import java.util.ArrayList;

/**
 * Created by ahmed on 13/06/14.
 */
public class QuickLink {

    private String name;
    private String amount;
    private String type;
    private String destination;
    private ArrayList<DstCode> dstCodes;

    public String getSingleDstCode() {
        return singleDstCode;
    }

    public void setSingleDstCode(String singleDstCode) {
        this.singleDstCode = singleDstCode;
    }

    private String singleDstCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public ArrayList<DstCode> getDstCodes() {

        if(dstCodes == null)
            dstCodes = new ArrayList<DstCode>();
        return dstCodes;
    }

    public void setDstCodes(ArrayList<DstCode> dstCodes) {
        this.dstCodes = dstCodes;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
