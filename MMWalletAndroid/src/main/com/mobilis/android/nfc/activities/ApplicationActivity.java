package com.mobilis.android.nfc.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.AccountBalance;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.fragments.BankSendMoneyFragment;
import com.mobilis.android.nfc.fragments.BillPaymentsListFragment;
import com.mobilis.android.nfc.fragments.BuyElectronicVoucherFragment;
import com.mobilis.android.nfc.fragments.CableTVFragment;
import com.mobilis.android.nfc.fragments.CashOutFragment;
import com.mobilis.android.nfc.fragments.CashOutVoucherFragment;
import com.mobilis.android.nfc.fragments.CashinFragment;
import com.mobilis.android.nfc.fragments.ChangePinFragment;
import com.mobilis.android.nfc.fragments.CustomerCreationFirstFragment;
import com.mobilis.android.nfc.fragments.CustomerCreationSecondFragment;
import com.mobilis.android.nfc.fragments.CustomerCreationThirdFragment;
import com.mobilis.android.nfc.fragments.CustomerLookupFragment;
import com.mobilis.android.nfc.fragments.DepositFundsFromCreditCardFragment;
import com.mobilis.android.nfc.fragments.ElectronicVoucherFragment;
import com.mobilis.android.nfc.fragments.GenerateTokenFragment;
import com.mobilis.android.nfc.fragments.OtherOperatorsSendMoneyFragment;
import com.mobilis.android.nfc.fragments.ReceiveMoneyFragment;
import com.mobilis.android.nfc.fragments.RedeemVoucherFragment;
import com.mobilis.android.nfc.fragments.SendMoneyFragment;
import com.mobilis.android.nfc.fragments.SendMoneyListFragment;
import com.mobilis.android.nfc.fragments.ServerConfigurationFragment;
import com.mobilis.android.nfc.fragments.TagRegistrationFragment;
import com.mobilis.android.nfc.fragments.TopUpFragment;
import com.mobilis.android.nfc.fragments.UtilitiesFragment;
import com.mobilis.android.nfc.interfaces.A2ACallback;
import com.mobilis.android.nfc.model.Customer;
import com.mobilis.android.nfc.model.CustomerRegistration;
import com.mobilis.android.nfc.model.LastTransactions;
import com.mobilis.android.nfc.model.LoginBalanceInternal;
import com.mobilis.android.nfc.model.TransferCode;
import com.mobilis.android.nfc.slidemenu.utils.LoginResponseConstants;
import com.mobilis.android.nfc.tabsfragments.AccountFragment;
import com.mobilis.android.nfc.tabsfragments.MenuFragment;
import com.mobilis.android.nfc.tabsfragments.QuickLinkFragment;
import com.mobilis.android.nfc.util.Constants;
import com.mobilis.android.nfc.widget.AndroidToAndroidNFCActivityLowerVersions;

import java.util.Locale;

public class ApplicationActivity extends Activity implements A2ACallback , View.OnClickListener{

    private final String TAG = ApplicationActivity.class.getSimpleName();
    public static String loginClientId;
    public static String loginClientPin;
    public static boolean isRefreshRequired;
    public static AccountBalance ACCOUNT_BALANCE = new AccountBalance();
    FragmentStatePagerAdapter mSectionsPagerAdapter;
    static ViewPager mViewPager;

    LinearLayout accountLL;
    LinearLayout quickLinkLL;
    LinearLayout menuLL;
    TextView actionbarTitleTV;
    ImageView accountIV;
    ImageView quickLinkIV;
    ImageView menuIV;
    LinearLayout tabsLinearLayout;

    BroadcastReceiver broadcastReceiver;
    ActionBar actionBar;

    static boolean isInSubMenu;
    static String firstExtraFragmentName;
    static String secondExtraFragmentName;
    static boolean receivedCustCreationIntent;
    static int fragmentSelection;

    private int VIEW_PAGER_HEIGHT = 180;
    private Customer newCustomer;
    private int variableNumberOfFragments;
    private int staticNumberOfFragment;
    private boolean activityHasBeenDestroyed;

