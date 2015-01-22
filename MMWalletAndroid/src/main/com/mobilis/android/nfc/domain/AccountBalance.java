package com.mobilis.android.nfc.domain;

import java.io.Serializable;

/**
 * Created by ahmed on 9/06/14.
 */
public class AccountBalance implements Serializable{

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String value;
}
