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
import com.mobilis.android.nfc.model.CashOut;
import com.mobilis.android.nfc.util.TextModifier;

import java.util.Locale;

/**
 * Created by ahmed on 10/06/14.
 */
public class CashOutFragment extends NFCFragment implements View.OnClickListener, DialogInterface.OnDismissListener, DialogInterface.OnShowListener  {

    private final String TAG = CashOutFragment.class.getSimpleName();
    TextView amountTV;
    TextView resultTV;
    EditText amountET;
    Button payButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;
    TextModifier textModifier;
    CashOut model;
    boolean pinDialogIsOn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cash_out_view, container, false);
        amountET = (EditText) rootView.findViewById(R.id.Fragment_CashOut_EditText_Amount);
        amountTV = (TextView) rootView.findViewById(R.id.Fragment_CashOut_TextView_Amount);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_CashOut_TextView_Result);
        payButton = (Button) rootView.findViewById(R.id.Fragment_CashOut_Button_Pay);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_CashOut_Progressbar);
        textModifier = new TextModifier(amountET, amountTV);
        payButton.setOnClickListener(this);
        amountET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                textModifier.addDecimalToText(s.toString());
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        model = new CashOut(getActivity());
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(model == null || !isAdded())
                    return;
                String action = intent.getAction();
                if(action.equalsIgnoreCase(INTENT.CASH_OUT.toString()))
                {
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(response.equalsIgnoreCase("OK"))
                        hideProgressBar("SUCCESSFUL");
                    else
                        hideProgressBar(response);
                }
                else if(action.equalsIgnoreCase(INTENT.NFC_SCANNED.toString()))
                {
                    if(amountET.getText().toString().isEmpty())
                        Toast.makeText(getActivity(), "Insert amount", Toast.LENGTH_SHORT).show();

                    if(NFCDialog != null && NFCDialog.isShowing())
                    {
                        showProgressBar();
                        NFCDialog.dismiss();
                        model.setNfcScanned(true);
                        model.setWorkingAmount(textModifier.getAmount());
                        model.setNFCId(intent.getStringExtra(INTENT.EXTRA_NFC_ID.toString()).toUpperCase(Locale.US));

                        if (getResources().getBoolean(R.bool.PAY_PIN_REQUIRED)) {
                            showPinDialog();
                        } else {
                            model.setPin(null);
                            model.getConnTaskManager().startBackgroundTask();
                        }
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CASH_OUT.toString()));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NFC_SCANNED.toString()));

    }

    private void showPinDialog(){
        if(pinDialogIsOn)
            return;
        pinDialogIsOn = true;
        final Dialog pinDialog = new Dialog(getActivity());
        pinDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pinDialog.setContentView(R.layout.pin_dialog);
        pinDialog.setCanceledOnTouchOutside(false);
        final TextView pinTextView = (TextView)pinDialog.findViewById(R.id.Dialog_PIN_TextView_PIN);
        final EditText pinEditText = (EditText)pinDialog.findViewById(R.id.Dialog_PIN_EditText_PIN);
        pinEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(pinEditText.getText().toString().isEmpty())
                    pinTextView.setVisibility(View.VISIBLE);
                else
                    pinTextView.setVisibility(View.INVISIBLE);
                if(pinEditText.getText().length() == 4)
                {
                    pinDialogIsOn = false;
                    pinDialog.dismiss();
                    showProgressBar();
                    model.setPin(pinEditText.getText().toString());
                    model.getConnTaskManager().startBackgroundTask();
                }
            }
        });
        pinDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId())
        {
            case R.id.Fragment_CashOut_Button_Pay:
                if(amountET.getText().toString().isEmpty())
                {
                   Toast.makeText(v.getContext(), getString(R.string.AMOUNT_IS_MANDATORY), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    private void showProgressBar(){
        amountET.setEnabled(false);
        payButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        amountET.setEnabled(true);
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        disableNFCScan();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        enableNFCScan();
    }
}
