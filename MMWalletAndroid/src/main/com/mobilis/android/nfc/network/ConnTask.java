package com.mobilis.android.nfc.network;

import android.os.AsyncTask;

import com.mobilis.android.nfc.model.AbstractModel;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ConnTask extends AsyncTask{
	
	private TaskHandler handler ;
	
	public ConnTask(AbstractModel modelAbstract) {
		setHandler(new TaskHandler(modelAbstract));
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		getHandler().prepareTransferTasks();
		
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
		
	}

	public TaskHandler getHandler() {
		return handler;
	}

	public void setHandler(TaskHandler helper) {
		this.handler = helper;
	}
	
    
}
