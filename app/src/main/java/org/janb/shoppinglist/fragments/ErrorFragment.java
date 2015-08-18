package org.janb.shoppinglist.fragments;


import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.janb.shoppinglist.R;

public class ErrorFragment extends Fragment implements View.OnClickListener {

    private String errorTitle;
    private String errorDescription;

    public ErrorFragment(String errorTitle, String errorDescription) {
        this.errorTitle = errorTitle;
        this.errorDescription = errorDescription;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_error, container, false);
        TextView errorTV = (TextView)rootView.findViewById(R.id.error_tv_description);
        errorTV.setText(errorDescription);
        Button btnRetry = (Button) rootView.findViewById(R.id.error_btn_retry);
        btnRetry.setOnClickListener(this);
        Button btnCache = (Button) rootView.findViewById(R.id.error_btn_cache);
        btnCache.setOnClickListener(this);
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

        }
    }
}
