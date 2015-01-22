package com.mobilis.android.nfc.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.model.Login;
import com.mobilis.android.nfc.model.Registration;

import java.util.Timer;
import java.util.TimerTask;


@SuppressWarnings({"unchecked", "rawtypes"})
public class ConnTaskManager implements TaskManager{
	private Context context;
	private AbstractModel model;
	private AsyncTask task;
//    private boolean noResponse;
    final String TAG = ConnTaskManager.class.getSimpleName();
    final int TIME_OUT_MILL = 45000;
	public ConnTaskManager(Context context, AbstractModel model) {
		setContext(context);
		setModel(model);
	}

	private void createTask(){
        Log.d(TAG, "creating new Task");
		setTask(new ConnTask(getModel()));
	}
		
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public AbstractModel getModel() {
		return model;
	}
	public void setModel(AbstractModel model) {
		this.model = model;
	}
	
	public int startBackgroundTask() {
		if(isConnectivityAvailable()){
			Log.d(TAG, "ConnTaskManager will create a task now then run it");
            Log.d(TAG, "getModel().getDBService() == null ? "+(getModel().getDBService() == null));

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
        if(getModel().getClass().getSimpleName().equalsIgnoreCase(Registration.class.getSimpleName()) ||
                getModel().getClass().getSimpleName().equalsIgnoreCase(Login.class.getSimpleName()))
        {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "Task time is up closing socket now...");
                        Log.d(TAG, "!SocketManager.socketIsInUse: " + !SocketManager.socketIsInUse);
                        SocketManager.socket.close();
                        SocketManager.socketIsInUse = false;
                    } catch (Exception e) {
                        Log.d(TAG, "TimerTask Exception occurred while trying to close socket");
                        Log.d(TAG, "TimerTask Exception: "+e.getStackTrace());
                        e.printStackTrace();
                    }
                    finally {
                        Log.d(TAG, "TimerTask finally clause..setting socket to null");
                        SocketManager.socket = null;
                        killTask();
                    }
                }
            };
            new Timer().schedule(task, TIME_OUT_MILL);
        }

	}

    public void killTask(){
        if(getTask() != null)
            getTask().cancel(true);
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
