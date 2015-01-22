package com.mobilis.android.nfc.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.model.TagPurchase;
import com.mobilis.android.nfc.network.ConnTaskManager;
import com.mobilis.android.nfc.util.Constants;
import com.mobilis.android.nfc.util.SharedPreferencesException;

import java.text.DecimalFormat;
import java.util.List;

public class TagPurchaseActivity extends Activity implements TextWatcher{

	private TagPurchase model;
	private Dialog displayDialog;
//	private final String FONT = "fonts/KTF-Roadbrush.ttf"; 
	private TextView fraudTV;
	String nfcId, desc, price, clientId;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tagviewer_view);

		fraudTV = (TextView)findViewById(R.id.fraudTV);
		fraudTV.setVisibility(View.INVISIBLE);
		
		setModel(new TagPurchase(this, this));
		desc = null;
		
		DecimalFormat df = new DecimalFormat("0.00##");
		
		nfcId = getIntent().getExtras().getString(getResString(R.string.EXTRA_DATA_NFCID));
		desc = getIntent().getExtras().getString(getResString(R.string.EXTRA_DATA_DESC));
		clientId = getIntent().getExtras().getString(getResString(R.string.EXTRA_DATA_EMBDEDDED_CLIENTID));
		String s = getIntent().getExtras().getString(getResString(R.string.EXTRA_DATA_PRICE));
		if (nfcId == null && s != null)
			price = df.format(Double.parseDouble(getIntent().getExtras().getString(getResString(R.string.EXTRA_DATA_PRICE)))/100).toString();
		else
			price = getIntent().getExtras().getString(getResString(R.string.EXTRA_DATA_PRICE));
		
		if(displayDialog != null && displayDialog.isShowing())
			displayDialog.dismiss();
	
		displayDialog = new Dialog(this);
		getModel().setDisplayDialog(displayDialog);
		displayDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		WindowManager.LayoutParams wmlp = displayDialog.getWindow().getAttributes();
		wmlp.y = 30;   //y position
		displayDialog.setContentView(R.layout.tag_purchase_dialog_view);   
		displayDialog.setCanceledOnTouchOutside(false);
		displayDialog.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				Log.d("mobilisDemo", "displayDialog is being dismissed..will shut down soon");
				model.getActivity().finish();
			}
		});
		
		Log.d("mobilisDemo", "beginning of TagPurchase Activity where desc is: "+Constants.itemDescription);
		if(nfcId != null)
			processPurchase(getIntent());
	
		Uri data = getIntent().getData();
		if(data != null)
		{
			handleQRCode();	
		}
		
		
	}

	public String getResString(int res){
		return getResources().getString(res);
	}
	
	@SuppressLint("UseValueOf")
	private void handleQRCode(){
		
		Uri data = getIntent().getData();
		String scheme = data.getScheme(); // "http"
		String host = data.getHost(); // "twitter.com"
		List<String> params = data.getPathSegments();
		
		String checksum = null;
		if(params.size() > 3)
			checksum = params.get(3);
		String cost = params.get(0); // "status"
		Log.d("ahmed", "QR Code..cost is: "+cost);
		DecimalFormat df = new DecimalFormat("0.00##");
		cost = df.format(Double.parseDouble(cost)/100);	
		final String desc = params.get(1); // "1234"
		final String merchantId = params.get(2);
		Log.d("ahmed", "QR Code..cost after /100 and formatting is: "+cost);
		Log.d("mobilisDemo", "******QR Code data******");
		Log.d("mobilisDemo", "scheme: "+scheme);
		Log.d("mobilisDemo", "host: "+host);
		Log.d("mobilisDemo", "params size: "+params.size());
		Log.d("mobilisDemo", "cost: "+cost);
		Log.d("mobilisDemo", "desc: "+desc);
		Log.d("mobilisDemo", "merchantId: "+merchantId);
		Log.d("mobilisDemo", "checksum: "+checksum);
		
		getModel().setChecksum(checksum);
		getModel().setWorkingAmount(cost);
		TextView enterPinTV = (TextView) displayDialog.findViewById(R.id.EnterPinTextV);
		TextView productDesc = (TextView) displayDialog.findViewById(R.id.productName);
		TextView productPrice = (TextView) displayDialog.findViewById(R.id.productPrice);
		Log.d("mobilisDemo", "inside procesPurchase desc is: "+Constants.itemDescription);

		productDesc.setText(desc);
		productPrice.setText(Constants.currency+" "+cost);
		enterPinTV.setText("Enter PIN:");
		
//		Typeface type = Typeface.createFromAsset(getAssets(),FONT);
//		productDesc.setTypeface(type);
//		productPrice.setTypeface(type);
		
		((TagPurchase) getModel()).setItemDescription(desc);
		getModel().setClientId(merchantId);
		
		final EditText pinET = (EditText) displayDialog.findViewById(R.id.ndefPinET);
		pinET.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(final TextView textView, int actionId, KeyEvent keyEvent) {
				if(getModel().pinExists(pinET.getText().toString())){
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						getModel().setClientPin(pinET.getText().toString());
						executeBackEndProcess(pinET);
					}
				}
				else
					getModel().showPinError(displayDialog.getContext());
				return false;
			}
		});
		pinET.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				displayDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		});
		pinET.requestFocus();
		
		
		Button okButton = (Button) displayDialog.findViewById(R.id.ndefButton);
		okButton.setClickable(true);
		okButton.setOnClickListener(new OnClickListener() {	
			public void onClick(View view) {
				if(getModel().pinExists(pinET.getText().toString()))
					executeBackEndProcess(pinET);
				else
					getModel().showPinError(displayDialog.getContext());
			}
		});
		displayDialog.show();
		
	
	}
	
	@Override
	public void onBackPressed() {
		
		super.onBackPressed();
	}
	public void afterTextChanged(Editable arg0) {
		
	}
	
	private void processPurchase(Intent intent) {
		Log.d("mobilisDemo", "processPurchase is called");
		if(displayDialog != null && displayDialog.isShowing())
			displayDialog.dismiss();
		TextView enterPinTV = (TextView) displayDialog.findViewById(R.id.EnterPinTextV);
		TextView productDesc = (TextView) displayDialog.findViewById(R.id.productName);
		TextView productPrice = (TextView) displayDialog.findViewById(R.id.productPrice);
		Log.d("mobilisDemo", "inside procesPurchase desc is: "+Constants.itemDescription);

		productDesc.setText(Constants.itemDescription);
		productPrice.setText(Constants.currency+" "+price);
		enterPinTV.setText("Enter PIN:");
		
//		Typeface type = Typeface.createFromAsset(getAssets(), FONT);
//		productDesc.setTypeface(type);
//		productPrice.setTypeface(type);

		getModel().setClientId(clientId);
		getModel().setNFCId(nfcId);
		getModel().setItemDescription(desc);
		getModel().setWorkingAmount(price);
			 
		final EditText pinET = (EditText) displayDialog.findViewById(R.id.ndefPinET);
		pinET.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(final TextView textView, int actionId, KeyEvent keyEvent) {
				if(getModel().pinExists(pinET.getText().toString())){
					if (actionId == EditorInfo.IME_ACTION_DONE) {
							getModel().setClientPin(pinET.getText().toString());
							executeBackEndProcess(pinET);	
						}
					}
				else
					getModel().showPinError(displayDialog.getContext());
				return false;
			}
		});
		pinET.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				displayDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		});
		pinET.requestFocus();
		
		Button okButton = (Button) displayDialog.findViewById(R.id.ndefButton);
		okButton.setClickable(true);
		okButton.setOnClickListener(new OnClickListener() {	
			public void onClick(View view) {
				if(getModel().pinExists(pinET.getText().toString()))
					executeBackEndProcess(pinET);
				else
					getModel().showPinError(displayDialog.getContext());
			}
		});
		displayDialog.show();

	}	

	public void executeBackEndProcess(EditText editText) {
		if(!model.isNetworkAvailable()){
			
			return;
		}
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		getModel().setClientPin(editText.getText().toString());
		
		getModel().startLoadingAnimation(displayDialog);
		try {
			getModel().saveConnectionDetails();
		} catch (SharedPreferencesException e) {
			getModel().setResponseStatus(666);
			getModel().verifyPostTaskResults();
			e.printStackTrace();
			return;
		}
		// SLEEP 2 SECONDS HERE ...
	    Handler handler = new Handler(); 
	    handler.postDelayed(new Runnable() { 
	         public void run() { 
	        	 Log.d("ahmed", "TagPurchaseActivity will start background process now");
	        	 getModel().getConnTaskManager().startBackgroundTask();
	         } 
	    }, 1000);
		
		
		
	}
	

	@Override
	public void onPause() {
	    super.onPause();
	    if(getModel().getDBService() != null)
	    	getModel().getDBService().close();
	}   
	
	@Override
	public void onResume() {
		fraudTV = (TextView)findViewById(R.id.fraudTV);
		fraudTV.setVisibility(View.INVISIBLE);
	    super.onResume();
	    if(getModel().getDBService() != null)
	    	getModel().getDBService().open();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(getModel() != null)
		{
			getModel().getDBService().close();
			if( ((ConnTaskManager)getModel().getConnTaskManager()).getTask() != null && !((ConnTaskManager)getModel().getConnTaskManager()).getTask().isCancelled())
				((ConnTaskManager)getModel().getConnTaskManager()).getTask().cancel(true);	    
		}
	
	}
	
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	public void clearUIComps() {}

	public void hideUIComps() {}

	public TagPurchase getModel() {
		return model;
	}

	public void setModel(TagPurchase model) {
		this.model = model;
	}

}
