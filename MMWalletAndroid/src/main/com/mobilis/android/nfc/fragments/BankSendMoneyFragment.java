package com.mobilis.android.nfc.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
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
import com.mobilis.android.nfc.model.BanksPayment;
import com.mobilis.android.nfc.model.ExchangeQuotation;
import com.mobilis.android.nfc.model.TransferCode;
import com.mobilis.android.nfc.util.BanksOnItemSelectedListener;
import com.mobilis.android.nfc.util.TextModifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 10/06/14.
 */
public class BankSendMoneyFragment extends ApplicationActivity.PlaceholderFragment implements View.OnClickListener{

    private final String TAG = BankSendMoneyFragment.class.getSimpleName();
    TextView amountTV;
    TextView bankAccountTV;
    TextView resultTV;
    EditText amountET;
    EditText bankAccountET;
    Button payButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;
    Spinner destinationCodeSpinner;
    AlertDialog exchangeQuotationDialog;
    BanksPayment model;
    TextModifier textModifier;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_banks_view, container, false);
        amountET = (EditText) rootView.findViewById(R.id.Fragment_Banks_EditText_Amount);
        amountTV = (TextView) rootView.findViewById(R.id.Fragment_Banks_TextView_Amount);
        bankAccountET = (EditText) rootView.findViewById(R.id.Fragment_Banks_EditText_BankAccount);
        bankAccountTV = (TextView) rootView.findViewById(R.id.Fragment_Banks_TextView_BankAccount);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_Banks_TextView_Result);
        payButton = (Button) rootView.findViewById(R.id.Fragment_Banks_Button_Pay);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_Banks_Progressbar);
        destinationCodeSpinner = (Spinner) rootView.findViewById(R.id.Fragment_Banks_Spinner);
        textModifier = new TextModifier(amountET, amountTV);

        List<String> spinnerList = new ArrayList<String>();
        for (TransferCode bpc : TransferCode.bankCodes) {
            spinnerList.add(bpc.getDescription());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, R.id.Spinner_TextView, spinnerList);

        destinationCodeSpinner.setAdapter(spinnerAdapter);
        destinationCodeSpinner.setOnItemSelectedListener(new BanksOnItemSelectedListener(getActivity()));
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
                textModifier.addDecimalToText(s.toString());
            }
        });
        bankAccountET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (bankAccountET.getText().toString().isEmpty())
                    bankAccountTV.setVisibility(View.VISIBLE);
                else
                    bankAccountTV.setVisibility(View.GONE);
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
                if(action.equalsIgnoreCase(INTENT.BANK.toString()))
                {
                    if(model == null)
                        return;
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(response.equalsIgnoreCase("OK"))
                        hideProgressBar("SUCCESSFUL");
                    else
                        hideProgressBar(response);
                }
                else if(intent.getAction().equalsIgnoreCase("EXCHANGE_QUOTATION"))
                {

                    if(model == null)
                        return;
//                    if(exchangeQuotationDialog != null && exchangeQuotationDialog.isShowing())
//                        return;
                    progressBar.setVisibility(View.INVISIBLE);
                    String name = intent.getStringExtra("NAME");
                    String merchantFess = intent.getStringExtra("MERCHANT_FEES");
                    String amount = intent.getStringExtra("AMOUNT");
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.dialog_exchange_quotaion, null);
                    builder.setView(view);

                    TextView title = new TextView(getActivity());
                    title.setText("Payment Confirmation");
                    title.setBackgroundColor(Color.WHITE);
                    title.setPadding(10, 30, 10, 30);
                    title.setGravity(Gravity.CENTER);
                    title.setTextColor(Color.BLACK);
                    title.setTextSize(18);
                    builder.setCustomTitle(title);

                    TextView nameTV = (TextView)view.findViewById(R.id.Dialog_Exchange_Quotation_TextView_Name);
                    TextView amountTV = (TextView)view.findViewById(R.id.Dialog_Exchange_Quotation_TextView_Amount);
                    TextView feesTV = (TextView)view.findViewById(R.id.Dialog_Exchange_Quotation_TextView_Fees);

                    nameTV.setTextColor(Color.BLACK);
                    amountTV.setTextColor(Color.BLACK);
                    feesTV.setTextColor(Color.BLACK);

                    nameTV.setText(name);
                    amountTV.setText(model.getWorkingAmount());
                    feesTV.setText(merchantFess);

                    Button confirm = (Button) view.findViewById(R.id.Dialog_Exchange_Quotation_Button_Confirm);
                    Button cancel = (Button) view.findViewById(R.id.Dialog_Exchange_Quotation_Button_Cancel);

                    exchangeQuotationDialog = builder.create();
                    confirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            exchangeQuotationDialog.dismiss();
                            showProgressBar();
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
                    hideProgressBar("");
                    exchangeQuotationDialog.show();
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.BANK.toString()));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.EXCHANGE_QUOTATION.toString()));
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
            case R.id.Fragment_Banks_Button_Pay:
                if(amountET.getText().toString().isEmpty())
                {
                    amountET.setError(getString(R.string.AMOUNT_IS_MANDATORY));
                    return;
                }
                ApplicationActivity.hideKeyboard(getActivity());
                if(bankAccountET.getText().toString().isEmpty())
                {
                    bankAccountET.setError("Bank Account is mandatory");
                    return;
                }

                showProgressBar();
                model = new BanksPayment(getActivity());
                model.setWorkingAmount(textModifier.getAmount());
                model.setPhoneNumber(bankAccountET.getText().toString());
                model.setDestinationCode(TransferCode.bankCodes.get(destinationCodeSpinner.getSelectedItemPosition()).getCode());

                ExchangeQuotation exchangeQuotation = new ExchangeQuotation(getActivity());
                exchangeQuotation.setWorkingAmount(model.getWorkingAmount());
                exchangeQuotation.setPhoneNumber(model.getPhoneNumber());
                exchangeQuotation.setDestinationCode(model.getDestinationCode());
                exchangeQuotation.getConnTaskManager().startBackgroundTask();
                break;
        }
    }

    private void showProgressBar(){
        amountET.setEnabled(false);
        bankAccountET.setEnabled(false);
        payButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        amountET.setEnabled(true);
        bankAccountET.setEnabled(true);
        payButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }

}
