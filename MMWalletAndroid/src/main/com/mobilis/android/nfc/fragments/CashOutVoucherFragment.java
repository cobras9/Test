package com.mobilis.android.nfc.fragments;

import android.app.Dialog;
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

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.CashOutVouchers;

/**
 * Created by ahmed on 10/06/14.
 */
public class CashOutVoucherFragment extends ApplicationActivity.PlaceholderFragment implements View.OnClickListener{

    private final String TAG = SendMoneyFragment.class.getSimpleName();
    TextView amountTV;
    TextView surnameTV;
    TextView givenNameTV;
    TextView msisdnTV;
    TextView resultTV;
    EditText amountET;
    EditText surnameET;
    EditText givenNameET;
    EditText msisdnET;
    Button payButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;

    CashOutVouchers model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cashout_voucher, container, false);
        amountET = (EditText) rootView.findViewById(R.id.Fragment_CashOutVoucher_EditText_Amount);
        amountTV = (TextView) rootView.findViewById(R.id.Fragment_CashOutVoucher_TextView_Amount);
        givenNameET = (EditText) rootView.findViewById(R.id.Fragment_CashOutVoucher_EditText_GivenName);
        givenNameTV = (TextView) rootView.findViewById(R.id.Fragment_CashOutVoucher_TextView_GivenName);
        msisdnET = (EditText) rootView.findViewById(R.id.Fragment_CashOutVoucher_EditText_MSISDN);
        msisdnTV = (TextView) rootView.findViewById(R.id.Fragment_CashOutVoucher_TextView_MSISDN);
        surnameET = (EditText) rootView.findViewById(R.id.Fragment_CashOutVoucher_EditText_SurName);
        surnameTV = (TextView) rootView.findViewById(R.id.Fragment_CashOutVoucher_TextView_SurName);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_CashOutVoucher_TextView_Result);
        payButton = (Button) rootView.findViewById(R.id.Fragment_CashOutVoucher_Button_Pay);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_CashOutVoucher_Progressbar);

        payButton.setOnClickListener(this);
        amountET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(amountET.getText().toString().isEmpty())
                    amountTV.setVisibility(View.VISIBLE);
                else
                    amountTV.setVisibility(View.GONE);
            }
        });
        msisdnET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (msisdnET.getText().toString().isEmpty())
                    msisdnTV.setVisibility(View.VISIBLE);
                else
                    msisdnTV.setVisibility(View.GONE);
            }
        });

        surnameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (surnameET.getText().toString().isEmpty())
                    surnameTV.setVisibility(View.VISIBLE);
                else
                    surnameTV.setVisibility(View.GONE);
            }
        });

        givenNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (givenNameET.getText().toString().isEmpty())
                    givenNameTV.setVisibility(View.VISIBLE);
                else
                    givenNameTV.setVisibility(View.GONE);
            }
        });

        msisdnET.setText(ApplicationActivity.loginClientId);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equalsIgnoreCase(INTENT.CASH_OUT_VOUCHER.toString()))
                {
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(response.equalsIgnoreCase("OK"))
                        hideProgressBar("SUCCESSFUL");
                    else
                        hideProgressBar(response);
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CASH_OUT_VOUCHER.toString()));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Fragment_CashOutVoucher_Button_Pay:
                if(givenNameET.getText().toString().isEmpty())
                {
                    givenNameET.setError("Mandatory field");
                    return;
                }
                if(surnameET.getText().toString().isEmpty())
                {
                    surnameET.setError("Mandatory field");
                    return;
                }
                if(msisdnET.getText().toString().isEmpty())
                {
                    msisdnET.setError("Mandatory field");
                    return;
                }
                if(amountET.getText().toString().isEmpty())
                {
                    amountET.setError("Mandatory field");
                    return;
                }

                ApplicationActivity.hideKeyboard(getActivity());
                showProgressBar();
                model = new CashOutVouchers(getActivity());
                model.setGivenName(givenNameET.getText().toString());
                model.setSurName(surnameET.getText().toString());
                model.setMsisdn(msisdnET.getText().toString());
                model.setRecepientMSISDN(msisdnET.getText().toString());
                model.setWorkingAmount(amountET.getText().toString());
                model.getConnTaskManager().startBackgroundTask();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    private void showProgressBar(){
        amountET.setEnabled(false);
        msisdnET.setEnabled(false);
        surnameET.setEnabled(false);
        givenNameET.setEnabled(false);
        payButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        amountET.setEnabled(true);
        msisdnET.setEnabled(true);
        surnameET.setEnabled(true);
        givenNameET.setEnabled(true);
        payButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }
}
