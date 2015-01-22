package com.mobilis.android.nfc.tabsfragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.activities.CustomerRegistrationActivity;
import com.mobilis.android.nfc.activities.VariableC2MPActivity;
import com.mobilis.android.nfc.domain.DstCode;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.domain.QuickLink;
import com.mobilis.android.nfc.fragments.NFCFragment;
import com.mobilis.android.nfc.fragments.TagRegistrationUtil;
import com.mobilis.android.nfc.interfaces.A2ACallback;
import com.mobilis.android.nfc.model.Customer;
import com.mobilis.android.nfc.model.CustomerUpdate;
import com.mobilis.android.nfc.model.Login;
import com.mobilis.android.nfc.model.QuickPay;
import com.mobilis.android.nfc.util.Constants;
import com.mobilis.android.nfc.util.CustomerLookupUtil;
import com.mobilis.android.nfc.util.QuickLinkSpinnerOnItemSelectedListener;
import com.mobilis.android.nfc.util.SecurePreferences;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ahmed on 6/06/14.
 */
public class QuickLinkFragment extends NFCFragment implements CustomerLookupUtil.Callable, A2ACallback, DialogInterface.OnDismissListener,DialogInterface.OnShowListener {

    private final String TAG = QuickLinkFragment.class.getSimpleName();
    LinearLayout button1;
    LinearLayout button2;
    LinearLayout button3;
    LinearLayout button4;
    LinearLayout button5;
    LinearLayout customerFoundLinearLayout;
    BroadcastReceiver broadcastReceiver;
    Dialog NFCDialog;
    Dialog qpDialog;
    Dialog registrationDialog;

    EditText amountET;
    EditText phoneNumberET;
    EditText CCContactNumberET;
    EditText CCGivenNameET;
    EditText CCSurNameET;
    EditText CCDOBET;

    Button payButton;
    Button CCLookupButton;
    Button CCUpdateButton;
    Button registerCustomerButton;

    Spinner transactionsSpinner;
    Spinner msisdnSpinner;
    ProgressBar progressBar;
    ProgressBar CCProgressBar ;
    TextView resultTV;
    TextView CCContactNumberTV;
    View rootView;

    ArrayList<Customer> customers;
    CustomerLookupUtil customerUtil;
    String destination;
    String destinationCode;
    String type;
    public static boolean isTagRegistrationDialogOn;
    boolean needToAdjustButton5Dimentions;

    public QuickPay getModel() {
        return model;
    }
    public void setModel(QuickPay model) {
        this.model = model;
    }

