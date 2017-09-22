package de.dennisweidmann.aba.Model.BTLE;

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

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.dennisweidmann.aba.Model.SQLHandler;
import de.dennisweidmann.aba.Stuff.ValueTypes;

public class BTLEHandler {
    private static final BTLEHandler instance = new BTLEHandler();

    public static synchronized BTLEHandler sharedInstance() {
        return instance;
    }

    private BTLEHandler() {
    }

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    public BTLEHandlerDelegate delegate;
    private boolean isScanning = false;
    private HashMap<String, BluetoothDevice> devicesHashMap = new HashMap<>();

    private int updateCount = 0;

    // India_Team
    private OnReadCharacteristicListner onReadCharacteristicListner;

    // India_Team
    public interface OnReadCharacteristicListner {
        public void onActionDataAvailable(String charactristic, String value);
        public void onConnectionChange(boolean state);
    }

    //private BluetoothDevice bluetoothDevice;
    private String bluetoothDevice = null;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private ScanSettings settings;
    private List<ScanFilter> filters;

    private BTLEService mBluetoothLeService;
    private boolean mConnected = false;
    private ServiceConnection mServiceConnection;

    private BluetoothGattCharacteristic FIRST_FIRST_CHARACTERISTIC;
    private BluetoothGattCharacteristic FIRST_SECOND_CHARACTERISTIC;
    private BluetoothGattCharacteristic FIRST_THIRD_CHARACTERISTIC;

    private BluetoothGattCharacteristic SECOND_FIRST_CHARACTERISTIC;

    private BluetoothGattCharacteristic HEART_FREQUENCY_CHARACTERISTIC;
    private BluetoothGattCharacteristic THIRD_SECOND_CHARACTERISTIC;

    private BluetoothGattCharacteristic FOURTH_FIRST_CHARACTERISTIC;
    private BluetoothGattCharacteristic FOURTH_SECOND_CHARACTERISTIC;


    private BluetoothGattCharacteristic BLOOD_PRESSURE_CHARACTERISTIC;
    private BluetoothGattCharacteristic FIFTH_SECOND_CHARACTERISTIC;
    private BluetoothGattCharacteristic FIFTH_THIRD_CHARACTERISTIC;

