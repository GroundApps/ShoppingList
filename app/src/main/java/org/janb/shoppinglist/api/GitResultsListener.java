package org.janb.shoppinglist.api;

import java.util.List;

public interface GitResultsListener {
    void onResponse(List<String> response);
    void onError(String error);
}

