package com.mobilis.android.nfc.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.TagRegistration;
import com.mobilis.android.nfc.slidemenu.utils.LoginResponseConstants;
import com.mobilis.android.nfc.tabsfragments.QuickLinkFragment;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ahmed on 10/06/14.
 */
public class TagRegistrationUtil implements View.OnClickListener{

    private final String TAG = SendMoneyFragment.class.getSimpleName();
    TextView resultTV;
    TextView pinTV;
    TextView accountIdTV;
    EditText accountIdET;
    EditText pinET;
    Button submitButton;
    ProgressBar progressBar;
    Spinner tagTypeSpinner;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;
    RelativeLayout pinRL;

    String pin = "";
    boolean isConfirmingPin;
    boolean isPinConfirmed;
    TagRegistration model;

    private final Activity activity;

    public TagRegistrationUtil(Activity activity){
        this.activity = activity;
        initBroadcastReceiver();
    }
    public void setUp(Dialog qpDialog) {
        pinRL = (RelativeLayout) qpDialog.findViewById(R.id.Dialog_TagRegistration_RelativeLayout_PIN);
        pinET = (EditText) qpDialog.findViewById(R.id.Dialog_TagRegistration_EditText_Pin);
        pinTV = (TextView) qpDialog.findViewById(R.id.Dialog_TagRegistration_TextView_Pin);
        accountIdET = (EditText) qpDialog.findViewById(R.id.Dialog_TagRegistration_EditText_PhoneNumber);
        accountIdTV = (TextView) qpDialog.findViewById(R.id.Dialog_TagRegistration_TextView_PhoneNumber);

        resultTV = (TextView) qpDialog.findViewById(R.id.Dialog_TagRegistration_TextView_Result);
        submitButton = (Button) qpDialog.findViewById(R.id.Dialog_TagRegistration_Button_Submit);
        progressBar = (ProgressBar) qpDialog.findViewById(R.id.Dialog_TagRegistration_Progressbar);
        tagTypeSpinner = (Spinner)qpDialog.findViewById(R.id.Dialog_TagRegistration_Spinner);

        if(activity.getString(R.string.app_name).toUpperCase(Locale.US).contains("AIRTEL") || activity.getString(R.string.app_name).toUpperCase(Locale.US).contains("NFC"))
            pinRL.setVisibility(View.GONE);

        submitButton.setOnClickListener(this);
        accountIdET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(accountIdET.getText().toString().isEmpty())
                    accountIdTV.setVisibility(View.VISIBLE);
                else
                    accountIdTV.setVisibility(View.GONE);
            }
        });
        pinET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                resultTV.setVisibility(View.GONE);
                if (pinET.getText().length() == 0 && !isConfirmingPin)
                    pinTV.setText("PIN");
                if (pinET.getText().length() == 0 && isConfirmingPin)
                    pinTV.setText(activity.getString(R.string.CONFIRM_PIN));

                if (pinET.getText().toString().length() > 0 && s.length() < 4) {
                    pinTV.setText("");
                } else if (pinET.getText().toString().length() == 4 && !isConfirmingPin) {
                    pin = pinET.getText().toString();
                    isConfirmingPin = true;
                    pinET.getText().clear();
                    pinTV.setText(activity.getString(R.string.CONFIRM_PIN));
                } else if (pinET.getText().toString().length() == 4 && isConfirmingPin) {
                    if (!pinET.getText().toString().toString().equalsIgnoreCase(pin)) {
                        isPinConfirmed = false;
                        pin = "";
                        pinET.getText().clear();
                        pinTV.setText("PINS DO NOT MATCH. TRY AGAIN");
                        isConfirmingPin = false;
                    } else
                        isPinConfirmed = true;
                }
                if (isPinConfirmed && s.length() < 4)
                    isPinConfirmed = false;
            }
        });
        final ArrayList<String> spinnerList = new ArrayList<String>();
        final ArrayList<TagRegistration.TAG_TYPE> tagTypesList =  new ArrayList<TagRegistration.TAG_TYPE>();
        if(LoginResponseConstants.walletOptions.isRegServicesTagPrimary()) {
            spinnerList.add(TagRegistration.TAG_TYPE.PRIMARY.getLabel());
            tagTypesList.add(TagRegistration.TAG_TYPE.PRIMARY);
        }
        if(LoginResponseConstants.walletOptions.isRegServicesTagSecondary()) {
            spinnerList.add(TagRegistration.TAG_TYPE.SECONDARY.getLabel());
            tagTypesList.add(TagRegistration.TAG_TYPE.SECONDARY);
        }
        if(LoginResponseConstants.walletOptions.isRegServicesTagReplace()) {
            spinnerList.add(TagRegistration.TAG_TYPE.REPLACEMENT.getLabel());
            tagTypesList.add(TagRegistration.TAG_TYPE.REPLACEMENT);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(activity, R.layout.spinner_tag_type, R.id.TagType_Spinner_TextView, spinnerList);
        tagTypeSpinner.setAdapter(spinnerAdapter);
        tagTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                model.setTagType(tagTypesList.get(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        tagTypeSpinner.setSelection(0);
        model = new TagRegistration(activity);
        if(spinnerList.size() > 0)
            model.setTagType(spinnerList.get(0));
    }

    public void initBroadcastReceiver() {

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equalsIgnoreCase(INTENT.TAG_REGISTRATION.toString()))
                {
                    isPinConfirmed = false;
                    isConfirmingPin = false;
                    pinET.getText().clear();
                    String resp = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(resp.equalsIgnoreCase("OK"))
                        hideProgressBar("TAG registered successfully");
                    else
                        hideProgressBar(resp);
                }
                else if(intent.getAction().equalsIgnoreCase(INTENT.DIALOG_TAG_REG_NFC_SCANNED.toString()))
                {
                    Log.d("AhmedB","Got DIALOG_TAG_REG_NFC_SCANNED intent!!");
                    if(NFCDialog != null && NFCDialog.isShowing())
                    {
                        NFCDialog.dismiss();
                        showProgressBar();
                        model.setNfcScanned(true);
                        model.setMsisdn(accountIdET.getText().toString());
                        model.setClientPin(pinET.getText().toString());
                        model.setNFCId(intent.getStringExtra(INTENT.EXTRA_NFC_ID.toString()).toUpperCase(Locale.US));
                        model.getConnTaskManager().startBackgroundTask();
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.TAG_REGISTRATION.toString()));
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.DIALOG_TAG_REG_NFC_SCANNED.toString()));
    }

    public void hideKeyboard() {
        ApplicationActivity.hideKeyboard(activity);
//        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Dialog_TagRegistration_Button_Submit:
                if(accountIdET.getText().toString().isEmpty())
                {
                    accountIdET.setError("Phone number is mandatory field");
                    return;
                }
                if(isPinLayoutVisible() && !isPinConfirmed)
                {
                    pinET.setError("PIN is mandatory field");
                    return;
                }
                hideKeyboard();
                LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(INTENT.NFC_DIALOG_ON.toString()));
                NFCDialog = new Dialog(v.getContext());
                NFCDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                NFCDialog.setContentView(R.layout.nfc_dialog);
                NFCDialog.setCanceledOnTouchOutside(false);
                NFCDialog.show();
                QuickLinkFragment.isTagRegistrationDialogOn = true;
                break;
        }
    }

    private boolean isPinLayoutVisible(){
        return pinRL.getVisibility() == View.VISIBLE;
    }


    private void showProgressBar(){
        pinET.setEnabled(false);
        submitButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        Log.d("AhmedB","setting progressbar visibility to Visible? "+(progressBar.getVisibility() == View.VISIBLE));
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        pinET.setEnabled(true);
        submitButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }
}
