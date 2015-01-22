package com.mobilis.android.nfc.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.ChangePIN;

/**
 * Created by ahmed on 10/06/14.
 */
public class ChangePinFragment extends ApplicationActivity.PlaceholderFragment implements View.OnClickListener{

    private final String TAG = SendMoneyFragment.class.getSimpleName();
    TextView resultTV;
    TextView newPinTV;
    TextView currentPinTV;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_changepin_view, container, false);
        newPinET = (EditText) rootView.findViewById(R.id.Fragment_ChangePin_EditText_NewPin);
        newPinTV = (TextView) rootView.findViewById(R.id.Fragment_ChangePin_TextView_NewPin);
        currentPinET = (EditText) rootView.findViewById(R.id.Fragment_ChangePin_EditText_CurrentPin);
        currentPinTV = (TextView) rootView.findViewById(R.id.Fragment_ChangePin_TextView_CurrentPin);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_ChangePin_TextView_Result);
        submitButton = (Button) rootView.findViewById(R.id.Fragment_ChangePin_Button_Submit);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_ChangePin_Progressbar);

        submitButton.setOnClickListener(this);
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
        currentPinET.requestFocus();
        currentPinET.postDelayed(new Runnable() {
            @Override
            public void run() {
                ApplicationActivity.showKeyboard(getActivity(), currentPinET);
            }
        },30);

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
                int pinLength = getResources().getInteger(R.integer.PIN_LENGTH);

                if (newPinET.getText().length() == 0 && !isConfirmingPin)
                    newPinTV.setText("PIN");
                if (newPinET.getText().length() == 0 && isConfirmingPin)
                    newPinTV.setText(getString(R.string.CONFIRM_PIN));

                if (newPinET.getText().toString().length() > 0 && s.length() < pinLength) {
                    newPinTV.setText("");
                } else if (newPinET.getText().toString().length() == pinLength && !isConfirmingPin) {
                    newPin = newPinET.getText().toString();
                    isConfirmingPin = true;
                    newPinET.getText().clear();
                    newPinTV.setText(getString(R.string.CONFIRM_PIN));
                } else if (newPinET.getText().toString().length() == pinLength && isConfirmingPin) {
                    if (!newPinET.getText().toString().toString().equalsIgnoreCase(newPin)) {
                        isPinConfirmed = false;
                        newPin = "";
                        newPinET.getText().clear();
                        newPinTV.setText(getResources().getString(R.string.TOAST_PIN_DO_NOT_MATCH));
                        isConfirmingPin = false;
                    } else
                        isPinConfirmed = true;
                }
                if (isPinConfirmed && s.length() < pinLength)
                    isPinConfirmed = false;
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equalsIgnoreCase(INTENT.CHANGE_PIN.toString()))
                {
                    isPinConfirmed = false;
                    isConfirmingPin = false;
                    newPinET.getText().clear();
                    String resp = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(resp.equalsIgnoreCase("OK"))
                        hideProgressBar("PIN changed successfully");
                    else
                        hideProgressBar(resp);
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CHANGE_PIN.toString()));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Fragment_ChangePin_Button_Submit:

                if(currentPinET.getText().toString() == null || currentPinET.getText().toString().isEmpty())
                {
                    Toast.makeText(v.getContext(), "Current PIN is mandatory field", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(newPin == null || newPin.isEmpty())
                {
                    Toast.makeText(v.getContext(), "New PIN is mandatory field", Toast.LENGTH_SHORT).show();
                    return;
                }
                ApplicationActivity.hideKeyboard(getActivity());
                showProgressBar();
                model = new ChangePIN(getActivity());
                model.setClientOldPIN(currentPinET.getText().toString());
                model.setClientNewPIN(newPin);
                model.getConnTaskManager().startBackgroundTask();
                break;
        }
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
}
