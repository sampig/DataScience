/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

/**
 * A utility for TagME.
 * 
 * @author Chenfeng Zhu
 *
 */
public class TagMeUtils {

  private static double threshold = 0.5;

  /**
   * Send a request to the URL and get the response.
   * 
   * @param url
   * @return
   */
  @Deprecated
  public static String requestGET(String url) {
    String result = null;
    SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
    try {
      sslContextBuilder.loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE);
      SSLContext sslContext = sslContextBuilder.build();
      // final SSLContext sslContext = SSLContext.getInstance("SSL");
      final TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(final java.security.cert.X509Certificate[] arg0,
            final String arg1) throws CertificateException {
          // do nothing and blindly accept the certificate
        }

        public void checkServerTrusted(final java.security.cert.X509Certificate[] arg0,
            final String arg1) throws CertificateException {
          // do nothing and blindly accept the server
        }
      }};
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
          new String[] {"TLSv1"}, null, NoopHostnameVerifier.INSTANCE);
      // SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      // SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
      // new String[] {"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"}, null,
      // NoopHostnameVerifier.INSTANCE);
      // new SSLConnectionSocketFactory(sslContext, new DefaultHostnameVerifier());
      HttpClientBuilder clientBuilder = HttpClients.custom().setSSLSocketFactory(sslSocketFactory);
      HttpClient client = clientBuilder.build();
      HttpGet request = new HttpGet(url);
      HttpResponse response;
      response = client.execute(request);
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        InputStream instream = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
          while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        instream.close();
        result = sb.toString();
      }
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    } catch (KeyStoreException e) {
      e.printStackTrace();
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  /**
   * Send the text to the TagME url and get the response.
   * 
   * @param text
   * @return
   */
  @Deprecated
  public static Map<String, String> getMapping(String text) {
    Map<String, String> map = new HashMap<>(0);
    String url = ConfigUtils.getTagME();
    try {
      url = url + URLEncoder.encode(text, "UTF-8");
      String response = TagMeUtils.requestGET(url);
      JsonReader jsonReader = Json.createReader(new StringReader(response));
      JsonObject jsonObject = jsonReader.readObject();
      for (JsonValue jsonValue : jsonObject.getJsonArray("annotations")) {
        JsonObject obj = jsonValue.asJsonObject();
        if (obj.getJsonNumber("link_probability") == null || obj.getString("spot") == null
            || obj.getString("title") == null) {
          continue;
        }
        if (obj.getJsonNumber("link_probability").doubleValue() > threshold
            || obj.getString("spot").equalsIgnoreCase(obj.getString("title"))) {
          map.put(obj.getString("spot"), obj.getString("title"));
        }
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return map;
  }

  /**
   * Send a request to the URL and get the response.
   * 
   * @param url
   * @return
   */
  @Deprecated
  public static String get(String url) {
    StringBuffer response = new StringBuffer();
    URL obj;
    try {
      obj = new URL(url);
      HttpURLConnection con = (HttpURLConnection) obj.openConnection();
      // optional default is GET
      con.setRequestMethod("GET");
      // add request header
      con.setRequestProperty("User-Agent", "Mozilla/5.0");
      int responseCode = con.getResponseCode();
      System.out.println("\nSending 'GET' request to URL : " + url);
      System.out.println("Response Code : " + responseCode);
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return response.toString();
  }

}
