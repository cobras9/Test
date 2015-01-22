package com.mobilis.android.nfc.model;

import android.app.Activity;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.util.CrashReportParam;

public class CrashReport extends AbstractModel{

    private String appName;
    private String appIp;
    private String appPort;
    private String packageName;
    private String packageVersion;
    private String phoneModel;
    private String androidVersion;
    private String stackTrace;
    public String lastNetworkRequest = "lastNetworkRequest";
    public String lastNetworkResponse = "lastNetworkResponse";
    private int numberOfStackTraces;
    private static final String specialCharacter =  new String("\n");

    public CrashReport(Activity activity) {
		super(activity, activity);
	}

	@Override
	public String getRequestParameters() {

        StringBuffer buffer = new StringBuffer();
        buffer.append(super.addDateTime());
        buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_CRASH_REPORT), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(getActivity()), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMerchantId(), false));
        buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID), "999999999999", false));
        buffer.append(getFullParamString(CrashReportParam.APP_NAME.toValue(), getAppName(), false));
        buffer.append(getFullParamString(CrashReportParam.APP_IP.toValue(), getAppIp(), false));
        buffer.append(getFullParamString(CrashReportParam.APP_PORT.toValue(), getAppPort(), false));
        buffer.append(getFullParamString(CrashReportParam.PACKAGE_NAME.toValue(), getPackageName(), false));
        buffer.append(getFullParamString(CrashReportParam.PACKAGE_VERSION.toValue(), getPackageVersion(), false));
        buffer.append(getFullParamString(CrashReportParam.PHONE_MODEL.toValue(), getPhoneModel(), false));
        buffer.append(getFullParamString(CrashReportParam.ANDROID_VERSION.toValue(), getAndroidVersion(), false));

        String request = getLastNetworkRequest();
        if(request != null) {
            request = changeMessageType(request, getResString(R.string.REQ_MESSAGE_TYPE), "Request" + getResString(R.string.REQ_MESSAGE_TYPE));
            request = changeMessageType(request, getResString(R.string.REQ_TERMINAL_ID), "Request" + getResString(R.string.REQ_TERMINAL_ID));
            request = changeMessageType(request, getResString(R.string.REQ_CLIENT_ID), "Request" + getResString(R.string.REQ_CLIENT_ID));
            request = changeMessageType(request, getResString(R.string.REQ_TRANSACTION_ID), "Request" + getResString(R.string.REQ_TRANSACTION_ID));
        }
        buffer.append(getFullParamString(CrashReportParam.LAST_NETWORK_REQUEST.toValue(), "("+request+")", false));

        String response = getLastNetworkResponse();
        if(response != null) {
            response = changeMessageType(response, getResString(R.string.REQ_MESSAGE_TYPE), "Response" + getResString(R.string.REQ_MESSAGE_TYPE));
            response = changeMessageType(response, getResString(R.string.REQ_TERMINAL_ID), "Response" + getResString(R.string.REQ_TERMINAL_ID));
            response = changeMessageType(response, getResString(R.string.REQ_CLIENT_ID), "Response" + getResString(R.string.REQ_CLIENT_ID));
            response = changeMessageType(response, getResString(R.string.REQ_TRANSACTION_ID), "Response" + getResString(R.string.REQ_TRANSACTION_ID));
        }
        buffer.append(getFullParamString(CrashReportParam.LAST_NETWORK_RESPONSE.toValue(), "("+response+")", false));

        String stackTrace = getStackTrace();
        stackTrace = stackTrace.replaceAll(specialCharacter, " ");
        buffer.append(getFullParamString(CrashReportParam.STACK_TRACE.toValue(), stackTrace, true));

        return buffer.toString();

	}

    private String changeMessageType(String param, String oldValue, String newValue){
        param = param.replace(oldValue, newValue);
        return param;
    }
	@Override
	public void verifyPostTaskResults() {}

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppIp() {
        return appIp;
    }

    public void setAppIp(String appIp) {
        this.appIp = appIp;
    }

    public String getAppPort() {
        return appPort;
    }

    public void setAppPort(String appPort) {
        this.appPort = appPort;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public int getNumberOfStackTraces() {
        return numberOfStackTraces;
    }

    public void setNumberOfStackTraces(int numberOfStackTraces) {
        this.numberOfStackTraces = numberOfStackTraces;
    }
    public String getLastNetworkRequest() {
        return lastNetworkRequest;
    }

    public void setLastNetworkRequest(String lastNetworkRequest) {
        this.lastNetworkRequest = lastNetworkRequest;
    }

    public String getLastNetworkResponse() {
        return lastNetworkResponse;
    }

    public void setLastNetworkResponse(String lastNetworkResponse) {
        this.lastNetworkResponse = lastNetworkResponse;
    }
}
