package com.mobilis.android.nfc.network;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.model.Login;
import com.mobilis.android.nfc.model.Registration;
import com.mobilis.android.nfc.util.Financial;
import com.mobilis.android.nfc.util.SecurePreferences;
import com.mobilis.android.nfc.util.SpecialTxl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.util.Locale;

public class TaskHandler {
    private AbstractModel model;
    private BufferedReader reader;
    private PrintWriter writer;
    private final String TAG_CONNTASK = "TaskHandler";
    private boolean exceptionOccurred;
    public TaskHandler(AbstractModel model){
        setModel(model);
    }

    public void prepareTransferTasks(){}

    public void processTask(){
        try {
            if(getsMgr() == null)
                setsMgr(new SocketManager(getModel()));
            try {
                getsMgr().createSSLSocket();

                String requestParams = getModel().getRequestParameters();
                Log.d(TAG_CONNTASK, "Sending String: "+requestParams);

                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(SocketManager.socket.getOutputStream())), true);
                if (writer != null && !writer.checkError()) {
                    writer.println(requestParams);
                    writer.flush();
                }
                getModel().setRequestStatus(getModel().getResInt(R.string.STATUS_OK));

                // Save last network request
                String savedParams = requestParams;
                model.getSharedPreference().edit().putString(SecurePreferences.LAST_REQUEST, AbstractModel.getDateAndTime()+":"+AbstractModel.maskPin(savedParams)).commit();

                Log.d(TAG_CONNTASK, "Sending message: "+requestParams);
                //receive the message which the server sends back
                reader = new BufferedReader(new InputStreamReader(SocketManager.socket.getInputStream()));
                String serverMessage = null;
                while (serverMessage == null ) {
                    serverMessage = reader.readLine();
                    if (serverMessage != null ) {
                        Log.d(TAG_CONNTASK, "Received String: "+serverMessage);
                        model.getSharedPreference().edit().putString(SecurePreferences.LAST_RESPONSE, AbstractModel.getDateAndTime()+":"+ serverMessage).commit();
                    }
                }
                getModel().setServerResponse(serverMessage);
                Log.d(TAG_CONNTASK, "Server response: "+getModel().getServerResponse());
            }
            catch (SocketTimeoutException se)
            {
                if(getModel() instanceof SpecialTxl)
                    getModel().setRequestStatus(-69);
            }
            catch (Exception e) {
                getModel().setResponseStatus(getModel().getResInt(R.string.STATUS_SOCKET_EXCEPTION));
                e.printStackTrace();
                exceptionOccurred = true;
                LocalBroadcastManager.getInstance(getModel().getActivity()).sendBroadcast(new Intent(INTENT.SERVER_COMM_TIME_OUT.toString()).putExtra(INTENT.EXTRA_ERROR.toString(), "Operation time out."));
                Log.d(TAG_CONNTASK,"TCP S: Error "+e);
            }

        } catch (Exception e) {
            getModel().setRequestStatus(getModel().getResInt(R.string.STATUS_SOCKET_EXCEPTION));
            e.printStackTrace();
            LocalBroadcastManager.getInstance(getModel().getActivity()).sendBroadcast(new Intent(INTENT.SERVER_COMM_TIME_OUT.toString()).putExtra(INTENT.EXTRA_ERROR.toString(), "Error: Unable to complete transaction"));
            Log.d(TAG_CONNTASK, "TCP C: Error"+ e);
        }
        finally {
            SocketManager.socketIsInUse = false;
        }
    }

    public int postTransferTaskCheck(){
        if(exceptionOccurred)
        {
            exceptionOccurred = false;
            return 0;
        }
        if(getModel().getServerResponse() == null) {
            LocalBroadcastManager.getInstance(getModel().getActivity()).sendBroadcast(new Intent(INTENT.SERVER_COMM_TIME_OUT.toString()).putExtra(INTENT.EXTRA_ERROR.toString(), "Null response from server"));
            return 0;
        }
        checkResponse(getModel().getServerResponse());
        getModel().verifyPostTaskResults();
        model.setTaskFinished(true);
        return 0;
    }

    private void checkResponse(String serverMessage) {
        if(serverMessage == null)
            return;
        getModel().setServerResponse(serverMessage);
//        if(getModel().getClass().getSimpleName().equalsIgnoreCase(EVDCodes.class.getSimpleName()))
//        {
//            serverMessage = "DISPLAYDATA=(MTNEVD|100|22,MTNEVD|200|5,AitelEVD|500|45),MessageType=EVOUCHERGETResp,CONNECTIONID=cd1dc7bb69e,TransactionId=0000000043,Status=0";
//            getModel().setServerResponse(serverMessage);
//        }
//        hackQuickLinks(serverMessage);
//        serverMessage = hackNewPinPrompt(serverMessage);
//        getModel().setServerResponse(serverMessage);

//        String applyHack = getModel().getSharedPreference().getString("HACK", null);
//        if(applyHack == null || applyHack.equalsIgnoreCase("DO")) {
//            getModel().getSharedPreference().edit().putString("HACK", "NO");
//            Log.d("RUA", "will call hackNewPinPrompt() now...");
//            getModel().setServerResponse(serverMessage);
//        }
        String[] responseParams = serverMessage.split(",");
        for (String param : responseParams)
        {
            if(param.toUpperCase(Locale.US).startsWith("STATUS="))
            {
                String[] status = param.split("=");
                getModel().setResponseStatus(Integer.parseInt(status[1]));
                break;
            }
        }
        if(getModel() instanceof Financial && getModel().getResponseStatus() == 0){
            Log.d(TAG_CONNTASK, "Sending UPDATE_BALANCE request to update balance");
            LocalBroadcastManager.getInstance(getModel().getActivity()).sendBroadcast(new Intent(INTENT.UPDATE_BALANCE.toString()));
        }
        if(getModel() instanceof Login && getModel().getResponseStatus() == 0)
            for (String param : responseParams) {
                Log.d(TAG_CONNTASK, "param is: "+param);
                Log.d(TAG_CONNTASK, "param.startsWith('CustomerId='')? "+(param.startsWith("CustomerId=")));
                if(param.startsWith("CustomerId="))
                {
                    Log.d(TAG_CONNTASK, "correcting merchantId (from loginRspo) to:"+param.substring(11));
                    getModel().setClientId(param.substring(11));
                }
            }
        // capture server error message if main status is not OK
        if (getModel().getResponseStatus() != getResInt(R.string.STATUS_OK))
            for (int i = 0; i < responseParams.length; i++)
                if(responseParams[i].substring(0, 8).equalsIgnoreCase(getResString(R.string.RESPONSE_MESSAGE)))
                    getModel().setServerError(responseParams[i].substring(8));

    }

    private void hackQuickLinks(String serverMessage) {
        if(model.getClass().getSimpleName().equalsIgnoreCase(Login.class.getSimpleName()) && serverMessage != null)
        {
            StringBuffer buffer = new StringBuffer();
            //            buffer.append(",CustomerType=01,");
            buffer.append("ProfileTags=(MenuA=0000,MenuB=000C,MenuC=007F,MenuD=0012,MenuE=0000,MenuF=00F0,MenuG=0000),");
            buffer.append("QuickLinks=Count=5,");
            buffer.append("Link1=(Name=Quick Pay,Type=C2MP, Amount=10),");
            buffer.append("Link2=(Name=Registration, Type=AtomicCustomerCreate),");
//            buffer.append("Link2=(Name=Quick Transfer,Type=OUTTXF, Dstcode=VFMM),");
            //            buffer.append("Link3=(Name=Pay bank,Type=OUTTXF, DstCode=050, Destination=0022029103),");
            buffer.append("Link3=(Name=NFC registration, Type=RegisterTag),");
            buffer.append("Link4=(Name=Airtime Topup,Type=OUTTXF, DstCode=(90802|10000|Etisalat 100,90803|20000|Etisalat 200,90806|0|Etisalat)),");
            buffer.append("Link5=(Type=C2MP),");


            buffer.append(",CreateFlags=17,");

            getModel().setServerResponse(buffer.toString().concat(serverMessage));

            /** QL & Menu **/
//            String response = "ProfileTags=(MenuA=0000,MenuB=000C,MenuC=007F,MenuD=0012,MenuE=0000,MenuF=00F0,MenuG=0000),QuickLinks=Count=4,Link1=(Name=200, Type=C2MP, Amount=200),Link2=(Name=400, Type=C2MP, Amount=400),Link3=(Name=600, Type=C2MP, Amount=600),Link4=(Name=800, Type=C2MP, Amount=800),,CreateFlags=0001,AllowedIdTypes=2b,TransactionId=0000000033,Status=0,CustomerId=255789284991,MessageType=CustomerLoginResp";
            /** Menu Only **/
//            String response = "ProfileTags=(MenuA=0000,MenuB=000C,MenuC=007F,MenuD=0000,MenuE=0000,MenuF=00F0,MenuG=0000),QuickLinks=,CreateFlags=0001,AllowedIdTypes=2b,TransactionId=0000000033,Status=0,CustomerId=255789284991,MessageType=CustomerLoginResp";
            /** Account Only **/
//            String response = "ProfileTags=(MenuA=0000,MenuB=0000,MenuC=0000,MenuD=0002,MenuE=0000,MenuF=0000,MenuG=0000),CreateFlags=0001,AllowedIdTypes=2b,TransactionId=0000000033,Status=0,CustomerId=255789284991,MessageType=CustomerLoginResp";
            /** Account & Menu **/
//            String response = "ProfileTags=(MenuA=0000,MenuB=000C,MenuC=007F,MenuD=0002,MenuE=0000,MenuF=00F0,MenuG=0000),QuickLinks=Count=1,Link1=(Name=200, Type=C2MP, Amount=200),CreateFlags=0001,AllowedIdTypes=2b,TransactionId=0000000033,Status=0,CustomerId=255789284991,MessageType=CustomerLoginResp";
            /** QL Only **/
//            String response = "ProfileTags=(MenuA=0000,MenuB=0000,MenuC=0000,MenuD=0000,MenuE=0000,MenuF=0000,MenuG=0000),QuickLinks=Count=1,Link1=(Name=200, Type=C2MP, Amount=200),CreateFlags=0001,AllowedIdTypes=2b,TransactionId=0000000033,Status=0,CustomerId=255789284991,MessageType=CustomerLoginResp";
//
//            getModel().setServerResponse(response);
        }
//        else
//          if(model.getClass().getSimpleName().equalsIgnoreCase(Login.class.getSimpleName()))
//              getModel().setServerResponse("CreateFlags=0001,AllowedIdTypes=2b,TransactionId=0000000009,Status=0,CustomerId=255789284991," +
//                      "QuickLinks=,ProfileTags=,");
        else
            getModel().setServerResponse(serverMessage);
    }

    private String hackNewPinPrompt(String serverMessage){
//        if(model.getClass().getSimpleName().equalsIgnoreCase(Login.class.getSimpleName())){
//            String response = "TransactionId=0000000033,Status=1000,CustomerId=255789284991,MessageType=Redirect to Change PIN menu";
//            getModel().setServerResponse(response);
//            return response;
//        }
//        Log.d("RUA", "model.getClass().getSimpleName(): "+model.getClass().getSimpleName());
        if(model.getClass().getSimpleName().equalsIgnoreCase(Registration.class.getSimpleName())){
            String response = "TransactionId=0000000033,Status=1000,CustomerId=255789284991,MessageType=Redirect to Change PIN menu";
            getModel().setServerResponse(response);
            return response;

        }
//        Log.d("RUA","rerurning response: "+serverMessage);
        return serverMessage;

    }

    private int getResInt(int res){
        return Integer.parseInt(getModel().getContext().getResources().getString(res));
    }

    private String getResString(int res){
        return getModel().getContext().getResources().getString(res);
    }

    private String getBalanceOfType(String balanceType){

        StringBuffer buffer = new StringBuffer();
        for(String s :getModel().getServerResponse().split(getResString(R.string.DELIMITER))){
            if(s.length() > 7 && s.substring(0, 8).equalsIgnoreCase("Balance=") && balanceType.equalsIgnoreCase("balance")){
                buffer.append(s.substring(8));
            }
            if(s.length() > 12 && s.substring(0, 13).equalsIgnoreCase("TopupBalance=") && balanceType.equalsIgnoreCase("balancetopup")){
                buffer.append(s.substring(13));
            }
        }
        return new String(buffer);

    }

    public AbstractModel getModel() {
        return model;
    }
    public void setModel(AbstractModel model) {
        this.model = model;
    }
    public SocketManager getsMgr() {
        return ConstantsNetwork.socketmanager;
    }
    public void setsMgr(SocketManager sMgr) {
        ConstantsNetwork.socketmanager = sMgr;
    }
}
