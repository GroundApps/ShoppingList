package org.janb.shoppinglist.api;

import android.os.AsyncTask;

import java.net.URL;
import java.net.URLConnection;

class checkHost extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... params) {
            try{
                URL myUrl = new URL("https://"+ params[0]);
                URLConnection connection = myUrl.openConnection();
                connection.setConnectTimeout(5000);
                connection.connect();
                return true;
            } catch (Exception e) {
                return false;
            }
    }
}
