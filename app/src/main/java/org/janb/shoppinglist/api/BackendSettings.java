package org.janb.shoppinglist.api;

public class BackendSettings {
    private String url, apikey;
    private Boolean ssl;

    public String getHost() {
        return url;
    }

    public String getAuth() {
        return apikey;
    }

    public Boolean getSsl() {
        return ssl;
    }
}
