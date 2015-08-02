package com.burke.kelv.timerdriving;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import static com.burke.kelv.timerdriving.Globals.*;

/**
 * Created by kelv on 22/02/2015.
 */
public class TimerService extends Service {
    private static final String LOG_TAG = "Timer Service";
    private static NotificationCompat.Builder builder;
    private static NotificationManager notificationManager;
    @Deprecated private static Timer viewUpdaterTimer;
    @Deprecated private static Timer notificationUpdaterTimer;
    @Deprecated private static int notificationMinutes;

    private static KTrip trip;

    @Override
    public void onCreate() {
        LocalBroadcastManager.getInstance(this).registerReceiver(timerDataReceiver, new IntentFilter(notificationTimerBroadcastId) );
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Received onStart Command");
        String action = intent.getAction();
        Boolean isNight = intent.getBooleanExtra("night",false);

        if (action.equals(Globals.ACTION.STARTFOREGROUND_ACTION)) {
            if (builder == null) {
                Log.i(LOG_TAG, "Received Start Foreground Intent ");
                Intent notificationIntent = new Intent(this, MainActivity.class);
                notificationIntent.setAction(Globals.ACTION.MAIN_ACTION);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                Intent stopIntent = new Intent(this, FinishTripActivity.class);
                stopIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pstopIntent = PendingIntent.getActivity(this, 0, stopIntent, 0);

                Intent playIntent = new Intent(this, TimerService.class);
                playIntent.setAction(Globals.ACTION.PLAY_ACTION);
                PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

                Intent pauseIntent = new Intent(this, TimerService.class);
                pauseIntent.setAction(Globals.ACTION.PAUSE_ACTION);
                PendingIntent pPauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher);

                builder = new NotificationCompat.Builder(this)
                        .setContentTitle("Driving Timer")
                        .setTicker("Driving Timer")
                        .setContentText("Current Trip: 00:00")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(
                                Bitmap.createScaledBitmap(icon, 256, 256, false))
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .addAction(android.R.drawable.ic_media_play, "Play",
                                pplayIntent)
                        .addAction(android.R.drawable.ic_media_pause, "Pause",
                                pPauseIntent)
                        .addAction(R.drawable.ic_media_stop,
                                "Stop", pstopIntent);

                notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                //notificationManager.notify(notificationID, builder.build());
            }
                startForeground(Globals.NOTIFICATION_ID.FOREGROUND_SERVICE,
                        builder.build());
        }
        else if (action.equals(Globals.ACTION.STOP_ACTION)) {
            Boolean stopping = false;
            if (trip != null) {
                if (trip.isNightTrip && isNight) {
                    stopping = true;
                } else if (!trip.isNightTrip && !isNight)  {
                    stopping = true;
                }
            }
            if (stopping) {
                Intent dialogIntent = new Intent(this, FinishTripActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
            }
        }
        else if (action.equals(Globals.ACTION.PLAY_ACTION)) {
            if (trip == null) trip = new KTrip(this,0,0,0,0,false,0,0,0,0,0,isNight,KTrip.STATUS.NOT_RUNNING,0,null,null); //TODO GET ORDER
            Boolean showSpeed = intent.getBooleanExtra("speedOverlay",false);



            if (showSpeed) {
                Intent speedIntent = new Intent(this, FloatingSpeedService.class);
                speedIntent.setAction(ACTION.STARTFOREGROUND_ACTION);
                startService(speedIntent);
            }
            startTrip();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    getDetailsFromLastTrip();
                    Log.d("time","time1");
                    Log.d("time","time2");
                }
            }).start();

            Log.i(LOG_TAG, "Clicked Play");
            //startTimer();
        }
        else if (action.equals(Globals.ACTION.PAUSE_ACTION)) {
            Log.i(LOG_TAG, "Clicked Pause");
            if (trip != null && !trip.isNightTrip && !isNight) trip.pause();
            if (trip != null && trip.isNightTrip && isNight) trip.pause();
        }
        else if (action.equals(Globals.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            if (trip != null) {
                trip = null;
                builder = null;
                stopForeground(true);
                stopSelf();
            }
        }
        else if (action.equals(ACTION.SHOW_HIDE_SPEED_OVERLAY_ACTION)) {
            Boolean showSpeed = intent.getBooleanExtra("speedOverlay",false);
            Intent speedIntent = new Intent(this, FloatingSpeedService.class);

            if (showSpeed) speedIntent.setAction(ACTION.STARTFOREGROUND_ACTION);
            else speedIntent.setAction(ACTION.STOPFOREGROUND_ACTION);

            startService(speedIntent);

            if (showSpeed && trip != null && trip.getLocationHelper() != null) trip.getLocationHelper().updateSpeedDialogWithLatestSpeed();
        }
        else if (action.equals(ACTION.USE_GLOBALS_CURRENT_TRIP)) {
            Globals.currentTrip.setContext(this);
            trip = Globals.currentTrip;

        }

        return START_NOT_STICKY;
    }

    private void startTrip() {
        trip.start();
    }

    private void getDetailsFromLastTrip(){
        Log.w("dihgroidhgrohdrgoidhogd","auieuyfa8isufeg");
        DBHelper dbHelper = MyApplication.getStaticDbHelper();
        Cursor cursor = dbHelper.getLastFinishedTrip(trip._id);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    trip.odometerStart = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ODO_END));
                    trip.carID = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_CAR_ID));
                    trip.parentID = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_PARENT));
                    trip.parking = ConversionHelper.intToBool(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_PARKING)));
                    trip.traffic = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_TRAFFIC));
                    trip.weather = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_WEATHER));
                    trip.roadTypeID = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ROAD_TYPE));
                    trip.timeOfDay = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_LIGHT));

                    trip.totalDrivingBeforeTime = dbHelper.getDBTime(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_DRIVING_TOTAL_AFTER)));
                    trip.totalDrivingBeforeTime = trip.totalDrivingBeforeTime != null ? trip.totalDrivingBeforeTime : new KTime(Globals.ZERO_TIME);
                    trip.totalDrivingBeforeTime.id = 0;

                    trip.totalNightBeforeTime = dbHelper.getDBTime(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_NIGHT_TOTAL_AFTER)));
                    trip.totalNightBeforeTime = trip.totalNightBeforeTime != null ? trip.totalNightBeforeTime : new KTime(Globals.ZERO_TIME);
                    trip.totalNightBeforeTime.id = 0;

                    trip.order = (cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ORDER)) + 1);

                    trip.updateAllRunningColumnsInDB();
                }
            }
            catch (Exception e ) {
                e.printStackTrace();
            }
            finally {
                cursor.close();
                if (trip.totalDrivingBeforeTime == null) trip.totalDrivingBeforeTime = new KTime(Globals.ZERO_TIME);
                if (trip.totalNightBeforeTime == null) trip.totalNightBeforeTime = new KTime(Globals.ZERO_TIME);
            }
        }
    }

    private BroadcastReceiver timerDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String time = intent.getStringExtra("minutes");
            Boolean listeningRequest = intent.getBooleanExtra("listeningRequest", false);
            if (time != null && !time.equals("")) {
                builder.setContentText("Current Trip: " + time);
                notificationManager.notify(Globals.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
            } if (listeningRequest) {
                Intent sendIntent = new Intent(Globals.notificationTimerListeningBroadcast);
                sendIntent.putExtra("listening", true);
                LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent);
            }
        }
    };

    @Override
    public void onDestroy() {
        Log.i(LOG, "In onDestroy of service");
        Intent sendIntent = new Intent(Globals.notificationTimerListeningBroadcast);
        sendIntent.putExtra("listening", false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(sendIntent);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerDataReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
    **@Deprecated
    **private void startTimer() {
    **    latestTime = new KTime(NOW);
    **   viewUpdaterTimer = new Timer();
    **   viewUpdaterTimer.scheduleAtFixedRate(new TimerTask() {
    **                                             @Override
    **                                             public void run() {
    **                                                 new Thread(new Runnable() {
    **                                                     public void run() {
    **                                                         KTime now = new KTime(NOW);
    ***                                                         if (latestTime.seconds != now.seconds) {
    ****                                                             latestTime = now;
    ****                                                             Log.i(LOG_TAG, "changed: " + latestTime.seconds);
    *                                                             Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
    **                                                             Runnable myRunnable = new Runnable() {
    **                                                                 public void run() {
    **                                                                     updateViews();
    **                                                                 }
    *                                                             };
    *                                                             mainHandler.post(myRunnable);
    *                                                         }
    *                                                     }
    *                                                 }).start();
    *                                             }
    *                                         },
    *            0,
    *            100);
    *    notificationUpdaterTimer = new Timer();
    *    notificationUpdaterTimer.scheduleAtFixedRate(new TimerTask() {
    *                                             @Override
    *                                             public void run() {
    *                                                 new Thread(new Runnable() {
    *                                                     public void run() {
    *                                                         final KTime now = new KTime(NOW);
    *                                                         if (notificationMinutes != now.minutes) {
    *                                                             notificationMinutes = now.minutes;
    *                                                             Log.i(LOG_TAG, "changed notification: " + notificationMinutes);
    *                                                             Handler mainHandler = new Handler(getApplicationContext().getMainLooper());*
    *                                                             Runnable myRunnable = new Runnable() {
    *                                                                 public void run() {
    *                                                                 }
    *                                                             };
    *                                                             mainHandler.post(myRunnable);
    *                                                         }
    *                                                     }
    *                                                 }).start();
    *                                             }
    *                                         },
    *            0,
    *            60000);
    *}
    *@Deprecated
    *private void stopTimer() {
    *    viewUpdaterTimer.cancel();
    *}
    *@Deprecated
    *private KTime updateElapsed() {
    *    elapsedTime = KTime.getDiffBetweenTimes(latestTime, startTime);
    *    elapsedTime.readable = (elapsedTime.hours+":"+elapsedTime.minutes+":"+elapsedTime.seconds);
    *    return elapsedTime;
    *}
    *@Deprecated
    *private void updateViews() {
    *    KTime time = updateElapsed();
    *    if (time.seconds == 0 && time.minutes > 0) updateNotification(time);
    *    Intent intent = new Intent(Globals.defaultTimerUpdateBroadcastId);
    *    intent.putExtra("elapsed", time.readable);
    *    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    *}
    *@Deprecated
    *private void updateNotification(GregorianCalendar time) {
    *    String noteTime = time.g + ":" + time.minutes;
    *    builder.setContentText("Current Trip: " + noteTime);
    *    notificationManager.notify(Globals.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
    *}
     **/
}
