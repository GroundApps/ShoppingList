package org.janb.shoppinglist.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirections;

import org.janb.shoppinglist.R;
import org.janb.shoppinglist.api.ListAPI;
import org.janb.shoppinglist.api.ResultsListener;
import org.janb.shoppinglist.model.ShoppingListAdapter;
import org.janb.shoppinglist.model.ShoppingListItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ShoppingListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener, ResultsListener, View.OnClickListener{

    SwipeActionAdapter mAdapter;
    private ListView mListView;
    private ShoppingListAdapter  shopListAdapter;
    private List<ShoppingListItem> ShoppingListItemList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListAPI api;
    private Context context;
    private FloatingActionsMenu action_main;
    private MaterialDialog dialog;
    private Boolean isFavorite = false;

    public ShoppingListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Shopping List");
        context = getActivity().getApplicationContext();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sholiitem_list, container, false);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (mListView == null || mListView.getChildCount() == 0) ?
                                0 : mListView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
        FloatingActionButton action_a = (FloatingActionButton) rootView.findViewById(R.id.main_action_a);
        action_a.setOnClickListener(this);
        FloatingActionButton action_b = (FloatingActionButton) rootView.findViewById(R.id.main_action_b);
        action_b.setOnClickListener(this);
        action_main = (FloatingActionsMenu) rootView.findViewById(R.id.main_multiple_actions);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
    }

    private void deleteItem(String itemTitle) {
        api = new ListAPI(context);
        api.setOnResultsListener(this);
        ListAPI.setFunction(ListAPI.FUNCTION_DELETEITEM);
        api.execute(itemTitle);
    }

    private void saveItem(String itemTitle, String itemCount) {
        api = new ListAPI(context);
        api.setOnResultsListener(this);
        ListAPI.setFunction(ListAPI.FUNCTION_SAVEITEM);
        api.execute(itemTitle, String.valueOf(itemCount));
    }

    private void updateItem(String itemTitle, String itemCount) {
        api = new ListAPI(context);
        api.setOnResultsListener(this);
        ListAPI.setFunction(ListAPI.FUNCTION_UPDATECOUNT);
        api.execute(itemTitle, String.valueOf(itemCount));
    }

    private void clearList() {
        api = new ListAPI(context);
        api.setOnResultsListener(this);
        ListAPI.setFunction(ListAPI.FUNCTION_CLEARLIST);
        api.execute();
    }

    private void getList() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        api = new ListAPI(getActivity().getApplicationContext());
        api.setOnResultsListener(this);
        ListAPI.setFunction(ListAPI.FUNCTION_GETLIST);
        api.execute();
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);
        String item_count = String.valueOf(ShoppingListItemList.get(position).getItemCount());
        dialog = new MaterialDialog.Builder(getActivity())
                .title(ShoppingListItemList.get(position).getItemTitle())
                .customView(R.layout.dialog_update_item,true)
                .positiveText("OK")
                .negativeText("Abbruch")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        TextView dialog_update_count = (TextView) dialog.findViewById(R.id.dialog_update_count);
                        if (!dialog_update_count.getText().toString().isEmpty()) {
                            updateItem(ShoppingListItemList.get(position).getItemTitle(), dialog_update_count.getText().toString());
                            if (isFavorite) {
                                addToFavorites(ShoppingListItemList.get(position).getItemTitle());
                                isFavorite = false;
                            }
                        }
                    }})
                .show();
        TextView dialog_update_count = (TextView) dialog.findViewById(R.id.dialog_update_count);
        dialog_update_count.setText(item_count);
        dialog_update_count.requestFocus();
        List<String> favorites = getFavorites();
        if (favorites.contains(ShoppingListItemList.get(position).getItemTitle())) {
            dialog.findViewById(R.id.dialog_update_favorite).setBackgroundResource(R.drawable.ic_action_important);
        } else {
            dialog.findViewById(R.id.dialog_update_favorite).setBackgroundResource(R.drawable.ic_action_not_important);
        }
        dialog.findViewById(R.id.dialog_update_favorite).setOnClickListener(this);
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void onRefresh() {
        getList();
    }

    public void parseJSON(String jsondata) {
        String item_title = null;
        String item_count = null;
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
                    item_title = row.getString("item");
                    item_count = row.getString("count");
                    itemData = new ShoppingListItem(item_title,Integer.parseInt(item_count));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ShoppingListItemList.add(itemData);
            }
        } else {
            onQueryError(ListAPI.ERROR_SERVER);
            Log.i("SHOPPING LIST", "No data received from server");
        }

    }

    @Override
    public void onListReceived(String list) {
        resetRefreshing();
        ShoppingListItemList = new ArrayList<>();
        Log.d("JSON", list);
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        prefs.edit().putString("cached_list", list).apply();
        parseJSON(list);
        shopListAdapter = new ShoppingListAdapter(getActivity(), ShoppingListItemList);
        mAdapter = new SwipeActionAdapter(shopListAdapter);
        mAdapter.setListView(getListView());
        setListAdapter(mAdapter);
        mAdapter.addBackground(SwipeDirections.DIRECTION_NORMAL_LEFT, R.layout.row_bg_left)
                .addBackground(SwipeDirections.DIRECTION_FAR_LEFT, R.layout.row_bg_left);
        mAdapter.setSwipeActionListener(new SwipeActionAdapter.SwipeActionListener() {
            @Override
            public boolean hasActions(int position) {
                return true;
            }

            @Override
            public boolean shouldDismiss(int position, int direction) {
                return direction == SwipeDirections.DIRECTION_NORMAL_LEFT;
            }

            @Override
            public void onSwipe(int[] positionList, int[] directionList) {
                for (int i = 0; i < positionList.length; i++) {
                    int direction = directionList[i];
                    int position = positionList[i];

                    switch (direction) {
                        case SwipeDirections.DIRECTION_NORMAL_LEFT:
                            deleteItem(ShoppingListItemList.get(position).getItemTitle());
                            break;
                        case SwipeDirections.DIRECTION_FAR_LEFT:
                            deleteItem(ShoppingListItemList.get(position).getItemTitle());
                            break;
                    }
                }
            }
        });
        mAdapter.notifyDataSetChanged();
        if(mAdapter.isEmpty()){
            setEmptyText("Nothing on your list... for now :)");
        }
    }

    @Override
    public void onQuerySuccess(String response) {
        resetRefreshing();
        action_main.animate().translationYBy(-80);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                action_main.animate().translationYBy(80);
            }
        }, 1800);
        Snackbar.make(action_main, "Success", Snackbar.LENGTH_SHORT).show();
        getList();
    }

    @Override
    public void onQueryError(int errorDescription) {
        resetRefreshing();
        ErrorFragment errFR = null;
        switch (errorDescription){
            case ListAPI.ERROR_AUTH:
                errFR = new ErrorFragment("Auth failure", "The server rejected the supplied auth key!");
                break;
            case ListAPI.ERROR_404:
                errFR = new ErrorFragment("API not found", "API could not be access on the supplied host.");
                break;
            case ListAPI.ERROR_CONNECT:
                errFR = new ErrorFragment("Connection failed", "Could not connect to the server at: " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("host","none configured!"));
                break;
            case ListAPI.ERROR_SERVER:
                errFR = new ErrorFragment("Server failure", "The server did not send any content back.");
                break;
            case ListAPI.ERROR_RESPONSE:
                errFR = new ErrorFragment("Server failure", "The server sent a response that did not make sense!");
                break;
            case ListAPI.ERROR_URL:
                errFR = new ErrorFragment("Server failure", "The supplied URL ("+PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("host","none configured!")+") is not valid.");
                break;
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, errFR);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();

    }

    private void resetRefreshing() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.main_action_a:
                action_main.collapse();
                dialog = new MaterialDialog.Builder(getActivity())
                        .customView(R.layout.dialog_add_custom,true)
                        .positiveText("OK")
                        .negativeText("Abbruch")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                TextView dialog_add_custom_what = (TextView) dialog.findViewById(R.id.dialog_add_custom_what);
                                TextView dialog_add_custom_how_much = (TextView) dialog.findViewById(R.id.dialog_add_custom_how_much);
                                if (!dialog_add_custom_what.getText().toString().isEmpty()) {
                                        saveItem(dialog_add_custom_what.getText().toString(), dialog_add_custom_how_much.getText().toString());
                                        if (isFavorite) {
                                            addToFavorites(dialog_add_custom_what.getText().toString());
                                            isFavorite = false;
                                        }
                                }
                            }})
                        .show();
                dialog.findViewById(R.id.dialog_add_custom_what).requestFocus();
                dialog.findViewById(R.id.dialog_add_custom_favorite).setOnClickListener(this);
                break;
            case R.id.main_action_b:
                final List<String> favorites = getFavorites();
                if (favorites.isEmpty()){
                    Toast.makeText(context, "No favorites yet. Add some!", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String[] simpleArray = new String[ favorites.size() ];
                favorites.toArray(simpleArray );
                action_main.collapse();
                dialog = new MaterialDialog.Builder(getActivity())
                        .items(simpleArray)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if(which != -1){
                                    saveItem(simpleArray[which], "1");
                                }
                                return true;
                            }
                        })
                        .positiveText("Ok")
                        .show();
                break;
            case R.id.dialog_add_custom_favorite:
                if (isFavorite){
                    isFavorite = false;
                    dialog.findViewById(R.id.dialog_add_custom_favorite).setBackgroundResource(R.drawable.ic_action_not_important);
                } else {
                    isFavorite = true;
                    dialog.findViewById(R.id.dialog_add_custom_favorite).setBackgroundResource(R.drawable.ic_action_important);
                }
                break;
            case R.id.dialog_update_favorite:
                if (isFavorite){
                  isFavorite = false;
                    dialog.findViewById(R.id.dialog_update_favorite).setBackgroundResource(R.drawable.ic_action_not_important);
                } else {
                    isFavorite = true;
                    dialog.findViewById(R.id.dialog_update_favorite).setBackgroundResource(R.drawable.ic_action_important);
                }
                break;
        }
    }

    private void addToFavorites(String itemTitle) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        List<String> favorites = getFavorites();
        favorites.add(itemTitle);
        JSONArray jsArray = new JSONArray(favorites);
        prefs.edit().putString("favorites", jsArray.toString()).apply();
    }

    private List<String> getFavorites() {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        JSONArray jsonArray;
        List<String> favorites = new ArrayList<>();
        try {
            jsonArray = new JSONArray(prefs.getString("favorites", ""));

            for (int i=0; i<jsonArray.length(); i++) {
                favorites.add( jsonArray.getString(i) );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return favorites;
    }

    @Override
    public void onStop(){
        super.onStop();
        if (api != null){
            api.cancel(true);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (api != null){
            api.cancel(true);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        getList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clearlist:
                clearList();
                return false;
            default:
                break;
        }

        return false;
    }

}
