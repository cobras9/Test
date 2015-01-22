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
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.RedeemVoucher;

/**
 * Created by ahmed on 10/06/14.
 */
public class RedeemVoucherFragment  extends ApplicationActivity.PlaceholderFragment implements View.OnClickListener{

    private final String TAG = SendMoneyFragment.class.getSimpleName();
    TextView amountTV;
    TextView claimCodeTV;
    TextView resultTV;
    EditText amountET;
    EditText claimCodeET;
    Button payButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;

    RedeemVoucher model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_redeem_voucher_view, container, false);
        amountET = (EditText) rootView.findViewById(R.id.Fragment_RedeemVoucher_EditText_Amount);
        amountTV = (TextView) rootView.findViewById(R.id.Fragment_RedeemVoucher_TextView_Amount);
        claimCodeET = (EditText) rootView.findViewById(R.id.Fragment_RedeemVoucher_EditText_ClaimCode);
        claimCodeTV = (TextView) rootView.findViewById(R.id.Fragment_RedeemVoucher_TextView_ClaimCode);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_RedeemVoucher_TextView_Result);
        payButton = (Button) rootView.findViewById(R.id.Fragment_RedeemVoucher_Button_Pay);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_RedeemVoucher_Progressbar);

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
        claimCodeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (claimCodeET.getText().toString().isEmpty())
                    claimCodeTV.setVisibility(View.VISIBLE);
                else
                    claimCodeTV.setVisibility(View.GONE);
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
                if(action.equalsIgnoreCase(INTENT.REDEEM_VOUCHER.toString()))
                {
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(response.equalsIgnoreCase("OK"))
                        hideProgressBar("SUCCESSFUL");
                    else
                        hideProgressBar(response);
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.REDEEM_VOUCHER.toString()));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Fragment_RedeemVoucher_Button_Pay:
                if(claimCodeET.getText().toString().isEmpty())
                {
                    Toast.makeText(v.getContext(), "Claim Code is mandatory field", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(amountET.getText().toString().isEmpty())
                {
                    Toast.makeText(v.getContext(), getString(R.string.AMOUNT_IS_MANDATORY), Toast.LENGTH_SHORT).show();
                    return;
                }
                ApplicationActivity.hideKeyboard(getActivity());
                showProgressBar();
                model = new RedeemVoucher(getActivity());
                model.setClaimCode(claimCodeET.getText().toString());
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
        claimCodeET.setEnabled(false);
        payButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        amountET.setEnabled(true);
        claimCodeET.setEnabled(true);
        payButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }
}
