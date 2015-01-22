package com.mobilis.android.nfc.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.interfaces.A2ACallback;
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.model.QuickPay;
import com.mobilis.android.nfc.tabsfragments.QuickLinkFragment;
import com.mobilis.android.nfc.util.NFCForegroundUtil;
import com.mobilis.android.nfc.util.SecurePreferences;
import com.mobilis.android.nfc.util.TextModifier;
import com.mobilis.android.nfc.widget.AndroidToAndroidNFCActivity;
import com.mobilis.android.nfc.widget.AndroidToAndroidNFCActivityLowerVersions;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Created by ahmed on 29/07/14.
 */

public class VariableC2MPActivity extends Activity implements View.OnClickListener, A2ACallback{

    final String TAG = VariableC2MPActivity.class.getSimpleName();
    private final int HEIGHT_DP = 190;
    BroadcastReceiver broadcastReceiver;
    Button editButton;
    EditText amountET;
    TextView amountMaskTV;
    TextView amountTV;
    TextView resultTV;
    ProgressBar progressBar;
    RelativeLayout amountRV;
    QuickPay model;
    int quickLinkIndex;
    DecimalFormat df;
    TextModifier textModifier;

    protected static AndroidToAndroidNFCActivity androidToAndroid;
    protected static AndroidToAndroidNFCActivityLowerVersions androidLowerVersion;
    protected NFCForegroundUtil nfcForegroundUtil = null;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent() is called in VariableC2MPActivity");
        Log.d(TAG, "amountTV.getText(): "+amountTV.getText());
        if(amountTV.getText().toString().equalsIgnoreCase("0")||amountTV.getText().toString().equalsIgnoreCase("00.00"))
        {
                    Toast.makeText(VariableC2MPActivity.this, "Amount can't be 0", Toast.LENGTH_LONG).show();
            return;
        }
        if(model == null)
            model = new QuickPay(this);
        Tag intentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        model.setC2MPTransaction(true);
        String nfcId = AndroidToAndroidNFCActivityLowerVersions.onTagDiscovered(intentTag , this).toUpperCase(Locale.US);
        Log.d(TAG, "got nfcId: "+nfcId);

        amountRV.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        model.setNFCId(nfcId);
        model.setWorkingAmount(amountTV.getText().toString());
        model.setPin(null);
        model.getConnTaskManager().startBackgroundTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadCastReceiver();
        adjustRootWidthAndHeight();
        if(model == null)
            model = new QuickPay(this);
        setUpNFC();
        amountTV = (TextView) findViewById(R.id.Activity_VariableAmount_TextView_Amount);
        amountTV.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onPause() {
        if(nfcForegroundUtil != null)
            nfcForegroundUtil.disableForeground();
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_variable_amount);

        quickLinkIndex = Integer.parseInt(getIntent().getStringExtra(INTENT.EXTRA_NUM.toString()));
        Log.d("RINA","quickLinkIndex: "+quickLinkIndex);

        model = new QuickPay(this);
        amountRV = (RelativeLayout) findViewById(R.id.Activity_VariableAmount_RelativeView_AmountPanel);
        editButton = (Button) findViewById(R.id.Activity_VariableAmount_Button_Edit);
        amountET = (EditText) findViewById(R.id.Activity_VariableAmount_EditText_Amount);
        amountMaskTV = (TextView) findViewById(R.id.Activity_VariableAmount_TextView_AmountMask);
        amountTV = (TextView) findViewById(R.id.Activity_VariableAmount_TextView_Amount);
        resultTV = (TextView) findViewById(R.id.Activity_VariableAmount_TextView_ResultTV);
        progressBar = (ProgressBar) findViewById(R.id.Activity_VariableAmount_Progressbar);
        if(getString(R.string.app_name).toUpperCase(Locale.US).contains("ASSOTEL"))
        {
            editButton.setTextColor(R.color.LOGIN_TEXT_COLOR);
        }

        textModifier = new TextModifier(amountET, amountMaskTV);

