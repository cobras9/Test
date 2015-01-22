package com.mobilis.android.nfc.util;

/**
 * Created by ahmed on 10/08/14.
 */
public enum CrashReportParam {

        APP_NAME("appName"),
        APP_IP("appIp"),
        APP_PORT("appPort"),
        PACKAGE_NAME("packageName"),
        PACKAGE_VERSION("packageVersion"),
        PHONE_MODEL("phoneModel"),
        ANDROID_VERSION("androidVersion"),
        STACK_TRACE("stackTrace"),
        LAST_NETWORK_REQUEST("lastNetworkRequest"),
        LAST_NETWORK_RESPONSE("lastNetworkResponse");

    private String value;
    private CrashReportParam(String value){
        this.value = value;
    }
    public String toValue(){
        return  value;
    }
}
