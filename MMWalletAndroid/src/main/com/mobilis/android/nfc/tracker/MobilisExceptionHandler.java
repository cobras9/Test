/*
Copyright (c) 2009 nullwire aps

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

Contributors: 
Mads Kristiansen, mads.kristiansen@nullwire.com
Glen Humphrey
Evan Charlton
Peter Hewitt
*/

package com.mobilis.android.nfc.tracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.model.CrashReport;
import com.mobilis.android.nfc.network.SocketManager;
import com.mobilis.android.nfc.util.SecurePreferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.util.Locale;

public class MobilisExceptionHandler {
	
	public static String TAG = MobilisExceptionHandler.class.getSimpleName();
	
	private static String[] stackTraceFileList = null;
    private static Dialog dialog;
    /**
	 * Register handler for unhandled exceptions.
	 * @param activity
	 */
	public static void register(final Activity activity) {
    	getReportParams(activity);
        int numberOfStackTraces = searchForStackTraces().length;
        if(numberOfStackTraces > 0)
        {
            dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_crash_report);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            Button yes = (Button) dialog.findViewById(R.id.Dialog_CrashReport_Button_Yes);
            Button no = (Button) dialog.findViewById(R.id.Dialog_CrashReport_Button_NO);
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.Dialog_CrashReport_Progressbar);
                    progressBar.setVisibility(View.VISIBLE);
                    submitStackTraces(activity);
                }
            });
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    deleteCrashLog();
                }
            });
            dialog.show();
        }
        RunRegistrationThread();
	}

    private static void getReportParams(Activity activity) {
        PackageManager pm = activity.getPackageManager();
        try {
            PackageInfo pi;
            pi = pm.getPackageInfo(activity.getPackageName(), 0);
            G.APP_NAME = activity.getString(R.string.app_name);
            G.IP = activity.getString(R.string.SERVER_IP);
            G.PORT = activity.getString(R.string.SERVER_PORT);
            G.APP_VERSION = pi.versionName;
            G.APP_PACKAGE = pi.packageName;
            G.FILES_PATH = activity.getFilesDir().getAbsolutePath();
            G.PHONE_MODEL = android.os.Build.MODEL;
            G.ANDROID_VERSION = android.os.Build.VERSION.RELEASE;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void RunRegistrationThread() {
        new Thread() {
            @Override
            public void run() {
                UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
                if (currentHandler != null) {
                    Log.d(TAG, "RunRegistrationThread() current handler class=" + currentHandler.getClass().getName());
                }
                // don't register again if already registered
                if (!(currentHandler instanceof DefaultExceptionHandler)) {
                    // Register default exceptions handler
                    Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(currentHandler));
                }
            }
        }.start();
    }
	
	/**
	 * Search for stack trace files.
	 * @return
	 */
	private static String[] searchForStackTraces() {
		if ( stackTraceFileList != null ) {
    		return stackTraceFileList;
		}
        File dir = new File(G.FILES_PATH + "/");
		// Try to create the files folder if it doesn't exist
		dir.mkdir();
		// Filter for ".stackTrace" files
		FilenameFilter filter = new FilenameFilter() { 
			public boolean accept(File dir, String name) {
                return name.toUpperCase(Locale.US).endsWith(".STACKTRACE");
			} 
		}; 
		return (stackTraceFileList = dir.list(filter));	
	}
	
	/**
	 * Look into the files folder to see if there are any "*.stacktrace" files.
	 * If any are present, submit them to the trace server.
	 */
	public static void submitStackTraces(Activity activity) {
		try {
			String[] list = searchForStackTraces();
            CrashReport model = new CrashReport(activity);
            model.setNumberOfStackTraces(list.length);
            SubmitStackTracesTask.numberOfReports = 0;
            if ( list != null && list.length > 0 ) {
				for (int i=0; i < list.length; i++) {
					String filePath = G.FILES_PATH+"/"+list[i];
					// Extract the version from the filename: "packagename-version-...."
					String version = list[i].split("-")[0];
					// Read contents of StackTrace
					StringBuilder contents = new StringBuilder();
					BufferedReader input =  new BufferedReader(new FileReader(filePath));
					String line = null;
					String androidVersion = null;
	                String phoneModel = null;
	                while (( line = input.readLine()) != null){
                        if (androidVersion == null) {
                            androidVersion = line;
                            continue;
                        }
                        else if (phoneModel == null) {
                            phoneModel = line;
                            continue;
                        }
                        contents.append(line);
			            contents.append(System.getProperty("line.separator"));
			        }
			        input.close();
			        String stackTrace;
                    stackTrace = contents.toString();

                    model.setAppName(G.APP_NAME);
                    model.setAppIp(G.IP);
                    model.setAppPort(G.PORT);
                    model.setPackageName(G.APP_PACKAGE);
                    model.setPackageVersion(version);
                    model.setPhoneModel(phoneModel);
                    model.setAndroidVersion(androidVersion);
                    model.setLastNetworkRequest(model.getSharedPreference().getString(SecurePreferences.LAST_REQUEST, null));
                    model.setLastNetworkResponse(model.getSharedPreference().getString(SecurePreferences.LAST_RESPONSE, null));
                    model.setStackTrace(stackTrace);
                    SubmitStackTracesTask task = new SubmitStackTracesTask(model);
                    task.execute();
            	}
			}
		} catch( Exception e ) {
            e.printStackTrace();
		}
	}

    private static void deleteCrashLog() {
        try {
            String[] list = searchForStackTraces();
            for ( int i = 0; i < list.length; i ++ ) {
                File file = new File(G.FILES_PATH+"/"+list[i]);
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            stackTraceFileList = null;
        }
    }

    private static class SubmitStackTracesTask extends AsyncTask<Void, Void, Void>{

        public static int numberOfReports;
        private CrashReport model;
        SocketManager socketManager;
        public SubmitStackTracesTask(CrashReport model){
            this.model = model;
        }

        @Override
        protected Void doInBackground(Void... params) {
            socketManager = new SocketManager(model);
            try {
                socketManager.createSSLSocket();
                //send the message to the server
                String requestParams = model.getRequestParameters();
                Log.d(TAG, "Sending CrashReport: "+requestParams);

                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(SocketManager.socket.getOutputStream())), true);
                if (writer != null && !writer.checkError()) {
                    writer.println(requestParams);
                    writer.flush();
                }
//                BufferedReader reader = new BufferedReader(new InputStreamReader(SocketManager.socket.getInputStream()));
//                String serverMessage = null;
//                while (serverMessage == null ) {
//                    serverMessage = reader.readLine();
//
//                }
//                Log.d(TAG,"Server response: "+serverMessage);
            }
            catch (SocketTimeoutException se)
            {
                SocketManager.socketIsInUse = false;
                se.printStackTrace();
            }
            catch (Exception e) {
                SocketManager.socketIsInUse = false;
                model.setResponseStatus(model.getResInt(R.string.STATUS_SOCKET_EXCEPTION));
                e.printStackTrace();
            }
            finally {
                try {
                    SocketManager.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                numberOfReports++;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(model.getNumberOfStackTraces() == numberOfReports)
            {
                SocketManager.socketIsInUse = false;
                ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.Dialog_CrashReport_Progressbar);
                progressBar.setVisibility(View.GONE);
                if(dialog.isShowing())
                    dialog.dismiss();
                Toast.makeText(model.getActivity(), "Thanks for your feedback", Toast.LENGTH_LONG).show();
            }
        }
    }
}
