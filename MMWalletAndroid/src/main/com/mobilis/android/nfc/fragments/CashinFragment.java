package com.mobilis.android.nfc.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.ExchangeQuotation;
import com.mobilis.android.nfc.model.ReceiveCashIn;
import com.mobilis.android.nfc.util.TextModifier;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Created by ahmed on 10/06/14.
 */
public class CashinFragment extends NFCFragment implements View.OnClickListener, DialogInterface.OnDismissListener, DialogInterface.OnShowListener  {

    private final String TAG = CashinFragment.class.getSimpleName();
    TextView amountTV;
    TextView accountIdTV;
    TextView resultTV;
    EditText amountET;
    EditText accountIdET;
    Button payButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;
    TextModifier textModifier;
    ReceiveCashIn model;
    boolean isExchangeQuotationDialogOn = false;
    Activity activity;
    AlertDialog exchangeQuotationDialog;
    BigDecimal totalAmountInt;
    String nfcTag = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        nfcTag = null;

        View rootView = inflater.inflate(R.layout.fragment_cash_in_view, container, false);
        amountET = (EditText) rootView.findViewById(R.id.Fragment_CashIn_EditText_Amount);
        amountTV = (TextView) rootView.findViewById(R.id.Fragment_CashIn_TextView_Amount);
        accountIdET = (EditText) rootView.findViewById(R.id.Fragment_CashIn_EditText_AccountId);
        accountIdTV = (TextView) rootView.findViewById(R.id.Fragment_CashIn_TextView_AccountId);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_CashIn_TextView_Result);
        payButton = (Button) rootView.findViewById(R.id.Fragment_CashIn_Button_Pay);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_CashIn_Progressbar);
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Fragment_CashIn_Button_Pay:
                if(amountET.getText().toString().isEmpty())
                {
                   Toast.makeText(v.getContext(), getString(R.string.AMOUNT_IS_MANDATORY), Toast.LENGTH_SHORT).show();
                    return;
                }

                ApplicationActivity.hideKeyboard(getActivity());
                model = new ReceiveCashIn(getActivity());
                if(accountIdET.getText().toString().isEmpty())
                {
                    model.setMsisdnTransaction(false);
                    NFCDialog = new Dialog(v.getContext());
                    NFCDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    NFCDialog.setContentView(R.layout.nfc_dialog);
                    NFCDialog.setCanceledOnTouchOutside(false);
                    NFCDialog.setOnShowListener(this);
                    NFCDialog.setOnDismissListener(this);
                    NFCDialog.show();
                }
                else{
                    showProgressBar();

                    if (getResources().getBoolean(R.bool.CASH_IN_EXCHANGE_QUOTE_ON)) {
                        ExchangeQuotation vModel = new ExchangeQuotation(activity);
                        vModel.setMsisdn(vModel.getMerchantId());
                        vModel.setWorkingAmount(textModifier.getAmount());
                        vModel.setCustomerDataMsisdn(accountIdET.getText().toString());
                        vModel.setPaymentType("C2MD");
                        vModel.getConnTaskManager().startBackgroundTask();
                    } else {
                        model.setMsisdnTransaction(true);
                        model.setMsisdn(accountIdET.getText().toString());
                        model.setWorkingAmount(textModifier.getAmount());
                        model.getConnTaskManager().startBackgroundTask();
                    }
                }
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
    @Override
    public void onDismiss(DialogInterface dialog) {
        disableNFCScan();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        enableNFCScan();
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        Log.d(DepositFundsFromCreditCardFragment.class.getSimpleName(), "onAttach() is called..");


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equalsIgnoreCase(INTENT.EXCHANGE_QUOTATION.toString()))
                {
                    progressBar.setVisibility(View.GONE);
                    if(isExchangeQuotationDialogOn)
                        return;
                    showExchangeQuotationDialog(intent);
                }
                else if(action.equalsIgnoreCase(INTENT.CASH_IN.toString()))
                {
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(response.equalsIgnoreCase("OK")) {
                        hideProgressBar("SUCCESSFUL");
                    } else {
                        hideProgressBar(response);
                    }
                }
                else if(action.equalsIgnoreCase(INTENT.NFC_SCANNED.toString()))
                {
                    if(NFCDialog != null && NFCDialog.isShowing())
                    {
                        NFCDialog.dismiss();
                        showProgressBar();

                        if (getResources().getBoolean(R.bool.CASH_IN_EXCHANGE_QUOTE_ON)) {
                            ExchangeQuotation vModel = new ExchangeQuotation(activity);
                            vModel.setMsisdn(vModel.getMerchantId());
                            vModel.setWorkingAmount(amountET.getText().toString());
                            vModel.setNfcScanned(true);
                            vModel.setNFCId(intent.getStringExtra(INTENT.EXTRA_NFC_ID.toString()).toUpperCase(Locale.US));
                            nfcTag = vModel.getNFCId();
                            vModel.setPaymentType("C2MD");
                            vModel.getConnTaskManager().startBackgroundTask();
                        } else {
                            model.setNfcScanned(true);
                            model.setNFCId(intent.getStringExtra(INTENT.EXTRA_NFC_ID.toString()).toUpperCase(Locale.US));
                            model.setMsisdnTransaction(false);
                            model.setWorkingAmount(textModifier.getAmount());
                            model.getConnTaskManager().startBackgroundTask();
                        }

                    }
                    if(amountET.getText().toString().isEmpty())
                        Toast.makeText(getActivity(), "Insert amount", Toast.LENGTH_SHORT).show();
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NFC_SCANNED.toString()));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.EXCHANGE_QUOTATION.toString()));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CASH_IN.toString()));

    }

    private void showExchangeQuotationDialog(Intent intent){
        Log.d(CashinFragment.class.getSimpleName(), "showExchangeQuotationDialog() is called");
        isExchangeQuotationDialogOn = true;
        progressBar.setVisibility(View.INVISIBLE);
        Log.d(TAG,intent.getExtras().toString());
        final String name = intent.getStringExtra("NAME");
        final String merchantFess = intent.getStringExtra("MERCHANT_FEES");
        final String amount = amountET.getText().toString();
        final String totalAmount = intent.getStringExtra(INTENT.EXTRA_TOTAL_AMOUNT.toString());

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_exchange_quotaion, null);
        builder.setView(view);

        TextView title = new TextView(activity);
        title.setText("Payment Confirmation");
        title.setBackgroundColor(Color.WHITE);
        title.setPadding(10, 30, 10, 30);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(18);
        builder.setCustomTitle(title);

        LinearLayout totalAmountLL = (LinearLayout)view.findViewById(R.id.Dialog_Exchange_Quotation_LinearLayout_TotalAmount);
        totalAmountLL.setVisibility(View.VISIBLE);

        TextView nameTV = (TextView)view.findViewById(R.id.Dialog_Exchange_Quotation_TextView_Name);
        TextView amountTV = (TextView)view.findViewById(R.id.Dialog_Exchange_Quotation_TextView_Amount);
        TextView feesTV = (TextView)view.findViewById(R.id.Dialog_Exchange_Quotation_TextView_Fees);
        TextView feesTitleTV = (TextView)view.findViewById(R.id.Dialog_Exchange_Quotation_TextView_FeesTitle);
        feesTitleTV.setText(getString(R.string.LABEL_FEE));
        TextView totalAmountTV = (TextView)view.findViewById(R.id.Dialog_Exchange_Quotation_TextView_TotalAmount);

        nameTV.setText(getString(R.string.RECEIVE_CASH_IN));
        amountTV.setText(textModifier.getAmount());
        Log.d(TAG, "merchantFees="+merchantFess);

        totalAmountTV.setText(totalAmount);
        feesTV.setText(merchantFess);

        Button confirm = (Button) view.findViewById(R.id.Dialog_Exchange_Quotation_Button_Confirm);
        Button cancel = (Button) view.findViewById(R.id.Dialog_Exchange_Quotation_Button_Cancel);

        exchangeQuotationDialog = builder.create();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exchangeQuotationDialog.dismiss();

                model = new ReceiveCashIn(getActivity());
                showProgressBar();
                model.setMsisdnTransaction(true);

                if (nfcTag  != null) {
                    model.setNfcScanned(true);
                    model.setNFCId(nfcTag);
                } else {
                    model.setMsisdn(accountIdET.getText().toString());
                }
                model.setWorkingAmount(totalAmount.toString());
                model.getConnTaskManager().startBackgroundTask();

            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideProgressBar("");
                exchangeQuotationDialog.dismiss();
            }
        });
        exchangeQuotationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isExchangeQuotationDialogOn = false;
            }
        });
        hideProgressBar("");
        exchangeQuotationDialog.show();
    }

}