    private QuickPay model;
    private static QuickPay model2;
    @Override
    public void finishedA2ACommunication(String scannedId) {


        // TODO - Test scan nfc and see which of logs print this one or the one in ApplicationActivity
//        SEE ABOVE
        Log.d(TAG,"QuickLinkFragment finishedA2ACommunication() is called");
        Log.d(TAG,"isTagRegistrationDialogOn? "+isTagRegistrationDialogOn);
        if(isTagRegistrationDialogOn){
            isTagRegistrationDialogOn = false;
            Intent intent = new Intent(INTENT.DIALOG_TAG_REG_NFC_SCANNED.toString());
            intent.putExtra(INTENT.EXTRA_NFC_ID.toString(), scannedId);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            return;
        }
        Log.d(TAG,"NFCDialog == null? "+(NFCDialog == null));
        if(NFCDialog != null)
            Log.d(TAG,"!NFCDialog.isShowing()? "+(!NFCDialog.isShowing()));
//        if(NFCDialog == null || !NFCDialog.isShowing())
//            return;

        Log.d(TAG,"Sending NFC_SCANNED intent");

        Intent intent = new Intent(INTENT.NFC_SCANNED.toString());
        intent.putExtra(INTENT.EXTRA_NFC_ID.toString(), scannedId);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
//        broadcastReceiver = null;
//        if(customerUtil!= null)
//            customerUtil.unregisterReceiver();
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ApplicationActivity.loginClientId = null;
    }


//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//      }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
        if(customerUtil!= null)
            customerUtil.unregisterReceiver();

    }

    @Override
    public void onResume() {
        super.onResume();
        needToAdjustButton5Dimentions = true;
        writeNameOnQL(rootView);
    }

    private void setupBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "receive intent in QuickLinkFragment");
                Log.d(TAG, "!isAdded()? "+(isAdded()));
                Log.d(TAG, "model == null? "+(model == null));
                Log.d(TAG, "intent.action: "+intent.getAction());
                if (model == null || !isAdded()) {
                    if(intent.getAction().equalsIgnoreCase(INTENT.NFC_SCANNED.toString())) {
                        Log.d(TAG, "received NFC_SCANNED INTENT");
                        Log.d(TAG, "isTagRegistrationDialogOn ? " + isTagRegistrationDialogOn);

                        if (isTagRegistrationDialogOn) {
                            isTagRegistrationDialogOn = false;
                            Log.d(TAG, "sending DIALOG_TAG_REG_NFC_SCANNED now...");
                            Intent newIntent = new Intent(INTENT.DIALOG_TAG_REG_NFC_SCANNED.toString());
                            newIntent.putExtra(INTENT.EXTRA_NFC_ID.toString(), intent.getStringExtra(INTENT.EXTRA_NFC_ID.toString()).toUpperCase(Locale.US));
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(newIntent);
                            return;
                        }
                    }
                    else
                        return;
                }
                else{
                    if(intent.getAction().equalsIgnoreCase(INTENT.NFC_DIALOG_ON.toString())){
                        isTagRegistrationDialogOn = true;
                    }
                    else if(intent.getAction().equalsIgnoreCase(INTENT.NFC_SCANNED.toString()))
                    {
                        Log.d(TAG, "received NFC_SCANNED INTENT");
                        Log.d(TAG, "isTagRegistrationDialogOn ? "+isTagRegistrationDialogOn);

                        if(isTagRegistrationDialogOn){
                            isTagRegistrationDialogOn = false;

                            Log.d(TAG, "sending DIALOG_TAG_REG_NFC_SCANNED now...");
                            Intent newIntent = new Intent(INTENT.DIALOG_TAG_REG_NFC_SCANNED.toString());
                            newIntent.putExtra(INTENT.EXTRA_NFC_ID.toString(), intent.getStringExtra(INTENT.EXTRA_NFC_ID.toString()).toUpperCase(Locale.US));
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(newIntent);
                            return;
                        }
                        Log.d(TAG, "Receive NFC_SCANNED intent");
                        Log.d(TAG, "model != null? "+(model != null));
                        Log.d(TAG, "model.isC2MPTransaction()? "+model.isC2MPTransaction());
                        if(model != null && model.isC2MPTransaction())
                        {
                            showProgressBar();
                            model.setNFCId(intent.getStringExtra(INTENT.EXTRA_NFC_ID.toString()).toUpperCase(Locale.US));

                            if (getResources().getBoolean(R.bool.PAY_PIN_REQUIRED)) {
                                showPinDialog();
                            } else {
                                model.setPin(null);
                                model.getConnTaskManager().startBackgroundTask();
                            }
                            disableNFCScan();

                        }
                        else if(NFCDialog != null && NFCDialog.isShowing()){
                            NFCDialog.dismiss();
                            showProgressBar();
                            model.setNfcScanned(true);
                            model.setNFCId(intent.getStringExtra(INTENT.EXTRA_NFC_ID.toString()).toUpperCase(Locale.US));
                            model.setDestination(destination);
                            model.setDestinationCode(destinationCode);
                            model.setWorkingAmount(amountET.getText().toString());
                            model.setPaymentType(type);

                        }
                    }
                    else if (intent.getAction().equalsIgnoreCase(INTENT.QUICK_PAY.toString())){
                        String response = model.getMessageFromServerResponse(intent.getStringExtra(INTENT.EXTRA_RESPONSE.toString()));
                        if(response.equalsIgnoreCase("OK"))
                            hideProgressBar(model.getResString(R.string.SUCCESS_MSG));
                        else
                            hideProgressBar(response);
                    }
                    else if (intent.getAction().equalsIgnoreCase(INTENT.DESTINATION_CHANGED.toString())){
                        destinationCode= intent.getStringExtra(INTENT.EXTRA_DESTINATION.toString());
                    }
                }
            }
        };

        if(broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.DESTINATION_CHANGED.toString()));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.QUICK_PAY.toString()));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NFC_SCANNED.toString()));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NFC_DIALOG_ON.toString()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_quickpay, container, false);
        LinearLayout layout1 = (LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout1);
        LinearLayout layout2 = (LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout2);
        LinearLayout layout3 = (LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout3);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        params.weight = 1;
        layout1.setLayoutParams(params);
        layout2.setLayoutParams(params);
        layout3.setLayoutParams(params);

        button1 =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout1_Button1);
        button2 =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout1_Button2);
        button3 =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout2_Button1);
        button4 =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout2_Button2);
        button5 =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout3_Button1);

        writeNameOnQL(rootView);
        showHideActiveQL(rootView);
        adjustQLHeightAndWidth();
        assignButtonImages(rootView);
        setUpQListeners();

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setupBroadcastReceiver();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**This is to adjust the fifth button height**/
        final ViewTreeObserver vto = button1.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(Constants.quickLinks.size() > 4)
                {
                    if(button1.getHeight() != 0 && button1.getWidth() != 0 && needToAdjustButton5Dimentions)
                    {
                        needToAdjustButton5Dimentions = false;
                        float height = button1.getHeight();
                        LinearLayout layout3 = (LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout3);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(height));
                        params.weight = 1;
                        layout3.setLayoutParams(params);
                        layout3.setMinimumHeight(Math.round(height));
                        layout3.invalidate();
//                        Log.d(TAG,"height: "+Math.round(height));
                    }
                }
            }
        });

    }

    private void setUpQListeners() {
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQLDialogWhenButtonCLicked(1);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQLDialogWhenButtonCLicked(2);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQLDialogWhenButtonCLicked(3);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQLDialogWhenButtonCLicked(4);
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQLDialogWhenButtonCLicked(5);
            }
        });
    }

    private void adjustQLHeightAndWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float px = 0;
        if(metrics.heightPixels > 600)
            px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, metrics);
        else
            px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, metrics) - 15;
        int heightToSubtract = (int)px;

        int extraSpace = 1;
        if (metrics.density > 1)
            extraSpace = 0;

        int buttonHeight = (metrics.heightPixels - heightToSubtract)/2 + extraSpace;
        int buttonWidth = metrics.widthPixels / 2;
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(buttonWidth, buttonHeight);
        button1.setLayoutParams(buttonParams);
        button2.setLayoutParams(buttonParams);
        button3.setLayoutParams(buttonParams);
        button4.setLayoutParams(buttonParams);
    }

    private void showHideActiveQL(View rootView) {
        LinearLayout button = null;
        switch (Constants.quickLinks.size()){
            case 1:
                button =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout1_Button2);
                button.setVisibility(View.INVISIBLE);
                button =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout2_Button1);
                button.setVisibility(View.INVISIBLE);
                button =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout2_Button2);
                button.setVisibility(View.INVISIBLE);
                break;
            case 2:
                button =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout2_Button1);
                button.setVisibility(View.INVISIBLE);
                button =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout2_Button2);
                button.setVisibility(View.INVISIBLE);
                break;
            case 3:
                button =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout2_Button2);
                button.setVisibility(View.INVISIBLE);
                break;
            case 5:
                LinearLayout layout3 = (LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout3);
                layout3.setVisibility(View.VISIBLE);
                button =(LinearLayout)rootView.findViewById(R.id.Fragment_QuickPay_LinearLayout_layout3_Button1);
                button.setVisibility(View.VISIBLE);
        }
    }

    private void assignButtonImages(View rootView) {
        int counter = 1;
        for (QuickLink quickLink: Constants.quickLinks){
            ImageView imageView = null;
            switch (counter){
                case 1:
                    imageView =(ImageView)rootView.findViewById(R.id.Fragment_QuickPay_ImageView_layout1_imageView1);
                    break;
                case 2:
                    imageView =(ImageView)rootView.findViewById(R.id.Fragment_QuickPay_ImageView_layout1_imageView2);
                    break;
                case 3:
                    imageView =(ImageView)rootView.findViewById(R.id.Fragment_QuickPay_ImageView_layout2_imageView1);
                    break;
                case 4:
                    imageView =(ImageView)rootView.findViewById(R.id.Fragment_QuickPay_ImageView_layout2_imageView2);
                    break;
                case 5:
                    imageView =(ImageView)rootView.findViewById(R.id.Fragment_QuickPay_ImageView_layout3_imageView1);
            }
            counter++;
            if(quickLink.getType().equalsIgnoreCase(getString(R.string.REGISTER_TAG)))
                imageView.setImageResource(R.drawable.quicklink_tag_registration);
            else if(quickLink.getType().equalsIgnoreCase(getString(R.string.ATOMIC_CUSTOMER_CREATE)))
                imageView.setImageResource(R.drawable.quicklink_registration);
            else if(quickLink.getType().equalsIgnoreCase(getString(R.string.PAYMENT_TYPE_C2MP)))
                imageView.setImageResource(R.drawable.quicklink_makepayment);
            else if(quickLink.getType().equalsIgnoreCase(getString(R.string.PAYMENT_TYPE_OUTTXF))){
                if(quickLink.getName().toUpperCase(Locale.US).contains("AIRTIME") || quickLink.getName().toUpperCase(Locale.US).contains("TOPUP"))
                    imageView.setImageResource(R.drawable.quicklink_airtimetopup);
                else
                    imageView.setImageResource(R.drawable.quicklink_receivepayment);
            }
        }
    }

    private void writeNameOnQL(View rootView) {
        int counter = 1;
        for (QuickLink quickLink: Constants.quickLinks){
            TextView buttonTitle = null;
            switch (counter){
                case 1:
                    buttonTitle =(TextView)rootView.findViewById(R.id.Fragment_QuickPay_TextView_layout1_textView1);
                    break;
                case 2:
                    buttonTitle =(TextView)rootView.findViewById(R.id.Fragment_QuickPay_TextView_layout1_textView2);
                    break;
                case 3:
                    buttonTitle =(TextView)rootView.findViewById(R.id.Fragment_QuickPay_TextView_layout2_textView1);
                    break;
                case 4:
                    buttonTitle =(TextView)rootView.findViewById(R.id.Fragment_QuickPay_TextView_layout2_textView2);
                    break;
                case 5:
                    buttonTitle =(TextView)rootView.findViewById(R.id.Fragment_QuickPay_TextView_layout3_textView1);
                    break;
            }
            counter++;
            if(quickLink.getType().equalsIgnoreCase(getString(R.string.PAYMENT_TYPE_MERCHANTPAYMENT)) &&
                    (quickLink.getAmount() == null || quickLink.getAmount().equalsIgnoreCase("0"))) {
                if(quickLink.getAmount() == null)
                    quickLink.setAmount("0");
                Login model = new Login(getActivity());
                String title = null;
                if(quickLink.getName() == null || quickLink.getName().isEmpty())
                    title = "";
                else
                    title = quickLink.getName()+"\n\n";
                buttonTitle.setText(title+model.getSharedPreference().getString(SecurePreferences.SAVED_VARIABLE_AMOUNT+(counter-1), title+"\n\n"+quickLink.getAmount()));
            }
            else
                buttonTitle.setText(quickLink.getName());
        }
    }

    private void showQLDialogWhenButtonCLicked(int buttonOrder) {
        qpDialog = new Dialog(getActivity());
        qpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        qpDialog.setCanceledOnTouchOutside(false);
        qpDialog.setOnDismissListener(this);

        QuickLink quickLink = Constants.quickLinks.get(buttonOrder - 1);
        if (quickLink.getType().equalsIgnoreCase(getString(R.string.PAYMENT_TYPE_MERCHANTPAYMENT)) &&
                (quickLink.getAmount() == null || quickLink.getAmount().equalsIgnoreCase("0")))
        {
            Log.d(TAG,"buttonOrder: "+buttonOrder);
            String extra = String.valueOf(buttonOrder);
            Log.d(TAG,"extra: "+extra);
            startActivity(new Intent(getActivity(), VariableC2MPActivity.class).putExtra(INTENT.EXTRA_NUM.toString(), extra));
            return;
        }
        if(quickLink.getType().trim().toUpperCase(Locale.US).equalsIgnoreCase(getString(R.string.ATOMIC_CUSTOMER_CREATE).toUpperCase(Locale.US))){
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.UPDATE_ACTION_BAR_TITLE.toString())
                    .putExtra(INTENT.EXTRA_TITLE.toString(), quickLink.getName()));
            showCustomerLookupDialog();
            return;
        }
        if(quickLink.getType().trim().toUpperCase(Locale.US).equalsIgnoreCase(getString(R.string.REGISTER_TAG).toUpperCase(Locale.US))){
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.UPDATE_ACTION_BAR_TITLE.toString())
                    .putExtra(INTENT.EXTRA_TITLE.toString(), quickLink.getName()));
            showTagRegistrationDialog();
            return;
        }
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.UPDATE_ACTION_BAR_TITLE.toString())
                .putExtra(INTENT.EXTRA_TITLE.toString(), quickLink.getName()));
        if(buttonOrder == 1){
            setUpQLDetailsDialog(qpDialog, 0);
        }
        if(buttonOrder == 2){
            setUpQLDetailsDialog(qpDialog, 1);
        }
        if(buttonOrder == 3){
            setUpQLDetailsDialog(qpDialog, 2);
        }
        if(buttonOrder == 4){
            setUpQLDetailsDialog(qpDialog, 3);
        }
        qpDialog.show();
    }

    private void showTagRegistrationDialog(){

        qpDialog.setContentView(R.layout.dialog_tag_registration);
        TagRegistrationUtil tagUtil = new TagRegistrationUtil(getActivity());
        isTagRegistrationDialogOn = true;
        model = null;
        tagUtil.setUp(qpDialog);
        qpDialog.setOnShowListener(this);
        qpDialog.setOnDismissListener(this);
        qpDialog.show();

    }
    private void showCustomerLookupDialog() {
        qpDialog.setContentView(R.layout.customer_lookup_dialog);
        customerFoundLinearLayout = (LinearLayout)qpDialog.findViewById(R.id.Dialog_CustomerLookup_LinearLayout_CustomerFound);
        registerCustomerButton = (Button)qpDialog.findViewById(R.id.Dialog_CustomerLookup_Button_RegisterCustomer);
        CCLookupButton = (Button) qpDialog.findViewById(R.id.Dialog_CustomerLookup_Button_Lookup);
        CCUpdateButton = (Button) qpDialog.findViewById(R.id.Dialog_CustomerLookup_Button_Update);
        CCContactNumberTV = (TextView) qpDialog.findViewById(R.id.Dialog_CustomerLookup_TextView_CustomerNameTV);
        CCContactNumberET = (EditText) qpDialog.findViewById(R.id.Dialog_CustomerLookup_EditText_CustomerNameET);
        CCGivenNameET = (EditText)qpDialog.findViewById(R.id.Dialog_CustomerLookup_EditText_GivenNameET);
        CCSurNameET = (EditText)qpDialog.findViewById(R.id.Dialog_CustomerLookup_EditText_SurNameET);
        CCDOBET = (EditText)qpDialog.findViewById(R.id.Dialog_CustomerLookup_EditText_DOBET);
        msisdnSpinner = (Spinner)qpDialog.findViewById(R.id.Dialog_CustomerLookup_Spinner);

        registerCustomerButton.setVisibility(View.GONE);

        CCContactNumberET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(CCContactNumberET.getText().toString().isEmpty())
                    CCContactNumberTV.setVisibility(View.VISIBLE);
                else
                    CCContactNumberTV.setVisibility(View.INVISIBLE);
            }
        });
        CCLookupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CCContactNumberET.getText().toString().isEmpty()) {
                    Toast.makeText(v.getContext(), getString(R.string.CONTACT_NUMBER_IS_MANDATORY), Toast.LENGTH_SHORT).show();
                    return;
                }
                registerCustomerButton.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(CCContactNumberET.getWindowToken(), 0);
                customerFoundLinearLayout.setVisibility(View.GONE);
                progressBar = (ProgressBar) qpDialog.findViewById(R.id.Dialog_CustomerLookup_ProgressBar);
                progressBar.setVisibility(View.VISIBLE);
                if (resultTV != null)
                    resultTV.setVisibility(View.GONE);
                resultTV = (TextView) qpDialog.findViewById(R.id.Dialog_CustomerLookup_TextView_ResultTV);
                resultTV.setVisibility(View.GONE);
                LookupCustomersBackgroundTask task = new LookupCustomersBackgroundTask();
                task.execute();
            }
        });
        CCUpdateButton.setVisibility(View.GONE); // TODO remove this to implement Update Customer feature

        CCUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomerUpdate cu = new CustomerUpdate(getActivity());
                cu.setCustomer(customers.get(msisdnSpinner.getSelectedItemPosition()));
                cu.getConnTaskManager().startBackgroundTask();
                progressBar = (ProgressBar) qpDialog.findViewById(R.id.Dialog_CustomerLookup_CustomerUpdateProgressBar);
                progressBar.setVisibility(View.VISIBLE);
                resultTV = (TextView)qpDialog.findViewById(R.id.Dialog_CustomerLookup_TextView_CustomerUpdateResultTV);
                resultTV.setVisibility(View.GONE);
            }
        });
        qpDialog.show();
    }

    @Override
    public void finishedExtractingCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
        progressBar.setVisibility(View.GONE);
        List<String> spinnerList = new ArrayList<String>();
        for (Customer customer: customers)
        {
            CCSurNameET.setText(customer.getSurName());
            CCGivenNameET.setText(customer.getGivenName());
            CCDOBET.setText(customer.getDOB());
            spinnerList.add(customer.getMSISDN());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, R.id.Spinner_TextView, spinnerList);
        msisdnSpinner.setAdapter(spinnerAdapter);
        customerFoundLinearLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void finishedUpdatingCustomer(String response) {
        progressBar.setVisibility(View.GONE);
        resultTV.setVisibility(View.VISIBLE);
        resultTV.setText(response);
    }

    @Override
    public void error(String errorMessage, int status) {
        customerFoundLinearLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(errorMessage);
        resultTV.setVisibility(View.VISIBLE);
        if(errorMessage.toUpperCase(Locale.US).contains("INVALID ACCOUNT") || status != Integer.parseInt(getString(R.string.STATUS_OK)))
        {
            registerCustomerButton.setVisibility(View.VISIBLE);
            registerCustomerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.UPDATE_ACTION_BAR_TITLE.toString())
                            .putExtra(INTENT.EXTRA_TITLE.toString(), "Customer Registration"));
                    startActivity(new Intent(getActivity(), CustomerRegistrationActivity.class).putExtra(INTENT.EXTRA_INITIAL_CONTACT_PHONE.toString(), CCContactNumberET.getText().toString()));

