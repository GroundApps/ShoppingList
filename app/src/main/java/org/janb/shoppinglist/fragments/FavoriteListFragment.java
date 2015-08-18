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
import android.widget.ArrayAdapter;
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
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class FavoriteListFragment extends ListFragment {

    private ListView mListView;
    private List<String> favorites;
    private Context context;
    SwipeActionAdapter mAdapter;

    public FavoriteListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Favorites");
        context = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
        return rootView;
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public void generateList() {
        final String[] content = new String[ favorites.size() ];
        favorites.toArray(content );
        ArrayAdapter<String> stringAdapter = new ArrayAdapter<String>(
                context,
                R.layout.row_favorites,
                R.id.text,
                new ArrayList<String>(Arrays.asList(content))
        );
        mAdapter = new SwipeActionAdapter(stringAdapter);
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
                return direction == SwipeDirections.DIRECTION_NORMAL_LEFT + SwipeDirections.DIRECTION_FAR_LEFT;
            }

            @Override
            public void onSwipe(int[] positionList, int[] directionList) {
                for (int i = 0; i < positionList.length; i++) {
                    int direction = directionList[i];
                    int position = positionList[i];

                    switch (direction) {
                        case SwipeDirections.DIRECTION_NORMAL_LEFT:
                            removeFromFavorites(favorites.get(position));
                            break;
                        case SwipeDirections.DIRECTION_FAR_LEFT:
                            removeFromFavorites(favorites.get(position));
                            break;
                    }
                }
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    private void removeFromFavorites(String itemName) {
        favorites.remove(itemName);
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        JSONArray jsArray = new JSONArray(favorites);
        prefs.edit().putString("favorites", jsArray.toString()).apply();
        favorites = getFavorites();
        generateList();
    }


    @Override
    public void onStart(){
        super.onStart();
        favorites = getFavorites();
        if(favorites.isEmpty()){
            setEmptyText("You have no favorites yet. Add some!");
        } else {
            generateList();
        }
    }

    private List<String> getFavorites() {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        JSONArray jsonArray = null;
        favorites = new ArrayList<String>();
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

}
