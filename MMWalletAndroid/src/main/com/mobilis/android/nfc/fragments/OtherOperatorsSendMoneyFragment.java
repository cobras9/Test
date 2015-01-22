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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.OtherOperatorPayment;
import com.mobilis.android.nfc.model.TransferCode;
import com.mobilis.android.nfc.util.OtherOperatorsOnItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 10/06/14.
 */
public class OtherOperatorsSendMoneyFragment extends ApplicationActivity.PlaceholderFragment implements View.OnClickListener{

    private final String TAG = OtherOperatorsSendMoneyFragment.class.getSimpleName();
    TextView amountTV;
    TextView phoneNumberTV;
    TextView resultTV;
    EditText amountET;
    EditText phoneNumberET;
    Button payButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;
    Spinner destinationCodeSpinner;

    OtherOperatorPayment model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_other_operators_view, container, false);
        amountET = (EditText) rootView.findViewById(R.id.Fragment_OtherOperators_EditText_Amount);
        amountTV = (TextView) rootView.findViewById(R.id.Fragment_OtherOperators_TextView_Amount);
        phoneNumberET = (EditText) rootView.findViewById(R.id.Fragment_OtherOperators_EditText_PhoneNumber);
        phoneNumberTV = (TextView) rootView.findViewById(R.id.Fragment_OtherOperators_TextView_PhoneNumber);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_OtherOperators_TextView_Result);
        payButton = (Button) rootView.findViewById(R.id.Fragment_OtherOperators_Button_Pay);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_OtherOperators_Progressbar);
        destinationCodeSpinner = (Spinner) rootView.findViewById(R.id.Fragment_OtherOperators_Spinner);

        List<String> spinnerList = new ArrayList<String>();
        for (TransferCode bpc : TransferCode.topupcreditCodes) {
            spinnerList.add(bpc.getDescription());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, R.id.Spinner_TextView, spinnerList);

        destinationCodeSpinner.setAdapter(spinnerAdapter);
        destinationCodeSpinner.setOnItemSelectedListener(new OtherOperatorsOnItemSelectedListener(getActivity()));
        destinationCodeSpinner.setSelection(0);

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
        phoneNumberET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (phoneNumberET.getText().toString().isEmpty())
                    phoneNumberTV.setVisibility(View.VISIBLE);
                else
                    phoneNumberTV.setVisibility(View.GONE);
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
                if(model == null)
                    return;
                String action = intent.getAction();
                if(action.equalsIgnoreCase(INTENT.OTHER_OPERATOR.toString()))
                {
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(response.equalsIgnoreCase("OK"))
                        hideProgressBar("SUCCESSFUL");
                    else
                        hideProgressBar(response);
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.OTHER_OPERATOR.toString()));
    }


    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Fragment_OtherOperators_Button_Pay:
                if(amountET.getText().toString().isEmpty())
                {
                    amountET.setError(getString(R.string.AMOUNT_IS_MANDATORY));
                    return;
                }

                ApplicationActivity.hideKeyboard(getActivity());
                if(phoneNumberET.getText().toString().isEmpty())
                {
                    phoneNumberET.setError("Phone number is mandatory");
                    return;
                }
                showProgressBar();
                model = new OtherOperatorPayment(getActivity());
                model.setWorkingAmount(amountET.getText().toString());
                model.setPhoneNumber(phoneNumberET.getText().toString());
                model.setDestinationCode(TransferCode.topupcreditCodes.get(destinationCodeSpinner.getSelectedItemPosition()).getCode());
                model.getConnTaskManager().startBackgroundTask();
                model.getConnTaskManager().startBackgroundTask();
                break;
        }
    }


    private void showProgressBar(){
        amountET.setEnabled(false);
        phoneNumberET.setEnabled(false);
        payButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        amountET.setEnabled(true);
        phoneNumberET.setEnabled(true);
        payButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }

}
