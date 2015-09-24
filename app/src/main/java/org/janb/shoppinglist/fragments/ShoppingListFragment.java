package org.janb.shoppinglist.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.gson.Gson;

import org.janb.shoppinglist.CONSTS;
import org.janb.shoppinglist.R;
import org.janb.shoppinglist.api.ListAPI;
import org.janb.shoppinglist.api.ResponseHelper;
import org.janb.shoppinglist.api.ResultsListener;
import org.janb.shoppinglist.model.PredictionDbAdapter;
import org.janb.shoppinglist.model.ShoppingListAdapter;
import org.janb.shoppinglist.model.ShoppingListItem;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class ShoppingListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener, ResultsListener, View.OnClickListener {

    ShoppingListAdapter mAdapter;
    private ListView mListView;
    private List<ShoppingListItem> ShoppingListItemList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListAPI api;
    private Context context;
    private FloatingActionsMenu action_main;
    private MaterialDialog dialog;
    private Boolean isImportant = false;
    private Boolean isFavorite = false;
    private ShoppingListFragment ref;
    private ShoppingListItem openedItem;
    private EditText dialogCount;
    private PredictionDbAdapter dbHelper;
    private AutoCompleteTextView dialog_add_custom_what;

    public ShoppingListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        setHasOptionsMenu(true);
        ref = this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.title_main));
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setLongClickable(true);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                final ShoppingListItem item = ShoppingListItemList.get(position);
                openedItem = item;
                dialog = new MaterialDialog.Builder(getActivity())
                        .customView(R.layout.dialog_update_item, true)
                        .positiveText(getResources().getString(R.string.ok))
                        .negativeText(getResources().getString(R.string.cancel))
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                String dialogCountText = ((EditText) dialog.findViewById(R.id.dialog_update_count)).getText().toString();
                                if (!dialogCountText.isEmpty() && !dialogCountText.equals(openedItem.getItemCountString())) {
                                    saveItem(item.getItemTitle(), dialogCountText);
                                }
                            }
                        })
                        .show();
                dialogCount = (EditText) dialog.findViewById(R.id.dialog_update_count);
                Button minusCount = (Button) dialog.findViewById(R.id.dialog_update_minus);
                minusCount.setOnClickListener(ref);
                Button plusCount = (Button) dialog.findViewById(R.id.dialog_update_plus);
                plusCount.setOnClickListener(ref);
                dialogCount.setText(String.valueOf(item.getItemCount()));
                TextView dialogTitle = (TextView)dialog.findViewById(R.id.dialog_update_title);
                dialogTitle.setText(item.getItemTitle());
                //dialog.findViewById(R.id.dialog_update_important).setOnClickListener(ref);
                TextView fav = (TextView)dialog.findViewById(R.id.dialog_update_favorite);
                fav.setOnClickListener(ref);
                if(getFavorites().contains(item.getItemTitle())){
                    isFavorite = true;
                    fav.setShadowLayer(15f, 0, 0, getResources().getColor(R.color.material_deep_teal_500));
                }
                /*if(item.isImportant){
                    dialog.findViewById(R.id.dialog_update_important).setBackgroundResource(R.drawable.ic_action_important);
                }
                */
                return true;
            }
        });
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
        action_main.setAlpha(0.7f);
        action_a.setAlpha(0.7f);
        action_b.setAlpha(0.7f);
        getList();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
    }

    private void saveItem(String itemTitle, String itemCount) {
        setRefreshing();
        api = new ListAPI(context);
        api.setOnResultsListener(this);
        ListAPI.setFunction(ListAPI.FUNCTION_SAVEITEM);
        api.execute(itemTitle, String.valueOf(itemCount));
    }
    private void saveMultiple(String jsonData) {
        setRefreshing();
        api = new ListAPI(context);
        api.setOnResultsListener(this);
        ListAPI.setFunction(ListAPI.FUNCTION_SAVE_MULTIPLE);
        api.execute(jsonData);
    }

    private void deleteMultiple(String jsonData) {
        setRefreshing();
        api = new ListAPI(context);
        api.setOnResultsListener(this);
        ListAPI.setFunction(ListAPI.FUNCTION_DELETE_MULTIPLE);
        api.execute(jsonData);
    }

    private void clearList() {
        setRefreshing();
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
    public void onListItemClick(ListView l, View view, final int position, long id) {
        super.onListItemClick(l, view, position, id);
        ShoppingListItem clickedItem = ShoppingListItemList.get(position);
        clickedItem.toggleChecked();
        int index = mListView.getFirstVisiblePosition();
        View v = mListView.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - mListView.getPaddingTop());
        mAdapter = new ShoppingListAdapter(getActivity(), ShoppingListItemList);
        setListAdapter(mAdapter);
        mListView.setSelectionFromTop(index, top);
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

    public void onResponse(final ResponseHelper response) {
        resetRefreshing();
        Log.d("API CODE", String.valueOf(response.getType()));
        if (response.getType() >= 5000 && response.getType() < 6000){
            onError(response);
            return;
        }

        if (response.getType() >= 6000){
            showAPIError(response.getContent());
            return;
        }

        switch(response.getType()){
            case CONSTS.API_SUCCESS_LIST:
                ShoppingListItemList = response.getItems();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buildList();
                    }
                });
                break;
            case CONSTS.API_SUCCESS_LIST_EMPTY:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ( ShoppingListItemList != null && mAdapter != null) {
                            ShoppingListItemList.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                        setEmptyText(getResources().getString(R.string.empty_view_list));
                    }
                });
                break;

            case CONSTS.API_SUCCESS_SAVE:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), response.getContent(),Toast.LENGTH_SHORT).show();
                        getList();
                    }
                });
                break;

            case CONSTS.API_SUCCESS_DELETE:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), response.getContent(),Toast.LENGTH_SHORT).show();
                        getList();
                    }
                });
                break;
            case CONSTS.API_SUCCESS_CLEAR:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), response.getContent(),Toast.LENGTH_SHORT).show();
                        getList();
                    }
                });
                break;
            case CONSTS.API_SUCCESS_UPDATE:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), response.getContent(),Toast.LENGTH_SHORT).show();
                        getList();
                    }
                });
                break;
            case CONSTS.API_SUCCESS_IMPORTANT:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), response.getContent(),Toast.LENGTH_SHORT).show();
                        getList();
                    }
                });
                break;
        }


    }

    private void showAPIError(final String content) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void buildList(){
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(ShoppingListItemList);
        prefs.edit().putString("cached_list", json).apply();

        mAdapter = new ShoppingListAdapter(getActivity(), ShoppingListItemList);
        setListAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        if(mAdapter.isEmpty()){
            setEmptyText(getResources().getString(R.string.empty_view_list));
        }
    }

    //Errors thrown by app and api
    @Override
    public void onError(ResponseHelper error) {
        if(error.getType() == CONSTS.APP_ERROR_IO) {
            getList();
            return;
        }

        resetRefreshing();
        ErrorFragment errFR;
        Bundle args = new Bundle();
        args.putString("error_code", error.getContent());
        errFR = new ErrorFragment();
        errFR.setArguments(args);
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

    private void setRefreshing() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.main_action_a:
                final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                action_main.collapse();
                dbHelper = new PredictionDbAdapter(context);
                dbHelper.open();

                dialog = new MaterialDialog.Builder(getActivity())
                        .customView(R.layout.dialog_add_custom, true)
                        .positiveText(getResources().getString(R.string.ok))
                        .negativeText(getResources().getString(R.string.cancel))
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        TextView dialog_add_custom_how_much = (TextView) dialog.findViewById(R.id.dialog_add_custom_how_much);
                                        imm.hideSoftInputFromWindow(dialog_add_custom_what.getWindowToken(), 0);
                                        if (!dialog_add_custom_what.getText().toString().isEmpty()) {
                                            saveItem(dialog_add_custom_what.getText().toString(), dialog_add_custom_how_much.getText().toString());
                                            dbHelper.addPrediction(dialog_add_custom_what.getText().toString());
                                            if (isFavorite) {
                                                addToFavorites(dialog_add_custom_what.getText().toString());
                                                isFavorite = false;
                                            }
                                        }
                                        dbHelper.close();
                                    }
                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        imm.hideSoftInputFromWindow(dialog_add_custom_what.getWindowToken(), 0);
                                        dbHelper.close();
                                    }
                                })
                                .show();

                PredictionAdapter adapter = new PredictionAdapter(dbHelper);
                dialog_add_custom_what = (AutoCompleteTextView) dialog.findViewById(R.id.dialog_add_custom_what);
                dialog_add_custom_what.setAdapter(adapter);
                dialog_add_custom_what.setOnItemClickListener(adapter);
                dialog_add_custom_what.setFocusable(true);
                dialog_add_custom_what.requestFocus();
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                dialog.findViewById(R.id.dialog_add_custom_favorite).setOnClickListener(this);
                dialog.findViewById(R.id.dialog_add_custom_important).setOnClickListener(this);
                break;
            case R.id.main_action_b:
                final List<String> favorites = getFavorites();
                if (favorites.isEmpty()){
                    Toast.makeText(context, getResources().getString(R.string.toast_no_favorites), Toast.LENGTH_SHORT).show();
                    return;
                }
                final String[] simpleArray = new String[ favorites.size() ];
                favorites.toArray(simpleArray);
                action_main.collapse();
                dialog = new MaterialDialog.Builder(getActivity())
                        .items(simpleArray)
                        .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {

                            @Override
                            public boolean onSelection(MaterialDialog materialDialog, Integer[] selectedItems, CharSequence[] charSequences) {
                                if(selectedItems.length > 0) {
                                    List<ShoppingListItem> itemList = new ArrayList<>();
                                    int i = 0;
                                    for (int selectedItem : selectedItems) {
                                        itemList.add(i++, new ShoppingListItem(favorites.get(selectedItem), 1));
                                    }
                                    Gson gson = new Gson();
                                    saveMultiple(gson.toJson(itemList));
                                }
                                return true;
                            }
                        })
                        .positiveText(getResources().getString(R.string.ok))
                        .show();
                break;
            case R.id.dialog_add_custom_important:
                if (isImportant){
                    isImportant = false;
                    dialog.findViewById(R.id.dialog_add_custom_important).setBackgroundResource(R.drawable.ic_action_not_important);
                } else {
                    isImportant = true;
                    dialog.findViewById(R.id.dialog_add_custom_important).setBackgroundResource(R.drawable.ic_action_important);
                }
                break;
            case R.id.dialog_add_custom_favorite:
                TextView favoriteAdd = (TextView)dialog.findViewById(R.id.dialog_add_custom_favorite);
                if (isFavorite){
                    isFavorite = false;
                    favoriteAdd.setShadowLayer(0, 0, 0, 0);
                } else {
                    isFavorite = true;
                    favoriteAdd.setShadowLayer(15f, 0, 0, getResources().getColor(R.color.material_deep_teal_500));
                }
                break;
            case R.id.dialog_update_important:
                openedItem.toggleImportant();
                if (isImportant){
                    isImportant = false;
                    dialog.findViewById(R.id.dialog_update_important).setBackgroundResource(R.drawable.ic_action_not_important);
                } else {
                    isImportant = true;
                    dialog.findViewById(R.id.dialog_update_important).setBackgroundResource(R.drawable.ic_action_important);
                }

                break;
            case R.id.dialog_update_favorite:
                TextView favoriteUpdate = (TextView)dialog.findViewById(R.id.dialog_update_favorite);
                if (isFavorite){
                    isFavorite = false;
                    favoriteUpdate.setShadowLayer(0, 0, 0, 0);
                    removeFromFavorites(openedItem.getItemTitle());
                } else {
                    isFavorite = true;
                    favoriteUpdate.setShadowLayer(15f, 0, 0, getResources().getColor(R.color.material_deep_teal_500));
                    addToFavorites(openedItem.getItemTitle());
                }
                break;
            case R.id.dialog_update_minus:
                if(!dialogCount.getText().toString().equals("1")) {
                    dialogCount = (EditText) dialog.findViewById(R.id.dialog_update_count);
                    dialogCount.setText(String.valueOf(Integer.valueOf(dialogCount.getText().toString()) - 1));
                }
                break;
            case R.id.dialog_update_plus:
                dialogCount = (EditText) dialog.findViewById(R.id.dialog_update_count);
                dialogCount.setText(String.valueOf(Integer.valueOf(dialogCount.getText().toString())+1));
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

    private void removeFromFavorites(String itemTitle) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        List<String> favorites = getFavorites();
        favorites.remove(itemTitle);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        List<ShoppingListItem> delItems = new ArrayList();
        switch (item.getItemId()) {
            case R.id.action_clearlist:
                int i = 0;
                for(ShoppingListItem SLitem:ShoppingListItemList){
                    if(SLitem.isChecked()){
                        delItems.add(i++ , new ShoppingListItem(SLitem.getItemTitle(), 1));
                    }
                }
                if(delItems.isEmpty()){
                    dialog = new MaterialDialog.Builder(getActivity())
                            .content(R.string.dialog_clear_confirm)
                            .positiveText(getResources().getString(R.string.ok))
                            .negativeText(getResources().getString(R.string.cancel))
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    clearList();
                                }
                            })
                            .show();
                } else {
                    Gson gson = new Gson();
                    deleteMultiple(gson.toJson(delItems));
                }
                return true;
            case R.id.action_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                StringBuilder sb = new StringBuilder();
                for(ShoppingListItem listItem:ShoppingListItemList){
                    if(listItem.isChecked()){
                        sb.append("[ X ]\t");
                    } else {
                        sb.append("[   ]\t");
                    }
                    sb.append(listItem.getItemTitle());
                    sb.append("\t\t");
                    sb.append(listItem.getItemCount());
                    sb.append("\n");
                }
                sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            default:
                break;
        }

        return true;
    }

    class PredictionAdapter extends CursorAdapter
            implements AdapterView.OnItemClickListener {

        private PredictionDbAdapter mDbHelper;

        public PredictionAdapter(PredictionDbAdapter dbHelper) {
            super(context, null, true);
            mDbHelper = dbHelper;
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (getFilterQueryProvider() != null) {
                return getFilterQueryProvider().runQuery(constraint);
            }

            return mDbHelper.getPrediction(
                    (constraint != null ? constraint.toString() : "@@@@"));
        }

        public void bindView(View view, Context context, Cursor cursor) {
            final int itemColumnIndex = cursor.getColumnIndexOrThrow("itemTitle");
            TextView predictionText = (TextView) view.findViewById(R.id.text);
            predictionText.setText(cursor.getString(itemColumnIndex));
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            final View view = inflater.inflate(R.layout.row_favorites,parent, false);
            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
            Cursor cursor = (Cursor) listView.getItemAtPosition(position);
            String itemTitle = cursor.getString(cursor.getColumnIndexOrThrow("itemTitle"));
            dialog_add_custom_what.setText(itemTitle);
            dialog_add_custom_what.setSelection(itemTitle.length());
        }
    }

}