//                    qpDialog.dismiss();
                }
            });

        }
    }

    private void setUpQLDetailsDialog(final Dialog qpDialog, final int index){

        model = new QuickPay(getActivity());
        destination = null;
        qpDialog.setContentView(R.layout.dialog_quick_pay_view);
        final TextView amountTV = (TextView) qpDialog.findViewById(R.id.Dialog_QuickLink_C2MP_TextView_Amount);
        final TextView phoneNumberTV = (TextView) qpDialog.findViewById(R.id.Dialog_QuickLink_C2MP_TextView_PhoneNumber);
        amountET = (EditText) qpDialog.findViewById(R.id.Dialog_QuickLink_C2MP_EditText_Amount);
        phoneNumberET = (EditText) qpDialog.findViewById(R.id.Dialog_QuickLink_C2MP_EditText_PhoneNumber);
        payButton = (Button) qpDialog.findViewById(R.id.Dialog_QuickLink_C2MP_Button_Pay);
        resultTV = (TextView) qpDialog.findViewById(R.id.Dialog_QuickLink_C2MP_TextView_Result);
        progressBar = (ProgressBar) qpDialog.findViewById(R.id.Dialog_QuickLink_C2MP_Progressbar);
        transactionsSpinner = (Spinner) qpDialog.findViewById(R.id.Dialog_QuickLink_C2MP_Spinner);
        qpDialog.setOnDismissListener(this);

        if(Constants.quickLinks.get(index).getType().equalsIgnoreCase(getString(R.string.PAYMENT_TYPE_MERCHANTPAYMENT)))
        {
            amountET.setText(Constants.quickLinks.get(index).getAmount());
            model.setC2MPTransaction(true);
            model.setWorkingAmount(Constants.quickLinks.get(index).getAmount());
            RelativeLayout spinnerLayout = (RelativeLayout) qpDialog.findViewById(R.id.Dialog_QuickLink_C2MP_RelativeLayout_SpinnerLayout);
            spinnerLayout.setVisibility(View.GONE);
            showNFCOnlyDialog(index, amountTV, phoneNumberTV);
            return;
        }
        model.setC2MPTransaction(false);
        type = Constants.quickLinks.get(index).getType();
        amountET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (amountET.getText().toString().isEmpty())
                    amountTV.setVisibility(View.VISIBLE);
                else
                    amountTV.setVisibility(View.GONE);
            }
        });
        phoneNumberET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (phoneNumberET.getText().toString().isEmpty()) {
                    phoneNumberTV.setVisibility(View.VISIBLE);
                    model.setDestinationProvided(false);
                    destination = null;
                } else {
                    phoneNumberTV.setVisibility(View.GONE);
                    model.setDestinationProvided(true);
                    destination = phoneNumberET.getText().toString();
                }
            }
        });
        if(Constants.quickLinks.get(index).getDstCodes().size() > 0)
        {
            model.setDestinationCodeProvided(true);
            ArrayList<String> list = new ArrayList<String>();
            for(DstCode dstCode : Constants.quickLinks.get(index).getDstCodes())
                list.add(dstCode.getDescription());
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, R.id.Spinner_TextView, list);
            transactionsSpinner.setAdapter(spinnerAdapter);
            transactionsSpinner.setOnItemSelectedListener(new QuickLinkSpinnerOnItemSelectedListener(qpDialog, index));
            transactionsSpinner.setSelection(0);

            amountET.setEnabled(false);
            amountET.setBackgroundColor(getResources().getColor(R.color.APP_MAIN_COLOR));
            amountET.setTextColor(getResources().getColor(R.color.TEXT_COLOR));
            amountTV.setGravity(Gravity.CENTER);
            amountTV.setVisibility(View.GONE);
        }
        else{
            RelativeLayout spinnerLayout = (RelativeLayout) qpDialog.findViewById(R.id.Dialog_QuickLink_C2MP_RelativeLayout_SpinnerLayout);
            spinnerLayout.setVisibility(View.GONE);
            if(Constants.quickLinks.get(index).getSingleDstCode() != null && !Constants.quickLinks.get(index).getSingleDstCode().isEmpty())
            {
                model.setDestinationCodeProvided(true);
                destinationCode = Constants.quickLinks.get(index).getSingleDstCode();
            }
            else
                model.setDestinationCodeProvided(false);
        }

        if(Constants.quickLinks.get(index).getAmount() != null && !Constants.quickLinks.get(index).getAmount().isEmpty())
        {
            amountET.setText(Constants.quickLinks.get(index).getAmount());
            amountET.setEnabled(false);
            amountET.setBackgroundColor(getResources().getColor(R.color.APP_MAIN_COLOR));
            amountET.setTextColor(getResources().getColor(R.color.TEXT_COLOR));
            amountET.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            amountTV.setVisibility(View.GONE);
        }
        if(Constants.quickLinks.get(index).getDestination() != null && !Constants.quickLinks.get(index).getDestination().isEmpty())
        {
            phoneNumberTV.setVisibility(View.GONE);
            phoneNumberET.setVisibility(View.GONE);
            destination = Constants.quickLinks.get(index).getDestination();
            model.setDestinationProvided(true);
        }
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model == null)
                    model = new QuickPay(getActivity());
                if(amountET.getText().toString().isEmpty())
                {
                    amountET.setError("Amount field is mandatory.");
                    return;
                }
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                if(destination != null && !destination.isEmpty())
                {
                    Log.d(QuickLinkFragment.class.getName(), "Mei calling NFCDialog.dismiss");
                    showProgressBar();
                    model.setDestination(destination);
                    model.setDestinationCode(destinationCode);
                    model.setWorkingAmount(amountET.getText().toString());
                    model.setPaymentType(type);
                    model.getConnTaskManager().startBackgroundTask();
                }
                else
                    showNFCDialog(v.getContext(), qpDialog);
            }
        });

    }

    private void showNFCOnlyDialog(int index, TextView amountTV, TextView phoneNumberTV) {

        amountET.setVisibility(View.GONE);
        amountTV.setVisibility(View.GONE);
        payButton.setVisibility(View.GONE);
        phoneNumberTV.setVisibility(View.GONE);
        progressBar.setVisibility(View.INVISIBLE);
        phoneNumberET.setVisibility(View.VISIBLE);
        resultTV.setVisibility(View.VISIBLE);
        resultTV.setText(getResources().getText(R.string.TAP_DEVICE));
        resultTV.setTextSize(24);
        resultTV.setEnabled(false);
        resultTV.setBackgroundColor(getResources().getColor(R.color.APP_MAIN_COLOR));
        resultTV.setTextColor(getResources().getColor(R.color.TEXT_COLOR));
        resultTV.setGravity(Gravity.CENTER);

        final DecimalFormat df = new DecimalFormat("#.00");

        phoneNumberET.setText(df.format(Double.parseDouble(Constants.quickLinks.get(index).getAmount())));
        phoneNumberET.setGravity(Gravity.CENTER);
        phoneNumberET.setTextSize(32);
        phoneNumberET.setTextColor(getResources().getColor(R.color.TEXT_COLOR));
        phoneNumberET.setTypeface(null, Typeface.BOLD);
        phoneNumberET.setEnabled(false);
        phoneNumberET.setBackgroundColor(getResources().getColor(R.color.APP_MAIN_COLOR));
        enableNFCScan();
    }

    private void showPinDialog(){

        final Dialog pinDialog = new Dialog(getActivity());
        pinDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pinDialog.setContentView(R.layout.pin_dialog);
        pinDialog.setCanceledOnTouchOutside(false);
        final TextView pinTextView = (TextView)pinDialog.findViewById(R.id.Dialog_PIN_TextView_PIN);
        final EditText pinEditText = (EditText)pinDialog.findViewById(R.id.Dialog_PIN_EditText_PIN);
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
                    showProgressBar();
                    model.getConnTaskManager().startBackgroundTask();
                }
            }
        });
        pinDialog.show();
    }

    private void showNFCDialog(Context context, Dialog qpDialog) {

        NFCDialog = new Dialog(context);
        NFCDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        NFCDialog.setContentView(R.layout.nfc_dialog);
        LinearLayout root = (LinearLayout) qpDialog.findViewById(R.id.Dialog_QuickPay_LinearLayout_Root);
        NFCDialog.getWindow().setLayout(root.getWidth(), root.getHeight());
        NFCDialog.getWindow().setBackgroundDrawableResource(R.drawable.background_nfc_window);
        NFCDialog.setCanceledOnTouchOutside(false);
        NFCDialog.setOnShowListener(this);
        NFCDialog.setOnDismissListener(this);
        NFCDialog.show();
    }

    private void showProgressBar(){

        if(amountET != null)
            amountET.setEnabled(false);
        if(payButton != null)
            payButton.setEnabled(false);
        if(progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
        if(resultTV != null)
            resultTV.setVisibility(View.INVISIBLE);
    }

    private void hideProgressBar(String response){

        if(amountET != null)
            amountET.setEnabled(true);
        if(payButton != null)
            payButton.setEnabled(true);
        if(progressBar != null)
            progressBar.setVisibility(View.INVISIBLE);
        if(resultTV != null)
        {
            resultTV.setText(response);
            resultTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        disableNFCScan();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        enableNFCScan();
    }

    private class LookupCustomersBackgroundTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            if(customerUtil == null)
                customerUtil = new CustomerLookupUtil(getActivity(), QuickLinkFragment.this);
            customerUtil.lookupCustomers(CCContactNumberET.getText().toString());
            return null;
        }
    }

    public static void setTabNameToOriginalName(Activity activity){
        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(INTENT.UPDATE_ACTION_BAR_TITLE.toString())
                .putExtra(INTENT.EXTRA_TITLE.toString(), activity.getString(R.string.TAB_2)));

    }

}
