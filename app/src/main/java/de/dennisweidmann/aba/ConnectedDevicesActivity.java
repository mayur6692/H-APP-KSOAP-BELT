package de.dennisweidmann.aba;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.dennisweidmann.aba.Model.APPCredentials;
import de.dennisweidmann.aba.Model.BTLE.BTLEHandler;
import de.dennisweidmann.aba.Stuff.SharedPreferenceKeys;

/**
 * Created by Mitesh Machhoya
 * miteshmachhoya@gmail.com
 * skype : miteshmachhoya
 * Android Application Developer
 */

// India_Team
public class ConnectedDevicesActivity extends AppCompatActivity {

    TextView txtConnected;
    Button btnRemoveDevice;
    String macAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        macAddress = APPCredentials.sharedPreferences(ConnectedDevicesActivity.this).getString(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString(), "");


        btnRemoveDevice = (Button) findViewById(R.id.btnRemoveDevice);
        txtConnected = (TextView) findViewById(R.id.txtConnected);

        txtConnected.setText("You are already connected with : " + macAddress);


        btnRemoveDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTLEHandler.sharedInstance().disconnectBTLEDevice(getApplicationContext());
                APPCredentials.sharedPreferencesEditor(ConnectedDevicesActivity.this).putString(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString(), "").apply();
                startActivity(new Intent(ConnectedDevicesActivity.this, ScanDevicesActivity.class));
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
