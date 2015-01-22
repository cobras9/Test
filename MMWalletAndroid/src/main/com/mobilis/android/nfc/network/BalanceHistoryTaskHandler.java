package com.mobilis.android.nfc.network;

import android.util.Log;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.model.LastTransactions;
import com.mobilis.android.nfc.util.SpecialTxl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.util.Locale;

public class BalanceHistoryTaskHandler{
	private LastTransactions model;
	private BufferedReader reader;
	private PrintWriter writer;
	private SocketManager sMgr;
	private final String TAG = BalanceHistoryTaskHandler.class.getSimpleName();
	
	public BalanceHistoryTaskHandler(LastTransactions model){
		setModel(model);
	}
	
	public void prepaerTransferTasks(){
		Log.d(TAG, "before showing progressDialog blockCounter is "+getModel().getBlockCounter());
		if(getModel().getBlockCounter() == 1)
		{
			Log.d(TAG, "showing progressDialog now");
//			getModel().getDisplayHandler().showProgressDialog();
		}
	}
	
	public void processTask(){

		try {
			Log.d(TAG, "mobilis..getModel() is: "+getModel());
    		setsMgr(new SocketManager(getModel()));
            try { 
            	Log.d(TAG, "mobilis..getsMgr() is: "+getsMgr());
            	getsMgr().createSSLSocket();
                //send the message to the server
            	Log.d(TAG, "mobilis..getRequestParameters() is: "+getModel().getRequestParameters());
            	String requestParams = getModel().getRequestParameters();
            	Log.d(TAG, "Sending String: "+requestParams);
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(SocketManager.socket.getOutputStream())), true);
                if (writer != null && !writer.checkError()) {
                    writer.println(requestParams);
                    writer.flush();
                }
                /** 1.A) */
                getModel().setRequestStatus(getModel().getResInt(R.string.STATUS_OK));
                
                Log.d(TAG, "Sending message: "+requestParams);
                Log.d(TAG, "TCP Client C: Sent.");
                Log.d(TAG, "TCP Client C: Done."); 
                
                //receive the message which the server sends back
                reader = new BufferedReader(new InputStreamReader(SocketManager.socket.getInputStream()));
                String serverMessage = null;
                while (serverMessage == null ) {
                	if(!getsMgr().isSocketAlive()){
                		/** 2.A) */
                		model.setResponseStatus(getModel().getResInt(R.string.STATUS_SOCKET_TIMEOUT));
                		break;
                	}
    				serverMessage = reader.readLine();
    				if(serverMessage != null)
    					Log.d(TAG, "Received String: "+serverMessage);
    			    if (serverMessage != null ) 
    			    	/** 2.B) */
    			    	checkResponse(serverMessage);
    			} 
                Log.d(TAG, "Server response: "+getModel().getServerResponse());
            } 
            catch (SocketTimeoutException se)
            {
            	if(getModel() instanceof SpecialTxl)
            	{
            		Log.d(TAG, "socket timeout exception occured: no response from server");
            		getModel().setRequestStatus(-69);

            	}
            }
            catch (Exception e) {
            	getModel().setResponseStatus(getModel().getResInt(R.string.STATUS_SOCKET_EXCEPTION));
            	e.printStackTrace();
            	Log.d(TAG,"TCP S: Error "+e);
            } 

        } catch (Exception e) {
        	/** 1.B) */
        	getModel().setRequestStatus(getModel().getResInt(R.string.STATUS_SOCKET_EXCEPTION));
        	e.printStackTrace();
        	Log.d(TAG, "TCP C: Error"+ e);
        }
        finally {
            SocketManager.socketIsInUse = false;
        }
    }
	
	public int postTransferTaskCheck(){
        Log.d(TAG, "postTransferTaskCheck() response Status: "+getModel().getResponseStatus());
		getModel().verifyPostTaskResults();
		model.setTaskFinished(true);
		return 0;
	}
	
	private void checkResponse(String serverMessage) {
//            Log.d(TAG, "adding last transactions to model data");
//            String blnc = "PrnData=(Printed at:              2014-08-12 20:52|MP140811.1837.C00008  789284824 100.00 CR MERCHPAY|MP140811.1007.C00006  789284824 100.00 CR MERCHPAY)";
//        String blnc = "PrnData=(Printed at:              2014-08-12 20:52|MP140811.1837.C00008  12:13 789284824 100.00 CR MERCHPAY|MP140811.1007.C00006  12:13 789284824 100.00 CR MERCHPAY)";

//        getModel().setServerResponse(blnc+","+"Status=0,TransactionId=0000000024,BlockCount=0,MessageType=LastTransactionsResp");

//        serverMessage = "Status=1000,TransactionId=0000000004,Message=Error: 00210,MessageType=LastTransactionsResp";


		getModel().setServerResponse(serverMessage);
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
			// capture server error message if main status is not OK
	    if (getModel().getResponseStatus() != getResInt(R.string.STATUS_OK))
			for (int i = 0; i < responseParams.length; i++)
				if(responseParams[i].substring(0, 8).equalsIgnoreCase(getResString(R.string.RESPONSE_MESSAGE)))
					getModel().setServerError(responseParams[i].substring(8));

        Log.d(TAG, "checkResponse() response Status: "+getModel().getResponseStatus());
		
	}
	
	private int getResInt(int res){
		return Integer.parseInt(getModel().getContext().getResources().getString(res));
	}
	
	private String getResString(int res){
		return getModel().getContext().getResources().getString(res);
	}
	

	public LastTransactions getModel() {
		return model;
	}
	public void setModel(LastTransactions model) {
		this.model = model;
	}
	public SocketManager getsMgr() {
		return sMgr;
	}
	public void setsMgr(SocketManager sMgr) {
		this.sMgr = sMgr;
	}
}
