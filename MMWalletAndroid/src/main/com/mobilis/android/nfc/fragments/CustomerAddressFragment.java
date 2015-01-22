package com.mobilis.android.nfc.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.CustomerRegistrationActivity;

/**
 * Created by ahmed on 29/07/14.
 */
public class CustomerAddressFragment extends CustomerRegistrationActivity.PlaceholderFragment {

    View rootView;
    EditText address1ET;
    EditText address2ET;
    EditText cityET;
    EditText stateET;
    EditText countryET;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_customer_address, container, false);
        setUpComponents();
        return rootView;
    }

    
    private void setUpComponents(){
        address1ET = (EditText) rootView.findViewById(R.id.Fragment_CustomerAddress_EditText_Address1ET);
        address2ET = (EditText) rootView.findViewById(R.id.Fragment_CustomerAddress_EditText_Address2ET);
        cityET = (EditText) rootView.findViewById(R.id.Fragment_CustomerAddress_EditText_CityET);
        stateET = (EditText) rootView.findViewById(R.id.Fragment_CustomerAddress_EditText_StateET);
        countryET = (EditText) rootView.findViewById(R.id.Fragment_CustomerAddress_EditText_CountryET);
        final TextView address1TV = (TextView) rootView.findViewById(R.id.Fragment_CustomerAddress_TextView_Address1TV);
        final TextView address2TV = (TextView) rootView.findViewById(R.id.Fragment_CustomerAddress_TextView_Address2TV);
        final TextView cityTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerAddress_TextView_CityTV);
        final TextView stateTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerAddress_TextView_StateTV);
        final TextView countryTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerAddress_TextView_CountryTV);
        address1ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(address1ET.getText().toString().isEmpty())
                    address1TV.setVisibility(View.VISIBLE);
                else
                    address1TV.setVisibility(View.GONE);
                ((CustomerRegistrationActivity)getActivity()).getCustomer().getCustomerAddress().setAddress1(address1ET.getText().toString());
            }
        });
        address2ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(address2ET.getText().toString().isEmpty())
                    address2TV.setVisibility(View.VISIBLE);
                else
                    address2TV.setVisibility(View.GONE);
                ((CustomerRegistrationActivity)getActivity()).getCustomer().getCustomerAddress().setAddress2(address2ET.getText().toString());
            }
        });
        cityET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (cityET.getText().toString().isEmpty())
                    cityTV.setVisibility(View.VISIBLE);
                else
                    cityTV.setVisibility(View.GONE);

                ((CustomerRegistrationActivity)getActivity()).getCustomer().getCustomerAddress().setCity(cityET.getText().toString());
            }
        });
        stateET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (stateET.getText().toString().isEmpty())
                    stateTV.setVisibility(View.VISIBLE);
                else
                    stateTV.setVisibility(View.GONE);

                ((CustomerRegistrationActivity)getActivity()).getCustomer().getCustomerAddress().setState(stateET.getText().toString());
            }
        });

        countryET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Spannable textToSpan = null;
                if (countryET.getText().toString().isEmpty())
                    countryTV.setVisibility(View.VISIBLE);
                else
                    countryTV.setVisibility(View.GONE);
                ((CustomerRegistrationActivity)getActivity()).getCustomer().getCustomerAddress().setCountry(countryET.getText().toString());
            }
        });

    }
}
