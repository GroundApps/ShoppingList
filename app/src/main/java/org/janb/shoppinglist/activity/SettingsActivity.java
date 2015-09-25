package org.janb.shoppinglist.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.janb.shoppinglist.CONSTS;
import org.janb.shoppinglist.R;
import org.janb.shoppinglist.api.BackendSettings;
import org.janb.shoppinglist.api.ResponseHelper;
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            Gson gson = new Gson();
            try {
                BackendSettings backendSettings = gson.fromJson(scanResult.getContents().toString(), BackendSettings.class);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("host", backendSettings.getHost());
                editor.putString("authkey", backendSettings.getAuth());
                editor.putBoolean("useSSL", backendSettings.getSsl());
                editor.commit();
                Toast.makeText(getApplicationContext(),"Success!", Toast.LENGTH_SHORT).show();
                refreshValues();
            } catch (Exception e){
                Toast.makeText(getApplicationContext(),"QR Code: Wrong data!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void refreshValues() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

}
