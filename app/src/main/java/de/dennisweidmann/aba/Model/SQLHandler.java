package de.dennisweidmann.aba.Model;

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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.dennisweidmann.aba.Stuff.SQLHandlerTables;
import de.dennisweidmann.aba.Stuff.ValueTypes;

public class SQLHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String VITAL_DATA_TABLE_KEY = "VitalData";
    public static final String VITAL_DATA_UID_KEY = "uid";
    public static final String VITAL_DATA_DEVICE_ADDRESS_KEY = "device_address";
    public static final String VITAL_DATA_TYPE_KEY = "type";
    public static final String VITAL_DATA_VALUE_KEY = "value";
    public static final String VITAL_DATA_TIMESTAMP_KEY = "timestamp";
    public static final String VITAL_DATA_IS_TRANSMITTED_KEY = "is_transmitted";

    private Context context;
    private SQLHandlerDelegate sqlHandlerDelegate;

    public interface SQLHandlerDelegate {
        void sqlHandlerDidReceiveContent(JSONArray contentArray, String requestTag);
    }

    public SQLHandler(Context context, SQLHandlerDelegate sqlHandlerDelegate) {
        super(context, "armband.db", null, DATABASE_VERSION);
        this.context = context;
        this.sqlHandlerDelegate = sqlHandlerDelegate;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String[] createStrings = createDBStrings();
        for (String createString : createStrings) {
            sqLiteDatabase.execSQL(createString);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    public void saveVitalData(JSONArray vitalData, String requestTag) {
        if (vitalData == null || vitalData.length() == 0) {
            return;
        }
        new SQLAsyncTask(SQLHandlerTables.VITAL_DATA, new String[]{VITAL_DATA_UID_KEY, VITAL_DATA_DEVICE_ADDRESS_KEY, VITAL_DATA_TYPE_KEY, VITAL_DATA_VALUE_KEY}, null, null, null, null, requestTag).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, vitalData);
    }

    public void loadVitalDataOfType(ValueTypes valueType, String requestTag) {
        if (valueType == null) {
            return;
        }
        new SQLAsyncTask(SQLHandlerTables.VITAL_DATA, new String[]{VITAL_DATA_UID_KEY, VITAL_DATA_DEVICE_ADDRESS_KEY, VITAL_DATA_TYPE_KEY, VITAL_DATA_VALUE_KEY, VITAL_DATA_TIMESTAMP_KEY}, VITAL_DATA_TYPE_KEY + " = ?", new String[]{valueType.toString()}, VITAL_DATA_TIMESTAMP_KEY + " DESC", "1", requestTag).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public void loadVitalDataNotTransmitted(String requestTag) {
        new SQLAsyncTask(SQLHandlerTables.VITAL_DATA, new String[]{VITAL_DATA_UID_KEY, VITAL_DATA_DEVICE_ADDRESS_KEY, VITAL_DATA_TYPE_KEY, VITAL_DATA_VALUE_KEY, VITAL_DATA_TIMESTAMP_KEY}, VITAL_DATA_IS_TRANSMITTED_KEY + " = ?", new String[]{"0"}, null, null, requestTag).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public void setVitalDataTransmitted(JSONArray vitalData) {
        if (vitalData == null || vitalData.length() == 0) {
            return;
        }
        new SQLAsyncTask(SQLHandlerTables.VITAL_DATA, new String[]{VITAL_DATA_UID_KEY, VITAL_DATA_IS_TRANSMITTED_KEY}, null, null, null, null, "UPDATE_TRANSMITTED").executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, vitalData);
    }

    private String[] createDBStrings() {
        String vitalDataTable = "CREATE TABLE IF NOT EXISTS " + VITAL_DATA_TABLE_KEY + " (" +
                VITAL_DATA_UID_KEY + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                VITAL_DATA_DEVICE_ADDRESS_KEY + " TEXT NOT NULL," +
                VITAL_DATA_TYPE_KEY + " TEXT NOT NULL," +
                VITAL_DATA_VALUE_KEY + " TEXT NOT NULL," +
                VITAL_DATA_TIMESTAMP_KEY + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                VITAL_DATA_IS_TRANSMITTED_KEY + " INTEGER NOT NULL DEFAULT 0" +
                ");";

        String vitalDataDeviceAddressIndex = "CREATE INDEX IF NOT EXISTS vital_data_device_address ON " + VITAL_DATA_TABLE_KEY + " (" + VITAL_DATA_DEVICE_ADDRESS_KEY + ");";
        String vitalDataTypeIndex = "CREATE INDEX IF NOT EXISTS vital_data_type ON " + VITAL_DATA_TABLE_KEY + " (" + VITAL_DATA_TYPE_KEY + ");";
        String vitalDataTimestampIndex = "CREATE INDEX IF NOT EXISTS vital_data_timestamp ON " + VITAL_DATA_TABLE_KEY + " (" + VITAL_DATA_TIMESTAMP_KEY + ");";
        String vitalDataIsTransmittedIndex = "CREATE INDEX IF NOT EXISTS vital_data_is_transmitted ON " + VITAL_DATA_TABLE_KEY + " (" + VITAL_DATA_IS_TRANSMITTED_KEY + ");";

        return new String[]{vitalDataTable, vitalDataDeviceAddressIndex, vitalDataTypeIndex, vitalDataTimestampIndex, vitalDataIsTransmittedIndex};
    }

    private class SQLAsyncTask extends AsyncTask<JSONArray, Float, JSONArray> {

        private String contentTable = null;
        private String[] contentProjection = null;
        private String contentWhere = null;
        private String[] contentWhereValues = null;
        private String contentOrder = null;
        private String contentLimit = null;
        private String requestTag = null;

        public SQLAsyncTask(SQLHandlerTables contentTable, String[] contentProjection, String contentWhere, String[] contentWhereValues, String contentOrder, String contentLimit, String requestTag) {
            if (contentTable == null) {
                return;
            }
            this.contentTable = getContentTableByID(contentTable);
            this.contentProjection = contentProjection;
            this.contentWhere = contentWhere;
            this.contentWhereValues = contentWhereValues;
            this.contentOrder = contentOrder;
            this.contentLimit = contentLimit;
            this.requestTag = requestTag;
        }

        @Override
        protected JSONArray doInBackground(JSONArray... params) {
            if (params == null || params.length < 1) {
                return loadDataFromDB();
            } else if (params.length > 0) {
                saveDataToDB(params);
            }
            return null;
        }

        private JSONArray loadDataFromDB() {
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();
            Cursor cursor = sqLiteDatabase.query(contentTable, contentProjection, contentWhere, contentWhereValues, null, null, contentOrder, contentLimit);

            JSONArray responseArray = new JSONArray();
            if (cursor.moveToFirst()) {
                do {
                    try {
                        JSONObject readObject = new JSONObject();
                        if (contentTable.equalsIgnoreCase(VITAL_DATA_TABLE_KEY)) {
                            for (String projectionKey : contentProjection) {
                                if (projectionKey.equalsIgnoreCase(VITAL_DATA_UID_KEY)) {
                                    readObject.put(projectionKey, cursor.getInt(cursor.getColumnIndex(projectionKey)));
                                } else if (projectionKey.equalsIgnoreCase(VITAL_DATA_DEVICE_ADDRESS_KEY)) {
                                    readObject.put(projectionKey, cursor.getString(cursor.getColumnIndex(projectionKey)));
                                } else if (projectionKey.equalsIgnoreCase(VITAL_DATA_TYPE_KEY)) {
                                    readObject.put(projectionKey, cursor.getString(cursor.getColumnIndex(projectionKey)));
                                } else if (projectionKey.equalsIgnoreCase(VITAL_DATA_VALUE_KEY)) {
                                    readObject.put(projectionKey, cursor.getString(cursor.getColumnIndex(projectionKey)));
                                } else if (projectionKey.equalsIgnoreCase(VITAL_DATA_TIMESTAMP_KEY)) {
                                    readObject.put(projectionKey, cursor.getString(cursor.getColumnIndex(projectionKey)));
                                } else if (projectionKey.equalsIgnoreCase(VITAL_DATA_IS_TRANSMITTED_KEY)) {
                                    readObject.put(projectionKey, cursor.getInt(cursor.getColumnIndex(projectionKey)));
                                }
                            }
                        }
                        responseArray.put(readObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();

            return responseArray;
        }

        private void saveDataToDB(JSONArray... params) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            for (int i = 0; i < params[0].length(); i++) {
                try {
                    ContentValues values = new ContentValues();
                    JSONObject saveObject = params[0].getJSONObject(i);
                    if (contentTable.equalsIgnoreCase(VITAL_DATA_TABLE_KEY)) {
                        for (String projectionKey : contentProjection) {
                            if (saveObject.has(projectionKey)) {
                                if (projectionKey.equalsIgnoreCase(VITAL_DATA_UID_KEY)) {
                                    values.put(projectionKey, saveObject.getInt(projectionKey));
                                } else if (projectionKey.equalsIgnoreCase(VITAL_DATA_DEVICE_ADDRESS_KEY)) {
                                    values.put(projectionKey, saveObject.getString(projectionKey));
                                } else if (projectionKey.equalsIgnoreCase(VITAL_DATA_TYPE_KEY)) {
                                    values.put(projectionKey, saveObject.getString(projectionKey));
                                } else if (projectionKey.equalsIgnoreCase(VITAL_DATA_VALUE_KEY)) {
                                    values.put(projectionKey, saveObject.getString(projectionKey));
                                } else if (projectionKey.equalsIgnoreCase(VITAL_DATA_TIMESTAMP_KEY)) {
                                    values.put(projectionKey, saveObject.getString(projectionKey));
                                } else if (projectionKey.equalsIgnoreCase(VITAL_DATA_IS_TRANSMITTED_KEY)) {
                                    values.put(projectionKey, saveObject.getInt(projectionKey));
                                }
                            }
                        }
                        try {
                            sqLiteDatabase.insertOrThrow(contentTable, null, values);
                        } catch (SQLiteException s) {
                            s.printStackTrace();
                            if (requestTag != null && requestTag.equalsIgnoreCase("UPDATE_TRANSMITTED")) {
                                int saveUID = saveObject.getInt(VITAL_DATA_UID_KEY);
                                ContentValues updateValues = new ContentValues();
                                updateValues.put(VITAL_DATA_IS_TRANSMITTED_KEY, 1);
                                sqLiteDatabase.update(contentTable, updateValues, VITAL_DATA_UID_KEY + " = ?", new String[]{String.valueOf(saveUID)});
                            }
                        }
                    }
                    //sqLiteDatabase.insert(contentTable, null, values);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            sqLiteDatabase.close();
        }

        private String getContentTableByID(SQLHandlerTables sqlHandlerTables) {
            if (sqlHandlerTables == null) {
                return null;
            }
            switch (sqlHandlerTables) {
                case VITAL_DATA:
                    return VITAL_DATA_TABLE_KEY;
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);
            if (sqlHandlerDelegate != null) {
                sqlHandlerDelegate.sqlHandlerDidReceiveContent(jsonArray, requestTag);
            }
            if (jsonArray == null && context != null && contentTable != null) {
                if (contentTable.equalsIgnoreCase(getContentTableByID(SQLHandlerTables.VITAL_DATA))) {
                    context.sendBroadcast(new Intent(APPCredentials.broadcastBluetoothUpdate), APPCredentials.broadcastPermission);
                }
            }
        }
    }
}

