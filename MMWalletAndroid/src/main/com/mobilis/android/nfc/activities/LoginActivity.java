package com.mobilis.android.nfc.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.AccountBalance;
import com.mobilis.android.nfc.domain.CONST;
import com.mobilis.android.nfc.domain.DstCode;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.domain.QuickLink;
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.model.Login;
import com.mobilis.android.nfc.model.TransferCode;
import com.mobilis.android.nfc.slidemenu.utils.LoginResponseConstants;
import com.mobilis.android.nfc.tracker.MobilisExceptionHandler;
import com.mobilis.android.nfc.util.Constants;
import com.mobilis.android.nfc.util.LockableScrollView;
import com.mobilis.android.nfc.util.SecurePreferences;
import com.mobilis.android.nfc.util.SharedPreferencesException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 3/06/14.
 */
public class LoginActivity extends Activity implements  View.OnClickListener {

    public static SecurePreferences mainSecurePreferences;

    Button button0;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    Button button6;
    Button button7;
    Button button8;
    Button button9;
    Button buttonRegister;
    Button buttonChangeId;
    Button buttonLogin;
    Button buttonCancel;
    TextView textAllPins;
/*    TextView textViewPin1;
    TextView textViewPin2;
    TextView textViewPin3;
    TextView textViewPin4;
    TextView textViewPin5;
    TextView textViewPin6;*/

    private static boolean isUserLoggedIn;
    private boolean gotBalance;
    private boolean gotBalanceHistory;
    private AccountBalance accountBalance;

//    Dialog newPinDialog;
    AbstractModel login;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                SharedPreferences sp = new SecurePreferences(LoginActivity.this);
//                String issue = sp.getString("ISSUE", null);
//                if(issue == null || issue.equalsIgnoreCase("Y")) {
//                    sp.edit().putString("ISSUE", "N").commit();
//                    throw new IntendedException();
//                }
//                else
//                {
//                    sp.edit().putString("ISSUE", "Y").commit();
//                }
//            }
//        }, 2000);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                showNewPinDialog();
//            }
//        }, 2000);

