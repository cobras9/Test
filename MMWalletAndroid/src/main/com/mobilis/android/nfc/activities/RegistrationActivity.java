package com.mobilis.android.nfc.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.model.Registration;
import com.mobilis.android.nfc.network.ConnTaskManager;
import com.mobilis.android.nfc.util.CustomUnderlineSpan;

public class RegistrationActivity extends Activity{

	Registration model;
	EditText phoneNumberET;
	EditText pinET;
	Button submitButton;

    TextView phoneNumberTV;
    TextView pinTV;
    TextView resultTV;
	String pin;
    boolean isConfirmingPin;
    boolean isPinConfirmed;
    boolean newPinScreenStarted;
    BroadcastReceiver broadcastReceiver;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.registration_view);

		phoneNumberET = (EditText) findViewById(R.id.Activity_Reg_EditText_PhoneNumber);
		pinET = (EditText) findViewById(R.id.Activity_Reg_EditText_PIN);
		submitButton = (Button) findViewById(R.id.Activity_Reg_Button_Submit);
        phoneNumberTV = (TextView) findViewById(R.id.Activity_Reg_TextView_PhoneNumber);
        resultTV = (TextView) findViewById(R.id.Activity_Reg_TextView_Result);
        pinTV = (TextView) findViewById(R.id.Activity_Reg_TextView_PIN);
		submitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
                Validator validator = validateMandatoryFields();
                if(validator.isValid)
                {
                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
                    model.saveMerchantId(phoneNumberET.getText().toString());
                    showProgressBar();
                    lockUIComps();
                    executeBackEndProcess();
                }
                else{
                    Toast.makeText(v.getContext(), validator.getMessage()+" is Mandatory field", Toast.LENGTH_SHORT).show();
                }
			}
		});
		
		phoneNumberET.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {
                resultTV.setVisibility(View.GONE);
                if(phoneNumberET.getText().toString().length() > 0)
                    phoneNumberTV.setText("");
                else
                    phoneNumberTV.setText("Phone Number");
            }
        });
        isConfirmingPin = false;
		pinET.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                int pinLength = getResources().getInteger(R.integer.PIN_LENGTH);
                resultTV.setVisibility(View.GONE);
                if (pinET.getText().length() == 0 && !isConfirmingPin)
                    pinTV.setText("PIN");
                if (pinET.getText().length() == 0 && isConfirmingPin)
                    pinTV.setText(getString(R.string.CONFIRM_PIN));

                if (pinET.getText().toString().length() > 0 && s.length() < pinLength) {
                    pinTV.setText("");
                } else if (pinET.getText().toString().length() == pinLength && !isConfirmingPin) {
                    pin = pinET.getText().toString();
                    isConfirmingPin = true;
                    pinET.getText().clear();
                    pinTV.setText(getString(R.string.CONFIRM_PIN));
                } else if (pinET.getText().toString().length() == pinLength && isConfirmingPin) {
                    if (!pinET.getText().toString().toString().equalsIgnoreCase(pin)) {
                        isPinConfirmed = false;
                        pin = "";
                        pinET.getText().clear();
                        pinTV.setText("PINS NOT MATCHING TRY AGAIN");
                        isConfirmingPin = false;
                    } else
                        isPinConfirmed = true;
                }
                if (isPinConfirmed && s.length() < pinLength)
                    isPinConfirmed = false;
            }
        });

        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        phoneNumberTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!phoneNumberET.isEnabled())
                    return false;
                InputMethodManager imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(phoneNumberET, InputMethodManager.SHOW_FORCED);
                phoneNumberET.requestFocus();
                return true;
            }
        });
        pinTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!pinET.isEnabled())
                    return false;
                InputMethodManager imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(pinET, InputMethodManager.SHOW_FORCED);
                pinET.requestFocus();
                return true;
            }
        });

        TextView title = (TextView) findViewById(R.id.Activity_Registration_TextView_title);
        SpannableString content = new SpannableString(getString(R.string.REGISTRATION_MSG_REGISTER));
        content.setSpan(new CustomUnderlineSpan(R.color.OFF_WHITE, 0, content.length()), 0, content.length(), 0);
        title.setText(content);
    }

    private void lockUIComps(){
        phoneNumberET.setEnabled(false);
        pinET.setEnabled(false);
        submitButton.setEnabled(false);
        resultTV.setVisibility(View.GONE);
    }
    private void unlockUIComps(){
        phoneNumberET.setEnabled(true);
        pinET.setEnabled(true);
        submitButton.setEnabled(true);
    }
    private void showProgressBar(){
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.Activity_Reg_ProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }
    private void hideProgressBarShowResult(String result){
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.Activity_Reg_ProgressBar);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(result);
        resultTV.setVisibility(View.VISIBLE);
    }

	private void executeBackEndProcess() {

        model.setMsisdn(phoneNumberET.getText().toString());
        model.setClientPin(pin);
	    Handler handler = new Handler(); 
	    handler.postDelayed(new Runnable() { 
	         public void run() { 
	        	 model.getConnTaskManager().startBackgroundTask();
	         } 
	    }, 1000);
	}

    @Override
    protected void onPause() {
        super.onPause();
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(pinET.getWindowToken(), 0);
    }

    @Override
	public void onResume() {
	    super.onResume();
        newPinScreenStarted = false;
        model = new Registration(this);
	    if(model.getDBService() != null)
	    	model.getDBService().open();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equalsIgnoreCase(INTENT.SERVER_COMM_TIME_OUT.toString()))
                {
                    hideProgressBarShowResult(intent.getStringExtra(INTENT.EXTRA_ERROR.toString()));
                    unlockUIComps();
                }
                else if(intent.getAction().equalsIgnoreCase(INTENT.REGISTRATION_RESULT.toString()))
                {
                    isPinConfirmed = false;
                    isConfirmingPin = false;
                    pinET.getText().clear();
                    String serverResponse = intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString());
                    String resp = model.getMessageFromServerResponse(serverResponse);
                    if(model != null)
                        model.saveMerchantId(phoneNumberET.getText().toString());

                    if(AbstractModel.getStatusFromServerResponse(serverResponse) == getResources().getInteger(R.integer.NEW_PIN_STATUS_CODE))
                    {
                        if(!newPinScreenStarted) {
                            newPinScreenStarted = true;
                            startActivity(new Intent(RegistrationActivity.this, ChangePinActivity.class));
                            hideProgressBarShowResult("Register Device again.");
                        }
                    }
                    else if(resp.equalsIgnoreCase("OK")) {
                        hideProgressBarShowResult("Device registered successfully");
                    }
                    else
                        hideProgressBarShowResult(resp);

                    unlockUIComps();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.REGISTRATION_RESULT.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.SERVER_COMM_TIME_OUT.toString()));
   	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(model != null)
		{
			if( ((ConnTaskManager)model.getConnTaskManager()).getTask() != null && !((ConnTaskManager)model.getConnTaskManager()).getTask().isCancelled())
				((ConnTaskManager)model.getConnTaskManager()).getTask().cancel(true);	    
		}
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
	
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}

    private Validator validateMandatoryFields(){
        Validator validator = new Validator();
        validator.setValid(true);
        if(phoneNumberET.getText().toString().isEmpty())
        {
            validator.setValid(false);
            validator.setMessage("Phone number");
            return validator;
        }
        if(!isPinConfirmed)
        {
            validator.setValid(false);
            validator.setMessage("PIN");
            return validator;
        }
        return validator;
    }

    private class Validator{
        private String message;
        private boolean isValid;
        private String getMessage(){ return this.message;}
        private void setMessage(String message){ this.message = message ;}
        private boolean isValid(){ return this.isValid;}
        private void setValid(boolean isValid){ this.isValid = isValid;}
    }

}
