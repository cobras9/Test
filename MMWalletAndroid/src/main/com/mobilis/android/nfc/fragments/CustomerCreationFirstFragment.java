package com.mobilis.android.nfc.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;

/**
 * Created by ahmed on 3/08/14.
 */
public class CustomerCreationFirstFragment extends ApplicationActivity.PlaceholderFragment{

    private FragmentStatePagerAdapter mSectionsPagerAdapter;

    private View rootView;
    EditText contactNumberET;
    EditText givenNameET;
    EditText surNameET;
    EditText emailAddressET;
    EditText dobET;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_customer_details, container, false);
        LinearLayout controls = (LinearLayout)getActivity().findViewById(R.id.Activity_Application_LinearLayout_CustomerCreationControls);
        controls.setVisibility(View.VISIBLE);
//        setUpComponents();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getActivity().getIntent().getStringExtra(INTENT.EXTRA_INITIAL_CONTACT_PHONE.toString()) != null) {
            final TextView contactNumberTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerDetails_TextView_ContactNumberTV);
            contactNumberET = (EditText) rootView.findViewById(R.id.Fragment_CustomerDetails_EditText_ContactNumberET);
            contactNumberET.setText(getActivity().getIntent().getStringExtra(INTENT.EXTRA_INITIAL_CONTACT_PHONE.toString()));
            contactNumberTV.setVisibility(View.INVISIBLE);
            givenNameET = (EditText) rootView.findViewById(R.id.Fragment_CustomerDetails_EditText_GivenNameET);
            givenNameET.requestFocus();
        }
    }

    private void setUpComponents(){
        contactNumberET = (EditText) rootView.findViewById(R.id.Fragment_CustomerDetails_EditText_ContactNumberET);
        givenNameET = (EditText) rootView.findViewById(R.id.Fragment_CustomerDetails_EditText_GivenNameET);
        surNameET = (EditText) rootView.findViewById(R.id.Fragment_CustomerDetails_EditText_SurNameET);
        emailAddressET = (EditText) rootView.findViewById(R.id.Fragment_CustomerDetails_EditText_EmailET);
        dobET = (EditText) rootView.findViewById(R.id.Fragment_CustomerDetails_EditText_DOBET);
        final TextView contactNumberTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerDetails_TextView_ContactNumberTV);
        final TextView givenNameTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerDetails_TextView_GivenNameTV);
        final TextView surNameTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerDetails_TextView_SurNameTV);
        final TextView dobTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerDetails_TextView_DOBTV);
        final TextView emailTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerDetails_TextView_EmailTV);

        if(givenNameET.getText().length() == 0)
            givenNameET.setError("Mandatory Field");
        else
            givenNameTV.setVisibility(View.GONE);

        if(surNameET.getText().length() == 0)
            surNameET.setError("Mandatory Field");
        else
            surNameTV.setVisibility(View.GONE);

        if(dobET.getText().length() != 8)
            dobET.setError("Mandatory Field");


        contactNumberET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(contactNumberET.getText().toString().isEmpty()) {
                    contactNumberTV.setVisibility(View.VISIBLE);
                    contactNumberET.setError("Mandatory Field");
                }
                else
                    contactNumberTV.setVisibility(View.GONE);
                ((ApplicationActivity)getActivity()).getNewCustomer().setMSISDN(contactNumberET.getText().toString());
            }
        });
        givenNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(givenNameET.getText().toString().isEmpty()) {
                    givenNameTV.setVisibility(View.VISIBLE);
                    givenNameET.setError("Mandatory Field");
                }
                else
                    givenNameTV.setVisibility(View.GONE);
                ((ApplicationActivity)getActivity()).getNewCustomer().setGivenName(givenNameET.getText().toString());
            }
        });
        surNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(surNameET.getText().toString().isEmpty()) {
                    surNameTV.setVisibility(View.VISIBLE);
                    surNameET.setError("Mandatory Field");
                }
                else
                    surNameTV.setVisibility(View.GONE);
                ((ApplicationActivity)getActivity()).getNewCustomer().setSurName(surNameET.getText().toString());
            }
        });
        emailAddressET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(emailAddressET.getText().toString().isEmpty())
                    emailTV.setVisibility(View.VISIBLE);
                else
                    emailTV.setVisibility(View.GONE);
                ((ApplicationActivity)getActivity()).getNewCustomer().setEmailAddress(emailAddressET.getText().toString());
            }
        });

        dobET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @SuppressWarnings("deprecation")
            @Override
            public void afterTextChanged(Editable s) {
                updateDOBFields(dobTV);
                dobET.setSelection(dobET.getText().toString().length(),dobET.getText().toString().length());
                ((ApplicationActivity)getActivity()).getNewCustomer().setDOB(dobET.getText().toString());
            }

        });
        contactNumberET.setText(((ApplicationActivity)getActivity()).getNewCustomer().getMSISDN());
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpComponents();
        dobET = (EditText) rootView.findViewById(R.id.Fragment_CustomerDetails_EditText_DOBET);
        final TextView dobTV = (TextView) rootView.findViewById(R.id.Fragment_CustomerDetails_TextView_DOBTV);
        updateDOBFields(dobTV);
    }

    private void updateDOBFields(TextView dobTV) {
        if(dobET.getText().toString().isEmpty())
            dobET.setCursorVisible(true);
        else
            dobET.setCursorVisible(false);
        if(dobET.getText().toString().length() == 8) {
            dobTV.setBackgroundResource(Color.TRANSPARENT);
            dobTV.setTextColor(Color.BLACK);
        }
        else {
//            dobTV.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_mandatory_field));
//            dobTV.setTextColor(Color.WHITE);
            dobET.setError("Mandatory Field");
        }
        Spannable textToSpan = null;
        if(dobET.getText().toString().isEmpty())
        {
            dobTV.setText("DOB yyyy/mm/dd *");
            dobTV.setVisibility(View.VISIBLE);
        }
        else if(dobET.getText().toString().length() == 1){
            dobTV.setTextColor(Color.DKGRAY);
            dobTV.setVisibility(View.VISIBLE);
            textToSpan = new SpannableString(dobET.getText().toString().concat("yyy/mm/dd"));
            textToSpan.setSpan(new ForegroundColorSpan(Color.DKGRAY), 1, textToSpan.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else if(dobET.getText().toString().length() == 2){
            textToSpan = new SpannableString(dobET.getText().toString().concat("yy/mm/dd"));
            textToSpan.setSpan(new ForegroundColorSpan(Color.DKGRAY), 2, textToSpan.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else if(dobET.getText().toString().length() == 3){
            textToSpan = new SpannableString(dobET.getText().toString().concat("y/mm/dd"));
            textToSpan.setSpan(new ForegroundColorSpan(Color.DKGRAY), 3, textToSpan.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        else if(dobET.getText().toString().length() == 4)
        {
            textToSpan = new SpannableString(dobET.getText().toString().concat("/mm/dd"));
            textToSpan.setSpan(new ForegroundColorSpan(Color.DKGRAY), 4, textToSpan.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else if(dobET.getText().toString().length() == 5)
        {
            textToSpan = new SpannableString(dobET.getText().toString().substring(0, 4)+"/"+dobET.getText().toString().substring(4, 5)+"m/dd");
            textToSpan.setSpan(new ForegroundColorSpan(Color.DKGRAY), 6, textToSpan.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else if(dobET.getText().toString().length() == 6)
        {
            textToSpan = new SpannableString(dobET.getText().toString().substring(0, 4).concat("/" + dobET.getText().toString().substring(4, 6) + "/dd"));
            textToSpan.setSpan(new ForegroundColorSpan(Color.DKGRAY), 7, textToSpan.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else if(dobET.getText().toString().length() == 7)
        {
            textToSpan = new SpannableString(dobET.getText().toString().substring(0, 4).concat("/"+dobET.getText().toString().substring(4, 6)).concat("/"+dobET.getText().toString().substring(6, 7)).concat("d"));
            textToSpan.setSpan(new ForegroundColorSpan(Color.DKGRAY), 9, textToSpan.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else if(dobET.getText().toString().length() == 8){
            textToSpan = new SpannableString(dobET.getText().toString().substring(0, 4)+"/"+dobET.getText().toString().substring(4, 6)+"/"+dobET.getText().toString().substring(6, 8));
        }
        if(!dobET.getText().toString().isEmpty()) {
            dobET.setTextColor(Color.TRANSPARENT);
            dobTV.setText(textToSpan);
        }
    }







}
