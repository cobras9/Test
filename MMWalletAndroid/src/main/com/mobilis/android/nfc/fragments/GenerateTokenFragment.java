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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.GenerateToken;
import com.mobilis.android.nfc.model.ReceivePayment;
import com.mobilis.android.nfc.util.TextModifier;

import java.util.Locale;

/**
 * Created by ahmed on 10/06/14.
 */
public class GenerateTokenFragment extends NFCFragment implements View.OnClickListener, DialogInterface.OnDismissListener, DialogInterface.OnShowListener {

    private final String TAG = GenerateTokenFragment.class.getSimpleName();
    TextView amountTV;
    TextView resultTV;
    EditText amountET;
    Button genButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;
    TextModifier textModifier;
    GenerateToken model;
    boolean receivedNFCScan;
    boolean pinDialogIsOn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_generate_token_view, container, false);
        amountET = (EditText) rootView.findViewById(R.id.Fragment_GenToken_EditText_Amount);
        amountTV = (TextView) rootView.findViewById(R.id.Fragment_GenToken_TextView_Amount);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_GenToken_TextView_Result);
        genButton = (Button) rootView.findViewById(R.id.Fragment_GenToken_Button);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_GenToken_Progressbar);
        genButton.setOnClickListener(this);
        textModifier = new TextModifier(amountET, amountTV);

        amountET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                textModifier.addDecimalToText(s.toString());
            }
        });
        amountET.requestFocus();
        amountET.postDelayed(new Runnable() {
            @Override
            public void run() {
                ApplicationActivity.showKeyboard(getActivity(), amountET);
            }
        },30);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        model = new GenerateToken(getActivity());

        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        }
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(!isAdded())
                    return;

                String action = intent.getAction();
                if(action.equalsIgnoreCase(INTENT.GENERATE_TOKEN.toString()))
                {
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(response.equalsIgnoreCase("OK")) {
                        hideProgressBar(context.getApplicationContext().getResources().getString(R.string.SUCCESS_MSG));
                    } else {
                        hideProgressBar(response);
                        intent = null;
                    }

                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.GENERATE_TOKEN.toString()));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NFC_SCANNED.toString()));

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId())
        {
            case R.id.Fragment_GenToken_Button:
                if(amountET.getText().toString().isEmpty())
                {
                    Toast.makeText(v.getContext(), getString(R.string.AMOUNT_IS_MANDATORY), Toast.LENGTH_SHORT).show();
                    return;
                }

                model.setNfcScanned(true);
                showProgressBar();
                model.setWorkingAmount(textModifier.getAmount());
                model.getConnTaskManager().startBackgroundTask();

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
        genButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        amountET.setEnabled(true);
        genButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        disableNFCScan();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        enableNFCScan();
    }

    @Override
    public void finishedA2ACommunication(String scannedId) {
    }
}