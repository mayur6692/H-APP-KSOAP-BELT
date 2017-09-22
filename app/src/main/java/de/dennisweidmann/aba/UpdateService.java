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

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.transport.HttpResponseException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Locale;
import java.util.Vector;

import de.dennisweidmann.aba.Model.APPCredentials;
import de.dennisweidmann.aba.Model.BTLE.BTLEHandler;
import de.dennisweidmann.aba.Model.SOAP.HealthData;
import de.dennisweidmann.aba.Model.SOAP.SensorValue;
import de.dennisweidmann.aba.Model.SQLHandler;
import de.dennisweidmann.aba.Stuff.SharedPreferenceKeys;

public class UpdateService extends IntentService implements BTLEHandler.BTLEHandlerDelegate, SQLHandler.SQLHandlerDelegate {

    private final int SERVICE_RUN_TIME = 1;

    private Intent intent;
    private String lastConnectedDevice;
    private Handler handler;
    private boolean isBluetooth = false;

    public UpdateService() {
        super("LiivieUpdateService");
    }

    public UpdateService(String name) {
        super("LiivieUpdateService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        this.intent = intent;

        if (intent.getAction().equalsIgnoreCase("de.dennisweidmann.aba.action.UPDATE_BLUETOOTH")) {
            isBluetooth = true;
            updateBluetoothData();
            handler = new Handler();
            handler.postDelayed(runnable, 1000 * 60 * SERVICE_RUN_TIME);
        } else if (intent.getAction().equalsIgnoreCase("de.dennisweidmann.aba.action.UPDATE_SERVER")) {
            updateServerData();
        } else {
            completeWakefulIntent();
        }
    }

    private void updateBluetoothData() {
        lastConnectedDevice = APPCredentials.sharedPreferences(this).getString(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString(), null);
        if (lastConnectedDevice == null) {
            completeWakefulIntent();
            return;
        }
        BTLEHandler.sharedInstance().delegate = this;
        BTLEHandler.sharedInstance().initBTLEManagerWithContext(this);
        BTLEHandler.sharedInstance().startBTLEDeviceDiscovery();
        Log.e("service", "updateBluetoothData");
    }

    private void updateServerData() {
        new SQLHandler(this, this).loadVitalDataNotTransmitted("NOT_TRANSMITTED");
        Log.e("service", "updateServerData");
    }

    private void completeWakefulIntent() {
        if (isBluetooth) {
            BTLEHandler.sharedInstance().stopBTLEDeviceDiscovery();
            BTLEHandler.sharedInstance().disconnectBTLEDevice(this);
        }

        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }

        if (intent != null) {
            //UpdateReceiver.completeWakefulIntent(intent);
        }
    }

    @Override
    public void bluetoothDidFindDevices(HashMap<String, BluetoothDevice> foundDevices) {
        if (foundDevices == null || lastConnectedDevice == null) {
            return;
        }
        for (BluetoothDevice foundDevice : foundDevices.values()) {
            if (foundDevice == null) {
                continue;
            }
            String foundDeviceAddress = foundDevice.getAddress();
            if (foundDeviceAddress == null || !foundDeviceAddress.equalsIgnoreCase(lastConnectedDevice)) {
                continue;
            }
            Log.e("service", "device found");
            BTLEHandler.sharedInstance().stopBTLEDeviceDiscovery();
            //BTLEHandler.sharedInstance().connectBTLEDevice(foundDevice, this);
            // India_Team
            BTLEHandler.sharedInstance().connectBTLEDevice(lastConnectedDevice, this);
            break;
        }
    }

    @Override
    public void bluetoothDidChangeConnection(boolean isConnected) {
        Log.e("service", "isConnected " + String.valueOf(isConnected));
        if (!isConnected) {
            completeWakefulIntent();
        }
    }

    @Override
    public void bluetoothDidFinishUpdating() {
        Log.e("service", "finished updating");
        //completeWakefulIntent();
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            completeWakefulIntent();
        }
    };

    @Override
    public void sqlHandlerDidReceiveContent(JSONArray contentArray, String requestTag) {
        if (requestTag == null || contentArray == null || contentArray.length() < 1) {
            completeWakefulIntent();
            return;
        }

        if (requestTag.equalsIgnoreCase("NOT_TRANSMITTED")) {
            JSONArray transmittedArray = new JSONArray();
            for (int i = 0; i < contentArray.length(); i++) {
                try {
                    JSONObject contentObject = contentArray.getJSONObject(i);
                    if (!contentObject.has(SQLHandler.VITAL_DATA_DEVICE_ADDRESS_KEY) || !contentObject.has(SQLHandler.VITAL_DATA_TYPE_KEY) || !contentObject.has(SQLHandler.VITAL_DATA_VALUE_KEY) || !contentObject.has(SQLHandler.VITAL_DATA_TIMESTAMP_KEY)) {
                        continue;
                    }
                    String deviceAddress = contentObject.getString(SQLHandler.VITAL_DATA_DEVICE_ADDRESS_KEY);
                    //String dataType = contentObject.getString(SQLHandler.VITAL_DATA_TYPE_KEY);
                    //TODO: Use real data types instead of static value
                    String dataValue = contentObject.getString(SQLHandler.VITAL_DATA_VALUE_KEY);
                    String dataTimeStamp = contentObject.getString(SQLHandler.VITAL_DATA_TIMESTAMP_KEY);
                    //if (deviceAddress == null || dataType == null || dataValue == null || dataTimeStamp == null) {continue;}
                    if (deviceAddress == null || dataValue == null || dataTimeStamp == null) {
                        continue;
                    }
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    calendar.setTime(simpleDateFormat.parse(dataTimeStamp));

                    ////////////KSOAP2 implementation
                    Vector<SensorValue> vector = new Vector<>();
                    SensorValue sensorValue = new SensorValue();
                    sensorValue.setType(1);
                    sensorValue.setValue(Double.valueOf(dataValue));
                    sensorValue.setMeasured(calendar);

                    vector.add(sensorValue);
                    HealthData healthData = new HealthData();
                    healthData.postValues(deviceAddress, vector);
                    ////////////KSOAP2 implementation end

                    transmittedArray.put(contentObject);
                } catch (KeyManagementException | JSONException | ParseException | IOException | XmlPullParserException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            Log.e("service", transmittedArray.toString());
            new SQLHandler(this, this).setVitalDataTransmitted(transmittedArray);
        } else if (requestTag.equalsIgnoreCase("UPDATE_TRANSMITTED")) {
            Log.e("service", "finished transmitting data");
            completeWakefulIntent();
        }
    }
}
