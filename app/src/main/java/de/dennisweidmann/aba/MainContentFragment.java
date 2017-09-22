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

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.dennisweidmann.aba.Model.BTLE.BTLEHandler;
import de.dennisweidmann.aba.Model.BTLE.BTLEService;
import de.dennisweidmann.aba.Model.SQLHandler;
import de.dennisweidmann.aba.Stuff.ValueTypes;

public class MainContentFragment extends Fragment implements SQLHandler.SQLHandlerDelegate {

    private static final int HEART_FREQUENCES = 0;
    private static final int BLOOD_PRESSURE = 1;
    private static final int HEARTS_RYTHM = 2;
    private static final int SLEEP_MONITOR = 3;
    private static final int PEDOMETER = 4;
    private Context context;
    public ValueTypes valueType;
    public int fragmentPosition = -1;

    private ImageView smallLeftRatioImage;
    private TextView smallLeftRatioText;

    private ImageView smallRightRatioImage;
    private TextView smallRightRatioText;

    public TextView typeText;
    private TextView valueText;
    private ImageView unitImage;
    private ImageView powerImage;
    private ImageView bluetoothImage;


    // India_Team
    private TextView txtArmBraclet,txtConnectionState;
    private Button btnClickMe;

    public static MainContentFragment newInstance() {
        MainContentFragment mainContentFragment = new MainContentFragment();
        return mainContentFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_main_content, container, false);
        this.context = container.getContext();

        smallLeftRatioImage = (ImageView) fragmentView.findViewById(R.id.small_left_ratio_image);
        smallLeftRatioImage.getDrawable().setColorFilter(ContextCompat.getColor(this.context, R.color.colorLogo), PorterDuff.Mode.SRC_IN);
        smallLeftRatioText = (TextView) fragmentView.findViewById(R.id.small_left_ratio_text);

        smallRightRatioImage = (ImageView) fragmentView.findViewById(R.id.small_right_ratio_image);
        smallRightRatioImage.getDrawable().setColorFilter(ContextCompat.getColor(this.context, R.color.colorLogo), PorterDuff.Mode.SRC_IN);
        smallRightRatioText = (TextView) fragmentView.findViewById(R.id.small_right_ratio_text);

        unitImage = (ImageView) fragmentView.findViewById(R.id.big_main_unit_image);
        unitImage.getDrawable().setColorFilter(ContextCompat.getColor(this.context, R.color.colorBlue), PorterDuff.Mode.SRC_IN);

        valueText = (TextView) fragmentView.findViewById(R.id.big_main_number_text);

        powerImage = (ImageView) fragmentView.findViewById(R.id.big_main_power_image);
        powerImage.getDrawable().setColorFilter(ContextCompat.getColor(this.context, R.color.colorBlue), PorterDuff.Mode.SRC_IN);

        bluetoothImage = (ImageView) fragmentView.findViewById(R.id.big_main_bluetooth_image);
        bluetoothImage.getDrawable().setColorFilter(ContextCompat.getColor(this.context, R.color.colorBlue), PorterDuff.Mode.SRC_IN);

        txtArmBraclet = (TextView) fragmentView.findViewById(R.id.txtArmBraclet);
        txtConnectionState = (TextView) fragmentView.findViewById(R.id.txtConnectionState);
        btnClickMe = (Button) fragmentView.findViewById(R.id.btnClickMe);

        typeText = (TextView) fragmentView.findViewById(R.id.big_main_type_text);

        if (valueType != null) {
            switch (valueType) {
                case HEART_FREQUENCY:
                    typeText.setText(getString(R.string.nav_tab_heart_frequency));
                    break;
                case BLOOD_PRESSURE:
                    typeText.setText(getString(R.string.nav_tab_blood_pressure));
                    break;
                case HEART_RHYTHM:
                    typeText.setText(getString(R.string.nav_tab_heart_rhythm));
                    break;
                case SLEEP_MONITOR:
                    typeText.setText(getString(R.string.nav_tab_sleep_monitor));
                    break;
                case STEP_COUNTER:
                    typeText.setText(getString(R.string.nav_tab_step_counter));
                    break;
            }
            if (fragmentPosition == 0) {
                updateContent(fragmentPosition);
            }
        }

        return fragmentView;
    }

    // India_Team
    // Note : The previous developer did wrong implementation of the fragment life cycle,
    // they have not follow the proper life cycle of android fragment.
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // India_Team
        BTLEHandler.sharedInstance().setOnReadCharacteristicListner(new BTLEHandler.OnReadCharacteristicListner() {
            @Override
            public void onActionDataAvailable(String charactristic, String characteristicValue) {

                if (charactristic == null && characteristicValue == null)
                    return;

                if (charactristic.equals(BTLEService.HEART_FREQUENCY_CHARACTERISTIC_DATA)) {
                    Log.e("Heart_Frequences", characteristicValue + "");
                    Toast.makeText(context, characteristicValue + "", Toast.LENGTH_SHORT).show();
                    valueText.setText(characteristicValue);
                } else if (charactristic.equals(BTLEService.BLOOD_PRESSURE_CHARACTERISTIC_DATA)) {
                    Log.e("Blood Pressure", characteristicValue + "");
                    Toast.makeText(context, characteristicValue + "", Toast.LENGTH_SHORT).show();
                    valueText.setText(characteristicValue);
                }
            }

            @Override
            public void onConnectionChange(boolean state) {

                if (state){
                    txtConnectionState.setText("Device connected");
                    txtConnectionState.setTextColor(Color.parseColor("#00FF00"));
                }else{
                    txtConnectionState.setText("Device not connected");
                    txtConnectionState.setTextColor(Color.parseColor("#FF0000"));
                }

            }
        });

        // India_Team
        btnClickMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //readVitalData(fragmentPosition);
                readVitalData(0);
            }
        });
    }

    // India_Team
    private void readVitalData(int fragmentPosition) {
        switch (fragmentPosition) {
            case HEART_FREQUENCES: {
                BTLEHandler.sharedInstance().readHeartFrequencyCharacteristic();
                break;
            }
            case BLOOD_PRESSURE: {
                BTLEHandler.sharedInstance().readBloodPressureCharacteristic();
                break;
            }
            case HEARTS_RYTHM: {

                break;
            }
            case SLEEP_MONITOR: {

                break;
            }
            case PEDOMETER: {

                break;
            }
        }
    }

    public void updateContent(int atPosition) {
        if (context == null || valueType == null || atPosition != fragmentPosition) {
            return;
        }

        // India_Team
        readVitalData(fragmentPosition);

        //new SQLHandler(context, this).loadVitalDataOfType(valueType, valueType.toString());
    }

    @Override
    public void sqlHandlerDidReceiveContent(JSONArray contentArray, String requestTag) {
        if (contentArray == null || valueType == null || contentArray.length() < 1) {
            return;
        }
        try {
            JSONObject contentObject = contentArray.getJSONObject(0);
            Log.e("json", contentObject.toString());
            if (contentObject == null) {
                return;
            }
            if (contentObject.has(SQLHandler.VITAL_DATA_VALUE_KEY)) {
                String contentValue = contentObject.getString(SQLHandler.VITAL_DATA_VALUE_KEY);
                if (contentValue != null) {
                    valueText.setText(contentValue);
                }
            }
            Log.e("contentObject", contentObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
