package com.mobilis.android.nfc.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.fragments.SendMoneyFragment;
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.model.ChangePIN;
import com.mobilis.android.nfc.model.Login;

/**
 * Created by ahmed on 15/08/14.
 */
public class ChangePinActivity extends Activity implements View.OnClickListener{

    private final String TAG = SendMoneyFragment.class.getSimpleName();
    TextView resultTV;
    TextView newPinTV;
    TextView currentPinTV;
    TextView customerIdTV;
    EditText customerIdET;
    EditText currentPinET;
    EditText newPinET;
    Button submitButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;

    String newPin;
    boolean isConfirmingPin;
    boolean isPinConfirmed;
    ChangePIN model;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_changepin_view);

        customerIdET = (EditText) findViewById(R.id.Activity_ChangePin_EditText_CustomerId);
        customerIdTV = (TextView) findViewById(R.id.Activity_ChangePin_TextView_CustomerId);
        newPinET = (EditText) findViewById(R.id.Activity_ChangePin_EditText_NewPin);
        newPinTV = (TextView) findViewById(R.id.Activity_ChangePin_TextView_NewPin);
        currentPinET = (EditText) findViewById(R.id.Activity_ChangePin_EditText_CurrentPin);
        currentPinTV = (TextView) findViewById(R.id.Activity_ChangePin_TextView_CurrentPin);
        resultTV = (TextView) findViewById(R.id.Activity_ChangePin_TextView_Result);
        submitButton = (Button) findViewById(R.id.Activity_ChangePin_Button_Submit);
        progressBar = (ProgressBar) findViewById(R.id.Activity_ChangePin_Progressbar);
        submitButton.setOnClickListener(this);

        Login login = new Login(this);
        String merchantId = AbstractModel.getMerchantId(login);
        if(merchantId != null)
        {
            customerIdTV.setVisibility(View.INVISIBLE);
            customerIdET.setText(merchantId);
            currentPinET.requestFocus();
        }
        else
            customerIdET.requestFocus();
        login = null;

        customerIdET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(customerIdET.getText().toString().isEmpty())
                    customerIdTV.setVisibility(View.VISIBLE);
                else
                    customerIdTV.setVisibility(View.GONE);
            }
        });

        currentPinET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(currentPinET.getText().toString().isEmpty())
                    currentPinTV.setVisibility(View.VISIBLE);
                else
                    currentPinTV.setVisibility(View.GONE);
            }
        });
        newPinET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                resultTV.setVisibility(View.GONE);
                if (newPinET.getText().length() == 0 && !isConfirmingPin)
                    newPinTV.setText("PIN");
                if (newPinET.getText().length() == 0 && isConfirmingPin)
                    newPinTV.setText(getString(R.string.CONFIRM_PIN));

                if (newPinET.getText().toString().length() > 0 && s.length() < 4) {
                    newPinTV.setText("");
                } else if (newPinET.getText().toString().length() == 4 && !isConfirmingPin) {
                    newPin = newPinET.getText().toString();
                    isConfirmingPin = true;
                    newPinET.getText().clear();
                    newPinTV.setText(getString(R.string.CONFIRM_PIN));
                } else if (newPinET.getText().toString().length() == 4 && isConfirmingPin) {
                    if (!newPinET.getText().toString().toString().equalsIgnoreCase(newPin)) {
                        isPinConfirmed = false;
                        newPin = "";
                        newPinET.setError("PIN's not Matching");
                        newPinET.getText().clear();
                        newPinTV.setText("New PIN");
                        isConfirmingPin = false;
                    } else
                        isPinConfirmed = true;
                }
                if (isPinConfirmed && s.length() < 4)
                    isPinConfirmed = false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                if(intent.getAction().equalsIgnoreCase(INTENT.REGISTRATION_RESULT.toString()))
//                {
//                    String resp = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
//                    if(resp.equalsIgnoreCase("OK"))
//                        hideProgressBar("PIN changed successfully. Press Back to go to Login screen");
//                    else
//                        hideProgressBar(resp);
//                }
//                else
//                if(intent.getAction().equalsIgnoreCase(INTENT.CHANGE_PIN.toString()))
                if(intent.getAction().equalsIgnoreCase(INTENT.CHANGE_PIN.toString()))
                {
                    isPinConfirmed = false;
                    isConfirmingPin = false;
                    newPinET.getText().clear();
                    String resp = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(resp.equalsIgnoreCase("OK")) {
//                        Registration model = new Registration(ChangePinActivity.this);
//                        model.setMsisdn(customerIdET.getText().toString());
//                        model.setClientPin(newPin);
                        hideProgressBar("PIN changed successfully");

                    }
                    else
                        hideProgressBar(resp);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CHANGE_PIN.toString()));
    }

    @Override
    public void onPause() {
        hideKeyboard();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Activity_ChangePin_Button_Submit:
                if(customerIdET.getText().toString() == null || customerIdET.getText().toString().isEmpty())
                {
                    customerIdET.setError("Mandatory Field");
                    return;
                }
                if(currentPinET.getText().toString() == null || currentPinET.getText().toString().isEmpty())
                {
                    currentPinET.setError("Mandatory Field");
                    return;
                }
                if(newPin == null || newPin.isEmpty())
                {
                    newPinET.setError("Mandatory Field");
                    return;
                }
                hideKeyboard();
                showProgressBar();
                model = new ChangePIN(ChangePinActivity.this);
                model.setEnteredCustomerId(customerIdET.getText().toString());
                model.setClientOldPIN(currentPinET.getText().toString());
                model.setClientNewPIN(newPin);
                model.getConnTaskManager().startBackgroundTask();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }


    private void showProgressBar(){
        newPinET.setEnabled(false);
        submitButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        newPinET.setEnabled(true);
        submitButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
