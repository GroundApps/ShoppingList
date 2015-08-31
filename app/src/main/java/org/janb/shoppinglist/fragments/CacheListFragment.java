package org.janb.shoppinglist.fragments;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.janb.shoppinglist.R;
import org.janb.shoppinglist.model.ShoppingListAdapter;
import org.janb.shoppinglist.model.ShoppingListItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CacheListFragment extends ListFragment {

    private ListView mListView;
    private List<ShoppingListItem> ShoppingListItemList;
    private Context context;
    private ShoppingListAdapter shopListAdapter;

    public CacheListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.title_cached));
        }
        context = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cache, container, false);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onListItemClick(ListView l, View view, final int position, long id) {
        super.onListItemClick(l, view, position, id);
        ShoppingListItem clickedItem = ShoppingListItemList.get(position);
        clickedItem.toggleChecked();
        shopListAdapter = new ShoppingListAdapter(getActivity(), ShoppingListItemList);
        int index = mListView.getFirstVisiblePosition();
        View v = mListView.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - mListView.getPaddingTop());
        shopListAdapter = new ShoppingListAdapter(getActivity(), ShoppingListItemList);
        setListAdapter(shopListAdapter);
        mListView.setSelectionFromTop(index, top);
    }

    private void getListFromCache() {
        ShoppingListItemList = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        parseJSON(prefs.getString("cached_list", ""));
        if (ShoppingListItemList.size() > 0) {
            shopListAdapter = new ShoppingListAdapter(getActivity(), ShoppingListItemList);
            setListAdapter(shopListAdapter);
        } else {
            setEmptyText(getResources().getString(R.string.empty_view_cache));
        }
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public void parseJSON(String jsondata) {
        Log.d("JSON DATA", jsondata);
        String item_title;
        String item_count;
        JSONArray array = null;
        ShoppingListItem itemData = null;
        try {
            array = new JSONArray(jsondata);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (array != null){
            for (int i = 0; i < array.length(); i++) {
                JSONObject row = null;
                try {
                    row = array.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    assert row != null;
                    item_title = row.getString("itemTitle");
                    item_count = row.getString("itemCount");
                    itemData = new ShoppingListItem(item_title,Integer.parseInt(item_count));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ShoppingListItemList.add(itemData);
            }
        } else {
            Log.i("SHOPPING LIST", "No data from cached version");
        }

    }

    @Override
    public void onStart(){
        super.onStart();
        getListFromCache();
    }

}
