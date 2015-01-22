package com.mobilis.android.nfc.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.Customer;
import com.mobilis.android.nfc.model.CustomerUpdate;
import com.mobilis.android.nfc.slidemenu.utils.LoginResponseConstants;
import com.mobilis.android.nfc.util.CustomerLookupUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ahmed on 3/08/14.
 */
public class CustomerLookupFragment extends ApplicationActivity.PlaceholderFragment implements  CustomerLookupUtil.Callable{

    View rootView;
    EditText CCContactNumberET;
    EditText CCGivenNameET;
    EditText CCSurNameET;
    EditText CCDOBET;
    TextView CCContactNumberTV;
    TextView resultTV;
    Button CCLookupButton;
    Button CCUpdateButton;
    Button registerCustomerButton;
    Spinner msisdnSpinner;
    ProgressBar progressBar;
    LinearLayout customerFoundLinearLayout;

    ArrayList<Customer> customers;
    CustomerLookupUtil customerUtil;
    BroadcastReceiver broadcastReceiver;
    private static Activity activity;
    @Override
    public void onResume() {
        super.onResume();
        registerBroadCastReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(activity == null)
            activity = getActivity();
        rootView = inflater.inflate(R.layout.fragment_customer_lookup, container, false);
        setUpComponents();
        return rootView;
    }
    
