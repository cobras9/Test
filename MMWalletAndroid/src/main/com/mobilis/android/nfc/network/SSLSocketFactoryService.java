package com.mobilis.android.nfc.network;

/**
	�* @Author Ahmed Shurrab
	�* www.mobilis.com
	�* Last Modified On: 04-09-2010
	�*
	�* A class that reads SSL Certificate from a SSL Server
	�* and then prints some basic details.
*/

import android.content.Context;

import com.mobilis.android.nfc.R;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.InputStream;
import java.security.KeyStore;

public class SSLSocketFactoryService {
	private static Context context;
	private static SSLSocketFactory ssFactory;
	
	public static synchronized SSLSocketFactory getSSLSocketFactoryService(Context context){
		if(ssFactory == null){
			setContext(context);
			ssFactory = createSSLSocketFactory();
		}
		return ssFactory;
	}
	
	private static SSLSocketFactory createSSLSocketFactory() {
	    try {
	        final KeyStore ks = KeyStore.getInstance("BKS"); 
	        final InputStream in = getContext().getResources().openRawResource( R.raw.keystore);  
	        try {
	            ks.load(in, "@ndr01dm0b1l15".toCharArray()); 
	        } finally {
	            in.close();
	        }
	        return new CustomSSLSocketFactory(ks);

	    } catch( Exception e ) {
	    	e.printStackTrace();
	        throw new RuntimeException(e);
	    }
	}

	public static  Context getContext() {
		return context;
	}

	public static  void setContext(Context context) {
		SSLSocketFactoryService.context = context;
	}
	
	public SSLSocketFactory getSsFactory() {
		return ssFactory;
	}
	
	public void setSsFactory(SSLSocketFactory ssFactory) {
		SSLSocketFactoryService.ssFactory = ssFactory;
	}
	

}