        editButton.setOnClickListener(this);
        registerBroadCastReceiver();

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
        df = new DecimalFormat("00.00##");
        setAmountTVToSavedVariableAmount();

    }

    private void adjustRootWidthAndHeight() {
        RelativeLayout root = (RelativeLayout)findViewById(R.id.Activity_VariableAmount_LinearLayout_RootView);
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT_DP, getResources().getDisplayMetrics());
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((metrics.widthPixels-20), height);
        params.leftMargin = 15;
        params.rightMargin = 15;
        params.gravity = Gravity.CENTER;
        root.setLayoutParams(params);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        QuickLinkFragment.setTabNameToOriginalName(this);
    }

    private void registerBroadCastReceiver(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(model == null)
                    return;
                if (intent.getAction().equalsIgnoreCase(INTENT.QUICK_PAY.toString())){
                    String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                    if(response.equalsIgnoreCase("OK"))
                        hideProgressBar("SUCCESSFUL");
                    else
                        hideProgressBar(response);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.QUICK_PAY.toString().toString()));
    }

    @Override
    public void onClick(View v) {
        resultTV.setVisibility(View.GONE);
        if(editButton.getText().toString().toUpperCase(Locale.US).equalsIgnoreCase("EDIT")) {
            if (getSavedVariableAmount() != null)
            {
                amountET.setText(getSavedVariableAmount());
            }
            amountTV.setVisibility(View.GONE);
            amountRV.setVisibility(View.VISIBLE);
            amountET.setVisibility(View.VISIBLE);
            AbstractModel.showKeyboard(this, amountET);
            editButton.setText("SAVE");
        }
        else{
            if(textModifier.getAmount().isEmpty() || amountET.getText().toString().isEmpty())
            {
                amountET.setError("Mandatory Field");
                return;
            }
            saveVariableAmount();
            setAmountTVToSavedVariableAmount();
            AbstractModel.hideKeyboard(this, amountET);
            amountTV.setVisibility(View.VISIBLE);
            amountRV.setVisibility(View.GONE);
            editButton.setText("EDIT");
        }
    }

    private void saveVariableAmount() {
        String savedAmount = df.format(Double.parseDouble(textModifier.getAmount().toString()));
        model.getSharedPreference().edit().putString(SecurePreferences.SAVED_VARIABLE_AMOUNT+quickLinkIndex, savedAmount);
    }


    private void setAmountTVToSavedVariableAmount() {
        Log.d("RINA","VariableC2MPActivity SecurePreferences.SAVED_VARIABLE_AMOUNT+quickLinkIndex: "+SecurePreferences.SAVED_VARIABLE_AMOUNT+quickLinkIndex);

        String savedAmount = model.getSharedPreference().getString(SecurePreferences.SAVED_VARIABLE_AMOUNT+quickLinkIndex, null);
        if(savedAmount == null)
        {
            amountTV.setText("0");
        }
        else{
            amountTV.setText(savedAmount);
        }
    }

    private String getSavedVariableAmount(){
        return model.getSharedPreference().getString(SecurePreferences.SAVED_VARIABLE_AMOUNT+quickLinkIndex, null);
    }

    private void setUpNFC(){
        if(NFCForegroundUtil.hasNFCFeature(this)){
            int buildVersion = Build.VERSION.SDK_INT;
            // Check if NFC Feature is enabled
            if(nfcForegroundUtil == null)
                nfcForegroundUtil = new NFCForegroundUtil(this);
            if (!isNFCEnabled())
            {
                Toast.makeText(this, getString(R.string.TOAST_NFC_DISABLED_MSG), Toast.LENGTH_LONG).show();
                if (buildVersion>= 16)
                    startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                else
                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
            }
            else {
                nfcForegroundUtil.enableForeground();
            }
            if(buildVersion > 18){
                // This will support NFC scan read for devices with SDK build >= 18 -- less than 18 is supported in ApplicationActivity.onNewIntent()
                androidToAndroid = new AndroidToAndroidNFCActivity(this, this);
                androidToAndroid.enableReadMode();
            }
            else
                androidLowerVersion = new AndroidToAndroidNFCActivityLowerVersions(this, this);
        }
    }

    private boolean isNFCEnabled() {
        return nfcForegroundUtil.getNfc().isEnabled();
    }


    @Override
    public void finishedA2ACommunication(String nfcId) {
        Log.d(TAG, "finishedA2ACommunication() is called in VariableC2MPActivity");
        Log.d(TAG, "amountTV.getText(): "+amountTV.getText());
        Log.d(TAG, "changing progressBar visibility to Visible");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
                amountTV.setVisibility(View.INVISIBLE);
            }
        });
        if(amountTV.getText().toString().equalsIgnoreCase("0")||amountTV.getText().toString().equalsIgnoreCase("00.00"))
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VariableC2MPActivity.this, "Amount can't be 0", Toast.LENGTH_LONG).show();
                }
            });

            return;
        }
        if(model == null)
            model = new QuickPay(this);
        model.setC2MPTransaction(true);
        model.setNFCId(nfcId);
        model.setWorkingAmount(amountTV.getText().toString());
        model.setPin(null);
        model.getConnTaskManager().startBackgroundTask();
    }

    private void hideProgressBar(String response){

        if(progressBar != null)
            progressBar.setVisibility(View.INVISIBLE);
        if(resultTV != null)
        {
            resultTV.setText(response);
            resultTV.setVisibility(View.VISIBLE);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                amountTV.setVisibility(View.VISIBLE);
                amountRV.setVisibility(View.GONE);
                Log.d(TAG,"amoutRV visibile? "+(amountRV.getVisibility() == View.VISIBLE));
                Log.d(TAG,"amoutET visibile? "+(amountET.getVisibility() == View.VISIBLE));
                Log.d(TAG,"amoutTV visibile? "+(amountTV.getVisibility() == View.VISIBLE));
            }
        });


    }


}
