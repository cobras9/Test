package com.mobilis.android.nfc.network;

import android.util.Log;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Allows you to trust certificates from additional KeyStores in addition to
 * the default KeyStore
 **/

public class CustomSSLSocketFactory extends SSLSocketFactory {
    protected SSLContext sslContext = SSLContext.getInstance("TLS");
    private static X509Certificate[] lastCertChain = null;
    
    public CustomSSLSocketFactory(KeyStore keyStore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(null, null, null, null, null, null);
        sslContext.init(null, new TrustManager[]{new CustomTrustManager(keyStore)}, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }



    public static X509Certificate[] getLastCertChain() {
		return lastCertChain;
	}

	public static void setLastCertChain(X509Certificate[] lastCertChain) {
		CustomSSLSocketFactory.lastCertChain = lastCertChain;
	}
	
	
	/**
     * Based on http://download.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#X509TrustManager
     **/
    private static class CustomTrustManager implements X509TrustManager {

        protected ArrayList<X509TrustManager> x509TrustManagers = new ArrayList<X509TrustManager>();

        protected CustomTrustManager(KeyStore... additionalkeyStores) {
            final ArrayList<TrustManagerFactory> factories = new ArrayList<TrustManagerFactory>();
            try {
                for( KeyStore keyStore : additionalkeyStores ) {
                    final TrustManagerFactory additionalTMF = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    additionalTMF.init(keyStore);
                    
                    Log.v("CustomTrustManager","TrustManagerFactory loaded with our keystore!!");
                    
                    if(keyStore.getCertificate("mobilis cert") != null)
                    	Log.v("CustomTrustManager","The public key of the certificate inside our keystore is: "+keyStore.getCertificate("mobilis cert").getPublicKey());
                    factories.add(additionalTMF);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            /**
             * Iterate over the returned trustmanagers, and hold on
             * to any that are X509TrustManagers
             **/
            for (TrustManagerFactory tmf : factories)
                for( TrustManager tm : tmf.getTrustManagers() )
                    if (tm instanceof X509TrustManager)
                        x509TrustManagers.add( (X509TrustManager)tm );


            if( x509TrustManagers.size()==0 )
                throw new RuntimeException("Couldn't find any X509TrustManagers");

        }

        /**
         * Delegate to the default trust manager.
         **/
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            final X509TrustManager defaultX509TrustManager = x509TrustManagers.get(0);
            defaultX509TrustManager.checkClientTrusted(chain, authType);
        }

        /**
         * Loop over the trustmanagers until we find one that accepts our server
         **/
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        	Log.v("CustomTrustManager","number of x509TrustManagers: "+x509TrustManagers.size());
            for( X509TrustManager tm : x509TrustManagers ) {
                try {
                    tm.checkServerTrusted(chain,authType);
                    return;
                } catch( CertificateException e ) {
                    e.printStackTrace();
                }
            }
            throw new CertificateException();
        }

        public X509Certificate[] getAcceptedIssuers() {
            final ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
            for( X509TrustManager tm : x509TrustManagers )
                list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
            return list.toArray(new X509Certificate[list.size()]);
        }
    }

}