package com.mobilis.android.nfc.domain;

/**
 * Created by ahmed on 9/06/14.
 */
public enum CONST {

    WIFI_WEAK("Wifi signal is weak"),
    MOBILE_DATA_WEAK("Mobile data signal is weak"),
    INTERNET_CONN_LOST("Internet connection lost"),

    HOCKEY_APP_ID("5adf71cbbe28c5fc3f78df90eda5d667");

    private final String name;

    private CONST(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
        return name;
    }
}
