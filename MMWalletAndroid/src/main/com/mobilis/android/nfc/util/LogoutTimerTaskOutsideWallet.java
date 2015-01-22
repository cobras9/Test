package com.mobilis.android.nfc.util;


import android.app.Activity;
import android.os.Handler;

import java.util.TimerTask;

public class LogoutTimerTaskOutsideWallet extends TimerTask {
	private Handler mHandler = new Handler();
	private Activity mwalletActivity;
	private Activity outsideActivity;
	
	public final static long LOGOUT_DELAY_TIME = 120000; // 2 minutes = 120 seconds
	private final long COUNTER_TIME = 30000; // 15 seconds
	
	private void restartTimer() {
//		LogoutSessionManagerOutsideWallet.logoutTimer = new Timer();
//		LogoutSessionManagerOutsideWallet.logoutTask = new LogoutTimerTaskOutsideWallet(mwalletActivity, outsideActivity);
//		LogoutSessionManagerOutsideWallet.logoutTimer.schedule(LogoutSessionManagerOutsideWallet.logoutTask, LogoutTimerTaskOutsideWallet.LOGOUT_DELAY_TIME);
	}
	
	public LogoutTimerTaskOutsideWallet(Activity mwalletActivity, Activity outsideActivity){
		this.mwalletActivity = mwalletActivity;
		this.outsideActivity = outsideActivity;
	}
//	private MyCountDownTimer timer;
    @Override
    public void run() {
//    	if(LoginFragment.isLoggedin){
//	        new Thread(new Runnable() {
//	            public void run() {
//	                    mHandler.post(new Runnable() {
//	                    public void run() {
//	                    	final Dialog dialog = new Dialog(outsideActivity, R.style.Dialog_Title);
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
////	                				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//	                				dialog.dismiss();
//	                				timer.cancel();
//	                				restartTimer();
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
//	                						mwalletActivity.finish();
//	                						Intent intent = new Intent(outsideActivity.getApplicationContext(), MainActivity.class);
//	                						outsideActivity.startActivity(intent);
//	                						outsideActivity.finish();
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
//
//				mwalletActivity.finish();
//				Intent intent = new Intent(outsideActivity.getApplicationContext(), MainActivity.class);
//				outsideActivity.startActivity(intent);
//				outsideActivity.finish();
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


