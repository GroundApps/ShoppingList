package org.janb.shoppinglist.fragments;


import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
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
import org.janb.shoppinglist.model.PredictionDbAdapterItem;
import org.janb.shoppinglist.service.JobSchedulerService;

import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collections;

import de.duenndns.ssl.MemorizingTrustManager;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private Preference clearPredictions, scanQR, generateQR, updateCheck, manageSelfSignedCertificates;

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
        updateCheck = findPreference("updateCheck");
        updateCheck.setOnPreferenceClickListener(this);
        manageSelfSignedCertificates = findPreference("manageSelfSignedCertificates");
        manageSelfSignedCertificates.setOnPreferenceClickListener(this);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            updateCheck.setEnabled(false);
            updateCheck.setSummary("Sorry. Only available for >= Lollipop.");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onPreferenceClick(Preference preference) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preference == clearPredictions) {
            PredictionDbAdapterItem dbHelper = new PredictionDbAdapterItem(getActivity().getApplicationContext());
            dbHelper.open();
            dbHelper.clearPredictions();
            dbHelper.close();
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_predictions_cleared), Toast.LENGTH_SHORT).show();
            return true;
        }
        if (preference == scanQR){
            IntentIntegrator integrator = new IntentIntegrator(getActivity());
            integrator.initiateScan(prefs.getBoolean("scanQRfront", false) ? 1 : 0);
            return true;
        }
        if (preference == generateQR){

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
            JobScheduler mJobScheduler = (JobScheduler)
                    getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if(prefs.getBoolean("updateCheck", false)){
                JobInfo.Builder builder = new JobInfo.Builder( 1,
                        new ComponentName( getActivity().getPackageName(),
                                JobSchedulerService.class.getName() ) );
                //604800 seconds = 7 days
                builder.setPeriodic(604800);
                builder.setPersisted(true);
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                if( mJobScheduler.schedule( builder.build() ) <= 0 ) {
                    Toast.makeText(getActivity(),"Could not start service.", Toast.LENGTH_SHORT).show();
                }
            } else {
                mJobScheduler.cancelAll();
            }

            return true;
        }

        if (preference == manageSelfSignedCertificates){
            final MemorizingTrustManager mtm = new MemorizingTrustManager(getActivity().getApplicationContext());
            final ArrayList<String> aliases = Collections.list(mtm.getCertificates());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item, aliases);
            new AlertDialog.Builder(getActivity()).setTitle("Tap Certificate to Delete")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                String alias = aliases.get(which);
                                mtm.deleteCertificate(alias);
                                Toast.makeText(getActivity(),"Deleted " + alias, Toast.LENGTH_SHORT).show();
                            } catch (KeyStoreException e) {
                                e.printStackTrace();
                                Toast.makeText(getActivity(),"Error " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .create().show();
        }
        return false;
    }

}

