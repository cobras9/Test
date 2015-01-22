package com.mobilis.android.nfc.network;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.LoginActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.util.SecurePreferences;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLSocket;

@SuppressWarnings("unused")
public class SocketManager {
    final String TAG = SocketManager.class.getSimpleName();
	public static SSLSocket socket;
	private AbstractModel model;
	public static String SERVER_IP;
	public static int SERVER_PORT;
    public static boolean socketIsInUse;
    private static boolean createNewSocket;
    static BroadcastReceiver broadcastReceiver;
	public SocketManager(AbstractModel model){
		setModel(model);
        if(broadcastReceiver != null)
            return;
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equalsIgnoreCase(INTENT.CLOSE_SOCKET.toString()))
                {
                    Log.d(TAG, "received broadcast to close socket");
                    Log.d(TAG, "socket != null ? "+(socket != null));
                    if(socket != null)
                        new CloseSocketTask().execute();
                }
            }
        };
        LocalBroadcastManager.getInstance(model.getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.CLOSE_SOCKET.toString()));
	} 
	
	private void getConnDetails() {
		SERVER_IP ="";
		SERVER_PORT = 0;

        String appVersionCode = String.valueOf(model.getAppVersionCode(model.getActivity()));
        String ip = LoginActivity.mainSecurePreferences.getString(appVersionCode+SecurePreferences.KEY_SERVER_IP, null);
        String port = LoginActivity.mainSecurePreferences.getString(appVersionCode+SecurePreferences.KEY_SERVER_PORT, null);
        if(ip == null)
        {
            ip = model.getActivity().getString(R.string.SERVER_IP);
            LoginActivity.mainSecurePreferences.edit().putString(appVersionCode+SecurePreferences.KEY_SERVER_IP, ip);
            LoginActivity.mainSecurePreferences.edit().commit();
        }
        if(port == null){
            port = model.getActivity().getString(R.string.SERVER_PORT);
            LoginActivity.mainSecurePreferences.edit().putString(appVersionCode+SecurePreferences.KEY_SERVER_PORT, port);
            LoginActivity.mainSecurePreferences.edit().commit();
        }

		SERVER_IP = ip;
		SERVER_PORT = Integer.parseInt(port);
		Log.v(TAG, "(pulling connection details in SockerManager) server ip: "+SERVER_IP+" server port: "+SERVER_PORT);
		 
	}
	
	public void createSocket() throws IOException{
//        if(socket == null){
//            getConnDetails();
//            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
//            Socket socket = new Socket(serverAddr, SERVER_PORT);
//            socket.setSoTimeout(60000);
//            setSocket(socket);
//        }
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
    public void createSSLSocket() throws IOException{
        getConnDetails();
        SocketManager.socketIsInUse = true;
        Log.v(TAG,"socket == null? "+(socket == null));
        if(socket != null)
        {
            Log.v(TAG,"socket.isClosed()? "+(socket.isClosed()));
            Log.v(TAG," socket.isInputShutdown()? "+( socket.isInputShutdown()));
            Log.v(TAG," socket.isOutputShutdown()? "+( socket.isOutputShutdown()));

        }
        if(socket == null || socket.isClosed() || createNewSocket){
            Log.d(TAG,"creating new socket..createNewSocket: "+createNewSocket);
            Log.d(TAG,"SocketManager.socket == null? : "+(SocketManager.socket == null));
            if(SocketManager.socket != null && SocketManager.socket.isConnected())
                SocketManager.socket.close();
            createNewSocket = false;
            SSLSocketFactory sFactory = SSLSocketFactoryService.getSSLSocketFactoryService(getModel().getContext());
            socket = (SSLSocket) sFactory.createSocket();
            Log.v(TAG,"Will connect the created socket now...");
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "Task time is up closing socket now...");
                        if(SocketManager.socket == null)
                            return;
                        Log.d(TAG, "!SocketManager.socketIsInUse: "+!SocketManager.socketIsInUse);
                        if(!SocketManager.socketIsInUse)
                            SocketManager.socket.close();
                        else
                            createNewSocket = true;
                    } catch (Exception e) {
                        Log.d(TAG, "TimerTask Exception occurred while trying to close socket");
                        Log.d(TAG, "TimerTask Exception: "+e.getStackTrace());
                        e.printStackTrace();
                    }
                    finally {
                        Log.d(TAG, "TimerTask finally clause..setting socket to null");
                        socket = null;
                    }
                }
            };
            new Timer().schedule(task, 90000);
        }

        Log.v(TAG,"again is socket.isClosed()? "+(socket.isClosed()));
        if(socket.isClosed())
        {
            Log.v(TAG,"Socket is closed!!");
            SSLSocketFactory sFactory = SSLSocketFactoryService.getSSLSocketFactoryService(getModel().getContext());
            socket = (SSLSocket) sFactory.createSocket();
            Log.d(TAG, "created new socket");
        }
        Log.v(TAG,"!socket.isConnected()? "+(!socket.isConnected()));
		if(!socket.isConnected())
        {
            Log.d(TAG, "IP ADDRESS:*"+SERVER_IP.trim()+"*");
            Log.d(TAG, "PORT:*"+SERVER_PORT+"*");

            byte[] ipAddress = asBytes(SERVER_IP.trim());
            Log.d(TAG, "ipAddress.length: "+ipAddress.length);
            socket.connect(new InetSocketAddress(InetAddress.getByAddress(ipAddress), SERVER_PORT), 6000);

//            socket.connect(new InetSocketAddress(SERVER_IP.trim(), SERVER_PORT), 60000);
            Log.d(TAG, "connecting socket on IP: "+SERVER_IP+" PORT: "+SERVER_PORT);
        }

        if(socket.isConnected())
			Log.v(TAG,"Socket is connected!!");
        Log.d(TAG,"socket.getRemoteSocketAddress(): "+socket.getRemoteSocketAddress());
		socket.setSoTimeout(90000);

        Log.d(TAG, "timeout=" + socket.getSoTimeout());

        // debugging statements
        Certificate[] serverCerts = socket.getSession().getPeerCertificates();
		Log.v(TAG, "Retreived Server's Certificate Chain");
		Log.v(TAG, serverCerts.length + "Certifcates Found\n\n\n");
		for (int i = 0; i < serverCerts.length; i++) {
			Certificate myCert = serverCerts[i];
			Log.v(TAG,"====Certificate:" + (i+1) + "====");
			Log.v(TAG,"-Public Key-\n" + myCert.getPublicKey());
			Log.v(TAG,"-Certificate Type-\n " + myCert.getType());
			Log.v(TAG,"");
		}
	}


	public boolean isSocketAlive(){
		if(socket != null)
			return socket.isConnected();
		else
			return false;
	}

	public AbstractModel getModel() {
		return model;
	}
	
	public void setModel(AbstractModel model) {
		this.model = model;
	}

    private class CloseSocketTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try {

                Log.d(TAG, "CloseSocketTask closing socket now...");
                SocketManager.socket.close();
            } catch (Exception e) {
                Log.d(TAG, "CloseSocketTask Exception occurred while trying to close socket");
                Log.d(TAG, "CloseSocketTask Exception: "+e.getStackTrace());
                e.printStackTrace();
            }
            finally {

                Log.d(TAG, "CloseSocketTask finally clause..setting socket to null");
                socket = null;
            }
            return null;
        }
    }

    public final static byte[] asBytes(String addr) {

        // Convert the TCP/IP address string to an integer value

        int ipInt = parseNumericAddress(addr);
        if ( ipInt == 0)
            return null;

        // Convert to bytes

        byte[] ipByts = new byte[4];

        ipByts[3] = (byte) (ipInt & 0xFF);
        ipByts[2] = (byte) ((ipInt >> 8) & 0xFF);
        ipByts[1] = (byte) ((ipInt >> 16) & 0xFF);
        ipByts[0] = (byte) ((ipInt >> 24) & 0xFF);

        // Return the TCP/IP bytes

        return ipByts;
    }

    public final static int parseNumericAddress(String ipaddr) {

        //  Check if the string is valid
        if ( ipaddr == null || ipaddr.length() < 7 || ipaddr.length() > 15)
            return 0;

        //  Check the address string, should be n.n.n.n format
        StringTokenizer token = new StringTokenizer(ipaddr,".");
        if ( token.countTokens() != 4)
            return 0;
        int ipInt = 0;
        int count = 0;
        while ( token.hasMoreTokens()) {
            //  Get the current token and convert to an integer value
            String ipNum = token.nextToken().toString().trim();
            int ipVal = 0;
            if(count == 3 && ipNum.length() > 3)
                ipNum = ipNum.substring(0, ipNum.length()-1);

            ipVal =  Integer.parseInt(ipNum);
            if ( ipVal < 0 || ipVal > 255)
                return 0;
            //  Add to the integer address
            Log.d(SocketManager.class.getSimpleName(), "ipNum: "+ipNum);
            Log.d(SocketManager.class.getSimpleName(), "ipVal: "+ipVal);

            ipInt = (ipInt << 8) + ipVal;
            count++;
        }
        //  Return the integer address
        return ipInt;
    }

}