    private TextView CustomerRegistrationResultTV;
    private ProgressBar CustomerRegistrationProgressBar;
    private Button CustomerRegistrationRegisterButton;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(ApplicationActivity.class.getSimpleName(), "onNewIntent() is called");
//        if(isNFCDialogOn)
//        {

//        int buildVersion = Build.VERSION.SDK_INT;
//        Log.d(TAG,"onNewIntent() is called in ApplicationActivity");
//        Log.d(TAG,"buildVersion is:"+buildVersion);
//        if(buildVersion <= 18){
            Log.d(TAG,"will get an instance if the Tag now and get its nfcId");
            // This will support NFC scan read for devices with SDK build less than 18
            Tag intentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String nfcId = AndroidToAndroidNFCActivityLowerVersions.onTagDiscovered(intentTag, this).toUpperCase(Locale.US);
            Log.d(TAG,"nfcId: "+nfcId);
            Log.d(TAG,"calling finishedA2ACommunication()");
            finishedA2ACommunication(nfcId);
//        }
    }

    // This will support NFC scan read for devices with SDK build less than 18 - 18 and above will be supported in NFCFragment
    @Override
    public void finishedA2ACommunication(String scannedId) {

        Log.d(TAG,"ApplicationActivity finishedA2ACommunication() is called");


        Log.d(TAG,"finishedA2ACommunication() in ApplicationActivity is called.");
        scannedId = scannedId.toUpperCase(Locale.US);
        Log.d(TAG,"scannedId: "+scannedId);
        Log.d(TAG,"Sending NFC_SCANNED intent now");

        Intent intent = new Intent(INTENT.NFC_SCANNED.toString());
        intent.putExtra(INTENT.EXTRA_NFC_ID.toString(), scannedId);
        Log.d(TAG, "sending "+INTENT.EXTRA_NFC_ID.toString()+" intent now...");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        newCustomer = new Customer();
    }

    private void setTabTextSize(TextView accountTitle, TextView quickPayTitle, TextView otherTitle) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int textSize = 0;
        if(metrics.heightPixels > 600)
            textSize = 17;
        else
            textSize = 14;
        accountTitle.setTextSize(textSize);
        quickPayTitle.setTextSize(textSize);
        otherTitle.setTextSize(textSize);
    }

    private void setTabSize() {
        LinearLayout.LayoutParams imageParams = (LinearLayout.LayoutParams)accountIV.getLayoutParams();//new LinearLayout.LayoutParams(250,20);
        if(Constants.getScreenHeight(this) > 600)
            imageParams.height = 170;
        else
            imageParams.height = 20;
        imageParams.topMargin = 10;
        imageParams.gravity = Gravity.CENTER_HORIZONTAL;
        accountIV.setLayoutParams(imageParams);
        quickLinkIV.setLayoutParams(imageParams);
        menuIV.setLayoutParams(imageParams);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_application);

        ACCOUNT_BALANCE = (AccountBalance)getIntent().getSerializableExtra(INTENT.EXTRA_ACCOUNT_BALANCE.toString());

        actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);


        LayoutInflater inflater = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.actionbar_view, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.TOP|Gravity.FILL_VERTICAL|Gravity.CLIP_VERTICAL;
        actionBar.setCustomView(customView, params);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM );

        ImageView homeIV = (ImageView)actionBar.getCustomView().findViewById(R.id.Actionbar_ImageView_Home);
        homeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
            }
        });
        actionbarTitleTV = (TextView)actionBar.getCustomView().findViewById(R.id.Actionbar_TextView_Title);
        tabsLinearLayout = (LinearLayout)findViewById(R.id.Linear);
        accountLL = (LinearLayout) findViewById(R.id.Activity_Application_LinearLayout_Tab_Account);
        quickLinkLL = (LinearLayout) findViewById(R.id.Activity_Application_LinearLayout_Tab_QuickPay);
        menuLL = (LinearLayout) findViewById(R.id.Activity_Application_LinearLayout_Tab_OTHER);

        accountIV = (ImageView) findViewById(R.id.Activity_Application_ImageView_Tab_Account);
        quickLinkIV = (ImageView) findViewById(R.id.Activity_Application_ImageView_Tab_QuickLink);
        menuIV = (ImageView) findViewById(R.id.Activity_Application_ImageView_Tab_Other);

        staticNumberOfFragment = getNumberOfFragmentsAtLogin();
        variableNumberOfFragments = staticNumberOfFragment;
        setTabSize();
        if(!LoginResponseConstants.accountTabAvailable)
            accountLL.setVisibility(View.GONE);
        if(!LoginResponseConstants.quickLinkTabAvailable)
            quickLinkLL.setVisibility(View.GONE);
        if(!isMenuTabAvailable())
            menuLL.setVisibility(View.GONE);

        accountLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
            }
        });
        quickLinkLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LoginResponseConstants.accountTabAvailable)
                    mViewPager.setCurrentItem(1);
                else
                    mViewPager.setCurrentItem(0);
            }
        });
        menuLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFullTabs())
                    mViewPager.setCurrentItem(2);
                else if((LoginResponseConstants.accountTabAvailable && !LoginResponseConstants.quickLinkTabAvailable) ||
                        (!LoginResponseConstants.accountTabAvailable && LoginResponseConstants.quickLinkTabAvailable) )
                     mViewPager.setCurrentItem(1);
                else
                    mViewPager.setCurrentItem(0);
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mSectionsPagerAdapter = new CustomFragmentStatePagerAdapter(ApplicationActivity.this.getFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if(getCurrentFocus()!=null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                if(position == 0 && LoginResponseConstants.accountTabAvailable) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(ApplicationActivity.class.getSimpleName(), "sending UPDATE_ACCOUNTS_BALANCES intent now...");
                            LocalBroadcastManager.getInstance(ApplicationActivity.this).sendBroadcast(new Intent(INTENT.UPDATE_ACCOUNTS_BALANCES.toString()));
                        }
                    },500);
                }
                if(getNumberOfFragmentsAtLogin() == 3)
                {
                    if(mViewPager.getCurrentItem() >= 2)
                        updatePageSelectionCircles(2);
                    else if(mViewPager.getCurrentItem() == 1)
                        updatePageSelectionCircles(1);
                    else if(mViewPager.getCurrentItem() == 0)
                        updatePageSelectionCircles(0);
                }
                else if(getNumberOfFragmentsAtLogin() == 2)
                {
                    if(mViewPager.getCurrentItem() >= 1)
                        updatePageSelectionCircles(1);
                    else if(mViewPager.getCurrentItem() == 0)
                        updatePageSelectionCircles(0);
                }
                else if(getNumberOfFragmentsAtLogin() == 1){
                        updatePageSelectionCircles(0);
                }
                updateTabsSelection(position);
            }
        });
        mViewPager.setCurrentItem(0);
        updateTabsSelection(0);

        if(LoginResponseConstants.accountTabAvailable)
            actionbarTitleTV.setText(getString(R.string.TAB_1));
        else if(LoginResponseConstants.quickLinkTabAvailable)
            actionbarTitleTV.setText(getString(R.string.TAB_2));
        else
            actionbarTitleTV.setText(getString(R.string.TAB_3));
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(ApplicationActivity.this.getFragmentManager() == null)
                    return;
                final FragmentManager fragmentManager = ApplicationActivity.this.getFragmentManager();
                String action = intent.getAction();
                Log.d(ApplicationActivity.class.getSimpleName(), "received intent: " + intent.getAction());

                if(action.equalsIgnoreCase(INTENT.UPDATE_ACTION_BAR_TITLE.toString())){
                    if(actionbarTitleTV == null)
                        return;
                    actionbarTitleTV.setText(intent.getStringExtra(INTENT.EXTRA_TITLE.toString()));
                }
                else if(action.equalsIgnoreCase(INTENT.DECREASE_VIEW_PAGER_HEIGHT.toString())){
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    if(metrics.heightPixels > 500)
                        VIEW_PAGER_HEIGHT = 290;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, convertDpToPixel(VIEW_PAGER_HEIGHT, context));
                    mViewPager.setLayoutParams(params);
                }
                else if(action.equalsIgnoreCase(INTENT.RESET_FRAGMENTS.toString()) && !activityHasBeenDestroyed){
                    tabsLinearLayout.setVisibility(View.VISIBLE);
                    resetViewPagerSize();
                    destroyCustomerRegistrationComponents();
                    receivedCustCreationIntent = false;
                    ApplicationActivity.hideCustomerRegistrationControl(ApplicationActivity.this);
                    if(variableNumberOfFragments <= staticNumberOfFragment)
                        return;
                    final int position = intent.getIntExtra(INTENT.EXTRA_POS.toString(), staticNumberOfFragment-1);
                    variableNumberOfFragments = staticNumberOfFragment;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSectionsPagerAdapter = new CustomFragmentStatePagerAdapter(fragmentManager);
                            mViewPager.setAdapter(mSectionsPagerAdapter);
                            mViewPager.setCurrentItem(position);
                        }
                    }, 100);
                }
                else if(action.equalsIgnoreCase(INTENT.NEW_FRAGMENT.toString())){

                    tabsLinearLayout.setVisibility(View.GONE);
                    variableNumberOfFragments = getNumberOfFragmentsAtLogin()+1;//intent.getIntExtra("NUM", staticNumberOfFragment);
                    firstExtraFragmentName = intent.getStringExtra(INTENT.EXTRA_FRAG_NAME.toString());
                    fragmentSelection = variableNumberOfFragments;
                    Log.d(ApplicationActivity.class.getSimpleName(), "variableNumberOfFragments: "+ variableNumberOfFragments);
                    Log.d(ApplicationActivity.class.getSimpleName(), "fragmentSelection: "+fragmentSelection);
                    Log.d(ApplicationActivity.class.getSimpleName(), "firstExtraFragmentName : "+firstExtraFragmentName);

                    mSectionsPagerAdapter = new CustomFragmentStatePagerAdapter(fragmentManager);
                    mViewPager.setAdapter(mSectionsPagerAdapter);
                    mViewPager.setCurrentItem(fragmentSelection, true);

                    return;

                }
                else if(action.equalsIgnoreCase(INTENT.NEW_NEW_FRAGMENT.toString())){
                    variableNumberOfFragments = getNumberOfFragmentsAtLogin()+2;//intent.getIntExtra("NUM", staticNumberOfFragment+1);
                    secondExtraFragmentName = intent.getStringExtra(INTENT.EXTRA_FRAG_NAME.toString());
                    fragmentSelection = variableNumberOfFragments;
                    mSectionsPagerAdapter = new CustomFragmentStatePagerAdapter(fragmentManager);
                    mViewPager.setAdapter(mSectionsPagerAdapter);
                    mViewPager.setCurrentItem(fragmentSelection, true);
                }


                else if(action.equalsIgnoreCase(INTENT.CUSTOMER_CREATION_SELECTION.toString())){
                    Log.d(ApplicationActivity.class.getSimpleName(), "received intent to create Customer..isQuickLinkTabAvailable ? "+isQuickLinkTabAvailable());
                    if(isQuickLinkTabAvailable()) {
                        variableNumberOfFragments = 6;
                        fragmentSelection = 3;
                    }
                    else {
                        variableNumberOfFragments = 5;
                        fragmentSelection = 2;
                    }
                    Log.d(ApplicationActivity.class.getSimpleName(), "received intent to create Customer..variableNumberOfFragments: "+ variableNumberOfFragments);
                    Log.d(ApplicationActivity.class.getSimpleName(), "received intent to create Customer..fragmentSelection: "+fragmentSelection);
                    receivedCustCreationIntent = true;
                    mSectionsPagerAdapter = new CustomFragmentStatePagerAdapter(fragmentManager);
                    mViewPager.setAdapter(mSectionsPagerAdapter);
                    Log.d(ApplicationActivity.class.getSimpleName(), "received intent to create Customer..setting current Item in mViewPager to: "+fragmentSelection);
                    Log.d(ApplicationActivity.class.getSimpleName(), "-----DONE-----");
                    mViewPager.setCurrentItem(fragmentSelection, true);
                    setUpCustomerRegistrationComponents();
                }

                else if(intent.getAction().equalsIgnoreCase(INTENT.CUSTOMER_REGISTRATION.toString())){
                    if(CustomerRegistrationResultTV == null) // This is to distinguish between CustomerRegistration message coming from QL and Menu Profile options
                        return;
                    String serverResponse = intent.getStringExtra(INTENT.EXTRA_SERVER_RESPONSE.toString());
                    if(validateCustomerRegistrationResponse(serverResponse)){
                        CustomerRegistrationResultTV.setText("Customer "+getNewCustomer().getMSISDN()+" registered successfully.");
                    }
                    else{
                        if(serverResponse != null)
                            CustomerRegistrationResultTV.setText(getMessage(serverResponse));
                    }
                    CustomerRegistrationProgressBar.setVisibility(View.GONE);
                    CustomerRegistrationResultTV.setVisibility(View.VISIBLE);

                }
                else if (intent.getAction().equalsIgnoreCase(INTENT.CUSTOMER_REGISTRATION_FIRST_CIRCLE.toString()))
                    updatePageSelectionCircles(0);
                else if (intent.getAction().equalsIgnoreCase(INTENT.CUSTOMER_REGISTRATION_SECOND_CIRCLE.toString()))
                    updatePageSelectionCircles(1);
                else if (intent.getAction().equalsIgnoreCase(INTENT.CUSTOMER_REGISTRATION_THIRD_CIRCLE.toString()))
                    updatePageSelectionCircles(2);
                else if(action.equalsIgnoreCase(INTENT.UPDATE_BALANCE.toString()) && !activityHasBeenDestroyed && LoginResponseConstants.accountTabAvailable){
                    Log.d(ApplicationActivity.class.getSimpleName(), "received UPDATE_BALANCE intent..");
                    Log.d(ApplicationActivity.class.getSimpleName(), "starting two background processes to update LastTransactions and Balance...");
                    LastTransactions lastTransactions = new LastTransactions(ApplicationActivity.this);
                    lastTransactions.getConnTaskManager().startBackgroundTask();
                    LoginBalanceInternal loginBalanceInternal = new LoginBalanceInternal(ApplicationActivity.this);
                    loginBalanceInternal.getConnTaskManager().startBackgroundTask();
                }
                else if(action.equalsIgnoreCase(INTENT.INTERNET_WIFI_SIGNAL_WEAK.toString())){
                    Toast.makeText(context, "Wifi signal is weak", Toast.LENGTH_SHORT).show();
                }
                else if(action.equalsIgnoreCase(INTENT.INTERNET_GSM_SIGNAL.toString())){
                    Toast.makeText(context, "Mobile data connection is weak", Toast.LENGTH_SHORT).show();
                }
                else if(action.equalsIgnoreCase(INTENT.INTERNET_NO_SIGNAL.toString())){
                    Toast.makeText(context, "Internet connection lost", Toast.LENGTH_SHORT).show();
                }
                else if(action.equalsIgnoreCase(INTENT.INTERNET_REGAINED.toString())){
                    Toast.makeText(context, intent.getStringExtra(INTENT.EXTRA_INTERNET.toString()), Toast.LENGTH_SHORT).show();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NEW_FRAGMENT.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.NEW_NEW_FRAGMENT.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.RESET_FRAGMENTS.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.UPDATE_BALANCE.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.INTERNET_WIFI_SIGNAL_WEAK.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.INTERNET_GSM_SIGNAL.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.INTERNET_NO_SIGNAL.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.INTERNET_REGAINED.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.UPDATE_ACTION_BAR_TITLE.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter((INTENT.CUSTOMER_CREATION_SELECTION.toString())));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter((INTENT.DECREASE_VIEW_PAGER_HEIGHT.toString())));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter((INTENT.CUSTOMER_REGISTRATION.toString())));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter((INTENT.CUSTOMER_REGISTRATION_FIRST_CIRCLE.toString())));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter((INTENT.CUSTOMER_REGISTRATION_SECOND_CIRCLE.toString())));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter((INTENT.CUSTOMER_REGISTRATION_THIRD_CIRCLE.toString())));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.REFRESH_ACCOUNT.toString()));

    }

    private TextView firstCircle ;
    private TextView secondCircle ;
    private TextView thirdCircle ;

    private void setUpCustomerRegistrationComponents(){
        firstCircle = (TextView) findViewById(R.id.Activity_Application_FirstPage);
        secondCircle = (TextView) findViewById(R.id.Activity_Application_SecondPage);
        thirdCircle = (TextView) findViewById(R.id.Activity_Application_ThirdPage);
        CustomerRegistrationRegisterButton = (Button)findViewById(R.id.Activity_Application_Button_Register);
        CustomerRegistrationProgressBar = (ProgressBar) findViewById(R.id.Activity_Application_ProgressBar);
        CustomerRegistrationResultTV = (TextView) findViewById(R.id.Activity_Application_TextView_ResultTV);
        CustomerRegistrationRegisterButton.setOnClickListener(this);
        firstCircle.setOnClickListener(this);
        secondCircle.setOnClickListener(this);
        thirdCircle.setOnClickListener(this);
        if(LoginResponseConstants.idType == null || LoginResponseConstants.idType.isEmpty())
            thirdCircle.setVisibility(View.GONE);
        updatePageSelectionCircles(0);
    }

    private void updatePageSelectionCircles(int index){
        if(firstCircle == null)
            return;
        firstCircle.setBackgroundResource(R.drawable.cus_reg_cell_shape);
        secondCircle.setBackgroundResource(R.drawable.cus_reg_cell_shape);
        thirdCircle.setBackgroundResource(R.drawable.cus_reg_cell_shape);
        if(index == 0)
            firstCircle.setBackgroundResource(R.drawable.cus_reg_cell_shape_fill);
        else if(index == 1)
            secondCircle.setBackgroundResource(R.drawable.cus_reg_cell_shape_fill);
        else if(index == 2)
            thirdCircle.setBackgroundResource(R.drawable.cus_reg_cell_shape_fill);

    }

    @Override
    public void onClick(View v) {
        int fragmentIndex = 0;
        if(isQuickLinkTabAvailable())
            fragmentIndex = 3;
        else
            fragmentIndex = 2;
        switch (v.getId()) {
            case R.id.Activity_Application_FirstPage:
                updatePageSelectionCircles(0);
                mViewPager.setCurrentItem(fragmentIndex);
                break;
            case R.id.Activity_Application_SecondPage:
                updatePageSelectionCircles(1);
                fragmentIndex++;
                mViewPager.setCurrentItem(fragmentIndex);
                break;
            case R.id.Activity_Application_ThirdPage:
                updatePageSelectionCircles(2);
                fragmentIndex = fragmentIndex+2;
                mViewPager.setCurrentItem(fragmentIndex);
                break;
            case R.id.Activity_Application_Button_Register:
                int customerCreationFirstFragmentIndex = 0;
                if(isQuickLinkTabAvailable())
                    customerCreationFirstFragmentIndex = 3;
                else
                    customerCreationFirstFragmentIndex = 2;
                if(!validateCustomerRegistrationRequest().equalsIgnoreCase("OK"))
                {
                    if(mViewPager.getCurrentItem() != customerCreationFirstFragmentIndex)
                        mViewPager.setCurrentItem(customerCreationFirstFragmentIndex);
//                    Toast.makeText(v.getContext(), validateCustomerRegistrationRequest(), Toast.LENGTH_SHORT).show();
                    return;
                }
                CustomerRegistrationResultTV.setVisibility(View.GONE);
                CustomerRegistrationProgressBar.setVisibility(View.VISIBLE);
                CustomerRegistration customerRegistration = new CustomerRegistration(ApplicationActivity.this);
                customerRegistration.setCustomer(getNewCustomer());
                customerRegistration.getConnTaskManager().startBackgroundTask();
                break;
        }
    }

    private void destroyCustomerRegistrationComponents(){
        firstCircle = null;
        secondCircle = null;
        thirdCircle = null;
        CustomerRegistrationRegisterButton = null;
        CustomerRegistrationProgressBar = null;
        CustomerRegistrationResultTV = null;
    }

    private void resetViewPagerSize() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mViewPager.setLayoutParams(params);
        mViewPager.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.Item_logout)
        {
            activityHasBeenDestroyed = true;
            this.finish();
        }
        if(item.getItemId() == R.id.Item_About)
        {
            Dialog aboutDialog = new Dialog(this);
            aboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            aboutDialog.setContentView(R.layout.about_dialog);
            ((TextView)aboutDialog.findViewById(R.id.Dialog_About_TextView_Content)).setText("APP Version "+getString(R.string.APP_VERSION_INTERNAL));
            if(getString(R.string.app_name).toUpperCase(Locale.US).contains("ASSOTEL"))
            {
                ((TextView)aboutDialog.findViewById(R.id.Dialog_About_TextView_Content)).setTextColor(R.color.LOGIN_TEXT_COLOR);
            }
            aboutDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateTabsSelection(int position){

        TextView accountTitle = (TextView) findViewById(R.id.Activity_Application_TextView_Tab_Account);
        TextView quickLinkTitle = (TextView) findViewById(R.id.Activity_Application_TextView_Tab_QuickPay);
        TextView menuTitle = (TextView) findViewById(R.id.Activity_Application_TextView_Tab_Other);
        accountTitle.setTextColor(getResources().getColor(R.color.TAB_PRESSED));
        quickLinkTitle.setTextColor(getResources().getColor(R.color.TAB_PRESSED));
        menuTitle.setTextColor(getResources().getColor(R.color.TAB_PRESSED));
        setTabTextSize(accountTitle, quickLinkTitle, menuTitle);

        if(position == 0) {
            if (LoginResponseConstants.accountTabAvailable)
                updateUIComponentsWithFocus(accountLL, accountTitle, accountIV, position);
            else if (LoginResponseConstants.quickLinkTabAvailable)
                updateUIComponentsWithFocus(quickLinkLL, quickLinkTitle, quickLinkIV, position);
            else
                updateUIComponentsWithFocus(menuLL, menuTitle, menuIV, position);
        }
        else if(position == 1) {
            // in case Account Tab is Available and QuickLink Tab is Available
            if (LoginResponseConstants.accountTabAvailable && LoginResponseConstants.quickLinkTabAvailable)
                updateUIComponentsWithFocus(quickLinkLL, quickLinkTitle, quickLinkIV, position);
            else
                updateUIComponentsWithFocus(menuLL, menuTitle, menuIV, position);
        }
        else {
            // in case Account Tab is Available and QuickLink is Available
            // in case Account Tab is NOT Available and QuickLink is Available
            // in case Account Tab is Available and QuickLink is NOT Available
            // in case Account Tab is NOT Available and QuickLink is NOT Available
            updateUIComponentsWithFocus(menuLL, menuTitle, menuIV, position);
        }
    }


    private void updateUIComponentsWithFocus(LinearLayout pressedLayout, TextView pressedTextView, ImageView pressedImageView, int position){
        accountLL.setBackgroundResource(R.drawable.tab_normal);
        quickLinkLL.setBackgroundResource(R.drawable.tab_normal);
        menuLL.setBackgroundResource(R.drawable.tab_normal);
        accountIV.setImageResource(R.drawable.account_pressed);
        quickLinkIV.setImageResource(R.drawable.quicklink_pressed);
        menuIV.setImageResource(R.drawable.menu_pressed);
        actionbarTitleTV.setTextColor(getResources().getColor(R.color.ACTIONBAR_TITLE_TEXT_COLOR));

        switch (pressedLayout.getId()){
            case R.id.Activity_Application_LinearLayout_Tab_Account:
                actionbarTitleTV.setText(getString(R.string.TAB_1));
                pressedImageView.setImageResource(R.drawable.account_pressed);
                break;
            case R.id.Activity_Application_LinearLayout_Tab_QuickPay:
                actionbarTitleTV.setText(getString(R.string.TAB_2));
                pressedImageView.setImageResource(R.drawable.quicklink_pressed);
                break;
            case R.id.Activity_Application_LinearLayout_Tab_OTHER:
                actionbarTitleTV.setText(getString(R.string.TAB_3));
                pressedImageView.setImageResource(R.drawable.menu_pressed);
                break;

        }
        pressedLayout.setBackgroundResource(R.drawable.tab_pressed);
        pressedTextView.setTextColor(getResources().getColor(R.color.TEXT_NORMAL));
        if(mViewPager.getCurrentItem() < getNumberOfFragmentsAtLogin())
           LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(INTENT.RESET_FRAGMENTS.toString()).putExtra(INTENT.EXTRA_POS.toString(), position));
    }

    private boolean isFullTabs(){
        if(LoginResponseConstants.accountTabAvailable && LoginResponseConstants.quickLinkTabAvailable && isMenuTabAvailable())
            return true;
        else
            return false;
    }

    public class CustomFragmentStatePagerAdapter extends FragmentStatePagerAdapter{

        public CustomFragmentStatePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(getApplicationContext(), position);
        }

        @Override
        public int getItemPosition(Object object) {
            return fragmentSelection;
        }

        @Override
        public int getCount() {
            return variableNumberOfFragments;
        }
    }

    @Override
    public void onBackPressed() {
        if(isInSubMenu) {
            isInSubMenu = false;
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(INTENT.RESET_FRAGMENTS.toString()).putExtra(INTENT.EXTRA_POS.toString(), staticNumberOfFragment - 1));
        }
        else
            this.finish();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment{
        private static Context context;
        private static final String TAG = PlaceholderFragment.class.getSimpleName();

        public static PlaceholderFragment newInstance( Context context, int sectionNumber) {
            PlaceholderFragment.context = context;
            Fragment fragment = null;
            Log.d(ApplicationActivity.class.getSimpleName(),"newInstance() is called isQuickLinkTabAvailable()? "+isQuickLinkTabAvailable());
            Log.d(ApplicationActivity.class.getSimpleName(),"newInstance() is called receivedCustCreationIntent? "+receivedCustCreationIntent);
            Log.d(ApplicationActivity.class.getSimpleName(), "newInstance() is called Constants.quickLinks.size(): " + Constants.quickLinks.size());
            Log.d(ApplicationActivity.class.getSimpleName(),"newInstance() is called sectionNumber "+sectionNumber);
            switch (sectionNumber){
                case 0:
                    if(LoginResponseConstants.accountTabAvailable)
                        fragment = new AccountFragment();
                    else if(LoginResponseConstants.quickLinkTabAvailable)
                        fragment = new QuickLinkFragment();
                    else
                        fragment = new MenuFragment();
                    break;
                case 1:
                    if(LoginResponseConstants.accountTabAvailable && LoginResponseConstants.quickLinkTabAvailable)
                        fragment = new QuickLinkFragment();
                    else if(!LoginResponseConstants.accountTabAvailable && LoginResponseConstants.quickLinkTabAvailable
                            || (LoginResponseConstants.accountTabAvailable && !LoginResponseConstants.quickLinkTabAvailable))
                        fragment = new MenuFragment();
                    else
                    {
                        if(receivedCustCreationIntent)
                            fragment = new CustomerCreationFirstFragment();
                        else
                            fragment = getFragment(context, fragment);
                    }
                    break;
                case 2:
                    if(receivedCustCreationIntent && !LoginResponseConstants.quickLinkTabAvailable)
                        fragment = new CustomerCreationFirstFragment();
                    else if(isMenuTabAvailable() && LoginResponseConstants.accountTabAvailable && LoginResponseConstants.quickLinkTabAvailable)
                        fragment = new MenuFragment();
                    else if(!LoginResponseConstants.accountTabAvailable && !LoginResponseConstants.quickLinkTabAvailable)
                        fragment = getFragmentSecondLevel(context, fragment);
                    else
                        fragment = getFragment(context, fragment);
                    break;
                case 3:
                    if(receivedCustCreationIntent && LoginResponseConstants.quickLinkTabAvailable)
                        fragment = new CustomerCreationFirstFragment();
                    else if(receivedCustCreationIntent && !LoginResponseConstants.quickLinkTabAvailable)
                        fragment = new CustomerCreationSecondFragment();
                    else if(LoginResponseConstants.quickLinkTabAvailable)
                        fragment = getFragment(context, fragment);
                    else
                        fragment = getFragmentSecondLevel(context, fragment);
                    break;
                case 4:
                    if(receivedCustCreationIntent && LoginResponseConstants.quickLinkTabAvailable)
                        fragment = new CustomerCreationSecondFragment();
                    else if(receivedCustCreationIntent && !isQuickLinkTabAvailable())
                        fragment = new CustomerCreationThirdFragment();
                    else
                        fragment = getFragmentSecondLevel(context, fragment);
                    break;
                case 5:
                    fragment = new CustomerCreationThirdFragment();
                    break;
            }
            Log.d(ApplicationActivity.class.getSimpleName(),"returned Fragment is: "+fragment.getClass().getSimpleName());
            Log.d(ApplicationActivity.class.getSimpleName(),"++++++++++++++++++++++++++++++++");

            return (PlaceholderFragment) fragment;
        }

        private static Fragment getFragmentSecondLevel(Context context, Fragment fragment) {
            if(secondExtraFragmentName.equalsIgnoreCase(context.getString(R.string.BillPayment_Cable_TV)))
                fragment = new CableTVFragment();
            else if(secondExtraFragmentName.equalsIgnoreCase(context.getString(R.string.BillPayment_Utilities)))
                fragment = new UtilitiesFragment();
            else if(secondExtraFragmentName.equalsIgnoreCase(context.getString(R.string.app_name)))
                fragment = new SendMoneyFragment();
            else if(secondExtraFragmentName.equalsIgnoreCase(context.getString(R.string.BANKS)))
                fragment = new BankSendMoneyFragment();
            else if(secondExtraFragmentName.equalsIgnoreCase(context.getString(R.string.OTHER_OPERATORS)))
                fragment = new OtherOperatorsSendMoneyFragment();
            LocalBroadcastManager.getInstance(context).sendBroadcast(
                    new Intent(INTENT.UPDATE_ACTION_BAR_TITLE.toString()).putExtra(INTENT.EXTRA_TITLE.toString(),secondExtraFragmentName));
            return fragment;
        }

        private static Fragment getFragment(Context context, Fragment fragment) {
            isInSubMenu = true;
            if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.MAKE_PAYMENT))){
                if(TransferCode.bankCodes.size() == 0 && TransferCode.topupcreditCodes.size() == 0)
                    fragment = new SendMoneyFragment();
                else
                    fragment = new SendMoneyListFragment();

            }
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.RECEIVE_PAYMENT)))
                fragment = new ReceiveMoneyFragment();
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.GIVE_CASH_OUT)))
                fragment = new CashOutFragment();
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.RECEIVE_CASH_IN)))
                fragment = new CashinFragment();
