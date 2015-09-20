package org.janb.shoppinglist.fragments;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.janb.shoppinglist.R;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FavoriteListFragment extends ListFragment implements AdapterView.OnItemLongClickListener {

    private ListView mListView;
    private List<String> favorites;
    private Context context;

    public FavoriteListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.title_favorites));
        context = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
        mListView.setLongClickable(true);
        mListView.setOnItemLongClickListener(this);
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
        favorites.toArray(content);
        ArrayAdapter<String> stringAdapter = new ArrayAdapter<String>(
                context,
                R.layout.row_favorites,
                R.id.text,
                new ArrayList<>(Arrays.asList(content))
        );
        setListAdapter(stringAdapter);
    }

    public void onListItemClick(ListView l, View view, final int position, long id) {
        super.onListItemClick(l, view, position, id);
        Toast.makeText(context, context.getResources().getString(R.string.favorite_delete_notice), Toast.LENGTH_SHORT).show();
}

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        removeFromFavorites(favorites.get(position));
        return true;
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
            setEmptyText(getResources().getString(R.string.empty_view_favorites));
        } else {
            generateList();
        }
    }

    private List<String> getFavorites() {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        JSONArray jsonArray;
        favorites = new ArrayList<>();
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