    private void setUpComponents(){
        customerFoundLinearLayout = (LinearLayout)rootView.findViewById(R.id.Fragment_CustomerLookup_LinearLayout_CustomerFound);
        registerCustomerButton = (Button)rootView.findViewById(R.id.Fragment_CustomerLookup_Button_RegisterCustomer);
        CCLookupButton = (Button) rootView.findViewById(R.id.Fragment_CustomerLookup_Button_Lookup);
        CCUpdateButton = (Button) rootView.findViewById(R.id.Fragment_CustomerLookup_Button_Update);
        CCContactNumberTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerLookup_TextView_CustomerNameTV);
        CCContactNumberET = (EditText) rootView.findViewById(R.id.Fragment_CustomerLookup_EditText_CustomerNameET);
        CCGivenNameET = (EditText)rootView.findViewById(R.id.Fragment_CustomerLookup_EditText_GivenNameET);
        CCSurNameET = (EditText)rootView.findViewById(R.id.Fragment_CustomerLookup_EditText_SurNameET);
        CCDOBET = (EditText)rootView.findViewById(R.id.Fragment_CustomerLookup_EditText_DOBET);
        msisdnSpinner = (Spinner)rootView.findViewById(R.id.Fragment_CustomerLookup_Spinner);

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
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(CCContactNumberET.getWindowToken(), 0);
                registerCustomerButton.setVisibility(View.GONE);
                customerFoundLinearLayout.setVisibility(View.GONE);
                progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_CustomerLookup_ProgressBar);
                progressBar.setVisibility(View.VISIBLE);
                if (resultTV != null)
                    resultTV.setVisibility(View.GONE);
                resultTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerLookup_TextView_ResultTV);
                resultTV.setVisibility(View.GONE);
                BackgroundTask task = new BackgroundTask();
                task.execute();
            }
        });
        CCUpdateButton.setVisibility(View.GONE); // TODO remove this to implement Update Customer feature

        CCUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomerUpdate cu = new CustomerUpdate(activity);
                cu.setCustomer(customers.get(msisdnSpinner.getSelectedItemPosition()));
                cu.getConnTaskManager().startBackgroundTask();
                progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_CustomerLookup_CustomerUpdateProgressBar);
                progressBar.setVisibility(View.VISIBLE);
                resultTV = (TextView)rootView.findViewById(R.id.Fragment_CustomerLookup_TextView_CustomerUpdateResultTV);
                resultTV.setVisibility(View.GONE);
            }
        });
    }

    public void extractCustomersFrom(String serverResponse)
    {
        ExtractCustomersTask task = new ExtractCustomersTask();
        task.execute(serverResponse);
    }


    private void registerBroadCastReceiver(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equalsIgnoreCase(INTENT.CUSTOMER_LOOKUP.toString())){
                    String serverResponse = intent.getStringExtra(INTENT.EXTRA_SERVER_RESPONSE.toString());
                    if(validateResponse(serverResponse))
                        extractCustomersFrom(serverResponse);
                    else
                        error(getMessage(serverResponse), getStatus(serverResponse));
                }
                else if(intent.getAction().equalsIgnoreCase(INTENT.CUSTOMER_UPDATE.toString())){
                    String serverResponse = intent.getStringExtra(INTENT.EXTRA_SERVER_RESPONSE.toString());
                    if(validateResponse(serverResponse))
                        finishedUpdatingCustomer("Customer updated successfully");
                    else
                        finishedUpdatingCustomer(getMessage(serverResponse));
                }

            }
        };
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CUSTOMER_LOOKUP.toString().toString()));
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CUSTOMER_UPDATE.toString().toString()));
    }

    public void unregisterReceiver(){
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    private class ExtractCustomersTask extends AsyncTask<String, Void, Void> {
        ArrayList<Customer> mCustomers;
        @Override
        protected Void doInBackground(String... params) {
            String serverResponse = params[0];
            String[] items = serverResponse.split(",");
            mCustomers = new ArrayList<Customer>();
            for (String item: items)
            {
                if(item.startsWith("DOB")){
                    Customer customer = new Customer();
                    String indexHalf = item.substring(3,item.length());
                    String[] temp = indexHalf.split("=");
                    String index = temp[0];
                    String dob = temp[1];
                    customer.setIndex(index);
                    customer.setDOB(dob);
                    mCustomers.add(customer);
                }
            }

            for (String item: items)
            {
                if(item.startsWith("GivenName")){
                    String indexHalf = item.substring(9,item.length());
                    String[] temp = indexHalf.split("=");
                    String index = temp[0];
                    String value = temp[1];
                    updateCustomer(index, "GivenName", value);
                }
                else if(item.startsWith("SurName")){
                    String indexHalf = item.substring(7,item.length());
                    String[] temp = indexHalf.split("=");
                    String index = temp[0];
                    String value = temp[1];
                    updateCustomer(index, "SurName", value);
                }
                else if(item.startsWith("MSISDN")){
                    String indexHalf = item.substring(6,item.length());
                    String[] temp = indexHalf.split("=");
                    String index = temp[0];
                    String value = temp[1];
                    updateCustomer(index, "MSISDN", value);
                }
                else if(item.toUpperCase(Locale.US).startsWith("CUSTOMERID")){
                    String indexHalf = item.substring(10,item.length());
                    String[] temp = indexHalf.split("=");
                    String index = temp[0];
                    String value = temp[1];
                    updateCustomer(index, "CUSTOMERID", value);
                }
            }

            return null;
        }
        private void updateCustomer(String index, String attribute, String value){
            for (Customer customer: mCustomers)
            {
                if(customer.getIndex().equalsIgnoreCase(index))
                {
                    if(attribute.equalsIgnoreCase("GivenName"))
                        customer.setGivenName(value);
                    else if(attribute.equalsIgnoreCase("SurName"))
                        customer.setSurName(value);
                    else if(attribute.equalsIgnoreCase("MSISDN"))
                        customer.setMSISDN(value);
                    else if(attribute.equalsIgnoreCase("CUSTOMERID"))
                        customer.setCustomerId(value);
                    break;
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finishedExtractingCustomers(mCustomers);
            for (Customer c: mCustomers){
                Log.d("AhmedC", "Index: " + c.getIndex());
                Log.d("AhmedC", "GiveName: "+c.getGivenName());
                Log.d("AhmedC", "SurName: "+c.getSurName());
                Log.d("AhmedC", "MSISDN: "+c.getMSISDN());
                Log.d("AhmedC", "DOB: "+c.getDOB());
                Log.d("AhmedC", "CustomerId: "+c.getCustomerId());
            }
        }
    }

    private boolean validateResponse(String serverMessage) {

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

    private int getStatus(String response){
        String result = null;
        String[] items = response.split(",");
        for (String item: items)
        {
            if(item.toUpperCase(Locale.US).startsWith("STATUS"))
            {
                String[] temp = item.split("=");
                result = temp[1];
                break;
            }
        }
        return Integer.parseInt(result);
    }

    private class BackgroundTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            if(customerUtil == null)
                customerUtil = new CustomerLookupUtil(activity, CustomerLookupFragment.this);
            customerUtil.lookupCustomers(CCContactNumberET.getText().toString());
            return null;
        }
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
        Log.d(CustomerLookupFragment.class.getSimpleName(), "activity == null? "+(activity==null));
        Log.d(CustomerLookupFragment.class.getSimpleName(), "spinnerList == null? "+(spinnerList==null));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(activity, R.layout.spinner_layout, R.id.Spinner_TextView, spinnerList);
        msisdnSpinner.setAdapter(spinnerAdapter);
        customerFoundLinearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void finishedUpdatingCustomer(String response) {

    }

    @Override
    public void error(String errorMessage, int status) {
        customerFoundLinearLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        resultTV.setText(errorMessage);
        resultTV.setVisibility(View.VISIBLE);
        if(errorMessage.toUpperCase(Locale.US).contains("INVALID ACCOUNT") || status != Integer.parseInt(getString(R.string.STATUS_OK)))
        {
            if(LoginResponseConstants.walletOptions.isCustomerRegistrationAvailable()) {
                registerCustomerButton.setVisibility(View.VISIBLE);
                registerCustomerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(INTENT.UPDATE_ACTION_BAR_TITLE.toString())
                                .putExtra(INTENT.EXTRA_TITLE.toString(), "Customer Registration"));
                        moveToCustomerRegistrationScreen(CCContactNumberET.getText().toString());
                    }
                });
            }
        }
    }

    private void moveToCustomerRegistrationScreen(String MSISDN){
        ((ApplicationActivity)activity).getNewCustomer().setMSISDN(MSISDN);
        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(INTENT.DECREASE_VIEW_PAGER_HEIGHT.toString()));
        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(INTENT.CUSTOMER_CREATION_SELECTION.toString()));
    }

}
