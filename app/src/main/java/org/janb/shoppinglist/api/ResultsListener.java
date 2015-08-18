package org.janb.shoppinglist.api;

public interface ResultsListener {
    public void onListReceived(String list);
    public void onQuerySuccess(String response);
    public void onQueryError(int errorDescription);
}

