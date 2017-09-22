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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import java.util.Queue;
import java.util.LinkedList;

public class BTLEService extends Service {

    private final static String TAG = BTLEService.class.getSimpleName();

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_RSSI_AVAILABLE = "com.example.bluetooth.le.ACTION_RSSI_AVAILABLE";

    public final static String EXTRA_DATA_RSSI = "com.example.bluetooth.le.EXTRA_DATA_RSSI";
    public final static String FIRST_FIRST_CHARACTERISTIC_DATA = "com.example.bluetooth.le.FIRST_FIRST_CHARACTERISTIC_DATA";
    public final static String FIRST_SECOND_CHARACTERISTIC_DATA = "com.example.bluetooth.le.FIRST_SECOND_CHARACTERISTIC_DATA";
    public final static String FIRST_THIRD_CHARACTERISTIC_DATA = "com.example.bluetooth.le.FIRST_THIRD_CHARACTERISTIC_DATA";
    public final static String SECOND_FIRST_CHARACTERISTIC_DATA = "com.example.bluetooth.le.SECOND_FIRST_CHARACTERISTIC_DATA";
    public final static String HEART_FREQUENCY_CHARACTERISTIC_DATA = "com.example.bluetooth.le.HEART_FREQUENCY_CHARACTERISTIC_DATA";
    public final static String THIRD_SECOND_CHARACTERISTIC_DATA = "com.example.bluetooth.le.THIRD_SECOND_CHARACTERISTIC_DATA";
    public final static String FOURTH_FIRST_CHARACTERISTIC_DATA = "com.example.bluetooth.le.FOURTH_FIRST_CHARACTERISTIC_DATA";
    public final static String FOURTH_SECOND_CHARACTERISTIC_DATA = "com.example.bluetooth.le.FOURTH_SECOND_CHARACTERISTIC_DATA";
    public final static String BLOOD_PRESSURE_CHARACTERISTIC_DATA = "com.example.bluetooth.le.BLOOD_PRESSURE_CHARACTERISTIC_DATA";
    public final static String FIFTH_SECOND_CHARACTERISTIC_DATA = "com.example.bluetooth.le.FIFTH_SECOND_CHARACTERISTIC_DATA";
    public final static String FIFTH_THIRD_CHARACTERISTIC_DATA = "com.example.bluetooth.le.FIFTH_THIRD_CHARACTERISTIC_DATA";

    public final static UUID FIRST_SERVICE = UUID.fromString(GATTAttributes.FIRST_SERVICE);
    public final static UUID FIRST_FIRST_CHARACTERISTIC = UUID.fromString(GATTAttributes.FIRST_FIRST_CHARACTERISTIC);
    public final static UUID FIRST_SECOND_CHARACTERISTIC = UUID.fromString(GATTAttributes.FIRST_SECOND_CHARACTERISTIC);
    public final static UUID FIRST_THIRD_CHARACTERISTIC = UUID.fromString(GATTAttributes.FIRST_THIRD_CHARACTERISTIC);

    public final static UUID SECOND_SERVICE = UUID.fromString(GATTAttributes.SECOND_SERVICE);
    public final static UUID SECOND_FIRST_CHARACTERISTIC = UUID.fromString(GATTAttributes.SECOND_FIRST_CHARACTERISTIC);

    public final static UUID THIRD_SERVICE = UUID.fromString(GATTAttributes.THIRD_SERVICE);
    public final static UUID HEART_FREQUENCY_CHARACTERISTIC = UUID.fromString(GATTAttributes.HEART_FREQUENCY_CHARACTERISTIC);
    public final static UUID THIRD_SECOND_CHARACTERISTIC = UUID.fromString(GATTAttributes.THIRD_SECOND_CHARACTERISTIC);

    public final static UUID FOURTH_SERVICE = UUID.fromString(GATTAttributes.FOURTH_SERVICE);
    public final static UUID FOURTH_FIRST_CHARACTERISTIC = UUID.fromString(GATTAttributes.FOURTH_FIRST_CHARACTERISTIC);
    public final static UUID FOURTH_SECOND_CHARACTERISTIC = UUID.fromString(GATTAttributes.FOURTH_SECOND_CHARACTERISTIC);

