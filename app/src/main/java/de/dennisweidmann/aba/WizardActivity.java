package de.dennisweidmann.aba;

/**
 * Created by Mitesh Machhoya
 * miteshmachhoya@gmail.com
 * skype : miteshmachhoya
 * Android Application Developer
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import de.dennisweidmann.aba.Model.APPCredentials;
import de.dennisweidmann.aba.Stuff.SharedPreferenceKeys;

public class WizardActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int NEXT_VIEW_REQUEST = 1;

    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        nextButton = (Button) findViewById(R.id.wizard_next_button);
        nextButton.setOnClickListener(this);
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case NEXT_VIEW_REQUEST:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == nextButton) {
            // startActivityForResult(new Intent(this, DiscoverDevicesActivity.class), NEXT_VIEW_REQUEST);

            // India_Team
            String macAddress = APPCredentials.sharedPreferences(this).getString(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString(), "");
            if (macAddress.equals("")) {
                startActivityForResult(new Intent(this, ScanDevicesActivity.class), NEXT_VIEW_REQUEST);
            } else {
                startActivityForResult(new Intent(this, ConnectedDevicesActivity.class), NEXT_VIEW_REQUEST);
            }
        }
    }
}
