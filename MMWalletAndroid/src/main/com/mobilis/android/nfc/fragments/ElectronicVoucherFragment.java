package com.mobilis.android.nfc.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.EVDCodes;
import com.mobilis.android.nfc.model.ElectronicVoucher;
import com.mobilis.android.nfc.model.VoucherCode;
import com.mobilis.android.nfc.model.VoucherExchangeQuotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ahmed on 10/06/14.
 */
public class ElectronicVoucherFragment extends NFCFragment implements View.OnClickListener , DialogInterface.OnDismissListener, DialogInterface.OnShowListener {

    private final String TAG = SendMoneyFragment.class.getSimpleName();

    RelativeLayout quantityRL;
    RelativeLayout msisdnRL;
    TextView quantityTV;
    TextView msisdnTV;
    TextView resultTV;
    TextView availableQuantityTV;
    EditText quantityET;
    EditText msisdnET;
    Button submitButton;
    ProgressBar progressBar;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;
    AlertDialog exchangeQuotationDialog;
    Spinner denominationSpinner;
    Spinner issuerSpinner;
    private static Activity activity;
    private int denominationPos;

    public static boolean isCashInOperation;
    boolean isWaitingOnNFCScan;
    boolean isExchangeQuotationDialogOn;
    String selectedIssuer;
    String selectedDenomination;
    int selectedAvailableQuantity;

    ElectronicVoucher model;

