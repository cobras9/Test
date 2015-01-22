

/******

 This is a backup file

 ******/

package com.mobilis.android.nfc.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.TagRegistration;
import com.mobilis.android.nfc.slidemenu.utils.LoginResponseConstants;
import com.mobilis.android.nfc.util.Constants;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ahmed on 10/06/14.
 */
public class TagRegistrationFragment extends NFCFragment implements View.OnClickListener, DialogInterface.OnDismissListener, DialogInterface.OnShowListener {

    private final String TAG = SendMoneyFragment.class.getSimpleName();
    TextView resultTV;
    TextView pinTV;
    TextView accountIdTV;
    TextView titleTextView;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tag_registration_view, container, false);
        pinRL = (RelativeLayout) rootView.findViewById(R.id.Fragment_TagRegistration_RelativeLayout_PIN);
        pinET = (EditText) rootView.findViewById(R.id.Fragment_TagRegistration_EditText_Pin);
        pinTV = (TextView) rootView.findViewById(R.id.Fragment_TagRegistration_TextView_Pin);
        accountIdET = (EditText) rootView.findViewById(R.id.Fragment_TagRegistration_EditText_PhoneNumber);
        accountIdTV = (TextView) rootView.findViewById(R.id.Fragment_TagRegistration_TextView_PhoneNumber);
        titleTextView = (TextView) rootView.findViewById(R.id.Fragment_TagRegistration_TextView_Title);

        resultTV = (TextView) rootView.findViewById(R.id.Fragment_TagRegistration_TextView_Result);
        submitButton = (Button) rootView.findViewById(R.id.Fragment_TagRegistration_Button_Submit);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_TagRegistration_Progressbar);
        tagTypeSpinner = (Spinner)rootView.findViewById(R.id.Fragment_TagRegistration_Spinner);

        if(getString(R.string.app_name).toUpperCase(Locale.US).contains("AIRTEL") || getString(R.string.app_name).toUpperCase(Locale.US).contains("NFC"))
            pinRL.setVisibility(View.GONE);

        setTabSize();
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
        accountIdET.requestFocus();
        accountIdET.postDelayed(new Runnable() {
            @Override
            public void run() {
                ApplicationActivity.showKeyboard(getActivity(), accountIdET);
            }
        },30);


        pinET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
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
                        pinTV.setText(getResources().getString(R.string.TOAST_PIN_DO_NOT_MATCH));
                        isConfirmingPin = false;
                    } else
                        isPinConfirmed = true;
                }
                if (isPinConfirmed && s.length() < pinLength)
                    isPinConfirmed = false;
            }
        });
        final ArrayList<String> spinnerList = new ArrayList<String>();
        final ArrayList<TagRegistration.TAG_TYPE> tagTypesList = new ArrayList<TagRegistration.TAG_TYPE>();
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
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_tag_type, R.id.TagType_Spinner_TextView, spinnerList);
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
        model = new TagRegistration(getActivity());
        if(spinnerList.size() > 0)
            model.setTagType(spinnerList.get(0));
        return rootView;
    }

    @Override
    public void finishedA2ACommunication(String scannedId) {
        if(NFCDialog != null && NFCDialog.isShowing()){
            Intent intent = new Intent(INTENT.NFC_SCANNED.toString());
            intent.putExtra(INTENT.EXTRA_NFC_ID.toString(), scannedId);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

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
                else if(intent.getAction().equalsIgnoreCase(INTENT.NFC_SCANNED.toString()))
                {
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.TAG_REGISTRATION.toString()));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NFC_SCANNED.toString()));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Fragment_TagRegistration_Button_Submit:
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
                ApplicationActivity.hideKeyboard(getActivity());
                NFCDialog = new Dialog(v.getContext());
                NFCDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                NFCDialog.setContentView(R.layout.nfc_dialog);
                NFCDialog.setCanceledOnTouchOutside(false);
                NFCDialog.setOnShowListener(this);
                NFCDialog.setOnDismissListener(this);
                NFCDialog.show();

                break;
        }
    }

    private void setTabSize() {

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)titleTextView.getLayoutParams();//new LinearLayout.LayoutParams(250,20);
        if(Constants.getScreenHeight(getActivity()) > 600)
            params.height = 160;
        else {
            params.height = 20;
            titleTextView.setTextSize(14);
        }
        params.gravity = Gravity.CENTER;
        titleTextView.setLayoutParams(params);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    private void showProgressBar(){
        pinET.setEnabled(false);
        submitButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        pinET.setEnabled(true);
        submitButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }

    private boolean isPinLayoutVisible(){
        return pinRL.getVisibility() == View.VISIBLE;
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        disableNFCScan();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        enableNFCScan();
    }
}
