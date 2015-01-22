package com.mobilis.android.nfc.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.Customer;
import com.mobilis.android.nfc.model.CustomerLookup;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ahmed on 25/07/14.
 */
public class CustomerLookupUtil {

    private Activity activity;
    private Callable callable;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(activity != null && broadcastReceiver != null)
            unregisterReceiver();
    }

    public CustomerLookupUtil(Activity activity, Callable callable){
        this.activity = activity;
        this.callable = callable;
        registerBroadCastReceiver();
    }

    public void lookupCustomers(String MSISDN){
        CustomerLookup customerLookup = new CustomerLookup(activity);
        customerLookup.setMsisdn(MSISDN);
        customerLookup.getConnTaskManager().startBackgroundTask();
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
                        callable.error(getMessage(serverResponse), getStatus(serverResponse));
                }
                else if(intent.getAction().equalsIgnoreCase(INTENT.CUSTOMER_UPDATE.toString())){
                    String serverResponse = intent.getStringExtra(INTENT.EXTRA_SERVER_RESPONSE.toString());
                    if(validateResponse(serverResponse))
                        callable.finishedUpdatingCustomer("Customer updated successfully");
                    else
                        callable.finishedUpdatingCustomer(getMessage(serverResponse));
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
            callable.finishedExtractingCustomers(mCustomers);
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
        if(serverMessage == null)
            return  false;
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

    public interface Callable{
        public void finishedExtractingCustomers(ArrayList<Customer> customers);
        public void finishedUpdatingCustomer(String response);
        public void error(String errorMessage, int status);
    }



}
