package com.mobilis.android.nfc.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.model.Login;

import java.util.ArrayList;

public class DefaultCurrencyFragment extends ApplicationActivity.PlaceholderFragment{

    Button updateButton;
    TextView resultTV;
    ProgressBar progressBar;

    String defaultCurrency;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.currency_view, container, false);

        updateButton = (Button) rootView.findViewById(R.id.Fragment_DefaultCurrency_Button_Update);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_DefaultCurrency_TextView_Result);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_DefaultCurrency_Progressbar);

        Spinner spinner = (Spinner) rootView.findViewById(R.id.currencySpinner);
        spinner.setAdapter(new CustomArrayAdapter<String>(getActivity(), getCurrencyList()));
        spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
        int index = 0;

        final AbstractModel model = new Login(getActivity());
        String savedCurr = model.getWorkingCurrency();
        index = getCurrencyList().indexOf(savedCurr);
        spinner.setSelection(index);
        defaultCurrency = savedCurr;

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.saveDefaultCurrency(defaultCurrency);
                progressBar.setVisibility(View.VISIBLE);
                resultTV.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        resultTV.setText("Default Currency: "+defaultCurrency);
                        resultTV.setVisibility(View.VISIBLE);
                    }
                }, 750);
            }
        });
        return rootView;
    }

	class CustomArrayAdapter<T> extends ArrayAdapter<T>
	{
	    public CustomArrayAdapter(Context ctx, ArrayList<T> objects)
	    {
	        super(ctx, android.R.layout.simple_spinner_item, objects);
	    }

	    @Override
	    public View getDropDownView(int position, View convertView, ViewGroup parent)
	    {
	        View view = super.getView(position, convertView, parent);

	        //we know that simple_spinner_item has android.R.id.text1 TextView:         

	        /* if(isDroidX) {*/
	            TextView text = (TextView)view.findViewById(android.R.id.text1);
                text.setPadding(30,30,0,30);
	            text.setTextColor(Color.BLACK);//choose your color :)         
	        /*}*/

	        return view;

	    }
	}
	class CustomOnItemSelectedListener implements OnItemSelectedListener {
		 
		  public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			  
			  ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
			  Log.d(DefaultCurrencyFragment.class.getSimpleName(), "selected currency: "+parent.getItemAtPosition(pos).toString());
              defaultCurrency =  parent.getItemAtPosition(pos).toString();
		  }
		 
		  @Override
		  public void onNothingSelected(AdapterView<?> arg0) {

		  }
	}
	
	private ArrayList<String> getCurrencyList(){
		ArrayList<String> currencyList = new ArrayList<String>();
		currencyList.add("AED");
		currencyList.add("AFN");
		currencyList.add("ALL");
		currencyList.add("AMD");
		currencyList.add("ANG");
		currencyList.add("AOA");
		currencyList.add("ARS");
		currencyList.add("AUD");
		currencyList.add("AWG");
		currencyList.add("AZN");
		currencyList.add("BAM");
		currencyList.add("BBD");
		currencyList.add("BDT");
		currencyList.add("BGN");
		currencyList.add("BHD");
		currencyList.add("BIF");
		currencyList.add("BMD");
		currencyList.add("BND");
		currencyList.add("BOB");
		currencyList.add("BRL");
		currencyList.add("BSD");
		currencyList.add("BTN");
		currencyList.add("BWP");
		currencyList.add("BYR");
		currencyList.add("BZD");
		currencyList.add("CAD");
		currencyList.add("CDF");
		currencyList.add("CHF");
		currencyList.add("CLP");
		currencyList.add("CNY");
		currencyList.add("COP");
		currencyList.add("CRC");
		currencyList.add("CUC");
		currencyList.add("CUP");
		currencyList.add("CVE");
		currencyList.add("CZK");
		currencyList.add("DJF");
		currencyList.add("DKK");
		currencyList.add("DOP");
		currencyList.add("DZD");
		currencyList.add("EGP");
		currencyList.add("ERN");
		currencyList.add("ETB");
		currencyList.add("EUR");
		currencyList.add("FJD");
		currencyList.add("FKP");
		currencyList.add("GBP");
		currencyList.add("GEL");
		currencyList.add("GGP");
		currencyList.add("GHS");
		currencyList.add("GIP");
		currencyList.add("GMD");
		currencyList.add("GNF");
		currencyList.add("GTQ");
		currencyList.add("GYD");
		currencyList.add("HKD");
		currencyList.add("HNL");
		currencyList.add("HRK");
		currencyList.add("HTG");
		currencyList.add("HUF");
		currencyList.add("IDR");
		currencyList.add("ILS");
		currencyList.add("IMP");
		currencyList.add("INR");
		currencyList.add("IQD");
		currencyList.add("IRR");
		currencyList.add("ISK");
		currencyList.add("JEP");
		currencyList.add("JMD");
		currencyList.add("JOD");
		currencyList.add("JPY");
		currencyList.add("KES");
		currencyList.add("KGS");
		currencyList.add("KHR");
		currencyList.add("KMF");
		currencyList.add("KPW");
		currencyList.add("KWD");
		currencyList.add("KYD");
		currencyList.add("KZT");
		currencyList.add("LAK");
		currencyList.add("LBP");
		currencyList.add("LKR");
		currencyList.add("LRD");
		currencyList.add("LSL");
		currencyList.add("LTL");
		currencyList.add("LVL");
		currencyList.add("LYD");
		currencyList.add("MAD");
		currencyList.add("MDL");
		currencyList.add("MGA");
		currencyList.add("MKD");
		currencyList.add("MMK");
		currencyList.add("MNT");
		currencyList.add("MOP");
		currencyList.add("MRO");
		currencyList.add("MUR");
		currencyList.add("MVR");
		currencyList.add("MWK");
		currencyList.add("MXN");
		currencyList.add("MYR");
		currencyList.add("MZN");
		currencyList.add("NAD");
		currencyList.add("NGN");
		currencyList.add("NIO");
		currencyList.add("NOK");
		currencyList.add("NPR");
		currencyList.add("NZD");
		currencyList.add("OMR");
		currencyList.add("PAB");
		currencyList.add("PEN");
		currencyList.add("PGK");
		currencyList.add("PHP");
		currencyList.add("PKR");
		currencyList.add("PLN");
		currencyList.add("PRB");
		currencyList.add("PYG");
		currencyList.add("QAR");
		currencyList.add("RON");
		currencyList.add("RSD");
		currencyList.add("RUB");
		currencyList.add("RWF");
		currencyList.add("SAR");
		currencyList.add("SBD");
		currencyList.add("SCR");
		currencyList.add("SDG");
		currencyList.add("SEK");
		currencyList.add("SGD");
		currencyList.add("SHP");
		currencyList.add("SLL");
		currencyList.add("SOS");
		currencyList.add("SRD");
		currencyList.add("SSP");
		currencyList.add("STD");
		currencyList.add("SYP");
		currencyList.add("SZL");
		currencyList.add("THB");
		currencyList.add("TJS");
		currencyList.add("TMT");
		currencyList.add("TND");
		currencyList.add("TOP");
		currencyList.add("TRY");
		currencyList.add("TRY");
		currencyList.add("TTD");
		currencyList.add("TWD");
		currencyList.add("TZS");
		currencyList.add("UAH");
		currencyList.add("UGX");
		currencyList.add("USD");
		currencyList.add("UYU");
		currencyList.add("UZS");
		currencyList.add("VEF");
		currencyList.add("VND");
		currencyList.add("VUV");
		currencyList.add("WST");
		currencyList.add("XAF");
		currencyList.add("XCD");
		currencyList.add("XOF");
		currencyList.add("XPF");
		currencyList.add("YER");
		currencyList.add("ZAR");
		currencyList.add("ZMW");
		return currencyList;
	}
}