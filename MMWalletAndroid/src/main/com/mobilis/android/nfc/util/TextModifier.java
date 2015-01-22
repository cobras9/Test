package com.mobilis.android.nfc.util;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ahmed on 26/06/14.
 */
public class TextModifier {

    private TextModifier textModifier;
    private TextView amountTV;
    private EditText amountET;
    private List<String> destinationCodes;

    public void setDestinationCodeList(List<String> spinnerList){
        this.destinationCodes = spinnerList;
    }

    public TextModifier(EditText amountET){
        this.amountET = amountET;
    }

    public TextModifier(EditText amountET, TextView amountTV){
        this.amountET = amountET;
        this.amountTV = amountTV;
    }

    public void addDecimalToText(String text){
        if(!amountET.isEnabled())
        {
            if(amountTV != null)
                amountTV.setVisibility(View.INVISIBLE);
            return;
        }

        amountET.setTextColor(Color.TRANSPARENT);
        StringBuilder builder = new StringBuilder(text.replace(".", ""));
        if(builder.toString().length() > 2)
            builder.insert(builder.toString().length()-2,".");
        if(text.toString().length() == 1)
        {
            String temp = builder.toString();
            builder.delete(0, builder.toString().length());
            builder.append("00."+"0"+temp);
        }
        else if(text.toString().length() == 2)
        {
            String temp = builder.toString();
            builder.delete(0, builder.toString().length());
            builder.append("00."+temp);
        }
        else if (text.toString().length() == 3)
        {
            String temp = builder.toString();
            builder.delete(0, builder.toString().length());
            builder.append("0"+temp);
        }

        if(amountET.getText().toString().isEmpty())
        {
            if(amountTV != null) {
                amountTV.setText("Amount");
                amountTV.setTextColor(Color.LTGRAY);
            }
            amountET.setCursorVisible(true);

        }
        else{
            if(amountTV != null) {
                amountTV.setText(builder.toString());
                amountTV.setTextColor(Color.BLACK);
            }
            amountET.setCursorVisible(false);
        }
        amountET.setSelection(amountET.getText().length(), amountET.getText().length());
    }

    public String getAmount() {
        if(amountTV != null)
            return amountTV.getText().toString();
        else
            return amountET.getText().toString();
    }

//    public String getAmount(int spinnerSelectedItemPosition) {
//        if(spinnerSelectedItemPosition != -1)
//            return destinationCodes.get(spinnerSelectedItemPosition);
//        else
//            return  amountTV.getText().toString();
//    }


    public void setAmountTV(TextView amountTV){
         this.amountTV = amountTV;
    }

}
