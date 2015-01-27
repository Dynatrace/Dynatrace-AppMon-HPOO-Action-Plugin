package com.hpoo.runFlow;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;


public class URLdownload{

	private static final Logger log = Logger.getLogger(RunFlow.class.getName());
	public URLdownload()
	{
		return;
	}
	public static String getURLString(String theURL, final String UserName, final String Password){
		Authenticator.setDefault (new Authenticator() {
    	    @Override
			protected PasswordAuthentication getPasswordAuthentication() {
    	        return new PasswordAuthentication (UserName, Password.toCharArray());
    	    }
    	});
        String FinalHTML = "";
        URL url;
        //log.fine("URL: " + theURL);
		try {
			//theURL = escapeHtml3(theURL);
			url = new URL(theURL);
	        
	        if(theURL.contains("https"))
			{
	        	final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	    	        @Override
	    	        public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
	    	        }
	    	        @Override
	    	        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
	    	        }
	    	        @Override
	    	        public X509Certificate[] getAcceptedIssuers() {
	    	            return null;
	    	        }
	    	    } };
	    	    
	    	    // Install the all-trusting trust manager
	    	    SSLContext sslContext;
	    	    SSLSocketFactory sslSocketFactory = null;
	    		try {
	    			sslContext = SSLContext.getInstance( "SSL" );
	    			sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
	    			sslSocketFactory = sslContext.getSocketFactory();
	    			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
	    		} catch (KeyManagementException e2) {
	    			e2.printStackTrace();
	    			log.log(Level.SEVERE, "Exception: ", e2);
	    		} catch (NoSuchAlgorithmException e3) {
	    			e3.printStackTrace();
	    			log.log(Level.SEVERE, "Exception: ", e3);
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			log.log(Level.SEVERE, "Exception: ", e);
	    		}
	        	HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
	                @Override
					public boolean verify(String s, SSLSession sslSession) {
	                    return true;
	                }
	            });
	        	HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
	        	con.setSSLSocketFactory( sslSocketFactory );
	        	Reader reader = new InputStreamReader(con.getInputStream());
		        while (true) {
		            int ch = reader.read();
		            if (ch==-1) {
		                break;
		            }
		            FinalHTML += (char)ch;
		        }
			}
	        else
	        {
	        	URLConnection con = url.openConnection();
	        	Reader reader = new InputStreamReader(con.getInputStream());
		        while (true) {
		            int ch = reader.read();
		            if (ch==-1) {
		                break;
		            }
		            FinalHTML += (char)ch;
		        }
	        }
		} catch (MalformedURLException e) {
			e.printStackTrace();
			log.log(Level.SEVERE, "Exception: ", e);
		} catch (IOException e) {
			e.printStackTrace();
			log.log(Level.SEVERE, "Exception: ", e);
		}
		 catch (Exception e) {
				e.printStackTrace();
				log.log(Level.SEVERE, "Exception: ", e);
		}
		return FinalHTML;
    }

}
