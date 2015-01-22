package com.mobilis.android.nfc.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.TransferCode;

import java.text.DecimalFormat;

/**
 * Created by ahmed on 8/05/14.
 */
public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

    Activity mActivity;

    public CustomOnItemSelectedListener(Activity activity){
        mActivity = activity;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        final EditText amount = (EditText) mActivity.findViewById(R.id.Fragment_TopUp_EditText_Amount);
        Double amountAsDouble = Double.parseDouble(TransferCode.topupAirtimeCodes.get(pos).getAmount()) / 100;
        final DecimalFormat df = new DecimalFormat("#.00");
        if(amountAsDouble > 0)
        {
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(INTENT.DISABLE_AMOUNT_EDIT_TEXT.toString()));
            amount.setText(df.format(amountAsDouble));
            amount.setEnabled(false);
            amount.setBackgroundColor(mActivity.getResources().getColor(R.color.APP_MAIN_COLOR));
            amount.setTextColor(Color.WHITE);
            amount.setTextSize(26);
            amount.setTypeface(null, Typeface.BOLD);
            amount.setGravity(Gravity.CENTER);
        }
        else{
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(INTENT.ENABLE_AMOUNT_EDIT_EXT.toString()));
            TextView amountTV = (TextView) mActivity.findViewById(R.id.Fragment_TopUp_EditText_Amount);
            amountTV.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            amount.setText("");
            amount.setEnabled(true);
            amount.setBackgroundColor(Color.TRANSPARENT);
            amount.setTextColor(Color.BLACK);
            amount.setTextSize(16);
            amount.setTypeface(null, Typeface.NORMAL);
            amount.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

}
