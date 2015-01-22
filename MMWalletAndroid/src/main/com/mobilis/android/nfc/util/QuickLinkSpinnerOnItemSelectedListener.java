package com.mobilis.android.nfc.util;

import android.app.Dialog;
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

import java.text.DecimalFormat;

/**
 * Created by ahmed on 8/05/14.
 */
public class QuickLinkSpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

    Dialog mDialog;
    int mIndex;
    public QuickLinkSpinnerOnItemSelectedListener(Dialog dialog, int index){
        mDialog = dialog;
        mIndex = index;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        final EditText amount = (EditText) mDialog.findViewById(R.id.Dialog_QuickLink_C2MP_EditText_Amount);
        Double amountAsDouble = Double.parseDouble(Constants.quickLinks.get(mIndex).getDstCodes().get(pos).getAmount()) / 100;
        final DecimalFormat df = new DecimalFormat("#.00");

        if(amountAsDouble > 0)
        {
            amount.setText(df.format(amountAsDouble));
            amount.setEnabled(false);
            amount.setBackgroundColor(mDialog.getContext().getResources().getColor(R.color.APP_MAIN_COLOR));
            amount.setTextColor(view.getResources().getColor(R.color.TEXT_COLOR));
            amount.setTextSize(26);
            amount.setTypeface(null, Typeface.BOLD);
            amount.setGravity(Gravity.CENTER);
        }
        else{
            TextView amountTV = (TextView) mDialog.findViewById(R.id.Dialog_QuickLink_C2MP_TextView_Amount);
            amountTV.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            amount.setText("");
            amount.setEnabled(true);
            amount.setBackgroundColor(Color.TRANSPARENT);
            amount.setTextColor(Color.BLACK);
            amount.setTextSize(16);
            amount.setTypeface(null, Typeface.NORMAL);
            amount.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        }
        LocalBroadcastManager.getInstance(mDialog.getContext()).sendBroadcast(new Intent(INTENT.DESTINATION_CHANGED.toString()).putExtra(INTENT.EXTRA_DESTINATION.toString(),
                Constants.quickLinks.get(mIndex).getDstCodes().get(pos).getCode()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

}
