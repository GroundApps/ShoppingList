package org.janb.shoppinglist.api;

public class BackendSettings {
    private String url;
    private String apikey;
    private Boolean ssl;

    public boolean allSet() {
        return !(this.apikey.isEmpty() || this.url.isEmpty());
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

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
