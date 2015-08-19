package org.janb.shoppinglist.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HttpsURLConnection;

public class ListAPI extends AsyncTask<String, Integer, String> {
    public static final  int FUNCTION_GETLIST = 1;
    public static final  int FUNCTION_SAVEITEM = 2;
    public static final  int FUNCTION_DELETEITEM = 3;
    public static final  int FUNCTION_CLEARLIST = 4;
    public static final  int FUNCTION_UPDATECOUNT = 5;

    public static final  int ERROR_SERVER = 900;
    public static final  int ERROR_CONNECT = 901;
    public static final  int ERROR_RESPONSE = 902;
    public static final  int ERROR_AUTH = 903;
    public static final  int ERROR_404 = 904;
    public static final  int ERROR_URL = 905;
    public static final  int ERROR_NO_HOST = 906;
    public static final  int ERROR = 999;

    ResultsListener listener;
    SharedPreferences prefs;
    static int chosenfunction;
    Context context;

    public ListAPI(Context context) {
        this.context = context;
    }

    public static void setFunction(int function){
        chosenfunction = function;
    }

    public void setOnResultsListener(ResultsListener listener) {
        this.listener = listener;
    }

    protected String doInBackground(String... params) {
        String result = null;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String AUTHKEY = prefs.getString("authkey", "");
        String URL = prefs.getString("host", "");
        if(URL.isEmpty()){
            listener.onQueryError(ERROR_NO_HOST);
            this.cancel(true);
            return null;
        }
        HashMap<String,String> parameters = new HashMap<>();

        switch (chosenfunction){
            case FUNCTION_GETLIST:
                parameters.put("function", "listall");
                parameters.put("auth", AUTHKEY);
               result = performPostCall(URL, parameters);
                break;
            case FUNCTION_SAVEITEM:
                parameters.put("function", "save");
                parameters.put("auth", AUTHKEY);
                parameters.put("item",params[0]);
                parameters.put("count",params[1]);
                result = performPostCall(URL, parameters);
                break;
            case FUNCTION_DELETEITEM:
                parameters.put("function", "delete");
                parameters.put("auth", AUTHKEY);
                parameters.put("item",params[0]);
                result = performPostCall(URL, parameters);
                break;
            case FUNCTION_CLEARLIST:
                parameters.put("function", "clear");
                parameters.put("auth", AUTHKEY);
                result = performPostCall(URL, parameters);
                break;
            case FUNCTION_UPDATECOUNT:
                parameters.put("function", "update");
                parameters.put("auth", AUTHKEY);
                parameters.put("item",params[0]);
                parameters.put("count",params[1]);
                result = performPostCall(URL, parameters);
                break;
        }



        return result;
    }

    public String  performPostCall(String requestURL, HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();
            switch (responseCode) {
                case HttpsURLConnection.HTTP_OK:
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    break;
                case HttpsURLConnection.HTTP_FORBIDDEN:
                    listener.onQueryError(ERROR_AUTH);
                    this.cancel(true);
                    break;
                case HttpsURLConnection.HTTP_NOT_FOUND:
                    listener.onQueryError(ERROR_404);
                    this.cancel(true);
                    break;
                case HttpsURLConnection.HTTP_INTERNAL_ERROR:
                    listener.onQueryError(ERROR_SERVER);
                    this.cancel(true);
                    break;

            }
        } catch (UnknownHostException | ConnectException e) {
            listener.onQueryError(ERROR_CONNECT);
            this.cancel(true);
            e.printStackTrace();
        } catch (MalformedURLException e) {
            listener.onQueryError(ERROR_URL);
            this.cancel(true);
            e.printStackTrace();
        } catch (IOException e) {
            listener.onQueryError(ERROR);
            this.cancel(true);
            e.printStackTrace();
        }
        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(String result) {
        if (result != null && listener != null) {
            switch (chosenfunction){
                case FUNCTION_GETLIST:
                    listener.onListReceived(result);
                    break;
                default:
                    listener.onQuerySuccess(result);
                    break;
            }

        } else {
            Log.e("SHOPPING LIST", "Result was NULL");
        }
    }
}
