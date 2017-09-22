package de.dennisweidmann.aba;

/*
The MIT License (MIT)

Copyright (c) 2017 Dennis Weidmann

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

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
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import de.dennisweidmann.aba.Model.APPCredentials;
import de.dennisweidmann.aba.Model.BTLE.BTLEHandler;
import de.dennisweidmann.aba.Stuff.SharedPreferenceKeys;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class DiscoverDevicesActivity extends AppCompatActivity implements View.OnClickListener, BTLEHandler.BTLEHandlerDelegate {

    private final DiscoverDevicesActivity self = this;
    private final int REQUEST_FINE_LOCATION_PERMISSION = 1;
    private final int REQUEST_BLUETOOTH_ENABLED = 2;

    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private HashMap<String, BluetoothDevice> foundDevices = new HashMap<>();
    private String deviceOneAddress;
    private String deviceTwoAddress;
    private String deviceLastConnectedAddress;

    private Button searchButton;
    private Button doneButton;

    private SectionedRecyclerViewAdapter sectionedRecyclerViewAdapter;
    private MySection sectionOne;
    private MySection sectionTwo;

    private TextView txtNoDeviceFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setResult(RESULT_CANCELED);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.discover_devices_list);

        deviceOneAddress = APPCredentials.sharedPreferences(this).getString(SharedPreferenceKeys.DEVICE_ONE_ADDRESS_S.toString(), null);
        deviceTwoAddress = APPCredentials.sharedPreferences(this).getString(SharedPreferenceKeys.DEVICE_TWO_ADDRESS_S.toString(), null);
        deviceLastConnectedAddress = APPCredentials.sharedPreferences(this).getString(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString(), null);

        sectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter();
        sectionOne = new MySection(MySection.SECTION_TYPE_ACTIVE);
        sectionedRecyclerViewAdapter.addSection(sectionOne);
        sectionTwo = new MySection(MySection.SECTION_TYPE_INACTIVE);
        sectionedRecyclerViewAdapter.addSection(sectionTwo);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(sectionedRecyclerViewAdapter);

        searchButton = (Button) findViewById(R.id.discover_devices_search_button);
        searchButton.setOnClickListener(this);
        doneButton = (Button) findViewById(R.id.discover_devices_done_button);
        doneButton.setOnClickListener(this);

        txtNoDeviceFound = (TextView) findViewById(R.id.txtNoDeviceFound);
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
        } else if (v.getTag() != null) {
            JSONObject viewTag = (JSONObject) v.getTag();
            if (viewTag == null || !viewTag.has("type") || !viewTag.has("address")) {
                return;
            }

            try {
                String viewType = viewTag.getString("type");
                final String viewAddress = viewTag.getString("address");

                if (viewType == null || viewAddress == null) {
                    return;
                }

                if (viewType.equalsIgnoreCase("activeItemView")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.discover_devices_connect_device);
                    builder.setPositiveButton(R.string.app_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            APPCredentials.sharedPreferencesEditor(self).putString(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString(), viewAddress).apply();
                            APPCredentials.sharedPreferencesEditor(self).putBoolean(SharedPreferenceKeys.IS_WIZARD_SEEN_B.toString(), true).apply();

                            BTLEHandler.sharedInstance().stopBTLEDeviceDiscovery();
                            //BTLEHandler.sharedInstance().connectBTLEDevice(viewAddress, self);
                            BTLEHandler.sharedInstance().connectBTLEDevice(viewAddress, getApplicationContext());

                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                    builder.setNegativeButton(R.string.app_cancel, null);
                    builder.create().show();
                } else if (viewType.equalsIgnoreCase("deleteActiveItemView")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.discover_devices_deactivate_device);
                    builder.setPositiveButton(R.string.app_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (deviceOneAddress != null && deviceOneAddress.equalsIgnoreCase(viewAddress)) {
                                deviceOneAddress = null;
                                APPCredentials.sharedPreferencesEditor(self).remove(SharedPreferenceKeys.DEVICE_ONE_ADDRESS_S.toString()).apply();
                                updateDevicesList();
                                if (deviceLastConnectedAddress != null && deviceLastConnectedAddress.equalsIgnoreCase(viewAddress)) {
                                    if (deviceTwoAddress != null) {
                                        APPCredentials.sharedPreferencesEditor(self).putString(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString(), deviceTwoAddress).apply();
                                    } else {
                                        APPCredentials.sharedPreferencesEditor(self).remove(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString()).apply();
                                        //deviceLastConnectedAddress = "";
                                    }
                                }
                                //updateDevicesList();
                            } else if (deviceTwoAddress != null && deviceTwoAddress.equalsIgnoreCase(viewAddress)) {
                                deviceTwoAddress = null;
                                APPCredentials.sharedPreferencesEditor(self).remove(SharedPreferenceKeys.DEVICE_TWO_ADDRESS_S.toString()).apply();
                                updateDevicesList();
                                if (deviceLastConnectedAddress != null && deviceLastConnectedAddress.equalsIgnoreCase(viewAddress)) {
                                    if (deviceOneAddress != null) {
                                        APPCredentials.sharedPreferencesEditor(self).putString(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString(), deviceOneAddress).apply();
                                    } else {
                                        APPCredentials.sharedPreferencesEditor(self).remove(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString()).apply();
                                    }
                                }
                                //updateDevicesList();
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.app_cancel, null);
                    builder.create().show();
                } else if (viewType.equalsIgnoreCase("inactiveItemView")) {
                    if (deviceOneAddress == null) {
                        deviceOneAddress = viewAddress;
                        APPCredentials.sharedPreferencesEditor(this).putString(SharedPreferenceKeys.DEVICE_ONE_ADDRESS_S.toString(), viewAddress).apply();
                        updateDevicesList();
                    } else if (deviceTwoAddress == null) {
                        deviceTwoAddress = viewAddress;
                        APPCredentials.sharedPreferencesEditor(this).putString(SharedPreferenceKeys.DEVICE_TWO_ADDRESS_S.toString(), viewAddress).apply();
                        updateDevicesList();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(R.string.discover_devices_both_devices_set);
                        builder.setPositiveButton(R.string.app_ok, null);
                        builder.create().show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                Toast.makeText(self, "start descovery", Toast.LENGTH_SHORT).show();
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
        sectionOne.updateSection(null, true);
        sectionTwo.updateSection(foundDevices, true);
    }

    @Override
    public void bluetoothDidChangeConnection(boolean isConnected) {
    }

    @Override
    public void bluetoothDidFinishUpdating() {
    }

    private class MySection extends StatelessSection {

        static final String SECTION_TYPE_ACTIVE = "SECTION_TYPE_ACTIVE";
        static final String SECTION_TYPE_INACTIVE = "SECTION_TYPE_INACTIVE";

        private String sectionType = null;
        private String[] devicesArray = new String[]{};

        MySection(String sectionType) {
            super(R.layout.section_device, R.layout.row_device);
            this.sectionType = sectionType;
            updateSection(null, true);
        }

        void updateSection(HashMap<String, BluetoothDevice> newDevices, boolean updateAdapter) {
            if (sectionType != null && sectionType.equalsIgnoreCase(SECTION_TYPE_ACTIVE)) {
                if (deviceOneAddress != null && deviceTwoAddress != null) {
                    devicesArray = new String[]{deviceOneAddress, deviceTwoAddress};
                } else if (deviceOneAddress != null) {
                    devicesArray = new String[]{deviceOneAddress};
                } else if (deviceTwoAddress != null) {
                    devicesArray = new String[]{deviceTwoAddress};
                } else {
                    devicesArray = new String[]{};
                }
            } else if (sectionType != null && sectionType.equalsIgnoreCase(SECTION_TYPE_INACTIVE)) {
                devicesArray = new String[]{};
                if (newDevices != null) {
                    for (BluetoothDevice foundDevice : newDevices.values()) {
                        if (foundDevice == null) {
                            continue;
                        }
                        String foundDeviceAddress = foundDevice.getAddress();
                        if (foundDeviceAddress == null) {
                            continue;
                        }
                        if ((deviceOneAddress == null || !foundDeviceAddress.equalsIgnoreCase(deviceOneAddress)) && (deviceTwoAddress == null || !foundDeviceAddress.equalsIgnoreCase(deviceTwoAddress))) {
                            boolean isOldDevice = false;
                            for (String oldDeviceAddress : devicesArray) {
                                if (!isOldDevice && oldDeviceAddress != null) {
                                    if (oldDeviceAddress.equalsIgnoreCase(foundDeviceAddress)) {
                                        isOldDevice = true;
                                    }
                                }
                            }
                            if (!isOldDevice) {
                                ArrayList<String> newDevicesArray = new ArrayList<>(Arrays.asList(devicesArray));
                                newDevicesArray.add(foundDeviceAddress);
                                devicesArray = newDevicesArray.toArray(devicesArray);
                            }
                        }
                    }

                    if (devicesArray.length > 0) {
                        txtNoDeviceFound.setVisibility(View.GONE);
                    } else {
                        txtNoDeviceFound.setVisibility(View.VISIBLE);
                    }

                    Log.e("devices", devicesArray.length + " count");
                }
            }
            if (sectionedRecyclerViewAdapter != null && updateAdapter) {
                sectionedRecyclerViewAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public int getContentItemsTotal() {
            return devicesArray.length;
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new MyHeaderViewHolder(view);
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new MyItemViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            MyHeaderViewHolder headerViewHolder = (MyHeaderViewHolder) holder;
            if (sectionType != null && sectionType.equalsIgnoreCase(SECTION_TYPE_ACTIVE)) {
                headerViewHolder.titleText.setText(getString(R.string.discover_devices_list_header_one));
            } else if (sectionType != null && sectionType.equalsIgnoreCase(SECTION_TYPE_INACTIVE)) {
                headerViewHolder.titleText.setText(getString(R.string.discover_devices_list_header_two));
            }
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyItemViewHolder itemViewHolder = (MyItemViewHolder) holder;

            String bluetoothDeviceAddress = devicesArray[position];
            if (sectionType != null && sectionType.equalsIgnoreCase(SECTION_TYPE_ACTIVE)) {
                if (bluetoothDeviceAddress != null) {
                    itemViewHolder.cardView.setVisibility(View.VISIBLE);
                    JSONObject viewTag = new JSONObject();
                    try {
                        viewTag.put("type", "activeItemView");
                        viewTag.put("address", bluetoothDeviceAddress);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    itemViewHolder.clickLayout.setTag(viewTag);
                    itemViewHolder.clickLayout.setOnClickListener(self);

                    JSONObject buttonTag = new JSONObject();
                    try {
                        buttonTag.put("type", "deleteActiveItemView");
                        buttonTag.put("address", bluetoothDeviceAddress);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    itemViewHolder.deleteButton.setVisibility(View.VISIBLE);
                    itemViewHolder.deleteButton.setTag(buttonTag);
                    itemViewHolder.deleteButton.setOnClickListener(self);

                    //if (BTLEHandler.sharedInstance().isConnectedDevice(bluetoothDeviceAddress)) {
                    if (deviceLastConnectedAddress != null && bluetoothDeviceAddress.equalsIgnoreCase(deviceLastConnectedAddress)) {
                        itemViewHolder.bluetoothImage.setImageDrawable(ContextCompat.getDrawable(self, R.drawable.ic_bluetooth_black_48dp));
                        //itemViewHolder.bluetoothText.setText(getString(R.string.main_connected));
                        itemViewHolder.bluetoothText.setText(bluetoothDeviceAddress);
                    } else {
                        itemViewHolder.bluetoothImage.setImageDrawable(ContextCompat.getDrawable(self, R.drawable.ic_bluetooth_disabled_black_48dp));
                        //itemViewHolder.bluetoothText.setText(getString(R.string.main_disconnected));
                        itemViewHolder.bluetoothText.setText(bluetoothDeviceAddress);
                    }
                } else {
                    itemViewHolder.cardView.setVisibility(View.INVISIBLE);
                    itemViewHolder.clickLayout.setTag(null);
                    itemViewHolder.clickLayout.setOnClickListener(null);
                    itemViewHolder.deleteButton.setVisibility(View.GONE);
                    itemViewHolder.deleteButton.setTag(null);
                    itemViewHolder.deleteButton.setOnClickListener(null);
                }
            } else if (sectionType != null && sectionType.equalsIgnoreCase(SECTION_TYPE_INACTIVE)) {
                if (bluetoothDeviceAddress != null) {
                    itemViewHolder.cardView.setVisibility(View.VISIBLE);
                    JSONObject viewTag = new JSONObject();
                    try {
                        viewTag.put("type", "inactiveItemView");
                        viewTag.put("address", bluetoothDeviceAddress);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    itemViewHolder.clickLayout.setTag(viewTag);
                    itemViewHolder.clickLayout.setOnClickListener(self);
                } else {
                    itemViewHolder.clickLayout.setTag(null);
                    itemViewHolder.clickLayout.setOnClickListener(null);
                }
                itemViewHolder.deleteButton.setVisibility(View.GONE);
                itemViewHolder.deleteButton.setTag(null);
                itemViewHolder.deleteButton.setOnClickListener(null);
            }

            if (position == (devicesArray.length - 1)) {
                itemViewHolder.cardSeparator.setVisibility(View.GONE);
                itemViewHolder.cardShadow.setVisibility(View.VISIBLE);
            } else {
                itemViewHolder.cardSeparator.setVisibility(View.VISIBLE);
                itemViewHolder.cardShadow.setVisibility(View.GONE);
            }
        }
    }

    private class MyHeaderViewHolder extends RecyclerView.ViewHolder {

        TextView titleText;

        MyHeaderViewHolder(View view) {
            super(view);
            titleText = (TextView) view.findViewById(R.id.section_title_text);
        }
    }

    private class MyItemViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        FrameLayout cardSeparator;
        FrameLayout cardShadow;
        FrameLayout circleView;
        FrameLayout clickLayout;
        ImageView bluetoothImage;
        TextView bluetoothText;
        ImageButton deleteButton;

        MyItemViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.row_device_card);
            cardSeparator = (FrameLayout) view.findViewById(R.id.row_device_separator);
            cardShadow = (FrameLayout) view.findViewById(R.id.row_device_shadow);
            circleView = (FrameLayout) view.findViewById(R.id.row_device_circle);
            clickLayout = (FrameLayout) view.findViewById(R.id.row_device_click_layout);
            bluetoothImage = (ImageView) view.findViewById(R.id.row_device_bluetooth_image);
            bluetoothText = (TextView) view.findViewById(R.id.row_device_bluetooth_text);
            deleteButton = (ImageButton) view.findViewById(R.id.row_device_delete_button);
        }
    }
}