    public final static UUID FIFTH_SERVICE = UUID.fromString(GATTAttributes.FIFTH_SERVICE);
    public final static UUID BLOOD_PRESSURE_CHARACTERISTIC = UUID.fromString(GATTAttributes.BLOOD_PRESSURE_CHARACTERISTIC);
    public final static UUID FIFTH_SECOND_CHARACTERISTIC = UUID.fromString(GATTAttributes.FIFTH_SECOND_CHARACTERISTIC);
    public final static UUID FIFTH_THIRD_CHARACTERISTIC = UUID.fromString(GATTAttributes.FIFTH_THIRD_CHARACTERISTIC);

    private Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();
    // India_Team
    //private Queue<BluetoothGattCharacteristic> characteristicReadQueue = new LinkedList<BluetoothGattCharacteristic>();

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            Log.e("status", "newState : " + newState + " status :" + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e("connection", "Connected.... in BTLEService");
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                //boolean rssiStatus = mBluetoothGatt.readRemoteRssi();
                mBluetoothGatt.discoverServices();
                broadcastUpdate(intentAction);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e("connection", "Disconnected.... in BTLEService");
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("characteristic", "onCharacteristicRead : " + characteristic.getUuid().toString());

            final byte[] data = characteristic.getValue();
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            //Log.e("blood presure val", stringBuilder.toString() + " value length : " + data.length);


            // India_Team
            //characteristicReadQueue.remove();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
            /*if (characteristicReadQueue.size() > 0) {
                Log.e("characteristic", "onCharacteristicRead in if");
                mBluetoothGatt.readCharacteristic(characteristicReadQueue.element());
            } else {
                Log.e("characteristic", "onCharacteristicRead in else");
            }*/
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.e("characteristic", "onCharacteristicChanged");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

            // India_Team
            /*if (characteristicReadQueue.size() > 0) {
                mBluetoothGatt.readCharacteristic(characteristicReadQueue.element());
            }*/
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            descriptorWriteQueue.remove();
            if (descriptorWriteQueue.size() > 0) {
                mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
            }
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_RSSI_AVAILABLE, rssi);
            }
        }
    };

    /*private void writeGattDescriptor(BluetoothGattDescriptor d){
        descriptorWriteQueue.add(d);
        if(descriptorWriteQueue.size() == 1){mBluetoothGatt.writeDescriptor(d);}
    }*/

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, int number) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA_RSSI, String.format(Locale.getDefault(), "rssi: %d", number));
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        if (BLOOD_PRESSURE_CHARACTERISTIC.equals((characteristic.getUuid()))) {
            final byte[] data = characteristic.getValue();
            final StringBuilder stringBuilder = new StringBuilder(data.length);

            Log.e("blood pressurr", stringBuilder.toString());

            //if (stringBuilder.length() >= 2) {
            if ((int) (data[0] & 0x01) == 0) {
                stringBuilder.append(String.format(Locale.getDefault(), "%d ", data[1]));
            } else {
                long newValue = (data[1] << 8);
                newValue = (long) ((int) newValue | (int) data[2]);
                stringBuilder.append(String.format(Locale.getDefault(), "%d ", newValue));
            }
            //}

            intent.putExtra(BLOOD_PRESSURE_CHARACTERISTIC_DATA, stringBuilder.toString());
        } else if (HEART_FREQUENCY_CHARACTERISTIC.equals((characteristic.getUuid()))) {
            final byte[] data = characteristic.getValue();
            final StringBuilder stringBuilder = new StringBuilder(data.length);

            //if (stringBuilder.length() >= 2) {
            if ((int) (data[0] & 0x01) == 0) {
                stringBuilder.append(String.format(Locale.getDefault(), "%d ", data[1]));
            } else {
                long newValue = (data[1] << 8);
                newValue = (long) ((int) newValue | (int) data[2]);
                stringBuilder.append(String.format(Locale.getDefault(), "%d ", newValue));
            }
            //}

            intent.putExtra(HEART_FREQUENCY_CHARACTERISTIC_DATA, stringBuilder.toString());
        }


        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BTLEService getService() {
            return BTLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }

        // India_Team
        /*characteristicReadQueue.add(characteristic);
        if ((characteristicReadQueue.size() == 1)) {
            Log.e("read", "size is 1 on read characteristic");
            mBluetoothGatt.readCharacteristic(characteristic);
        } else {
            Log.e("read", "size is 1 on read characteristic fail");
        }*/

        if (mBluetoothGatt.readCharacteristic(characteristic)) {
            Log.e(TAG, "read characteristic success");
        } else {
            Log.e(TAG, "read characteristic fail");
        }

    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

}
