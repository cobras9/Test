package com.mobilis.android.nfc.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.activities.CustomerRegistrationActivity;
import com.mobilis.android.nfc.slidemenu.utils.CustomerType;
import com.mobilis.android.nfc.slidemenu.utils.IDType;
import com.mobilis.android.nfc.slidemenu.utils.LoginResponseConstants;

import java.util.ArrayList;

/**
 * Created by ahmed on 29/07/14.
 */
public class CustomerIdVerificationFragment extends CustomerRegistrationActivity.PlaceholderFragment {

    View rootView;
    EditText IdET;
    Spinner spinner;
    Spinner customerTypeSpinner;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_customer_idverification, container, false);
        activity = getActivity();
        setUpComponents();
        return rootView;
    }

    
    private void setUpComponents(){

        IdET = (EditText) rootView.findViewById(R.id.Fragment_CustomerIdVerification_EditText_IdET);
        spinner = (Spinner) rootView.findViewById(R.id.Fragment_CustomerIdVerification_Spinner);
        customerTypeSpinner = (Spinner) rootView.findViewById(R.id.Fragment_CustomerIdVerification_Spinner_CustomerType);
        final TextView idTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerIdVerification_TextView_IdTV);

        IdET.setEnabled(false);
        IdET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(IdET.getText().toString().isEmpty()) {
                    idTV.setVisibility(View.VISIBLE);
                    idTV.setText(getString(R.string.ID_VERIFICATION_TYPE));
                }
                else
                    idTV.setVisibility(View.GONE);
                ((CustomerRegistrationActivity)getActivity()).getCustomer().setCustomerVerificationId(IdET.getText().toString());
            }
        });
        final ArrayList<String> spinnerList = new ArrayList<String>();
        spinnerList.add("NONE");
        for (IDType type: LoginResponseConstants.idType){
            spinnerList.add(type.getIDType().toString());
        }
        setUpIdVerifiedSpinner(idTV, spinnerList);
        setUpCustomerTypeSpinner();
    }

    private void setUpIdVerifiedSpinner(final TextView idTV, final ArrayList<String> spinnerList) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, R.id.Spinner_TextView, spinnerList);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String idType = spinnerList.get(position);
                if(idType.equalsIgnoreCase("NONE"))
                {
                    ((CustomerRegistrationActivity)getActivity()).getCustomer().setiDVerified(false);
                    ((CustomerRegistrationActivity)getActivity()).getCustomer().setIdType(null);
                    IdET.setEnabled(false);
                    idTV.setText(getString(R.string.NOT_AVAILABLE));
                    if(!IdET.getText().toString().isEmpty()) {
                        IdET.setTextColor(Color.TRANSPARENT);
                        idTV.setVisibility(View.VISIBLE);
                    }
                }
                else if(idType.equalsIgnoreCase(IDType.TYPE.PHOTO_ID.toString()))
                {
                    idTV.setVisibility(View.VISIBLE);
                    idTV.setText("Verified");
                    IdET.setEnabled(false);
                    IdET.setTextColor(Color.TRANSPARENT);
                }
                else{
                    ((CustomerRegistrationActivity)getActivity()).getCustomer().setiDVerified(true);
                    ((CustomerRegistrationActivity)getActivity()).getCustomer().setIdType(spinnerList.get(position));
                    IdET.setEnabled(true);
                    IdET.setTextColor(Color.BLACK);
                    idTV.setText(getString(R.string.ID_VERIFICATION_TYPE));
                    if(!IdET.getText().toString().isEmpty())
                        idTV.setVisibility(View.INVISIBLE);
                }
             }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(0);
    }

    private void setUpCustomerTypeSpinner() {
        final ArrayList<String> customerTypesList = new ArrayList<String>();
        final ArrayList<CustomerType> custTypesObjList = new ArrayList<CustomerType>();
        for (CustomerType customerType: LoginResponseConstants.customerTypes) {
            customerTypesList.add(customerType.getCustomerType().getLabel());
            custTypesObjList.add(customerType);
        }

        ArrayAdapter<String> customerTypeSpinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, R.id.Spinner_TextView, customerTypesList);
        customerTypeSpinner.setAdapter(customerTypeSpinnerAdapter);
        customerTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setCustomerType(custTypesObjList.get(position).getCustomerType().getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        customerTypeSpinner.setSelection(0);
        setCustomerType(customerTypesList.get(0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity = null;
    }

    private void setCustomerType(String customerType){
        final String activityName = activity.getClass().getSimpleName();
        if(activityName.equalsIgnoreCase(ApplicationActivity.class.getSimpleName()))
            ((ApplicationActivity)activity).getNewCustomer().setCustomerType(customerType);
        else if(activityName.equalsIgnoreCase(CustomerRegistrationActivity.class.getSimpleName()))
            ((CustomerRegistrationActivity)activity).getCustomer().setCustomerType(customerType);
    }

}
