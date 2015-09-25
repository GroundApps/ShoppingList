package org.janb.shoppinglist.fragments;


import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.janb.shoppinglist.R;
import org.janb.shoppinglist.activity.SettingsActivity;

public class ErrorFragment extends Fragment implements View.OnClickListener {

    private String errorDescription;
    private Boolean showButtons, gotoSettings;

    public ErrorFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle b = getArguments();
        this.errorDescription = b.getString("error_code");
        if(((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.title_main));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_error, container, false);
        TextView errorTV = (TextView)rootView.findViewById(R.id.error_tv_description);
        errorTV.setText(errorDescription);
        Button btnRetry = (Button) rootView.findViewById(R.id.error_btn_retry);
        Button btnCache = (Button) rootView.findViewById(R.id.error_btn_cache);
        Button btnSettings = (Button) rootView.findViewById(R.id.error_btn_settings);
        btnRetry.setOnClickListener(this);
        btnCache.setOnClickListener(this);
        btnSettings.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onClick(View view) {
        android.app.FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (view.getId()) {
            case R.id.error_btn_retry:
                ShoppingListFragment listFR = new ShoppingListFragment();
                transaction.replace(R.id.fragment_container, listFR);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.error_btn_cache:
                CacheListFragment cacheFR = new CacheListFragment();
                transaction.replace(R.id.fragment_container, cacheFR, "CACHE_FRAGMENT");
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.error_btn_settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);

        }
    }
}
