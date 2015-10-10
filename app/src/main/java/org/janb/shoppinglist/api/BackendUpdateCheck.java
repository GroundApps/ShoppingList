package org.janb.shoppinglist.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.janb.shoppinglist.CONSTS;
import org.janb.shoppinglist.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

public class BackendUpdateCheck extends AsyncTask<String, Integer, Boolean> {

    GitResultsListener listener;
    SharedPreferences prefs;
    Context context;
    String CURRENT_BACKEND_VERSION;

    public BackendUpdateCheck(Context context) {
        this.context = context;
    }

    public void setOnResultsListener(GitResultsListener listener) {
        this.listener = listener;
    }

     public Boolean doInBackground(String... params) {
         prefs = PreferenceManager.getDefaultSharedPreferences(context);
         CURRENT_BACKEND_VERSION = prefs.getString("CURRENT_BACKEND_VERSION", "0");
         performPostCall(context.getResources().getString(R.string.GITHUB_RELEASE_URL));
         return true;
    }

    public void  performPostCall(String requestURL) {
        URL url;
        String response = "";

        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", context.getString(R.string.HttpUserAgent));
            conn.setRequestProperty("Accept", "application/json; charset=utf-8");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setDoInput(true);

            int responseCode = conn.getResponseCode();
            Log.d("GIT URL", url.toString());
            Log.d("GIT RESPONSE", String.valueOf(responseCode));
            switch (responseCode) {
                case HttpsURLConnection.HTTP_OK:
                    Log.d("CURRENT BACKEND VERSION", String.valueOf(CURRENT_BACKEND_VERSION));
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    Log.d("GIT RESPONSE RAW", response);
                    JsonObject jo = new JsonParser().parse(response).getAsJsonObject();
                    String gitVersion = jo.get("name").toString();
                    gitVersion = "1";
                    List responseList = new ArrayList<>();
                    responseList.add(CONSTS.GIT_RESPONSE_VERSION_TEXT, gitVersion);
                    responseList.add(CONSTS.GIT_RESPONSE_BODY_TEXT, jo.get("body").toString());
                    listener.onResponse(responseList);
                    this.cancel(true);
                    break;
                case HttpsURLConnection.HTTP_INTERNAL_ERROR:
                    listener.onError("500 Server Error");
                    this.cancel(true);
                    break;
                case HttpsURLConnection.HTTP_FORBIDDEN:
                    listener.onError("403 Forbidden");
                    this.cancel(true);
                    break;
                case HttpsURLConnection.HTTP_NOT_FOUND:
                    listener.onError("404 Not Found");
                    this.cancel(true);
                    break;
            }
        } catch (UnknownHostException e ){
            listener.onError("UnknownHostException");
            this.cancel(true);
            e.printStackTrace();
        } catch(ConnectException e) {
            listener.onError("ConnectException");
            this.cancel(true);
            e.printStackTrace();
        } catch (MalformedURLException e) {
            listener.onError("MalformedURLException");
            this.cancel(true);
            e.printStackTrace();
        } catch (IOException e) {
            listener.onError("IOException");
            this.cancel(true);
            e.printStackTrace();
        } catch (Exception e) {
            listener.onError("Exception");
            this.cancel(true);
            e.printStackTrace();
        }
    }
}
