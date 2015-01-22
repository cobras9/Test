package com.mobilis.android.nfc.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.model.LastTransactions;


@SuppressWarnings({"unchecked", "rawtypes"})
public class BalanceHistoryTaskManager implements TaskManager{
	private Context context;
	private LastTransactions model;
	private AsyncTask task;
	
	public BalanceHistoryTaskManager(Context context, AbstractModel model) {
		setContext(context);
		setModel((LastTransactions)model);
	}

	private void createTask(){
		setTask(new BalanceHistoryConnTask(getModel()));
	}
		
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public LastTransactions getModel() {
		return model;
	}
	public void setModel(LastTransactions model) {
		this.model = model;
	}
	
	public int startBackgroundTask() {
		if(isConnectivityAvailable()){
			getModel().getDBService().open();
			resetStatus();
			getModel().setTaskFinished(false);
			createTask();
			run();
		}
		else{
//			getModel().getDisplayHandler().setStatusAlertMessage(getModel().getContext().getResources().getString(R.string.TOAST_NO_NETWORK));
//			getModel().getDisplayHandler().showStatusAlert();
		}
		return 0;
	}
	
	private boolean isConnectivityAvailable(){
		ConnectivityManager conMgr =  (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo i = conMgr.getActiveNetworkInfo();
		  if (i == null)
		    return false;
		  if (!i.isConnected())
		    return false;
		  if (!i.isAvailable())
		    return false;
		  return true;
	}

	private void run(){
		getTask().execute(0);
	}

	public AsyncTask getTask() {
		return task;
	}

	public void setTask(AsyncTask task) {
		this.task = task;
	}
	
	private void resetStatus(){
		getModel().setRequestStatus(getModel().getResInt(R.string.STATUS_INITIALVALUE));
		getModel().setResponseStatus(getModel().getResInt(R.string.STATUS_INITIALVALUE));
	}
	
}
