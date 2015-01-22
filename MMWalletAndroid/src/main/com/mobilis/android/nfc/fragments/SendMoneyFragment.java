

/******

 This is a backup file

 ******/

package com.mobilis.android.nfc.fragments;

import android.app.Activity;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.activities.MagTekModel;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.MakePaymentLocal;
import com.mobilis.android.nfc.util.TextModifier;

import java.util.Locale;

/**
 * Created by ahmed on 10/06/14.
 */
public class SendMoneyFragment  extends NFCFragment implements View.OnClickListener, DialogInterface.OnDismissListener, DialogInterface.OnShowListener {//}, TextWatcher{

    private final String TAG = SendMoneyFragment.class.getSimpleName();
    TextView amountTV;
    TextView accountIdTV;
    TextView resultTV;
    EditText amountET;
    EditText accountIdET;
    Button payButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;
    ScrollView scrollView;
    TextModifier textModifier;
    MakePaymentLocal model;
    MagTekModel magTekModel;
    boolean receivedNFCScan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_send_money_view, container, false);
        scrollView = (ScrollView) rootView.findViewById(R.id.Fragment_SendMoney_ScrollView);
        amountET = (EditText) rootView.findViewById(R.id.Fragment_SendMoney_EditText_Amount);
        amountTV = (TextView) rootView.findViewById(R.id.Fragment_SendMoney_TextView_Amount);
        accountIdET = (EditText) rootView.findViewById(R.id.Fragment_SendMoney_EditText_AccountId);
        accountIdTV = (TextView) rootView.findViewById(R.id.Fragment_SendMoney_TextView_AccountId);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_SendMoney_TextView_Result);
        payButton = (Button) rootView.findViewById(R.id.Fragment_SendMoney_Button_Pay);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_SendMoney_Progressbar);
        textModifier = new TextModifier(amountET, amountTV);

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

        accountIdET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (accountIdET.getText().length() > 0)
                    accountIdTV.setVisibility(View.GONE);
                else
                    accountIdTV.setVisibility(View.VISIBLE);
            }
        });

        return rootView;

    }

    @Override

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(SendMoneyFragment.class.getSimpleName(), "onReceive is called with action: " + intent.getAction());
                Log.d(SendMoneyFragment.class.getSimpleName(), "!isAdded()? " + (!isAdded()));
                if (!isAdded())
                    return;
                String action = intent.getAction();
                if (action.equalsIgnoreCase(INTENT.SEND_MONEY_LOCAL.toString())) {
                    if (model == null)
                        return;
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if (response.equalsIgnoreCase("OK"))
                        hideProgressBar("SUCCESSFUL");
                    else
                        hideProgressBar(response);
                } else if (action.equalsIgnoreCase(INTENT.NFC_SCANNED.toString())) {
                    Log.d(SendMoneyFragment.class.getSimpleName(), "receivedNFCScan? " + (receivedNFCScan));
                    Log.d(SendMoneyFragment.class.getSimpleName(), "model == null? " + (model == null));
                    Log.d(SendMoneyFragment.class.getSimpleName(), "NFCDialog != null" + (NFCDialog != null));
                    if (NFCDialog != null)
                        Log.d(SendMoneyFragment.class.getSimpleName(), "NFCDialog.isShowing()? " + (NFCDialog.isShowing()));
                    if (receivedNFCScan)
                        return;
                    if (model == null)
                        return;
                    if (NFCDialog != null && NFCDialog.isShowing())
                        NFCDialog.dismiss();
                    if (amountET.getText().toString().isEmpty())
                        Toast.makeText(getActivity(), "Insert amount", Toast.LENGTH_SHORT).show();
                    receivedNFCScan = true;
                    model.setNFCId(intent.getStringExtra(INTENT.EXTRA_NFC_ID.toString()).toUpperCase(Locale.US));
                    model.setNfcScanned(true);
                    model.setDestinationProvided(false);
                    model.setMsisdnTransaction(false);

                    if (getString(R.string.app_name).toUpperCase(Locale.US).contains(getString(R.string.AIRTEL)) || getString(R.string.app_name).toUpperCase().contains("NFC")) {
                        showPinDialog();
                        return;
                    }
                    startBackgroundProcess();
                }
                intent=null;
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.SEND_MONEY_LOCAL.toString()));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NFC_SCANNED.toString()));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        magTekModel = new MagTekModel(getActivity());
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause() is call..hiding keyboard now...");
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Fragment_SendMoney_Button_Pay:
                if (amountET.getText().toString().isEmpty()) {
                    Toast.makeText(v.getContext(), getString(R.string.AMOUNT_IS_MANDATORY), Toast.LENGTH_SHORT).show();
                    return;
                }

                ApplicationActivity.hideKeyboard(getActivity());
                model = new MakePaymentLocal(getActivity());
                if (accountIdET.getText().toString().isEmpty()) {
                    model.setMsisdnTransaction(false);
                    model.setDestinationProvided(false);
                    NFCDialog = new Dialog(v.getContext());
                    NFCDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    NFCDialog.setContentView(R.layout.nfc_dialog);
                    NFCDialog.setCanceledOnTouchOutside(false);
                    NFCDialog.setOnShowListener(this);
                    NFCDialog.setOnDismissListener(this);
                    NFCDialog.show();
                } else {
//                    showProgressBar();
                    model.setNfcScanned(false);
                    model.setDestinationProvided(true);
                    model.setMsisdnTransaction(true);
                    model.setDestination(accountIdET.getText().toString());
                    model.setWorkingAmount(textModifier.getAmount());
                    showPinDialog();
//                    model.getConnTaskManager().startBackgroundTask();
                }
                break;
        }
    }

    private void showProgressBar() {
        amountET.setEnabled(false);
        accountIdET.setEnabled(false);
        payButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
        scrollView.scrollTo(0, scrollView.getBottom());
    }

    private void hideProgressBar(String response) {
        amountET.setEnabled(true);
        accountIdET.setEnabled(true);
        payButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }


    @Override
    public void finishedA2ACommunication(String scannedId) {

    }

    private void startBackgroundProcess() {
        showProgressBar();
//        model.setDestinationProvided(true);
//        model.setMsisdnTransaction(true);
//        model.setDestination(accountIdET.getText().toString());
        model.setWorkingAmount(textModifier.getAmount());
        model.getConnTaskManager().startBackgroundTask();
//        Intent intent = new Intent(INTENT.NFC_SCANNED.toString());
//        intent.putExtra(INTENT.EXTRA_NFC_ID.toString(), model.getNFCId());
//        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void showPinDialog() {
        receivedNFCScan = false;
        final Dialog pinDialog = new Dialog(getActivity());
        pinDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pinDialog.setContentView(R.layout.pin_dialog);
        pinDialog.setCanceledOnTouchOutside(false);
        final TextView pinTextView = (TextView) pinDialog.findViewById(R.id.Dialog_PIN_TextView_PIN);
        final EditText pinEditText = (EditText) pinDialog.findViewById(R.id.Dialog_PIN_EditText_PIN);
        pinEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pinEditText.getText().toString().isEmpty())
                    pinTextView.setVisibility(View.VISIBLE);
                else
                    pinTextView.setVisibility(View.INVISIBLE);
                if (pinEditText.getText().length() == getResources().getInteger(R.integer.PIN_LENGTH)) {
                    model.setPin(pinEditText.getText().toString());
                    pinDialog.dismiss();

                    startBackgroundProcess();
//                    if(accountIdET.getText().toString().isEmpty())
//                    {
//                        model.setMsisdnTransaction(false);
////                        NFCDialog = new Dialog(getActivity());
////                        NFCDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
////                        NFCDialog.setContentView(R.layout.nfc_dialog);
////                        NFCDialog.setCanceledOnTouchOutside(false);
////                        NFCDialog.show();
//                    }
//                    else{
//                        showProgressBar();
//                        model.setDestinationProvided(true);
//                        model.setNfcScanned(false);
//                        model.setMsisdnTransaction(true);
//                        model.setDestination(accountIdET.getText().toString());
//                        model.setWorkingAmount(textModifier.getAmount());
//                        model.getConnTaskManager().startBackgroundTask();
////                    }
                }
            }
        });
        pinDialog.show();
    }
//
//    public void hideKeyboard(EditText editText){
//
//        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
//       imm.hideSoftInputFromWindow(amountET.getWindowToken(),0);
//    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        disableNFCScan();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        enableNFCScan();
    }
}
