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
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.BillPayment;
import com.mobilis.android.nfc.model.TransferCode;
import com.mobilis.android.nfc.util.UtilitiesOnItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 10/06/14.
 */
public class UtilitiesFragment extends ApplicationActivity.PlaceholderFragment implements View.OnClickListener {

    private final String TAG = UtilitiesFragment.class.getSimpleName();
    TextView amountTV;
    TextView payeeRefTV;
    TextView resultTV;
    EditText amountET;
    EditText payeeRefET;
    Button payButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;
    Spinner destinationCodeSpinner;

    BillPayment model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_utilities_view, container, false);
        amountET = (EditText) rootView.findViewById(R.id.Fragment_Utilities_EditText_Amount);
        amountTV = (TextView) rootView.findViewById(R.id.Fragment_Utilities_TextView_Amount);
        payeeRefET = (EditText) rootView.findViewById(R.id.Fragment_Utilities_EditText_PayeeRef);
        payeeRefTV = (TextView) rootView.findViewById(R.id.Fragment_Utilities_TextView_PayeeRef);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_Utilities_TextView_Result);
        payButton = (Button) rootView.findViewById(R.id.Fragment_Utilities_Button_Pay);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_Utilities_Progressbar);
        destinationCodeSpinner = (Spinner) rootView.findViewById(R.id.Fragment_Utilities_Spinner);

        List<String> spinnerList = new ArrayList<String>();
        for (TransferCode bpc : TransferCode.utilityCodes) {
            spinnerList.add(bpc.getDescription());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, R.id.Spinner_TextView, spinnerList);

        destinationCodeSpinner.setAdapter(spinnerAdapter);
        destinationCodeSpinner.setOnItemSelectedListener(new UtilitiesOnItemSelectedListener(getActivity()));
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
        payeeRefET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (payeeRefET.getText().toString().isEmpty())
                    payeeRefTV.setVisibility(View.VISIBLE);
                else
                    payeeRefTV.setVisibility(View.GONE);
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
                String action = intent.getAction();
                if(action.equalsIgnoreCase(INTENT.CABLE_TV.toString()))
                {
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(response.equalsIgnoreCase("OK"))
                        hideProgressBar("SUCCESSFUL");
                    else
                        hideProgressBar(response);
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CABLE_TV.toString()));
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Fragment_Utilities_Button_Pay:
                if(amountET.getText().toString().isEmpty())
                {
                   Toast.makeText(v.getContext(), getString(R.string.AMOUNT_IS_MANDATORY), Toast.LENGTH_SHORT).show();
                    return;
                }
                ApplicationActivity.hideKeyboard(getActivity());
                if(payeeRefET.getText().toString().isEmpty())
                {
                    Toast.makeText(v.getContext(), "Payee reference is mandatory", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgressBar();
                model = new BillPayment(getActivity());
                model.setWorkingAmount(amountET.getText().toString());
                model.setPayeeRef(payeeRefET.getText().toString());
                model.setPayeeId(TransferCode.utilityCodes.get(destinationCodeSpinner.getSelectedItemPosition()).getCode());
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
        payeeRefET.setEnabled(false);
        payButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        amountET.setEnabled(true);
        payeeRefET.setEnabled(true);
        payButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }

}
