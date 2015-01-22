package com.mobilis.android.nfc.model;

import android.app.Activity;

import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.dao.DBService;
import com.mobilis.android.nfc.network.ConnTaskManager;
import com.mobilis.android.nfc.server.messages.LoginRequest;
import com.mobilis.android.nfc.server.messages.LoginResponse;
import com.mobilis.android.nfc.util.SecurePreferences;

import static com.mobilis.android.nfc.util.Constants.setMerchantLoggedin;

public class Login extends AbstractModel {

	public Login(Activity activity) {
		setMerchantLoggedin(false);
		setContext(activity);
		setActivity(activity);
		setService(DBService.getService(getContext()));
		setIMEA(getIMEIFromPhone(activity));
		setConnTaskManager(new ConnTaskManager(getContext(), this));
		setSharedPreference(new SecurePreferences(activity));
	}

	@Override
	public String getRequestParameters() {
        LoginRequest request = new LoginRequest(this);
        return request.getServerMessage(ApplicationActivity.loginClientPin);
	}

	@Override
	public void verifyPostTaskResults() {
        LoginResponse response = new LoginResponse(this);
        response.verifyPostResults();
	}

}
