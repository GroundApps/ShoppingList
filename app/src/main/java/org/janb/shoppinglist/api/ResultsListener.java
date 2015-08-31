package org.janb.shoppinglist.api;

public interface ResultsListener {
    void onResponse(ResponseHelper responseHelper);
    void onError(ResponseHelper responseHelper);
}

