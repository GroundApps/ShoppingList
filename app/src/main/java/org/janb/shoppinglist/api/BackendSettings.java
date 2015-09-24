package org.janb.shoppinglist.api;

public class BackendSettings {
    private String host, apikey;
    private Boolean ssl;

    public String getHost() {
        return host;
    }

    public String getAuth() {
        return apikey;
    }

    public Boolean getSsl() {
        return ssl;
    }
}
