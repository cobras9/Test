package com.mobilis.android.nfc.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.CONST;

import net.hockeyapp.android.Constants;
import net.hockeyapp.android.FeedbackActivity;
import net.hockeyapp.android.FeedbackManager;

public class HockeyAppFeedBack extends FeedbackManager{
	private static final String APP_ID= CONST.HOCKEY_APP_ID.toString();

	public static void showFeedbackActivityFromFragment(Context context, Activity activity){
		if (context != null) {
		      Intent intent = new Intent();
		      intent.setClass(context, FeedbackActivity.class);
		      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		      intent.putExtra("url", getURLString(context));
		      context.startActivity(intent);
		      activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		    }
	}
	private static String getURLString(Context context) {
	    return Constants.BASE_URL+"api/2/apps/" + APP_ID + "/feedback/";
	  }
}
