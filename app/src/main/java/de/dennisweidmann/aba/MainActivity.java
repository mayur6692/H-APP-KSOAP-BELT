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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;

import de.dennisweidmann.aba.Model.APPCredentials;
import de.dennisweidmann.aba.Model.BTLE.BTLEHandler;
import de.dennisweidmann.aba.Stuff.SharedPreferenceKeys;
import de.dennisweidmann.aba.Stuff.ValueTypes;
import de.dennisweidmann.aba.Subclasses.SmoothViewPager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener, ViewPager.OnPageChangeListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private SmoothViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private boolean isDraggingViewPager = false;

    private IntentFilter intentFilter = new IntentFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        drawerLayout.addDrawerListener(this);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.tab_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        /*viewPager = (SmoothViewPager) findViewById(R.id.main_view_pager);
        viewPager.setPageTransformer(false, new ViewPagerTransformer());
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(this);*/


        // India_Team
        if (savedInstanceState == null) {
            Fragment newFragment = MainContentFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, newFragment).commit();
        }


        //UpdateReceiver.setupUpdateAlarmManager(getApplicationContext());

        intentFilter.addAction(APPCredentials.broadcastBluetoothUpdate);

        /*//TODO: Dummy Data
        try {
            JSONArray dataArray = new JSONArray();
            JSONObject newData = new JSONObject();
            newData.put(SQLHandler.VITAL_DATA_DEVICE_ADDRESS_KEY, "223232323223");
            newData.put(SQLHandler.VITAL_DATA_TYPE_KEY, "1");
            newData.put(SQLHandler.VITAL_DATA_VALUE_KEY, "95");
            dataArray.put(newData);
            new SQLHandler(this, null).saveVitalData(dataArray, null);
        } catch (JSONException e) {e.printStackTrace();}
        //TODO: Dummy Data end*/

        // India_Team
        String macAddress = APPCredentials.sharedPreferences(MainActivity.this).getString(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString(), "");
        if (!macAddress.equals("")) {
            BTLEHandler.sharedInstance().connectBTLEDevice(macAddress, getApplicationContext());
        }


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        registerReceiver(updateReceiver, intentFilter);
        if (!APPCredentials.sharedPreferences(this).getBoolean(SharedPreferenceKeys.IS_WIZARD_SEEN_B.toString(), false)) {
            startActivity(new Intent(this, WizardActivity.class));
            return;
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(updateReceiver);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.nav_menu_devices:
                //startActivity(new Intent(this, DiscoverDevicesActivity.class));
                //startActivity(new Intent(this, ScanDevicesActivity.class));

                // India_Team

                String macAddress = APPCredentials.sharedPreferences(this).getString(SharedPreferenceKeys.DEVICE_LAST_CONNECTED_ADDRESS_S.toString(), "");
                if (macAddress.equals("")) {
                    startActivity(new Intent(this, ScanDevicesActivity.class));
                } else {
                    startActivity(new Intent(this, ConnectedDevicesActivity.class));
                }


                return true;
            case R.id.nav_menu_settings:
                return true;
            case R.id.nav_menu_wizard:
                startActivity(new Intent(this, WizardActivity.class));
                return true;
            case R.id.nav_menu_about:
                return true;
            case R.id.nav_tab_heart_frequency:
                showPageWithID(0);
                return true;
            case R.id.nav_tab_blood_pressure:
                showPageWithID(1);
                return true;
            case R.id.nav_tab_heart_rhythm:
                showPageWithID(2);
                return true;
            case R.id.nav_tab_sleep_monitor:
                showPageWithID(3);
                return true;
            case R.id.nav_tab_step_counter:
                showPageWithID(4);
                return true;
        }
        return false;
    }

    private void showPageWithID(int newPageID) {
        int currentPage = viewPager.getCurrentItem();
        currentPage -= newPageID;
        if (currentPage < 0) {
            currentPage *= -1;
        }
        if (currentPage == 1) {
            viewPager.isProgrammatically = true;
            viewPager.setCurrentItem(newPageID, true);
            viewPagerAdapter.getItem(newPageID).updateContent(newPageID);
        } else {
            viewPager.setPageTransformer(false, null);
            viewPager.isProgrammatically = false;
            viewPager.setCurrentItem(newPageID);
            viewPagerAdapter.getItem(newPageID).updateContent(newPageID);
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        switch (state) {
            case ViewPager.SCROLL_STATE_DRAGGING:
                isDraggingViewPager = true;
                break;
            case ViewPager.SCROLL_STATE_IDLE:
                if (isDraggingViewPager) {
                    int currentPosition = viewPager.getCurrentItem();
                    switch (currentPosition) {
                        case 0:
                            bottomNavigationView.setSelectedItemId(R.id.nav_tab_heart_frequency);
                            viewPagerAdapter.getItem(0).updateContent(0);
                            break;
                        case 1:
                            bottomNavigationView.setSelectedItemId(R.id.nav_tab_blood_pressure);
                            break;
                        case 2:
                            bottomNavigationView.setSelectedItemId(R.id.nav_tab_heart_rhythm);
                            break;
                        case 3:
                            bottomNavigationView.setSelectedItemId(R.id.nav_tab_sleep_monitor);
                            break;
                        case 4:
                            bottomNavigationView.setSelectedItemId(R.id.nav_tab_step_counter);
                            break;
                    }
                }
                viewPager.setPageTransformer(false, new ViewPagerTransformer());
                isDraggingViewPager = false;
                viewPager.isProgrammatically = false;
                break;
        }
    }

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null || !intent.getAction().equalsIgnoreCase(APPCredentials.broadcastBluetoothUpdate)) {
                return;
            }
            int currentPosition = viewPager.getCurrentItem();
            viewPagerAdapter.getItem(currentPosition).updateContent(currentPosition);
        }
    };

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private SparseArray<MainContentFragment> currentFragments = new SparseArray<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public MainContentFragment getItem(int position) {
            MainContentFragment mainContentFragment = currentFragments.get(position);
            if (mainContentFragment == null) {
                mainContentFragment = new MainContentFragment();
                mainContentFragment.fragmentPosition = position;
                switch (position) {
                    case 0:
                        mainContentFragment.valueType = ValueTypes.HEART_FREQUENCY;
                        break;
                    case 1:
                        mainContentFragment.valueType = ValueTypes.BLOOD_PRESSURE;
                        break;
                    case 2:
                        mainContentFragment.valueType = ValueTypes.HEART_RHYTHM;
                        break;
                    case 3:
                        mainContentFragment.valueType = ValueTypes.SLEEP_MONITOR;
                        break;
                    case 4:
                        mainContentFragment.valueType = ValueTypes.STEP_COUNTER;
                        break;
                }
                currentFragments.append(position, mainContentFragment);
            }
            return mainContentFragment;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    private class ViewPagerTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) {
                view.setAlpha(0);
            } else if (position <= 1) {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else {
                view.setAlpha(0);
            }
        }
    }
}
