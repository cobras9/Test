package com.mobilis.android.nfc.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.model.Login;

/**
 * Created by ahmed on 10/06/14.
 */
public class ServerConfigurationFragment extends ApplicationActivity.PlaceholderFragment{

    private EditText editTextServerIPMask;
    private EditText editTextServerIP;
    private EditText editTextServerPortMask;
    private EditText editTextServerPort;
    private AbstractModel model;

    Button updateButton;
    TextView resultTV;
    ProgressBar progressBar;

    private String serverIp;
    private String serverPort;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.configuration_view, container, false);
        super.onCreate(savedInstanceState);
        setEditTextServerIP((EditText) rootView.findViewById(R.id.editTextServerIP));
        setEditTextServerIPMask((EditText) rootView.findViewById(R.id.editTextServerIPMask));

        setEditTextServerPort((EditText) rootView.findViewById(R.id.editTextServerPort));
        setEditTextServerPortMask((EditText) rootView.findViewById(R.id.editTextServerPortMask));

        registerTextChangeListeners(editTextServerIP, editTextServerIPMask);
        registerTextChangeListeners(editTextServerPort, editTextServerPortMask);

        updateButton = (Button) rootView.findViewById(R.id.Fragment_ServerConfiguration_Button_Update);
        resultTV = (TextView) rootView.findViewById(R.id.Fragment_ServerConfiguration_TextView_Result);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_ServerConfiguration_Progressbar);

        setModel(new Login(getActivity()));
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newIP = fixUpServerIP(editTextServerIPMask.getText().toString());
                final String newPort = editTextServerPortMask.getText().toString();
                if(newIP != null && !newIP.isEmpty())
                    getModel().saveIPAddress(newIP);
                if(newPort != null && !newPort.isEmpty())
                    getModel().savePort(newPort);
                progressBar.setVisibility(View.VISIBLE);
                resultTV.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        String result = "IP: "+getModel().getIPAddress()+" \n"+
                                        "PORT: "+getModel().getPort();
                        resultTV.setText(result);
                        resultTV.setVisibility(View.VISIBLE);
                    }
                }, 1000);
            }
        });
        return rootView;
    }

    private void registerTextChangeListeners(final EditText et, final EditText mask) {
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(et.equals(editTextServerIP)){
                    if(editTextServerIPMask.getText().toString().isEmpty())
                        return;
                    serverIp = editTextServerIPMask.getText().toString();
                }
                else{
                    if(editTextServerPortMask.getText().toString().isEmpty())
                        return;
                    serverPort = editTextServerPortMask.getText().toString();
                }
            }
        });
        et.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                StringBuffer server = new StringBuffer(s);
                if(et.equals(editTextServerIP)){

                    if (server.length() >= 3){
                        server.insert(3, ".");
                    }
                    if (server.length() >= 7){
                        server.insert(7, ".");
                    }
                    if (server.length() >= 11){
                        server.insert(11, ".");
                    }
                }
                mask.setText(String.valueOf(server));

            }
            public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
            public void afterTextChanged(Editable s) {}
        });
    }


    public String fixUpServerIP(String serverIP){
        if(serverIP != null && serverIP.length() > 14)
        {
            StringBuffer buffer = new StringBuffer();
            String[] array = new String[4];
            int index=0;
            for(int i=0; i < 4; i++){
                if(i == 0)
                    array[i]= serverIP.substring(0, 3);
                if(i == 1)
                    array[i]= serverIP.substring(4, 7);
                if(i == 2)
                    array[i]= serverIP.substring(8, 11);
                if(i == 3)
                    array[i]= serverIP.substring(12, 15);
                index = index+3;
            }
            for (int i = 0; i < array.length; i++) {}
            String newString = new String();
            int counter = 0;
            for (String octet : array) {
                for (int i = 0; i < octet.length(); i++) {

                    if(octet.substring(i, i+1).equals("0") && i != 2){
                    }
                    else{
                        newString = octet.substring(i, octet.length());
                        break;
                    }
                }
                if(counter == (array.length - 1))
                    buffer.append(newString);
                else
                    buffer.append(newString+".");
                counter++;
            }
            return new String(buffer);
        }
        else
            return serverIP;

    }


    private boolean isCorrectIPConf(){
        return (editTextServerIPMask.getText().toString().length() > 14);
    }
    private boolean isCorrectPortConf(){
        return (editTextServerPortMask.getText().toString().length() > 0);
    }
    public AbstractModel getModel() {
        return model;
    }
    public void setModel(AbstractModel model) {
        this.model = model;
    }
    public EditText getEditTextServerIP() {
        return editTextServerIP;
    }
    public void setEditTextServerIP(EditText editTextServerIP) {
        this.editTextServerIP = editTextServerIP;
    }
    public EditText getEditTextServerPort() {
        return editTextServerPort;
    }
    public void setEditTextServerPort(EditText editTextServerPort) {
        this.editTextServerPort = editTextServerPort;
    }
    public EditText getEditTextServerIPMask() {
        return editTextServerIPMask;
    }
    public void setEditTextServerIPMask(EditText editTextServerIPMask) {
        this.editTextServerIPMask = editTextServerIPMask;
    }
    public EditText getEditTextServerPortMask() {
        return editTextServerPortMask;
    }
    public void setEditTextServerPortMask(EditText editTextServerPortMask) {
        this.editTextServerPortMask = editTextServerPortMask;
    }
}

