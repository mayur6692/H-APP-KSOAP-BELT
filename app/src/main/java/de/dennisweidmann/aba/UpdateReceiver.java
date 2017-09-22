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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;

public class UpdateReceiver extends WakefulBroadcastReceiver {

    private static final int BLUETOOTH_UPDATE_INTERVAL = 1;
    private static final int SERVER_UPDATE_INTERVAL = 6;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) {return;}
        if (intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED")) {
            setupUpdateAlarmManager(context);
        } else if (intent.getAction().equalsIgnoreCase("de.dennisweidmann.aba.action.UPDATE_BLUETOOTH")) {
            Intent bluetoothServiceIntent = new Intent(context, UpdateService.class);
            bluetoothServiceIntent.setAction("de.dennisweidmann.aba.action.UPDATE_BLUETOOTH");
            startWakefulService(context, bluetoothServiceIntent);
        } else if (intent.getAction().equalsIgnoreCase("de.dennisweidmann.aba.action.UPDATE_SERVER")) {
            Intent serverServiceIntent = new Intent(context, UpdateService.class);
            serverServiceIntent.setAction("de.dennisweidmann.aba.action.UPDATE_SERVER");
            startWakefulService(context, serverServiceIntent);
        }
    }

    public static void setupUpdateAlarmManager (Context context) {
        if (context == null) {return;}
        AlarmManager serverAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        serverAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 5000, 1000 * 60 * SERVER_UPDATE_INTERVAL, serverAlarmPendingIntent(context));

        AlarmManager bluetoothAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        bluetoothAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 5000, 1000 * 60 * BLUETOOTH_UPDATE_INTERVAL, bluetoothAlarmPendingIntent(context));
    }

    private static Intent bluetoothAlarmIntent (Context context) {
        Intent intent = new Intent(context, UpdateReceiver.class);
        intent.setAction("de.dennisweidmann.aba.action.UPDATE_BLUETOOTH");
        return intent;
    }

    private static Intent serverAlarmIntent (Context context) {
        Intent intent = new Intent(context, UpdateReceiver.class);
        intent.setAction("de.dennisweidmann.aba.action.UPDATE_SERVER");
        return intent;
    }

    private static PendingIntent bluetoothAlarmPendingIntent (Context context) {
        return PendingIntent.getBroadcast(context, 0, bluetoothAlarmIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent serverAlarmPendingIntent (Context context) {
        return PendingIntent.getBroadcast(context, 0, serverAlarmIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