        Constants.internetConnectionLost = false;
        //setupComponents();
        Constants.setApplicationContext(getApplicationContext());
        mainSecurePreferences = new SecurePreferences(this);
        login = new Login(this);
        checkScreenHeight();
    }

    private void checkScreenHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        LockableScrollView scrollView = (LockableScrollView) findViewById(R.id.Activity_Login_ScrollView);
        if(metrics.heightPixels > 400)
            scrollView.setScrollable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MobilisExceptionHandler.register(this);
        Constants.startedQPProcess = false;
        Constants.quickLinks = new ArrayList<QuickLink>();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(INTENT.CLOSE_SOCKET.toString()));

        setupComponents();
        showKeyboardHideProgressBar();
        setUserLoggedIn(false);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(INTENT.TIME_OUT_ERROR.equalsName(action))
                {
                    showErrorBox(intent.getStringExtra(INTENT.EXTRA_ERROR.toString()));
                }
                else if(INTENT.NEW_PIN_REQUIRED.equalsName(action))
                {
                    showKeyboardHideProgressBar();
                    startActivity(new Intent(LoginActivity.this, ChangePinActivity.class));
                }
                else if(INTENT.GOT_BALANCE.equalsName(action) && !isUserLoggedIn() && !gotBalance)
                {
                    gotBalance = true;
                    accountBalance = (AccountBalance) intent.getSerializableExtra(INTENT.EXTRA_ACCOUNT_BALANCE.toString());
                    if(gotBalance && gotBalanceHistory){
                        gotBalance = false;
                        gotBalanceHistory = false;
                        setUserLoggedIn(true);
                        overridePendingTransition(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
                        startActivity(new Intent(LoginActivity.this, ApplicationActivity.class).putExtra(INTENT.EXTRA_ACCOUNT_BALANCE.toString(), accountBalance));
                    }
                }
                else if(INTENT.GOT_BALANCE_TRANSACTIONS.equalsName(action) && !isUserLoggedIn())
                {
                    gotBalanceHistory = true;
                    if(gotBalance && gotBalanceHistory){
                        gotBalance = false;
                        gotBalanceHistory = false;
                        setUserLoggedIn(true);
                        overridePendingTransition(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
                        startActivity(new Intent(LoginActivity.this, ApplicationActivity.class).putExtra(INTENT.EXTRA_ACCOUNT_BALANCE.toString(), accountBalance));
                    }
                }
                else if(INTENT.GOT_QUICK_LINKS_PROCEED_TO_LOGIN.toString().equalsIgnoreCase(action))
                {
                    setUserLoggedIn(true);
                    overridePendingTransition(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
                    startActivity(new Intent(LoginActivity.this, ApplicationActivity.class).putExtra(INTENT.EXTRA_ACCOUNT_BALANCE.toString(), accountBalance));
                }
                else if (INTENT.LOGIN_SHOW_KEYPAD.equalsName(action)){
                    showKeyboardHideProgressBar();
                }
                else if (INTENT.SERVER_COMM_TIME_OUT.equalsName(action))
                {
                    showErrorBox(intent.getStringExtra(INTENT.EXTRA_ERROR.toString()));
                }
                else if (INTENT.QUICK_LINKS_SETUP.equalsName(action))
                {
                    if(Constants.quickLinks.size() != 0)
                        return;
                    BuildQuickLinksTask qlTask = new BuildQuickLinksTask();
                    qlTask.execute(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                }
                else if(action.equalsIgnoreCase(INTENT.CODE_BILL_PAYMENT.toString())){
                    BillPaymentCodeTask task = new BillPaymentCodeTask(intent.getStringExtra(INTENT.EXTRA_SERVER_RESPONSE.toString()));
                    task.execute();
                }
                else if(action.equalsIgnoreCase(INTENT.CODE_AIRTIME_TOP_UP.toString())){
                    TopUpAirtimeCodeTask task = new TopUpAirtimeCodeTask(intent.getStringExtra(INTENT.EXTRA_SERVER_RESPONSE.toString()));
                    task.execute();
                }
                else if(action.equalsIgnoreCase(INTENT.CODE_CREDIT_TOP_UP.toString())){
                    TopUpCreditCodeTask task = new TopUpCreditCodeTask(intent.getStringExtra(INTENT.EXTRA_SERVER_RESPONSE.toString()));
                    task.execute();
                }
                else if(action.equalsIgnoreCase(INTENT.CODE_UTILITY.toString())){
                    UtilityCodeTask task = new UtilityCodeTask(intent.getStringExtra(INTENT.EXTRA_SERVER_RESPONSE.toString()));
                    task.execute();
                }
                else if(action.equalsIgnoreCase(INTENT.CODE_BANK.toString())){
                    BankCodeTask task = new BankCodeTask(intent.getStringExtra(INTENT.EXTRA_SERVER_RESPONSE.toString()));
                    task.execute();
                }
                else if(action.equalsIgnoreCase(INTENT.INTERNET_WIFI_SIGNAL_WEAK.toString())){
                    Toast.makeText(context, CONST.WIFI_WEAK.toString(), Toast.LENGTH_SHORT).show();
                }
                else if(action.equalsIgnoreCase(INTENT.INTERNET_GSM_SIGNAL.toString())){
                    Toast.makeText(context, CONST.MOBILE_DATA_WEAK.toString(), Toast.LENGTH_SHORT).show();
                }
                else if(action.equalsIgnoreCase(INTENT.INTERNET_NO_SIGNAL.toString())){
                    Toast.makeText(context, CONST.INTERNET_CONN_LOST.toString(), Toast.LENGTH_SHORT).show();
                }
                else if(action.equalsIgnoreCase(INTENT.INTERNET_REGAINED.toString())){
                    Toast.makeText(context, intent.getStringExtra(INTENT.EXTRA_INTERNET.toString()), Toast.LENGTH_SHORT).show();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.GOT_BALANCE.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.GOT_BALANCE_TRANSACTIONS.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.LOGIN_SHOW_KEYPAD.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.SERVER_COMM_TIME_OUT.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.QUICK_LINKS_SETUP.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CODE_AIRTIME_TOP_UP.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CODE_BANK.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CODE_BILL_PAYMENT.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CODE_CREDIT_TOP_UP.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CODE_UTILITY.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.INTERNET_WIFI_SIGNAL_WEAK.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.INTERNET_GSM_SIGNAL.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.INTERNET_NO_SIGNAL.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.INTERNET_REGAINED.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NEW_PIN_REQUIRED.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NEW_PIN_RESULT.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.GOT_QUICK_LINKS_PROCEED_TO_LOGIN.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.TIME_OUT_ERROR.toString()));
    }

    private void startLoggingProcess(){
        ApplicationActivity.loginClientPin = textAllPins.getText().toString();
        if(!AbstractModel.isNetworkAvailable(this))
        {
            showErrorBox(getString(R.string.NO_INTERNET_CONNECTION));
            return;
        }
        hideKeyboardShowProgressBar();
        try {
            login.saveConnectionDetails();
        }
        catch (SharedPreferencesException e) {
            e.printStackTrace();
            return ;
        }
        login.getConnTaskManager().startBackgroundTask();

    }

    @Override
    public void onClick(View view) {

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(50);

        if(view.getId() == R.id.Activity_Login_Button_Register){
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        }
        else if (view.getId() == R.id.Activity_Login_Button_PhoneNumber){
            showMerchantIdBox();
        }else if (view.getId() == R.id.Activity_Login_Login){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startLoggingProcess();
                    }
                }, 50);
        }else if (view.getId() == R.id.Activity_Login_Cancel){
            showKeyboardHideProgressBar();
        }
        else{
            addTextToPinBox((Button)view);
        }
    }
    private void showLogin(){
        buttonLogin.setVisibility(View.VISIBLE);
        buttonCancel.setVisibility(View.VISIBLE);
        buttonRegister.setVisibility(View.GONE);
        buttonChangeId.setVisibility(View.GONE);
    }

    private void hideLogin(){
        buttonLogin.setVisibility(View.GONE);
        buttonCancel.setVisibility(View.GONE);
        buttonRegister.setVisibility(View.VISIBLE);
        buttonChangeId.setVisibility(View.VISIBLE);
    }
    private void addTextToPinBox(Button button){
        textAllPins.setText(textAllPins.getText().toString()+button.getText().toString());
        if(textAllPins.getText().length()>0){
            showLogin();
        }else{
            hideLogin();
        }
    }

    private void setupComponents() {
        button0 = (Button) findViewById(R.id.Activity_Login_Button_0);
        button1 = (Button) findViewById(R.id.Activity_Login_Button_1);
        button2 = (Button) findViewById(R.id.Activity_Login_Button_2);
        button3 = (Button) findViewById(R.id.Activity_Login_Button_3);
        button4 = (Button) findViewById(R.id.Activity_Login_Button_4);
        button5 = (Button) findViewById(R.id.Activity_Login_Button_5);
        button6 = (Button) findViewById(R.id.Activity_Login_Button_6);
        button7 = (Button) findViewById(R.id.Activity_Login_Button_7);
        button8 = (Button) findViewById(R.id.Activity_Login_Button_8);
        button9 = (Button) findViewById(R.id.Activity_Login_Button_9);
        buttonRegister = (Button) findViewById(R.id.Activity_Login_Button_Register);
        buttonChangeId = (Button) findViewById(R.id.Activity_Login_Button_PhoneNumber);
        buttonLogin= (Button) findViewById(R.id.Activity_Login_Login);
        buttonCancel= (Button) findViewById(R.id.Activity_Login_Cancel);
        textAllPins = (TextView) findViewById(R.id.activity_login_all_pins);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
        button6.setOnClickListener(this);
        button7.setOnClickListener(this);
        button8.setOnClickListener(this);
        button9.setOnClickListener(this);
        button0.setOnClickListener(this);
        buttonRegister.setOnClickListener(this);
        buttonChangeId.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    private void hideKeyboardShowProgressBar(){
        TableLayout keypad = (TableLayout) findViewById(R.id.Login_TableLayout);
        LinearLayout progress = (LinearLayout) findViewById(R.id.LinearLayout_ProgressBar);
        LinearLayout error = (LinearLayout) findViewById(R.id.Activity_Login_LinearLayout_ErrorBox);
        error.setVisibility(View.GONE);
        keypad.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }
    private void showKeyboardHideProgressBar(){
        textAllPins.setText("");

        TableLayout keypad = (TableLayout) findViewById(R.id.Login_TableLayout);
        LinearLayout progress = (LinearLayout) findViewById(R.id.LinearLayout_ProgressBar);
        keypad.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);

        buttonRegister = (Button) findViewById(R.id.Activity_Login_Button_Register);
        SpannableString content = new SpannableString(getString(R.string.LOGIN_MSG_REGISTER));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        buttonRegister.setText(content);


        buttonChangeId = (Button) findViewById(R.id.Activity_Login_Button_PhoneNumber);
        SpannableString content2 = new SpannableString(getString(R.string.LOGIN_MSG_PHONE_NUMBER));
        content2.setSpan(new UnderlineSpan(), 0, content2.length(), 0);
        buttonChangeId.setText(content2);

        content2 = new SpannableString(getString(R.string.LOGIN_MSG_LOGIN));
        content2.setSpan(new UnderlineSpan(), 0, content2.length(), 0);
        buttonLogin.setText(content2);
        content2 = new SpannableString(getString(R.string.LOGIN_MSG_CANCEL));
        content2.setSpan(new UnderlineSpan(), 0, content2.length(), 0);
        buttonCancel.setText(content2);

        LinearLayout error = (LinearLayout) findViewById(R.id.Activity_Login_LinearLayout_ErrorBox);
        error.setVisibility(View.GONE);
        hideLogin();
    }

    private void showMerchantIdBox(){
        final Dialog merchantIdBoxDialog = new Dialog(this);
        merchantIdBoxDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        merchantIdBoxDialog.setContentView(R.layout.dialog_merchantid_box);
        merchantIdBoxDialog.setCanceledOnTouchOutside(false);
        final Button button = (Button) merchantIdBoxDialog.findViewById(R.id.Dialog_merchantIdBox_Button_Submit);
        final ProgressBar progressBar = (ProgressBar) merchantIdBoxDialog.findViewById(R.id.Dialog_merchantIdBox_Progressbar);
        final EditText editText = (EditText) merchantIdBoxDialog.findViewById(R.id.Dialog_merchantIdBox_EditText_Id);
        final TextView textView = (TextView) merchantIdBoxDialog.findViewById(R.id.Dialog_merchantIdBox_TextView_Id);
        progressBar.setVisibility(View.INVISIBLE);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editText.getText().length() > 0)
                    textView.setVisibility(View.GONE);
                else
                    textView.setVisibility(View.VISIBLE);
            }
        });
        editText.requestFocus();
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        },30);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(editText.getText().toString().isEmpty())
                {
                    merchantIdBoxDialog.dismiss();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                login.saveMerchantId(editText.getText().toString());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(v.getContext(), "saved "+editText.getText().toString(), Toast.LENGTH_SHORT).show();
                        merchantIdBoxDialog.dismiss();
                    }
                }, 750);
            }
        });
        merchantIdBoxDialog.show();
    }

    private void showErrorBox(String message){
        textAllPins.setText("");
        TableLayout keypad = (TableLayout) findViewById(R.id.Login_TableLayout);
        LinearLayout progress = (LinearLayout) findViewById(R.id.LinearLayout_ProgressBar);
        LinearLayout error = (LinearLayout) findViewById(R.id.Activity_Login_LinearLayout_ErrorBox);
        TextView textView = (TextView)findViewById(R.id.Activity_Login_TextView_ErrorBox);
        textView.setText(message);
        Button button = (Button) findViewById(R.id.Activity_Login_Button_ErrorButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showKeyboardHideProgressBar();
            }
        });
        error.setVisibility(View.VISIBLE);
        keypad.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    public static void setUserLoggedIn(boolean status){
        isUserLoggedIn = status;
    }

    public static boolean isUserLoggedIn(){
        return isUserLoggedIn;
    }

    private static String getName(String input){
        String[] openBracketArray = input.split("\\(");
        String[] closedBracketArray = openBracketArray[1].split("\\)");
        String[] nameArray = closedBracketArray[0].split("Name=");
        if(nameArray.length <= 1)
            return null;
        String[] commaArray = nameArray[1].split(",");
        return commaArray[0];
    }
    private static String getAmount(String input){

        String[] openBracketArray = input.split("\\(");
        String[] closedBracketArray = openBracketArray[1].split("\\)");
        String[] nameArray = closedBracketArray[0].split("Amount=");
        if(nameArray.length <= 1)
            return null;
        String[] commaArray = nameArray[1].split(",");
        return commaArray[0];
    }
    private static String getType(String input){
        String[] openBracketArray = input.split("\\(");
        String[] closedBracketArray = openBracketArray[1].split("\\)");
        String[] nameArray = closedBracketArray[0].split("Type=");
        String[] commaArray = nameArray[1].split(",");
        return commaArray[0];

    }

    private static String getDestination(String input){
        String[] openBracketArray = input.split("\\(");
        String[] closedBracketArray = openBracketArray[1].split("\\)");
        String[] nameArray = closedBracketArray[0].split("Destination=");
        if(nameArray.length <= 1)
            return "";
        String[] commaArray = nameArray[1].split(",");
        return commaArray[0].replace(")", "");
    }
    private static String getDstCode(String input){
        String[] openBracketArray = input.split("\\(");
        if(openBracketArray.length > 2){
            for (int i = 0; i < openBracketArray.length; i++) {
                if(openBracketArray[i].split("\\|").length > 1)
                    return openBracketArray[i];
            }
            return "";
        }
        String[] closedBracketArray = openBracketArray[1].split("\\)");
        String[] nameArray = closedBracketArray[0].split("Dstcode=");
        String[] nameArray2 = closedBracketArray[0].split("DstCode=");
        String[] commaArray = null;
        if(nameArray.length <= 1 && nameArray2.length <= 1)
            return "";
        else if(nameArray.length > 1)
            commaArray = nameArray[1].split(",");
        else
            commaArray = nameArray2[1].split(",");
        return commaArray[0].replace(")", "");
    }

    private class BuildQuickLinksTask extends  AsyncTask<String, Void, ArrayList<QuickLink>>{
        @Override
        protected ArrayList<QuickLink> doInBackground(String... params) {
            ArrayList<QuickLink> qls = new ArrayList<QuickLink>();
            String[] ql = params[0].split("QuickLinks=");
            if (ql.length <= 1)
                return qls;
            String[] string = ql[1].split("Count=");
            if(string.length < 2)
                return qls;

            String QLCountArray[] = string[1].split(",");
            int count = Integer.parseInt(QLCountArray[0]);

            count++;
            for (int i = 1; i < count; i++) {
                QuickLink quickLink = new QuickLink();
                String delimiter = "Link"+i+"=";
                String[] temp2 = ql[1].split(delimiter);
                String[] temp3 = temp2[1].split("\\)");
                quickLink.setName(getName(temp3[0]));
                quickLink.setAmount(getAmount(temp3[0]));
                quickLink.setType(getType(temp3[0]));
                quickLink.setDestination(getDestination(temp3[0]));
                String dstCode = getDstCode(temp3[0]);
                String[] commaArray = dstCode.split(",");
                if(commaArray.length <= 1)
                    quickLink.setSingleDstCode(dstCode);
                else{
                    for (int j = 0; j < commaArray.length; j++) {
                        String[] det = commaArray[j].split("\\|");
                        if(det.length > 2){
                            DstCode dc = new DstCode();
                            dc.setCode(det[0]);
                            dc.setAmount(det[1]);
                            dc.setDescription(det[2]);
                            quickLink.getDstCodes().add(dc);
                        }
                    }
                }
                qls.add(quickLink);
            }
            return qls;
        }
        @Override
        protected void onPostExecute(ArrayList<QuickLink> quickLinks) {
            super.onPostExecute(quickLinks);
            Constants.quickLinks = quickLinks;
            if (!LoginResponseConstants.accountTabAvailable) {
                LocalBroadcastManager.getInstance(LoginActivity.this).sendBroadcast(new Intent(INTENT.GOT_QUICK_LINKS_PROCEED_TO_LOGIN.toString()));
            }
        }
    }

    private class BillPaymentCodeTask extends AsyncTask<Void, Void, List<TransferCode>>{
        String serverResponse;
        public BillPaymentCodeTask(String serverResponse){
            this.serverResponse = serverResponse;
        }
        @Override
        protected List<TransferCode> doInBackground(Void... params) {
            List<TransferCode> billPaymentCodes = new ArrayList<TransferCode>();
            String[] firstHalf = serverResponse.split("\\(");
            if(firstHalf.length <= 1)
            {// error occurred - no DSP DATA
                return billPaymentCodes;
            }
            String[] secondHalf = firstHalf[1].split("\\)");
            String[] displayData = secondHalf[0].split(",");
            for (String string : displayData) {
                String[] items = string.split("\\|");
                TransferCode bpc = new TransferCode();

                bpc.setCode(items[0]);
                bpc.setAmount(items[1]);
                bpc.setDescription(items[2]);
                billPaymentCodes.add(bpc);
            }
            return billPaymentCodes;
        }

        @Override
        protected void onPostExecute(List<TransferCode> transferCodes) {
            super.onPostExecute(transferCodes);
            TransferCode.billPaymentCodes = transferCodes;
        }
    }

    private class TopUpAirtimeCodeTask extends AsyncTask<Void, Void, List<TransferCode>>{
        String serverResponse;
        public TopUpAirtimeCodeTask(String serverResponse){
            this.serverResponse = serverResponse;
        }
        @Override
        protected List<TransferCode> doInBackground(Void... params) {
            List<TransferCode> topUpAirtimeCodes = new ArrayList<TransferCode>();
            String[] firstHalf = serverResponse.split("DISPLAYDATA=\\(");
            if(firstHalf.length <= 1)
            {// error occurred - no DSP DATA
                return topUpAirtimeCodes;
            }
            String[] secondHalf = firstHalf[1].split("=");
            String[] details = secondHalf[0].split(",");
            for (int i = 0; i < details.length; i++) {
                String[] codes = details[i].split("\\|");
                if(codes.length > 1)
                {
                    TransferCode bpc = new TransferCode();
                    bpc.setCode(codes[0]);
                    bpc.setAmount(codes[1]);
                    String desc = codes[2];
                    if(desc.contains("("))
                        desc = desc.replace(")", "");
                    bpc.setDescription(desc);
                    topUpAirtimeCodes.add(bpc);
                }
            }
            String lastItem = topUpAirtimeCodes.get(topUpAirtimeCodes.size()-1).getDescription();
            if(lastItem.endsWith(")"))
                topUpAirtimeCodes.get(topUpAirtimeCodes.size()-1).setDescription(lastItem.replace(")",""));
            return topUpAirtimeCodes;
        }
        @Override
        protected void onPostExecute(List<TransferCode> transferCodes) {
            super.onPostExecute(transferCodes);
            TransferCode.topupAirtimeCodes = transferCodes;
        }
    }

    private class TopUpCreditCodeTask extends AsyncTask<Void, Void, List<TransferCode>>{
        String serverResponse;
        public TopUpCreditCodeTask(String serverResponse){
            this.serverResponse = serverResponse;
        }
        @Override
        protected List<TransferCode> doInBackground(Void... params) {

            List<TransferCode> topUpCreditCodes = new ArrayList<TransferCode>();
            String[] firstHalf = serverResponse.split("\\(");
            if(firstHalf.length <= 1)
            {// error occurred - no DSP DATA
                return topUpCreditCodes;
            }
            String[] secondHalf = firstHalf[1].split("\\)");
            String[] displayData = secondHalf[0].split(",");
            for (String string : displayData) {
                String[] items = string.split("\\|");
                TransferCode bpc = new TransferCode();
                bpc.setCode(items[0]);
                bpc.setAmount(items[1]);
                bpc.setDescription(items[2]);
                topUpCreditCodes.add(bpc);
            }
            return topUpCreditCodes;
        }
        @Override
        protected void onPostExecute(List<TransferCode> transferCodes) {
            super.onPostExecute(transferCodes);
            TransferCode.topupcreditCodes = transferCodes;
        }
    }

    private class BankCodeTask extends AsyncTask<Void, Void, ArrayList<TransferCode>>{
        String serverResponse;
        public BankCodeTask(String serverResponse){
            this.serverResponse = serverResponse;
        }
        @Override
        protected ArrayList<TransferCode> doInBackground(Void... params) {
            ArrayList<TransferCode> bankCodes = new ArrayList<TransferCode>();
            String[] firstHalf = serverResponse.split("\\(");
            if(firstHalf.length <= 1)
            {// error occurred - no DSP DATA
                return bankCodes;
            }
            String[] secondHalf = firstHalf[1].split("\\)");
            String[] displayData = secondHalf[0].split(",");
            for (String string : displayData) {
                String[] items = string.split("\\|");
                TransferCode bpc = new TransferCode();
                bpc.setCode(items[0]);
                bpc.setAmount(items[1]);
                bpc.setDescription(items[2]);
                bankCodes.add(bpc);
            }
            return bankCodes;
        }
        @Override
        protected void onPostExecute(ArrayList<TransferCode> transferCodes) {
            super.onPostExecute(transferCodes);
            TransferCode.bankCodes = transferCodes;
        }
    }

    private class UtilityCodeTask extends AsyncTask<Void, Void, List<TransferCode>>{
        String serverResponse;
        public UtilityCodeTask(String serverResponse){
            this.serverResponse = serverResponse;
        }
        @Override
        protected List<TransferCode> doInBackground(Void... params) {
            List<TransferCode> utilityCodes = new ArrayList<TransferCode>();
            String[] firstHalf = serverResponse.split("\\(");
            if(firstHalf.length <= 1)
            {// error occurred - no DSP DATA
                return utilityCodes;
            }
            String[] secondHalf = firstHalf[1].split("\\)");
            String[] displayData = secondHalf[0].split(",");
            for (String string : displayData) {
                String[] items = string.split("\\|");
                TransferCode bpc = new TransferCode();

                bpc.setCode(items[0]);
                bpc.setAmount(items[1]);
                bpc.setDescription(items[2]);
                utilityCodes.add(bpc);
            }
            return utilityCodes;
        }
        @Override
        protected void onPostExecute(List<TransferCode> transferCodes) {
            super.onPostExecute(transferCodes);
            TransferCode.utilityCodes = transferCodes;
        }
    }

    //    DISPLAYDATA=(MTNEVD|100|22,MTNEVD|200|5,AitelEVD|500|45)
//    The DISPLAYDATA is basically going to contain the ISSUER|DENOMINATION|NUMBER Available.



}
