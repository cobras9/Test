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
import android.util.Log;
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
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.model.TopUpInternational;
import com.mobilis.android.nfc.model.TransferCode;
import com.mobilis.android.nfc.util.CustomOnItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 10/06/14.
 */
public class TopUpFragment extends NFCFragment implements View.OnClickListener{

    private final String TAG = TopUpFragment.class.getSimpleName();
    TextView amountTV;
    TextView accountIdTV;
    TextView resultTV;
    EditText amountET;
    EditText accountIdET;
    Button payButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;
    Spinner destinationCodeSpinner;
    TopUpInternational model;

    TextView ccNumberTV;
    TextView ccExpiryTV;
    EditText ccNumberET;
    EditText ccExpiryET;

    boolean isCreditCardSupported = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_up_view, container, false);
        amountET = (EditText) rootView.findViewById(R.id.Fragment_TopUp_EditText_Amount);
        amountTV = (TextView) rootView.findViewById(R.id.Fragment_TopUp_TextView_Amount);
        accountIdET = (EditText) rootView.findViewById(R.id.Fragment_TopUp_EditText_AccountId);
        accountIdTV = (TextView) rootView.findViewById(R.id.Fragment_TopUp_TextView_AccountId);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_TopUp_TextView_Result);
        payButton = (Button) rootView.findViewById(R.id.Fragment_TopUp_Button_Pay);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_TopUp_Progressbar);
        destinationCodeSpinner = (Spinner) rootView.findViewById(R.id.Fragment_TopUp_Spinner);

        ccNumberTV = (TextView) rootView.findViewById(R.id.Fragment_TopUp_TextView_CreditCardNumber);
        ccExpiryTV = (TextView) rootView.findViewById(R.id.Fragment_TopUp_TextView_CreditCardExpiry);
        ccNumberET = (EditText) rootView.findViewById(R.id.Fragment_TopUp_EditText_CreditCardNumber);
        ccExpiryET = (EditText) rootView.findViewById(R.id.Fragment_TopUp_EditText_CreditCardExpiry);

        if (isCreditCardSupported) {
            ccNumberTV.setVisibility(View.VISIBLE);
            ccNumberET.setVisibility(View.VISIBLE);
            ccExpiryTV.setVisibility(View.VISIBLE);
            ccExpiryET.setVisibility(View.VISIBLE);

            ccNumberET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if(ccNumberET.getText().toString().isEmpty())
                        ccNumberTV.setVisibility(View.VISIBLE);
                    else
                        ccNumberTV.setVisibility(View.INVISIBLE);
                }
            });

            ccExpiryET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if(ccExpiryET.getText().toString().isEmpty())
                        ccExpiryTV.setVisibility(View.VISIBLE);
                    else
                        ccExpiryTV.setVisibility(View.INVISIBLE);
                }
            });

        } else {
            ccNumberTV.setVisibility(View.INVISIBLE);
            ccNumberET.setVisibility(View.INVISIBLE);
            ccExpiryTV.setVisibility(View.INVISIBLE);
            ccExpiryET.setVisibility(View.INVISIBLE);
        }
        List<String> spinnerList = new ArrayList<String>();
        for (TransferCode bpc : TransferCode.topupAirtimeCodes) {
            spinnerList.add(bpc.getDescription());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, R.id.Spinner_TextView, spinnerList);
        destinationCodeSpinner.setAdapter(spinnerAdapter);
        destinationCodeSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener(getActivity()));
        destinationCodeSpinner.setSelection(0);
        payButton.setOnClickListener(this);

        amountET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(amountET.getText().toString().isEmpty())
                    amountTV.setVisibility(View.VISIBLE);
                else
                    amountTV.setVisibility(View.INVISIBLE);
            }
        });
        accountIdET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(accountIdET.getText().toString().isEmpty())
                    accountIdTV.setVisibility(View.VISIBLE);
                else
                    accountIdTV.setVisibility(View.GONE);
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
                if (action.equalsIgnoreCase(INTENT.TOP_UP.toString())) {
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if (response.equalsIgnoreCase("OK"))
                        hideProgressBar("SUCCESSFUL");
                    else
                        hideProgressBar(response);
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.TOP_UP.toString()));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Fragment_TopUp_Button_Pay:
                if(amountET.getText().toString().isEmpty())
                {
                    amountET.setError(getString(R.string.AMOUNT_IS_MANDATORY));
                    return;
                }
                ApplicationActivity.hideKeyboard(getActivity());
                showProgressBar();
                model = new TopUpInternational(getActivity());
                if(AbstractModel.isNumeric(amountET.getText().toString()))
                    model.setWorkingAmount(amountET.getText().toString());
                else
                    model.setWorkingAmount(amountTV.getText().toString());
                Log.d("PROB","model.getWorkingAmount(): "+model.getWorkingAmount());
                model.setPhoneNumber(accountIdET.getText().toString());
                model.setDestinationCode(TransferCode.topupAirtimeCodes.get(destinationCodeSpinner.getSelectedItemPosition()).getCode());

                if (ccNumberET.length() > 0) {
                    model.setCreditCardNumber(ccNumberET.getText().toString());
                }

                if (ccExpiryET.length() > 0) {
                    model.setCreditCardExpiry(ccExpiryET.getText().toString());
                }

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
        accountIdET.setEnabled(false);
        payButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        amountET.setEnabled(true);
        accountIdET.setEnabled(true);
        payButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }


    @Override
    public void finishedA2ACommunication(String scannedId) {
        Intent intent = new Intent(INTENT.NFC_SCANNED.toString());
        intent.putExtra(INTENT.EXTRA_NFC_ID.toString(), scannedId);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
}