    // India_Team
    public void setOnReadCharacteristicListner(OnReadCharacteristicListner onReadCharacteristicListner) {
        this.onReadCharacteristicListner = onReadCharacteristicListner;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            if (BTLEService.ACTION_GATT_CONNECTED.equals(intent.getAction())) {
                Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show();
                Log.e("connection state", "Connected....");
                mConnected = true;
                if (delegate != null) {
                    delegate.bluetoothDidChangeConnection(true);
                }
                // India_Team
                onReadCharacteristicListner.onConnectionChange(true);
            } else if (BTLEService.ACTION_GATT_DISCONNECTED.equals(intent.getAction())) {
                mConnected = false;
                if (delegate != null) {
                    delegate.bluetoothDidChangeConnection(false);
                }
                // India_Team
                onReadCharacteristicListner.onConnectionChange(false);
            } else if (BTLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(intent.getAction())) {
                attachGattServices(mBluetoothLeService.getSupportedGattServices());
                //updateBTLEValues();
            } else if (BTLEService.ACTION_DATA_AVAILABLE.equals(intent.getAction())) {
                /*SQLHandler sqlHandler = new SQLHandler(context, null);
                if (intent.hasExtra(BTLEService.HEART_FREQUENCY_CHARACTERISTIC_DATA) && bluetoothDevice != null) {
                    String characteristicValue = intent.getStringExtra(BTLEService.HEART_FREQUENCY_CHARACTERISTIC_DATA);
                    //String deviceAddress = bluetoothDevice.getAddress();
                    String deviceAddress = bluetoothDevice;
                    if (characteristicValue != null && deviceAddress != null) {
                        JSONArray valueArray = new JSONArray();
                        JSONObject valueObject = new JSONObject();
                        try {
                            valueObject.put(SQLHandler.VITAL_DATA_DEVICE_ADDRESS_KEY, deviceAddress);
                            valueObject.put(SQLHandler.VITAL_DATA_TYPE_KEY, ValueTypes.HEART_FREQUENCY.toString());
                            valueObject.put(SQLHandler.VITAL_DATA_VALUE_KEY, characteristicValue);
                            valueArray.put(valueObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sqlHandler.saveVitalData(valueArray, BTLEService.HEART_FREQUENCY_CHARACTERISTIC_DATA);
                    }
                    updateCount--;
                } else if (intent.hasExtra(BTLEService.BLOOD_PRESSURE_CHARACTERISTIC_DATA) && bluetoothDevice != null) {
                    String characteristicValue = intent.getStringExtra(BTLEService.BLOOD_PRESSURE_CHARACTERISTIC_DATA);

                    Log.e("blood pure in action", characteristicValue + "");

                    //String deviceAddress = bluetoothDevice.getAddress();
                    String deviceAddress = bluetoothDevice;
                    if (characteristicValue != null && deviceAddress != null) {
                        JSONArray valueArray = new JSONArray();
                        JSONObject valueObject = new JSONObject();
                        try {
                            valueObject.put(SQLHandler.VITAL_DATA_DEVICE_ADDRESS_KEY, deviceAddress);
                            valueObject.put(SQLHandler.VITAL_DATA_TYPE_KEY, ValueTypes.BLOOD_PRESSURE.toString());
                            valueObject.put(SQLHandler.VITAL_DATA_VALUE_KEY, characteristicValue);
                            valueArray.put(valueObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sqlHandler.saveVitalData(valueArray, BTLEService.BLOOD_PRESSURE_CHARACTERISTIC_DATA);
                    }
                    updateCount--;
                }*/

                // India_Team
                String characteristicValue=null;
                String characteristic=null;

                if (intent.hasExtra(BTLEService.HEART_FREQUENCY_CHARACTERISTIC_DATA) && bluetoothDevice != null) {
                    characteristicValue = intent.getStringExtra(BTLEService.HEART_FREQUENCY_CHARACTERISTIC_DATA);
                    characteristic = BTLEService.HEART_FREQUENCY_CHARACTERISTIC_DATA;

                } else if (intent.hasExtra(BTLEService.BLOOD_PRESSURE_CHARACTERISTIC_DATA) && bluetoothDevice != null) {
                    characteristicValue = intent.getStringExtra(BTLEService.BLOOD_PRESSURE_CHARACTERISTIC_DATA);
                    characteristic = BTLEService.BLOOD_PRESSURE_CHARACTERISTIC_DATA;
                }



                /*if (delegate != null && updateCount <= 0) {
                    delegate.bluetoothDidFinishUpdating();
                }*/

                // India_Team
                onReadCharacteristicListner.onActionDataAvailable(characteristic, characteristicValue);
            }
        }
    };

    public interface BTLEHandlerDelegate {
        void bluetoothDidFindDevices(HashMap<String, BluetoothDevice> foundDevices);

        void bluetoothDidChangeConnection(boolean isConnected);

        void bluetoothDidFinishUpdating();
    }

    public void initBTLEManagerWithContext(Context context) {
        if (bluetoothAdapter != null || context == null) {
            return;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    public void startBTLEDeviceDiscovery() {
        //if (bluetoothAdapter == null || isScanning) {
        if (bluetoothAdapter == null ) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 21) {
            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            }
            if (filters == null) {
                filters = new ArrayList<ScanFilter>();
            }
            if (settings == null) {
                settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

            }
            isScanning = true;
            // India_Team
            devicesHashMap.clear();
            bluetoothLeScanner.startScan(filters, settings, getScanCallback());
            Log.e("scan", "start scan");
        } else {
            isScanning = true;
            // India_Team
            devicesHashMap.clear();
            bluetoothAdapter.startLeScan(getLeScanCallback());
            Log.e("scan", "start le scan");
        }
    }

    public void stopBTLEDeviceDiscovery() {
        if (!isScanning) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            if (bluetoothLeScanner != null && scanCallback != null) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        } else {
            if (bluetoothAdapter != null && leScanCallback != null) {
                bluetoothAdapter.stopLeScan(leScanCallback);
            }
        }
        isScanning = false;
    }

    public boolean isConnectedDevice(String testDeviceAddress) {
        if (mConnected && bluetoothDevice != null) {
            if (testDeviceAddress != null) {
                //String bluetoothDeviceAddress = bluetoothDevice.getAddress();
                String bluetoothDeviceAddress = bluetoothDevice;
                if (bluetoothDeviceAddress != null && bluetoothDeviceAddress.equalsIgnoreCase(testDeviceAddress)) {
                    return true;
                }
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback getScanCallback() {
        if (scanCallback == null) {
            if (Build.VERSION.SDK_INT >= 21) {
                scanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        if (result != null) {
                            BluetoothDevice device = result.getDevice();

                            Log.e("scan", "scan result :" + device.getAddress());
                            Log.e("scan", "scan result hashmap size :" + devicesHashMap.size());

                            if (device != null && isValidDevice(device)) {
                                String deviceID = device.getAddress();
                                if (deviceID != null && !devicesHashMap.containsKey(deviceID)) {
                                    devicesHashMap.put(deviceID, device);
                                    if (delegate != null) {
                                        delegate.bluetoothDidFindDevices(devicesHashMap);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        super.onBatchScanResults(results);

                        if (results != null) {
                            for (ScanResult result : results) {
                                if (result != null) {
                                    BluetoothDevice device = result.getDevice();

                                    /*Log.e("scan", "batch scan result :" + device.getAddress());*/

                                    if (device != null && isValidDevice(device)) {
                                        String deviceID = device.getAddress();
                                        if (deviceID != null && !devicesHashMap.containsKey(deviceID)) {
                                            devicesHashMap.put(deviceID, device);
                                            if (delegate != null) {
                                                delegate.bluetoothDidFindDevices(devicesHashMap);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        Log.e("scan", "scan fail");
                        bluetoothLeScanner.stopScan(this);
                        isScanning = false;
                    }
                };
            }
        }
        return scanCallback;
    }

    private BluetoothAdapter.LeScanCallback getLeScanCallback() {
        if (leScanCallback == null) {
            leScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device != null && isValidDevice(device)) {
                        String deviceID = device.getAddress();
                        if (deviceID != null && !devicesHashMap.containsKey(deviceID)) {
                            devicesHashMap.put(deviceID, device);
                            if (delegate != null) {
                                delegate.bluetoothDidFindDevices(devicesHashMap);
                            }
                        }
                    }
                }
            };
        }
        return leScanCallback;
    }

    private boolean isValidDevice(BluetoothDevice foundDevice) {
        if (foundDevice != null) {
            String foundDeviceName = foundDevice.getName();
            if (foundDeviceName != null && (foundDeviceName.equalsIgnoreCase("V07") || foundDeviceName.equalsIgnoreCase("V07S"))) {
                return true;
            }
        }
        return false;
    }

    //public void connectBTLEDevice(BluetoothDevice btleDevice, Context context) {
    public void connectBTLEDevice(String btleDevice, Context context) {
        if (btleDevice == null || context == null) {
            return;
        }
        //String deviceAddress = btleDevice.getAddress();
        // India_Team
        String deviceAddress = btleDevice;
        if (deviceAddress != null) {
            //bluetoothDevice = btleDevice;

            // India_Team
            bluetoothDevice = btleDevice;
            if (mBluetoothLeService != null) {
                mBluetoothLeService.disconnect();
                mBluetoothLeService.close();
                context.unregisterReceiver(mGattUpdateReceiver);
                mServiceConnection = null;
                mBluetoothLeService = null;
            }
            context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            Intent gattServiceIntent = new Intent(context, BTLEService.class);
            context.bindService(gattServiceIntent, getmServiceConnection(), Context.BIND_AUTO_CREATE);
        }
    }

    public void disconnectBTLEDevice(Context context) {
        if (!mConnected || mBluetoothLeService == null || mServiceConnection == null || bluetoothDevice == null) {
            return;
        }
        mBluetoothLeService.disconnect();
        mBluetoothLeService.close();
        context.unregisterReceiver(mGattUpdateReceiver);
        mServiceConnection = null;
        mBluetoothLeService = null;
        bluetoothDevice = null;
        onReadCharacteristicListner.onConnectionChange(false);

    }

    public void updateBTLEValues() {
        if (!mConnected || mBluetoothLeService == null || mServiceConnection == null || bluetoothDevice == null) {
            return;
        }
        updateCount = 2;
        mBluetoothLeService.readCharacteristic(HEART_FREQUENCY_CHARACTERISTIC);
        mBluetoothLeService.readCharacteristic(BLOOD_PRESSURE_CHARACTERISTIC);
    }


    // India_Team
    public void readHeartFrequencyCharacteristic() {
        if (!mConnected || mBluetoothLeService == null || mServiceConnection == null || bluetoothDevice == null) {
            return;
        }
        mBluetoothLeService.readCharacteristic(HEART_FREQUENCY_CHARACTERISTIC);
    }

    // India_Team
    public void readBloodPressureCharacteristic() {
        if (!mConnected || mBluetoothLeService == null || mServiceConnection == null || bluetoothDevice == null) {
            return;
        }
        mBluetoothLeService.readCharacteristic(BLOOD_PRESSURE_CHARACTERISTIC);
    }

    private ServiceConnection getmServiceConnection() {
        if (mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (bluetoothDevice == null) {
                        return;
                    }
                    //String deviceAddress = bluetoothDevice.getAddress();
                    String deviceAddress = bluetoothDevice;
                    if (deviceAddress == null) {
                        return;
                    }
                    mBluetoothLeService = ((BTLEService.LocalBinder) service).getService();
                    mBluetoothLeService.initialize();
                    mBluetoothLeService.connect(deviceAddress);

                    Log.e("TAG", "request for connection");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mBluetoothLeService = null;
                    Log.e("TAG", "service connection fail");
                }
            };
        }
        return mServiceConnection;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BTLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BTLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BTLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BTLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void attachGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        for (BluetoothGattService gattService : gattServices) {
            if (gattService != null) {
                if (gattService.getUuid().equals(UUID.fromString(GATTAttributes.FIRST_SERVICE))) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        if (gattCharacteristic.getUuid().equals(UUID.fromString(GATTAttributes.FIRST_FIRST_CHARACTERISTIC))) {
                            FIRST_FIRST_CHARACTERISTIC = gattCharacteristic;
                        } else if (gattCharacteristic.getUuid().equals(UUID.fromString(GATTAttributes.FIRST_SECOND_CHARACTERISTIC))) {
                            FIRST_SECOND_CHARACTERISTIC = gattCharacteristic;
                        } else if (gattCharacteristic.getUuid().equals(UUID.fromString(GATTAttributes.FIRST_THIRD_CHARACTERISTIC))) {
                            FIRST_THIRD_CHARACTERISTIC = gattCharacteristic;
                        }
                    }
                } else if (gattService.getUuid().equals(UUID.fromString(GATTAttributes.SECOND_SERVICE))) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        if (gattCharacteristic.getUuid().equals(UUID.fromString(GATTAttributes.SECOND_FIRST_CHARACTERISTIC))) {
                            SECOND_FIRST_CHARACTERISTIC = gattCharacteristic;
                        }
                    }
                } else if (gattService.getUuid().equals(UUID.fromString(GATTAttributes.THIRD_SERVICE))) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        if (gattCharacteristic.getUuid().equals(UUID.fromString(GATTAttributes.HEART_FREQUENCY_CHARACTERISTIC))) {
                            HEART_FREQUENCY_CHARACTERISTIC = gattCharacteristic;
                        } else if (gattCharacteristic.getUuid().equals(UUID.fromString(GATTAttributes.THIRD_SECOND_CHARACTERISTIC))) {
                            THIRD_SECOND_CHARACTERISTIC = gattCharacteristic;
                        }
                    }
                } else if (gattService.getUuid().equals(UUID.fromString(GATTAttributes.FOURTH_SERVICE))) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        if (gattCharacteristic.getUuid().equals(UUID.fromString(GATTAttributes.FOURTH_FIRST_CHARACTERISTIC))) {
                            FOURTH_FIRST_CHARACTERISTIC = gattCharacteristic;
                        } else if (gattCharacteristic.getUuid().equals(UUID.fromString(GATTAttributes.FOURTH_SECOND_CHARACTERISTIC))) {
                            FOURTH_SECOND_CHARACTERISTIC = gattCharacteristic;
                        }
                    }
                } else if (gattService.getUuid().equals(UUID.fromString(GATTAttributes.FIFTH_SERVICE))) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        if (gattCharacteristic.getUuid().equals(UUID.fromString(GATTAttributes.BLOOD_PRESSURE_CHARACTERISTIC))) {
                            BLOOD_PRESSURE_CHARACTERISTIC = gattCharacteristic;
                        } else if (gattCharacteristic.getUuid().equals(UUID.fromString(GATTAttributes.FIFTH_SECOND_CHARACTERISTIC))) {
                            FIFTH_SECOND_CHARACTERISTIC = gattCharacteristic;
                        } else if (gattCharacteristic.getUuid().equals(UUID.fromString(GATTAttributes.FIFTH_THIRD_CHARACTERISTIC))) {
                            FIFTH_THIRD_CHARACTERISTIC = gattCharacteristic;
                        }
                    }
                }
            }
        }
    }
}
