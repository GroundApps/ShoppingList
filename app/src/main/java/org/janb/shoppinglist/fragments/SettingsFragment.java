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

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;

import org.janb.shoppinglist.R;
import org.janb.shoppinglist.activity.MainActivity;
import org.janb.shoppinglist.activity.SettingsActivity;
import org.janb.shoppinglist.api.BackendSettings;
import org.janb.shoppinglist.api.BackendUpdateCheck;
import org.janb.shoppinglist.api.GitResultsListener;
import org.janb.shoppinglist.model.PredictionDbAdapter;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, GitResultsListener {
    private Preference clearPredictions, scanQR, generateQR, updateCheck;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        clearPredictions = findPreference("clearPredictions");
        clearPredictions.setOnPreferenceClickListener(this);
        scanQR = findPreference("scanQR");
        scanQR.setOnPreferenceClickListener(this);
        generateQR = findPreference("generateQR");
        generateQR.setOnPreferenceClickListener(this);
        updateCheck = findPreference("updateCheckTEST");
        updateCheck.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == clearPredictions) {
            PredictionDbAdapter dbHelper = new PredictionDbAdapter(getActivity().getApplicationContext());
            dbHelper.open();
            dbHelper.clearPredictions();
            dbHelper.close();
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_predictions_cleared), Toast.LENGTH_SHORT).show();
            return true;
        }
        if (preference == scanQR){
            IntentIntegrator integrator = new IntentIntegrator(getActivity());
            integrator.initiateScan();
            return true;
        }
        if (preference == generateQR){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            BackendSettings backendSettings = new BackendSettings();
            backendSettings.setUrl(prefs.getString("host", ""));
            backendSettings.setApikey(prefs.getString("authkey", ""));
            backendSettings.setSsl(prefs.getBoolean("useSSL", false));
            if(!backendSettings.allSet()){
                Toast.makeText(getActivity(), R.string.toast_qr_missing_values,Toast.LENGTH_SHORT).show();
                Log.d("QR CODE","Missing host or authkey");
                return true;
            }
            Gson gson = new Gson();
            IntentIntegrator integrator = new IntentIntegrator(getActivity());
            integrator.addExtra("ENCODE_SHOW_CONTENTS", false);
            integrator.addExtra("PROMPT_MESSAGE", "ShoppingList - Quick Setup");
            integrator.shareText(gson.toJson(backendSettings, BackendSettings.class));
            return true;
        }
        if (preference == updateCheck){
            BackendUpdateCheck check = new BackendUpdateCheck(getActivity());
            check.setOnResultsListener(this);
            check.execute();
            return true;
        }
        return false;
    }

    @Override
    public void onResponse(final String response) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onError(final String error) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

