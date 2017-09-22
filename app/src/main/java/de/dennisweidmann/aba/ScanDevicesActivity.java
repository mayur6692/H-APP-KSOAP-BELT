package de.dennisweidmann.aba;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.dennisweidmann.aba.Model.APPCredentials;
import de.dennisweidmann.aba.Model.BTLE.BTLEHandler;
import de.dennisweidmann.aba.Stuff.SharedPreferenceKeys;
import de.dennisweidmann.aba.adapter.DeviceScanAdapter;


/**
 * Created by Mitesh Machhoya
 * miteshmachhoya@gmail.com
 * skype : miteshmachhoya
 * Android Application Developer
 */

// India_Team
// This is whole new screen design and implemented by me for scan devices
public class ScanDevicesActivity extends AppCompatActivity implements View.OnClickListener, BTLEHandler.BTLEHandlerDelegate {

    private final int REQUEST_FINE_LOCATION_PERMISSION = 1;
    private final int REQUEST_BLUETOOTH_ENABLED = 2;
    private RecyclerView recyclerView;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private HashMap<String, BluetoothDevice> foundDevices;
    private Button searchButton;
    private Button doneButton;
    private TextView txtNoDeviceFound;

    private DeviceScanAdapter deviceScanAdapter;
    List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        foundDevices = new HashMap<String, BluetoothDevice>();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        deviceScanAdapter = new DeviceScanAdapter();
        deviceScanAdapter.setDeviceFound(devices);
        recyclerView = (RecyclerView) findViewById(R.id.discover_devices_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(deviceScanAdapter);

        searchButton = (Button) findViewById(R.id.discover_devices_search_button);
        searchButton.setOnClickListener(this);
        doneButton = (Button) findViewById(R.id.discover_devices_done_button);
        doneButton.setOnClickListener(this);

        txtNoDeviceFound = (TextView) findViewById(R.id.txtNoDeviceFound);


        deviceScanAdapter.setOnItemClickListner(new DeviceScanAdapter.OnItemClickListner() {
            @Override
            public void itemClickListner(final View view, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanDevicesActivity.this);
                builder.setMessage(R.string.discover_devices_connect_device);
                builder.setPositiveButton(R.string.app_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        APPCredentials.sharedPreferencesEditor(ScanDevicesActivity.this).putString(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString(), view.getTag().toString()).apply();
                        APPCredentials.sharedPreferencesEditor(ScanDevicesActivity.this).putBoolean(SharedPreferenceKeys.IS_WIZARD_SEEN_B.toString(), true).apply();

                        BTLEHandler.sharedInstance().stopBTLEDeviceDiscovery();
                        BTLEHandler.sharedInstance().connectBTLEDevice(view.getTag().toString(), getApplicationContext());

                        setResult(RESULT_OK);
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.app_cancel, null);
                builder.create().show();
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        initBTLE();
    }

    @Override
    protected void onPause() {
        BTLEHandler.sharedInstance().delegate = null;
        BTLEHandler.sharedInstance().stopBTLEDeviceDiscovery();
        super.onPause();
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

    @Override
    public void onClick(View v) {
        if (v == searchButton) {
            initBTLE();
        } else if (v == doneButton) {
            APPCredentials.sharedPreferencesEditor(this).putBoolean(SharedPreferenceKeys.IS_WIZARD_SEEN_B.toString(), true).apply();
            setResult(RESULT_OK);
            finish();
        }
    }

    private void initBTLE() {
        if (mBluetoothAdapter == null) {
            return;
        }

        boolean isLocationGranted = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
            } else {
                isLocationGranted = true;
            }
        } else {
            isLocationGranted = true;
        }

        if (isLocationGranted) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLED);
            } else {
                BTLEHandler.sharedInstance().initBTLEManagerWithContext(getApplicationContext());
                BTLEHandler.sharedInstance().delegate = this;
                BTLEHandler.sharedInstance().startBTLEDeviceDiscovery();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_FINE_LOCATION_PERMISSION:
                initBTLE();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_BLUETOOTH_ENABLED:
                initBTLE();
                break;
        }
    }

    @Override
    public void bluetoothDidFindDevices(HashMap<String, BluetoothDevice> foundDevices) {
        if (foundDevices == null) {
            return;
        }
        this.foundDevices = foundDevices;
        updateDevicesList();
    }

    private void updateDevicesList() {
        Collection<BluetoothDevice> collection = foundDevices.values();
        devices = new ArrayList<BluetoothDevice>(collection);
        deviceScanAdapter.setDeviceFound(devices);

        if (deviceScanAdapter.getItemCount() > 0) {
            txtNoDeviceFound.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void bluetoothDidChangeConnection(boolean isConnected) {
    }

    @Override
    public void bluetoothDidFinishUpdating() {
    }

}
