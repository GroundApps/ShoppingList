package org.janb.shoppinglist.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import org.janb.shoppinglist.R;
import org.janb.shoppinglist.activity.MainActivity;
import org.janb.shoppinglist.activity.SettingsActivity;
import org.janb.shoppinglist.model.PredictionDbAdapter;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private Preference clearPredictions, scanQR;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        clearPredictions = findPreference("clearPredictions");
        clearPredictions.setOnPreferenceClickListener(this);
        //scanQR = findPreference("scanQR");
        //scanQR.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == clearPredictions) {
            PredictionDbAdapter dbHelper = new PredictionDbAdapter(getActivity().getApplicationContext());
            dbHelper.open();
            dbHelper.clearPredictions();
            dbHelper.close();
            Toast.makeText(getActivity().getApplicationContext(), "Predictions cleared", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (preference == scanQR){
            IntentIntegrator integrator = new IntentIntegrator(getActivity());
            integrator.initiateScan();
            return true;
        }
        return false;
    }
}