//            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.RECEIVE_CASH_IN_CC)))
//                fragment = new DepositFundsFromCreditCardFragment();
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.REDEEM_VOUCHER)))
                fragment = new RedeemVoucherFragment();
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.TOP_UP)))
                fragment = new TopUpFragment();
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.BillPayments))){
                if(TransferCode.utilityCodes.size() != 0 && TransferCode.topupcreditCodes.size() != 0)
                    fragment = new BillPaymentsListFragment();
                else if(TransferCode.utilityCodes.size() == 0 && TransferCode.topupcreditCodes.size() == 0)
                    fragment = new CableTVFragment();
                else if(TransferCode.billPaymentCodes.size() == 0 && TransferCode.topupcreditCodes.size() == 0)
                    fragment = new UtilitiesFragment();
            }
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.CHANGE_PIN)))
                fragment = new ChangePinFragment();
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.TAG_REGISTRATION)))
                fragment = new TagRegistrationFragment();
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.CONFIG_SERVER)))
                fragment = new ServerConfigurationFragment();
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.SELECT_CURRENCY)))
                fragment = new DefaultCurrencyFragment();
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.CUSTOMER_REGISTRATION)))
                fragment = new CustomerLookupFragment();
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.CASH_OUT_VOUCHER)))
                fragment = new CashOutVoucherFragment();

            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.CASH_IN_VOUCHER))) {
                fragment = new ElectronicVoucherFragment();
                ElectronicVoucherFragment.isCashInOperation = true;
            }
            else if(firstExtraFragmentName.equalsIgnoreCase(context.getString(R.string.SELL_VOUCHER))) {
                fragment = new ElectronicVoucherFragment();
                ElectronicVoucherFragment.isCashInOperation = false;
            } else if (firstExtraFragmentName.equals(context.getString(R.string.BUY_VOUCHER))) {
                fragment = new BuyElectronicVoucherFragment();
                BuyElectronicVoucherFragment.isCashInOperation = false;
            } else if (firstExtraFragmentName.equals(context.getString(R.string.BUY_BULK_VOUCHER))) {
                fragment = new BuyElectronicVoucherFragment();
                BuyElectronicVoucherFragment.isCashInOperation = true;
            } else if (firstExtraFragmentName.equals(context.getString(R.string.GENERATE_TOKEN))) {
                fragment = new GenerateTokenFragment();
            }

            return fragment;
        }

    }


    private static boolean isMenuTabAvailable(){

        return (LoginResponseConstants.walletOptions.isMakePayment() ||
                LoginResponseConstants.walletOptions.isReceivePaymentAvailable()  ||
                LoginResponseConstants.walletOptions.isGiveCashOutAvailable() ||
                LoginResponseConstants.walletOptions.isReceiveCashInAvailable() ||
                LoginResponseConstants.walletOptions.isRedeemVoucherAvailable() ||
                LoginResponseConstants.walletOptions.isBillPayments() ||
                LoginResponseConstants.walletOptions.isSendTopup() ||
                LoginResponseConstants.walletOptions.isRegServicesTag() ||
                LoginResponseConstants.walletOptions.isChangePinAvailable() ||
                LoginResponseConstants.walletOptions.isServerConfigurable() ||
                LoginResponseConstants.walletOptions.isSelectCurrencyAvailable());
    }


    public static void hideCustomerRegistrationControl(Activity activity){
        LinearLayout controls = (LinearLayout)activity.findViewById(R.id.Activity_Application_LinearLayout_CustomerCreationControls);
        controls.setVisibility(View.GONE);
    }

    private static boolean isQuickLinkTabAvailable(){
        return Constants.quickLinks.size() != 0;
    }

    public static int convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int)(dp * (metrics.densityDpi / 160f));
        return px;
    }

    private String validateCustomerRegistrationRequest(){
        if(getNewCustomer().getMSISDN() == null || getNewCustomer().getMSISDN().isEmpty())
            return "Contact Number is mandatory field.";
        if(getNewCustomer().getGivenName() == null || getNewCustomer().getGivenName().isEmpty())
            return "Given Name is mandatory field.";
        if(getNewCustomer().getSurName() == null || getNewCustomer().getSurName().isEmpty())
            return "Surname mandatory field.";
        if(getNewCustomer().getDOB() == null  || getNewCustomer().getDOB().isEmpty() || getNewCustomer().getDOB().length() < 8)
            return "DOB is mandatory field.";
        return "OK";
    }

    private boolean validateCustomerRegistrationResponse(String serverMessage) {
        if(serverMessage == null)
            return false;
        String[] items = serverMessage.split(",");
        for (String item: items)
        {
            if(item.toUpperCase().startsWith("STATUS"))
            {
                String[] temp = item.split("=");
                if(temp[1].equalsIgnoreCase("0"))
                    return true;
                else
                    return false;
            }
        }
        return true;

    }
    private String getMessage(String response){
        String result = null;
        String[] items = response.split(",");
        for (String item: items)
        {
            if(item.toUpperCase(Locale.US).startsWith("MESSAGE"))
            {
                String[] temp = item.split("=");
                result = temp[1];
                break;
            }
        }
        return result;
    }

    private int getNumberOfFragmentsAtLogin(){
        // YES Account Yes QL Yes Menu
        if(LoginResponseConstants.accountTabAvailable && LoginResponseConstants.quickLinkTabAvailable && isMenuTabAvailable())
            return 3;
        // No Account Yes QL Yes Menu
        else  if(!LoginResponseConstants.accountTabAvailable && LoginResponseConstants.quickLinkTabAvailable && isMenuTabAvailable())
            return 2;
        // Yes Account No QL Yes Menu
        else  if(LoginResponseConstants.accountTabAvailable && !LoginResponseConstants.quickLinkTabAvailable && isMenuTabAvailable())
            return 2;
        // Yes Account YES QL No Menu
        else  if(LoginResponseConstants.accountTabAvailable && LoginResponseConstants.quickLinkTabAvailable && !isMenuTabAvailable())
            return 2;
        // No Account No QL Yes Menu
        else  if(!LoginResponseConstants.accountTabAvailable && !LoginResponseConstants.quickLinkTabAvailable&& isMenuTabAvailable())
            return 1;
        // No Account Yes QL No Menu
        else  if(!LoginResponseConstants.accountTabAvailable && LoginResponseConstants.quickLinkTabAvailable && !isMenuTabAvailable())
            return 1;
        // Yes Account No QL No Menu
        else  if(LoginResponseConstants.accountTabAvailable && !LoginResponseConstants.quickLinkTabAvailable&& !isMenuTabAvailable())
            return 1;
        return 0;
    }

    public static void hideKeyboard(Activity activity){
        if(activity != null && activity.getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void showKeyboard(Activity activity, EditText editText) {
        if(activity != null ) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
        }
    }

    public Customer getNewCustomer() {
        return newCustomer;
    }

    public void setNewCustomer(Customer newCustomer) {
        this.newCustomer = newCustomer;
    }



}
