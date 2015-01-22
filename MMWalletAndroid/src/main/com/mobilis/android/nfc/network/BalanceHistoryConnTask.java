package com.mobilis.android.nfc.network;

import android.os.AsyncTask;
import android.util.Log;

import com.mobilis.android.nfc.model.LastTransactions;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BalanceHistoryConnTask extends AsyncTask{
	
	private BalanceHistoryTaskHandler handler ;
	private LastTransactions model;
	
	public BalanceHistoryConnTask(LastTransactions lastTransactions) {
		setModel(lastTransactions);
		setHandler(new BalanceHistoryTaskHandler(lastTransactions));
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		getHandler().prepaerTransferTasks();
		
	}
	
	@Override
	protected Integer doInBackground(Object... params) {
    	getHandler().processTask();
        return null;
	}
	
	
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);	
		getHandler().postTransferTaskCheck();
		Log.d(BalanceHistoryConnTask.class.getSimpleName(), "+++++++ in BalanceHistoryCOnnTask.onPostExecute() blockCounter is: "+getModel().getBlockCounter());
		
	}

	public BalanceHistoryTaskHandler getHandler() {
		return handler;
	}

	public void setHandler(BalanceHistoryTaskHandler helper) {
		this.handler = helper;
	}

	public LastTransactions getModel() {
		return model;
	}

	public void setModel(LastTransactions model) {
		this.model = model;
	}

    
}
