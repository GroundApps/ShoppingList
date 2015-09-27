package org.janb.shoppinglist.api;

public interface GitResultsListener {
    void onResponse(String response);
    void onError(String error);
}

