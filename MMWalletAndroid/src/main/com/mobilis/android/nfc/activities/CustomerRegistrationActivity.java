package com.mobilis.android.nfc.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.fragments.CustomerAddressFragment;
import com.mobilis.android.nfc.fragments.CustomerDetailsFragment;
import com.mobilis.android.nfc.fragments.CustomerIdVerificationFragment;
import com.mobilis.android.nfc.model.Customer;
import com.mobilis.android.nfc.model.CustomerRegistration;
import com.mobilis.android.nfc.slidemenu.utils.LoginResponseConstants;
import com.mobilis.android.nfc.tabsfragments.QuickLinkFragment;

import java.util.Locale;

/**
 * Created by ahmed on 29/07/14.
 */

public class CustomerRegistrationActivity extends Activity implements View.OnClickListener{

    private final int HEIGHT_DP = 430;
    private Customer customer;
    BroadcastReceiver broadcastReceiver;
    private int VIEW_PAGER_HEIGHT = 260;

    ViewPager mViewPager;
    FragmentStatePagerAdapter mSectionsPagerAdapter;
    ProgressBar progressBar;
    Button registerButton;
    TextView firstCircle ;
    TextView secondCircle ;
    TextView thirdCircle ;
    TextView resultTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_customer_registration);
        adjustRootWidthAndHeight();
        initPageAdapter();
        registerBroadCastReceiver();
        setCustomer(new Customer());
    }

    private void adjustRootWidthAndHeight() {
        LinearLayout root = (LinearLayout)findViewById(R.id.Activity_CustomerRegistration_LinearLayout_RootView);
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT_DP, getResources().getDisplayMetrics());
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(metrics.widthPixels, height);
        params.leftMargin = 15;
        params.rightMargin = 15;
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        root.setLayoutParams(params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadCastReceiver();
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
                if(intent.getAction().equalsIgnoreCase(INTENT.CUSTOMER_REGISTRATION.toString())){
                    String serverResponse = intent.getStringExtra(INTENT.EXTRA_SERVER_RESPONSE.toString());
                    if(validateResponse(serverResponse)){
                        resultTV.setText("Customer "+getCustomer().getMSISDN()+" registered successfully.");
                    }
                    else{
                        if(serverResponse != null)
                            resultTV.setText(getMessage(serverResponse));
                    }
                    progressBar.setVisibility(View.GONE);
                    resultTV.setVisibility(View.VISIBLE);
                    adjustRootWidthAndHeight();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CUSTOMER_REGISTRATION.toString().toString()));
    }

    private void initPageAdapter(){
        firstCircle = (TextView) findViewById(R.id.Activity_CustomerRegistration_FirstPage);
        secondCircle = (TextView) findViewById(R.id.Activity_CustomerRegistration_SecondPage);
        thirdCircle = (TextView) findViewById(R.id.Activity_CustomerRegistration_ThirdPage);
        registerButton = (Button) findViewById(R.id.Activity_CustomerRegistration_Button_Register);
        progressBar = (ProgressBar) findViewById(R.id.Activity_CustomerRegistration_ProgressBar);
        resultTV = (TextView) findViewById(R.id.Activity_CustomerRegistration_TextView_ResultTV);
        mViewPager = (ViewPager) findViewById(R.id.Dialog_CustomerRegistration_Pager);
        mSectionsPagerAdapter = new CustomFragmentStatePagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updatePageSelectionCircles(position);
            }
        });
        mViewPager.setCurrentItem(0);
        updatePageSelectionCircles(0);

        firstCircle.setOnClickListener(this);
        secondCircle.setOnClickListener(this);
        thirdCircle.setOnClickListener(this);
        registerButton.setOnClickListener(this);

        if(LoginResponseConstants.idType == null || LoginResponseConstants.idType.isEmpty())
            thirdCircle.setVisibility(View.GONE);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if(metrics.heightPixels > 500)
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ApplicationActivity.convertDpToPixel(VIEW_PAGER_HEIGHT, this));
            mViewPager.setLayoutParams(params);
        }

    }

    private void updatePageSelectionCircles(int index){
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
        switch (v.getId()) {
            case R.id.Activity_CustomerRegistration_FirstPage:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.Activity_CustomerRegistration_SecondPage:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.Activity_CustomerRegistration_ThirdPage:
                mViewPager.setCurrentItem(2);
                break;
            case R.id.Activity_CustomerRegistration_Button_Register:
                if(!validateRequest().equalsIgnoreCase("OK"))
                {
                    if(mViewPager.getCurrentItem() != 0)
                        mViewPager.setCurrentItem(0);
//                    Toast.makeText(v.getContext(),validateRequest(), Toast.LENGTH_SHORT).show();
                    return;
                }
                resultTV.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                adjustRootWidthAndHeight();
                CustomerRegistration customerRegistration = new CustomerRegistration(CustomerRegistrationActivity.this);
                customerRegistration.setCustomer(getCustomer());
                customerRegistration.getConnTaskManager().startBackgroundTask();
                break;
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public class CustomFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

        public CustomFragmentStatePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(CustomerRegistrationActivity.this, position);
        }

        @Override
        public int getCount() {
            if(LoginResponseConstants.idType == null || LoginResponseConstants.idType.isEmpty())
                return 2;
            return 3;
        }
    }

    public static class PlaceholderFragment extends Fragment{
        private static Context context;
        private static final String TAG = PlaceholderFragment.class.getSimpleName();

        public static PlaceholderFragment newInstance( Context context, int sectionNumber) {
            PlaceholderFragment.context = context;
            Fragment fragment = null;
            switch (sectionNumber){
                case 0:
                    fragment = new CustomerDetailsFragment();
                    break;
                case 1:
                    fragment = new CustomerAddressFragment();
                    break;
                case 2:
                    fragment = new CustomerIdVerificationFragment();
                    break;
            }
            return (PlaceholderFragment) fragment;
        }


        public void hideKeyboard() {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }
    private boolean validateResponse(String serverMessage) {
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

    private String validateRequest(){
        if(getCustomer().getMSISDN() == null || getCustomer().getMSISDN().isEmpty())
            return "Contact Number is mandatory field.";
        if(getCustomer().getGivenName() == null || getCustomer().getGivenName().isEmpty())
            return "Given Name is mandatory field.";
        if(getCustomer().getSurName() == null || getCustomer().getSurName().isEmpty())
            return "Surname mandatory field.";
        if(getCustomer().getDOB() == null  || getCustomer().getDOB().isEmpty() || getCustomer().getDOB().length() < 8)
            return "DOB is mandatory field.";
        return "OK";
    }

}
