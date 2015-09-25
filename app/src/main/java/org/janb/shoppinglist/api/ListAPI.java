package org.janb.shoppinglist.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

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
import org.janb.shoppinglist.CONSTS;
import org.janb.shoppinglist.R;

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

public class ListAPI extends AsyncTask<String, Integer, Boolean> {
    public static final  int FUNCTION_GETLIST = 1;
    public static final  int FUNCTION_SAVEITEM = 2;
    public static final  int FUNCTION_DELETEITEM = 3;
    public static final  int FUNCTION_CLEARLIST = 4;
    public static final  int FUNCTION_UPDATECOUNT = 5;
    public static final  int FUNCTION_DELETE_MULTIPLE = 6;
    public static final  int FUNCTION_SAVE_MULTIPLE = 7;


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

     public Boolean doInBackground(String... params) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String AUTHKEY = prefs.getString("authkey", "");
        String URL = prefs.getString("host", "");
         if(URL.isEmpty()){
             listener.onError(new ResponseHelper(CONSTS.APP_ERROR_CONFIG_NO_HOST, context.getResources().getString(R.string.error_no_host_configured)));
             this.cancel(true);
         }
         URL = URL.replace("https://", "");
         URL = URL.replace("http://", "");
         String lastChar = URL.substring(URL.length() - 1);
         if(!lastChar.equals("/") || !lastChar.equals("p")){
             URL = URL+"/";
         }
         if(prefs.getBoolean("useSSL", false)) {
             URL = "https://" + URL;
         } else {
             URL = "http://" + URL;
         }
        HashMap<String,String> parameters = new HashMap<>();

        switch (chosenfunction){
            case FUNCTION_GETLIST:
                parameters.put("function", "listall");
                parameters.put("auth", AUTHKEY);
                performPostCall(URL, parameters);
                break;
            case FUNCTION_SAVEITEM:
                parameters.put("function", "save");
                parameters.put("auth", AUTHKEY);
                parameters.put("item",params[0]);
                parameters.put("count",params[1]);
                performPostCall(URL, parameters);
                break;
            case FUNCTION_DELETEITEM:
                parameters.put("function", "delete");
                parameters.put("auth", AUTHKEY);
                parameters.put("item",params[0]);
                performPostCall(URL, parameters);
                break;
            case FUNCTION_CLEARLIST:
                parameters.put("function", "clear");
                parameters.put("auth", AUTHKEY);

                performPostCall(URL, parameters);
                break;
            case FUNCTION_UPDATECOUNT:
                parameters.put("function", "update");
                parameters.put("auth", AUTHKEY);
                parameters.put("item",params[0]);
                parameters.put("count",params[1]);
                performPostCall(URL, parameters);
                break;
            case FUNCTION_DELETE_MULTIPLE:
                parameters.put("function", "deleteMultiple");
                parameters.put("auth", AUTHKEY);
                parameters.put("jsonArray",params[0]);
                performPostCall(URL, parameters);
                break;
            case FUNCTION_SAVE_MULTIPLE:
                parameters.put("function", "saveMultiple");
                parameters.put("auth", AUTHKEY);
                parameters.put("jsonArray",params[0]);
                Log.d("PARAMS", parameters.toString());
                performPostCall(URL, parameters);
                break;
        }

        return true;
    }

    public void  performPostCall(String requestURL, HashMap<String, String> postDataParams) {
        URL url;
        String response = "";
        Double backend_version = 0.0;
        ResponseHelper responseHelper;

        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", context.getString(R.string.HttpUserAgent));
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
            Log.d("RESPONSE CODE", String.valueOf(responseCode));
            Log.d("URL", requestURL);
            switch (responseCode) {
                case HttpsURLConnection.HTTP_OK:
                    if (conn.getHeaderField("ShoLiBackendVersion") != null) {
                        backend_version = Double.parseDouble(conn.getHeaderField("ShoLiBackendVersion"));
                    }
                    //Check if backend version has the minimum required version to work with the app
                    Log.d("BACKEND VERSION", String.valueOf(backend_version));
                    if(backend_version < 1 ){
                        listener.onError(new ResponseHelper(CONSTS.API_ERROR_NO_VERSION, context.getResources().getString(R.string.error_no_header)));
                        this.cancel(true);
                        return;
                    }
                    if (backend_version < CONSTS.MINIMUM_REQUIRED_BACKEND_VERSION){
                        listener.onError(new ResponseHelper(CONSTS.APP_BACKEND_VERSION, "Backend version: " + String.valueOf(backend_version) + "\n" + context.getResources().getString(R.string.error_backend_version) + String.valueOf(CONSTS.MINIMUM_REQUIRED_BACKEND_VERSION) + "\n" + context.getResources().getString(R.string.error_backend_version_update_notice)));
                        this.cancel(true);
                        return;
                    }

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    Log.d("RESPONSE RAW", response);
                    Gson gson = new Gson();
                    try {
                        responseHelper = gson.fromJson(response, ResponseHelper.class);
                    } catch (Exception e){
                        listener.onError(new ResponseHelper(CONSTS.APP_ERROR_RESPONSE, "The response did not make sense:" + response));
                        this.cancel(true);
                        return;
                    }
                    listener.onResponse(responseHelper);
                    this.cancel(true);
                    break;
                case HttpsURLConnection.HTTP_INTERNAL_ERROR:
                    listener.onError(new ResponseHelper(CONSTS.API_ERROR_SERVER, context.getResources().getString(R.string.error_server_error)));
                    this.cancel(true);
                    break;
                case HttpsURLConnection.HTTP_FORBIDDEN:
                    listener.onError(new ResponseHelper(CONSTS.API_ERROR_403, context.getResources().getString(R.string.error_auth)));
                    this.cancel(true);
                    break;
                case HttpsURLConnection.HTTP_NOT_FOUND:
                    listener.onError(new ResponseHelper(CONSTS.API_ERROR_404, context.getResources().getString(R.string.error_not_found)));
                    this.cancel(true);
                    break;
            }
        } catch (UnknownHostException e ){
            listener.onError(new ResponseHelper(CONSTS.APP_ERROR_HOST_NOT_FOUND, context.getResources().getString(R.string.error_host_not_found)));
            this.cancel(true);
            e.printStackTrace();
        } catch(ConnectException e) {
            listener.onError(new ResponseHelper(CONSTS.APP_ERROR_CONNECT, context.getResources().getString(R.string.error_connect)));
            this.cancel(true);
            e.printStackTrace();
        } catch (MalformedURLException e) {
            listener.onError(new ResponseHelper(CONSTS.APP_ERROR_URL_EXCEPTION, context.getResources().getString(R.string.error_url)));
            this.cancel(true);
            e.printStackTrace();
        } catch (IOException e) {
            listener.onError(new ResponseHelper(CONSTS.APP_ERROR_IO, "IO Exception"));
            this.cancel(true);
            e.printStackTrace();
        } catch (Exception e) {
            listener.onError(new ResponseHelper(CONSTS.APP_ERROR_UNKNOWN, "Unknown error! Please check logcat."));
            e.printStackTrace();
        }
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

}
