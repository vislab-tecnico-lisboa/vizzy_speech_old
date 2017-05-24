package com.github.woz_dialog.ros_woz_dialog_project;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class TTSHTTPClient {

    private String APP_ID = "Insert Your App Id";
    private String APP_KEY = "Insert Your 128-Byte App Key";
    private String DEVICE_ID = "0000";
    private String VOICE = "Samantha";
    private String LANGUAGE = "en_US";
    private String CODEC = "audio/x-wav;codec=pcm;bit=16;rate=22000";	//MP3
    private String TEXT = "Hello World. This is a greeting from Nuance.";

    private static short PORT = (short) 443;
    private static String HOSTNAME = "tts.nuancemobility.net";
    private static String TTS = "/NMDPTTSCmdServlet/tts";

    private HttpClient httpclient = null;

    private URI uri;
    private HttpPost httppost;
    HttpResponse response;


    public TTSHTTPClient(String app_id, String app_key, String device_id, String voice, String language, String codec)
    {
        APP_ID = app_id;
        APP_KEY = app_key;
        DEVICE_ID = device_id;
        VOICE = voice;
        CODEC = codec;

    }

    public void performTTS(String TEXT)
    {
        try {
            httpclient = getHttpClient();
            uri = getURI();
            httppost = getHeader(uri, TEXT);

            System.out.println("executing request " + httppost.getRequestLine());

            response = httpclient.execute(httppost);

            processResponse(response);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            if(httpclient != null)
               httpclient.getConnectionManager().shutdown();
        }

    }



    private HttpClient getHttpClient() throws NoSuchAlgorithmException, KeyManagementException
    {
        // Standard HTTP parameters
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUseExpectContinue(params, false);

        // Initialize the HTTP client
        httpclient = new DefaultHttpClient(params);

        // Initialize/setup SSL
        TrustManager easyTrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] arg0, String arg1)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] arg0, String arg1)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                // TODO Auto-generated method stub
                return null;
            }
        };

        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, new TrustManager[] { easyTrustManager }, null);
        SSLSocketFactory sf = new SSLSocketFactory(sslcontext);
        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme sch = new Scheme("https", sf, PORT);	// PORT = 443
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);

        // Return the initialized instance of our httpclient
        return httpclient;
    }

    private URI getURI() throws Exception
    {
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();

        qparams.add(new BasicNameValuePair("appId", APP_ID));
        qparams.add(new BasicNameValuePair("appKey", APP_KEY));
        qparams.add(new BasicNameValuePair("id",  DEVICE_ID));
        qparams.add(new BasicNameValuePair("voice", VOICE));
        //qparams.add(new BasicNameValuePair("ttsLang", LANGUAGE));

        URI uri = URIUtils.createURI("https", HOSTNAME, PORT, TTS, URLEncodedUtils.format(qparams, "UTF-8"), null);

        return uri;
    }

    private HttpPost getHeader(URI uri, String TEXT) throws UnsupportedEncodingException
    {
        HttpPost httppost = new HttpPost(uri);
        httppost.addHeader("Content-Type",  "text/plain");
        httppost.addHeader("Accept", CODEC);

        // We'll also set the content of the POST request now...
        HttpEntity entity = new StringEntity(TEXT, "utf-8");

        this.TEXT = TEXT;

        httppost.setEntity(entity);

        return httppost;
    }

    private void processResponse(HttpResponse response) throws IllegalStateException, IOException
    {
        HttpEntity resEntity = response.getEntity();

        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());

        // The request failed. Check out the status line to see what the problem is.
        //	Typically an issue with one of the parameters passed in...
        if (resEntity == null)
            return;

        // Grab the date
        Header date = response.getFirstHeader("Date");
        if( date != null )
            System.out.println("Date: " + date.getValue());

        // ALWAYS grab the Nuance-generated session id. Makes it a WHOLE LOT EASIER for us to hunt down your issues in our logs
        Header sessionid = response.getFirstHeader("x-nuance-sessionid");
        if( sessionid != null )
            System.out.println("x-nuance-sessionid: " + sessionid.getValue());

        // Check to see if we have a 200 OK response. Otherwise, review the technical documentation to understand why you recieved
        //	the HTTP error code that came back
        String status = response.getStatusLine().toString();
        boolean okFound = ( status.indexOf("200 OK") > -1 );
        if( okFound )
        {
            System.out.println("Response content length: " + resEntity.getContentLength());
            System.out.println("Chunked?: " + resEntity.isChunked());
        }

        // Grab the returned audio (or error message) returned in the body of the response
        InputStream in = resEntity.getContent();
        byte[] buffer = new byte[1024 * 16];
        int len;

        // Open up a stream to write audio to file
        OutputStream fos = null;
        String file = null;
        if (okFound)	// We have audio
        {
            file = TEXT + ".wav";
        }  else			// No audio...
        {
            file = "log-err-"+ TEXT + ".htm";
        }

        // Attempt to write to file...
        try {
            fos = new FileOutputStream(file);

            while((len = in.read(buffer)) > 0){
                fos.write(buffer, 0 , len);
            }
        } catch (Exception e) {
            System.err.println("Failed to save file: " + e.getMessage());
            e.printStackTrace();
        }

        // Finish up...
        finally {
            if(fos != null)
                try {
                    fos.close();
                    System.out.println("Saved file: " + file);
                } catch (IOException e) {
                }
        }

        System.out.println("----------------------------------------");

        // And we're done.
        resEntity.consumeContent();
    }

}
