package com.burke.kelv.timerdriving;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import static com.burke.kelv.timerdriving.Globals.LOG;
import static com.burke.kelv.timerdriving.Globals.currentTrip;

/**
 * Created by kelv on 11/03/2015.
 */

public class AlarmReceiver extends BroadcastReceiver {
    static PendingIntent pendingIntent;
    static final String broadcastSendId = Globals.notificationTimerBroadcastId;

    public static void activate(Context context) {
        AlarmManager alarmService = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        pendingIntent = PendingIntent.getBroadcast(context, 1, alarmIntent, 0);
        Log.i(LOG, "pending intent: " + pendingIntent);
        if (pendingIntent != null) {
            alarmService.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 20000, pendingIntent);
            Log.i(LOG, "scheduled intent: " + pendingIntent);
            Log.i(LOG, alarmService.toString());
        }
    }

    public static void stop(Context context) {
        AlarmManager alarmService = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmService.cancel(pendingIntent);
        Log.i(LOG, "stopping");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent it = new Intent(broadcastSendId);
        if (currentTrip != null) {
            String elapsed = KTime.getProperReadable(currentTrip.returnElapsed(), Globals.TIMEFORMAT.H_MM);
            it.putExtra("minutes", elapsed);
            it.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            LocalBroadcastManager.getInstance(context).sendBroadcast(it);
            Log.i(LOG, "minutess:" + elapsed + " time:" + KTime.getProperReadable(new KTime(Globals.NOW), Globals.TIMEFORMAT.HH_MM_SS));
        }
    }
}
