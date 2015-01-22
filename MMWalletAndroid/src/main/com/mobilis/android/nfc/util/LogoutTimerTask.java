package com.mobilis.android.nfc.util;


import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import java.util.TimerTask;

public class LogoutTimerTask extends TimerTask {
	private Handler mHandler = new Handler();
	private Activity activity;
	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
	public final static long LOGOUT_DELAY_TIME = 120000; // 2 minutes = 120 seconds
	private final long COUNTER_TIME = 31000; // 15 seconds
	
	private void startLogoutTimer() {
//		LogoutSessionManager.logoutTimer = new Timer();
//		LogoutSessionManager.logoutTask = new LogoutTimerTask(getActivity());
//		LogoutSessionManager.logoutTimer.schedule(LogoutSessionManager.logoutTask, LogoutTimerTask.LOGOUT_DELAY_TIME);
	}
	
	public LogoutTimerTask(Activity activity){
		Log.d("ahmed", "activity class name is: "+activity.getClass().getName());
		this.setActivity(activity);
	}
//	private MyCountDownTimer timer;
    @Override
    public void run() {
//    	if(LoginFragment.isLoggedin){
//	        new Thread(new Runnable() {
//	            public void run() {
//	                    mHandler.post(new Runnable() {
//	                    public void run() {
//	                    	final Dialog dialog = new Dialog(getActivity(), R.style.Dialog_Title);
//	                		dialog.getWindow().getAttributes().windowAnimations = R.style.Logout_Dialog_Bounce_Theme_ENTER;
//	                		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//	                		dialog.setContentView(R.layout.logout_dialog);
//	                		dialog.setCancelable(false);
//	                		dialog.setTitle("Wallet idle time out");
//	                		final TextView messageTV = (TextView) dialog.findViewById(R.id.dialogMessage);
//	                		messageTV.setTextSize(18);
//	                		Button cancelButton = (Button) dialog.findViewById(R.id.negativeButton);
//	                		cancelButton.setText("Cancel");
//	                		cancelButton.setOnClickListener(new OnClickListener() {
//	                			public void onClick(View v) {
//	                				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//	                				dialog.dismiss();
//	                				timer.cancel();
//	                				startLogoutTimer();
//	                			}
//	                		});
//	                		Button positiveButton = (Button) dialog.findViewById(R.id.positiveButton);
//	                		positiveButton.setText("Ok");
//	                		positiveButton.setOnClickListener(new OnClickListener() {
//	                			public void onClick(View v) {
//	                				dialog.dismiss();
//	                				Handler handler = new Handler();
//	                				handler.postDelayed(new Runnable() {
//	                					public void run() {
//	                						timer.cancel();
//	                						((MainActivity) getActivity()).switchContent(new LoginFragment());
//	                					}
//	                				}, 750);
//	                			}
//	                		});
//	                		dialog.show();
//	                		timer = new MyCountDownTimer(COUNTER_TIME, 1000, dialog);
//	                		timer.start();
//	                    }
//	                });
//	            }
//	        }).start();
//    	}
    }	

//	private class MyCountDownTimer extends CountDownTimer {
//		private TextView tv;
//		private Dialog dialog;
//		public MyCountDownTimer(long startTime, long interval, Dialog dialog) {
//			super(startTime, interval);
//			this.dialog = dialog;
//			setTv((TextView) dialog.findViewById(R.id.dialogMessage));
//		}
//
//		@Override
//		public void onFinish() {
//			Log.d("ahmed", "onFinished() is called in CountDownTimer");
//			if(this.dialog.isShowing()){
//				this.dialog.dismiss();
//				((MainActivity) getActivity()).switchContent(new LoginFragment());
//
//			}
//		}
//
//		@Override
//		public void onTick(long millisUntilFinished) {
//			Log.d("ahmed", "onTick is called");
//			getTv().setText("Log out in " + millisUntilFinished/1000+" sec.");
//		}
//
//		public TextView getTv() {
//			return tv;
//		}
//
//		public void setTv(TextView tv) {
//			this.tv = tv;
//		}
//
//	}
}


