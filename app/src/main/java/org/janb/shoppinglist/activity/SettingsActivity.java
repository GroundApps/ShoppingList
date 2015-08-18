package org.janb.shoppinglist.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.janb.shoppinglist.CONSTS;
import org.janb.shoppinglist.R;
import org.janb.shoppinglist.fragments.SettingsFragment;
import org.janb.shoppinglist.fragments.ShoppingListFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        android.app.FragmentManager fragmentManager = getFragmentManager();
                SettingsFragment settingsFR = new SettingsFragment();
                        fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, settingsFR)
                        .commit();
    }

}