    private ProgressDialog dialog = null;
    private List<VoucherCode> voucherCodes = new ArrayList<VoucherCode>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_electronic_voucher_view, container, false);

        EVDCodes evdCodes = new EVDCodes(activity);
        evdCodes.setOwner(evdCodes.getMerchantId());
        evdCodes.getConnTaskManager().startBackgroundTask();

        dialog = new ProgressDialog(activity);
        dialog.show();

        return rootView;

    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {

        quantityRL = (RelativeLayout) rootView.findViewById(R.id.Fragment_SellVoucher_RelativeLayout_Quantity);
        msisdnRL = (RelativeLayout) rootView.findViewById(R.id.Fragment_SellVoucher_RelativeLayout_MSISDN);
        quantityET = (EditText) rootView.findViewById(R.id.Fragment_SellVoucher_EditText_Quantity);
        quantityTV = (TextView) rootView.findViewById(R.id.Fragment_SellVoucher_TextView_Quantity);
        msisdnET = (EditText) rootView.findViewById(R.id.Fragment_SellVoucher_EditText_MSISDN);
        msisdnTV = (TextView) rootView.findViewById(R.id.Fragment_SellVoucher_TextView_MSISDN);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_SellVoucher_TextView_Result);
        availableQuantityTV = (TextView) rootView.findViewById(R.id.Fragment_SellVoucher_TextView_AvailableQuantity);
        submitButton = (Button) rootView.findViewById(R.id.Fragment_SellVoucher_Button_Pay);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_SellVoucher_Progressbar);
        denominationSpinner = (Spinner) rootView.findViewById(R.id.Fragment_SellVoucher_Spinner);
        issuerSpinner = (Spinner) rootView.findViewById(R.id.Fragment_SellVoucher_Spinner_Issuer);

        submitButton.setOnClickListener(this);
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

        quantityET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (quantityET.getText().toString().isEmpty())
                    quantityTV.setVisibility(View.VISIBLE);
                else
                    quantityTV.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void finishedA2ACommunication(String scannedId) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if(isCashInOperation) {
            availableQuantityTV.setVisibility(View.VISIBLE);
            quantityRL.setVisibility(View.VISIBLE);
            msisdnRL.setVisibility(View.VISIBLE);
        }
        else
        {
            availableQuantityTV.setVisibility(View.VISIBLE);
            quantityRL.setVisibility(View.GONE);
            msisdnRL.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Fragment_SellVoucher_Button_Pay:

                if(isCashInOperation && quantityET.getText().toString().isEmpty())
                {
                    quantityET.setError("Mandatory field");
                    return;
                }
                if(isCashInOperation && (Integer.parseInt(quantityET.getText().toString()) > selectedAvailableQuantity))
                {
                    quantityET.setError("Not enough EVD");
                    return;
                }
                ApplicationActivity.hideKeyboard(getActivity());
                if(msisdnET.getText().toString().isEmpty())
                {
                    NFCDialog = new Dialog(v.getContext());
                    NFCDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    NFCDialog.setContentView(R.layout.nfc_dialog);
                    NFCDialog.setCanceledOnTouchOutside(false);
                    NFCDialog.setCanceledOnTouchOutside(false);
                    NFCDialog.setOnShowListener(this);
                    NFCDialog.setOnDismissListener(this);

                    NFCDialog.show();
                    isWaitingOnNFCScan = true;

                    return;
                }
                showProgressBar();
                VoucherExchangeQuotation vModel = new VoucherExchangeQuotation(activity);
                vModel.setNfcScanned(false);
                vModel.setMsisdn(msisdnET.getText().toString());
                vModel.setQuantity(quantityET.getText().toString());
                vModel.setDestinationCode(voucherCodes.get(denominationSpinner.getSelectedItemPosition()).getDestinationCode());
                Log.d(TAG, "onClick denomination =" + selectedDenomination);
//                vModel.setDenomination(VoucherCode.voucherCodes.get(denominationSpinner.getSelectedItemPosition()).getDenomination().get(0));
                vModel.setDenomination(selectedDenomination);
                vModel.setWorkingAmount(selectedDenomination);
                vModel.getConnTaskManager().startBackgroundTask();

                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        activity = null;
//        LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadcastReceiver);
//        broadcastReceiver = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(ElectronicVoucherFragment.class.getSimpleName(), "onDetach() is called");
        activity = null;
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(ElectronicVoucherFragment.class.getSimpleName(), "onAttach() is called..");

        ElectronicVoucherFragment.activity = activity;
        model = new ElectronicVoucher(activity);


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equalsIgnoreCase(INTENT.EXCHANGE_QUOTATION.toString()))
                {
                    progressBar.setVisibility(View.GONE);
                    Log.d(ElectronicVoucherFragment.class.getSimpleName(), "isExchangeQuotationDialogOn? "+isExchangeQuotationDialogOn);
                    if(isExchangeQuotationDialogOn)
                        return;
                    showExchangeQuotationDialog(intent);
                }
                else if(action.equalsIgnoreCase(INTENT.ELECTRONIC_VOUCHER.toString()))
                {
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(response.equalsIgnoreCase("OK")) {
                        int quantity = 1;

                        try {
                           quantity = Integer.parseInt(quantityET.getText().toString());
                        } catch (NumberFormatException e) {
                            Log.d(TAG, "Set quantity to 1");
                        }

                        selectedAvailableQuantity = selectedAvailableQuantity - quantity;
                        availableQuantityTV.setText("Available Quantity: " + selectedAvailableQuantity);

                        for (VoucherCode vCode: voucherCodes)
                        {
                            if(vCode.getIssuer().equalsIgnoreCase(selectedIssuer))
                            {
                                vCode.getAvailableQuantity().set(denominationPos, String.valueOf(selectedAvailableQuantity));
                                break;
                            }
                        }

                        hideProgressBar("SUCCESSFUL");
                    } else {
                        hideProgressBar(response);
                    }
                }
                else if(action.equalsIgnoreCase(INTENT.NFC_SCANNED.toString()))
                {
                    if(!isWaitingOnNFCScan)
                        return;
                    isWaitingOnNFCScan = false;
                    if(NFCDialog != null && NFCDialog.isShowing())
                        NFCDialog.dismiss();
                    showProgressBar();
                    model.setNfcScanned(true);
                    model.setNFCId(intent.getStringExtra(INTENT.EXTRA_NFC_ID.toString()).toUpperCase(Locale.US));
                    VoucherExchangeQuotation vModel = new VoucherExchangeQuotation(ElectronicVoucherFragment.activity);
                    vModel.setQuantity(quantityET.getText().toString());
                    vModel.setNfcScanned(true);
                    vModel.setNFCId(intent.getStringExtra(INTENT.EXTRA_NFC_ID.toString()).toUpperCase(Locale.US));
                    vModel.setDestinationCode(selectedIssuer);
                    vModel.setDenomination(selectedDenomination);
                    vModel.setWorkingAmount(selectedDenomination);
                    vModel.getConnTaskManager().startBackgroundTask();
                    intent = null;

                }
                else if(action.equalsIgnoreCase(INTENT.CODE_EVD.toString())){
                    decodeVoucherResponse(intent.getStringExtra(INTENT.EXTRA_SERVER_RESPONSE.toString()));
                    dialog.dismiss();
                }

            }
        };

        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NFC_SCANNED.toString()));
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.EXCHANGE_QUOTATION.toString()));
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.ELECTRONIC_VOUCHER.toString()));
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CODE_EVD.toString()));
    }

    private void showExchangeQuotationDialog(Intent intent){
        Log.d(ElectronicVoucherFragment.class.getSimpleName(), "showExchangeQuotationDialog() is called");
        isExchangeQuotationDialogOn = true;
        progressBar.setVisibility(View.INVISIBLE);
        final String name = intent.getStringExtra("NAME");
        final String merchantFess = intent.getStringExtra("MERCHANT_FEES");
        final String amount = intent.getStringExtra("AMOUNT");
        final String totalAmount = intent.getStringExtra(INTENT.EXTRA_TOTAL_AMOUNT.toString());

        Log.d(ElectronicVoucherFragment.class.getSimpleName(), "Before building alert dialog is activity == null ? "+(activity == null));
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
        feesTitleTV.setText("Discount: ");
        TextView totalAmountTV = (TextView)view.findViewById(R.id.Dialog_Exchange_Quotation_TextView_TotalAmount);

        if(isCashInOperation)
            nameTV.setText(getString(R.string.CASH_IN_VOUCHER));
        else
            nameTV.setText(getString(R.string.SELL_VOUCHER));

        if(totalAmount != null && !totalAmount.isEmpty())
            totalAmountTV.setText(totalAmount);
        else
            totalAmountTV.setText(selectedDenomination);

        amountTV.setText(selectedDenomination);
        feesTV.setText(merchantFess);


        Button confirm = (Button) view.findViewById(R.id.Dialog_Exchange_Quotation_Button_Confirm);
        Button cancel = (Button) view.findViewById(R.id.Dialog_Exchange_Quotation_Button_Cancel);

        exchangeQuotationDialog = builder.create();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exchangeQuotationDialog.dismiss();
                showProgressBar();
                model.setQuantity(quantityET.getText().toString());
                model.setIssuer(selectedIssuer);
                model.setDenomination(selectedDenomination);
                model.setWorkingAmount(selectedDenomination);
                model.setOwner(model.getMerchantId());
                if(!model.isNfcScanned())
                    model.setMsisdn(msisdnET.getText().toString());
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

    private void showProgressBar(){
        msisdnET.setEnabled(false);
        quantityET.setEnabled(false);
        submitButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.GONE);
    }

    private void hideProgressBar(String response){
        msisdnET.setEnabled(true);
        quantityET.setEnabled(true);
        submitButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(response);
        resultTV.setVisibility(View.VISIBLE);
    }

    private class IssuerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            ArrayList<String> denominationList = new ArrayList<String>();
            for (String denomination: voucherCodes.get(pos).getDenomination())
                denominationList.add(denomination);

            ArrayAdapter<String> denominationAdapter = new ArrayAdapter<String>(activity, R.layout.spinner_layout, R.id.Spinner_TextView, denominationList);
            denominationSpinner.setAdapter(denominationAdapter);
            denominationSpinner.setOnItemSelectedListener(new DenominationOnItemSelectedListener());
            denominationSpinner.setSelection(0);

            selectedIssuer = issuerSpinner.getSelectedItem().toString();
            selectedDenomination = voucherCodes.get(pos).getDenomination().get(0);
            selectedAvailableQuantity = Integer.valueOf(voucherCodes.get(pos).getAvailableQuantity().get(0));
            availableQuantityTV.setText("Available Quantity: "+selectedAvailableQuantity);
            Log.d(ElectronicVoucherFragment.class.getSimpleName(),"Setting selectedIssuer to: "+selectedIssuer);
            Log.d(ElectronicVoucherFragment.class.getSimpleName(),"Setting selectedDenomination to: "+selectedDenomination);
            Log.d(ElectronicVoucherFragment.class.getSimpleName(),"Setting selectedAvailableQuantity to: "+selectedAvailableQuantity);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }

    }
    private class DenominationOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            for (VoucherCode vCode: voucherCodes)
            {
                if(vCode.getIssuer().equalsIgnoreCase(selectedIssuer))
                {
                    denominationPos = pos;
                    selectedDenomination = vCode.getDenomination().get(pos);
                    selectedAvailableQuantity = Integer.valueOf(vCode.getAvailableQuantity().get(pos));
                    availableQuantityTV.setText("Available Quantity: "+selectedAvailableQuantity);
                    Log.d(ElectronicVoucherFragment.class.getSimpleName(),"Setting selectedDenomination to: "+selectedDenomination);
                    Log.d(ElectronicVoucherFragment.class.getSimpleName(),"Setting selectedAvailableQuantity to: "+selectedAvailableQuantity);
                    break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }

    }

        private void decodeVoucherResponse(String serverResponse){

            List<VoucherCode> evdCodes = new ArrayList<VoucherCode>();
            String[] firstHalf = serverResponse.split("DISPLAYDATA=\\(");
            Log.d("evdCodeTask", serverResponse);
            Log.d("firstHalf", String.valueOf(firstHalf.length));

            if(firstHalf == null || firstHalf.length <= 1 || firstHalf[1].startsWith(")"))
            {// error occurred - no DSP DATA
                voucherCodes = evdCodes;
                Log.d(activity.getClass().getSimpleName(), "Voucher codes was not found");
            }
            String[] secondHalf = firstHalf[1].split("\\)");
            String[] displayData = secondHalf[0].split(",");
            int counter = 0;
            for (String string : displayData) { // MTNEVD|100|22
                String[] items = string.split("\\|");
                if(counter == 0){
                    VoucherCode voucherCode = new VoucherCode();
                    voucherCode.setIssuer(items[0]);
                    voucherCode.getDenomination().add(items[1]);
                    voucherCode.getAvailableQuantity().add(items[2]);
                    voucherCodes.add(voucherCode);
                }
                else{
                    boolean vCodeExists = false;
                    for (VoucherCode vCode: voucherCodes)
                    {
                        if(vCode.getIssuer().equalsIgnoreCase(items[0]))
                        {
                            vCode.getDenomination().add(items[1]);
                            vCode.getAvailableQuantity().add(items[2]);
                            vCodeExists = true;
                            break;
                        }
                    }
                    if(!vCodeExists)
                    {
                        VoucherCode voucherCode = new VoucherCode();
                        voucherCode.setIssuer(items[0]);
                        voucherCode.getDenomination().add(items[1]);
                        voucherCode.getAvailableQuantity().add(items[2]);
                        voucherCodes.add(voucherCode);
                    }
                }
                counter++;
            }
            Log.d(activity.getClass().getSimpleName(), "Number of voucher codes: "+voucherCodes.size());


            ArrayList<String> issuerList = new ArrayList<String>();
            ArrayList<String> denominationList = new ArrayList<String>();

            for (VoucherCode vCode: voucherCodes)
            {
                issuerList.add(vCode.getIssuer());
            }

            if(voucherCodes.size() > 0)
            {
                // Since we're always defaulting the issuer to the first one found in the list, then
                // populate the denominations for that particular issuer as well.
                for (String denomination: voucherCodes.get(0).getDenomination())
                    denominationList.add(denomination);
            }

            ArrayAdapter<String> issuerAdapter = new ArrayAdapter<String>(activity, R.layout.spinner_layout, R.id.Spinner_TextView, issuerList);
            ArrayAdapter<String> denominationAdapter = new ArrayAdapter<String>(activity, R.layout.spinner_layout, R.id.Spinner_TextView, denominationList);

            issuerSpinner.setAdapter(issuerAdapter);
            issuerSpinner.setOnItemSelectedListener(new IssuerOnItemSelectedListener());

            denominationSpinner.setAdapter(denominationAdapter);
            denominationSpinner.setOnItemSelectedListener(new DenominationOnItemSelectedListener());

            if(voucherCodes.size() > 0) {
                issuerSpinner.setSelection(0);
                denominationSpinner.setSelection(0);

                selectedIssuer = issuerList.get(0);
                selectedDenomination = denominationList.get(0);
                selectedAvailableQuantity = Integer.valueOf(voucherCodes.get(0).getAvailableQuantity().get(0));
                availableQuantityTV.setText("Available Quantity: "+selectedAvailableQuantity);
            }
        }


    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        disableNFCScan();
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        enableNFCScan();
    }

}
