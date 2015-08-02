package com.burke.kelv.timerdriving;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import static com.burke.kelv.timerdriving.Globals.LOG;
import static com.burke.kelv.timerdriving.Globals.defaultTimerRunning;
import static com.burke.kelv.timerdriving.Globals.defaultTimerUpdateBroadcastId;

/**
 * Created by kelv on 2/03/2015.
 */
public class KTimerHelper {
    public static interface TYPE {
        public final static int MINUTE = 1;
        public final static int SECOND = 2;
    }
    public static interface TimerHelperInterface {
        public KTime returnElapsed();
    }
    public static final String broadcastID = "timer helper";

    private Timer timer;
    private double interval;
    private int type;
    private Boolean needToSync;
    private KTrip tripToQuery;
    public KSubTrip subToQuery;
    private Boolean running;
    private Boolean listening;
    private String broadcastSendId;
    private KTime lastTimeSent;
    private Context context;
    private IntentFilter intentFilter;
    private Boolean receiverRegistered = false;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    public KTimerHelper() { }

    public KTimerHelper(Context context, int Type, KTrip tripQuery) {
        this.context = context;
        this.tripToQuery = tripQuery;
        this.running = false;
        if (Type == TYPE.MINUTE) {
            this.interval = 60;
            this.type = TYPE.MINUTE;
            this.needToSync = true;
            this.broadcastSendId = Globals.notificationTimerBroadcastId;
            this.intentFilter = new IntentFilter(Globals.notificationTimerListeningBroadcast);
            this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            this.pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        }
        else if (Type == TYPE.SECOND) {
            this.interval = 1;
            this.type = TYPE.SECOND;
            this.needToSync = false;
            this.broadcastSendId = Globals.defaultTimerUpdateBroadcastId;
            this.intentFilter = new IntentFilter(Globals.defaultTimerListeningBroadcast);
        }
    }
    public KTimerHelper(Context context,int Type, KTrip tripQuery, KSubTrip subTrip) {
        this.context = context;
        this.tripToQuery = tripQuery;
        this.subToQuery = subTrip;
        this.running = false;
        if (Type == TYPE.MINUTE) {
            this.interval = 60;
            this.type = TYPE.MINUTE;
            this.needToSync = true;
            this.broadcastSendId = Globals.notificationTimerBroadcastId;
            this.intentFilter = new IntentFilter(Globals.notificationTimerListeningBroadcast);
            this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            this.pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        }
        else if (Type == TYPE.SECOND) {
            this.interval = 1;
            this.type = TYPE.SECOND;
            this.needToSync = false;
            this.broadcastSendId = Globals.defaultTimerUpdateBroadcastId;
            this.intentFilter = new IntentFilter(Globals.defaultTimerListeningBroadcast);
        }
    }
    public void setContext(Context context) {
        this.context = context;
    }

    private void startWithDelay(final double delayInSec) {
        if (!running) {
            if (tripToQuery.status == KTrip.STATUS.RUNNING) {
                if (type == TYPE.SECOND) {
                    timer = new Timer();
                    running = true;
                    new Thread(new Runnable() {
                        public void run() {
                            timer.scheduleAtFixedRate(new TimerTask() {
                                                          @Override
                                                          public void run() {
                                                              KTime now = new KTime(Globals.NOW);
                                                              if (lastTimeSent != null) {
                                                                  if (type == TYPE.MINUTE) {
                                                                      if (lastTimeSent.minutes == now.minutes) {
                                                                          running = false;
                                                                          timer.cancel();
                                                                          startWithDelay(60);
                                                                      } else {
                                                                          KTime elapsed = tripToQuery.returnElapsed();
                                                                          Log.i(LOG, "sending minute:" +
                                                                                  KTime.getProperReadable(elapsed, Globals.TIMEFORMAT.H_MM_SS));
                                                                          Intent intent = new Intent(broadcastSendId);
                                                                          intent.putExtra("minutes", KTime.getProperReadable(elapsed, Globals.TIMEFORMAT.H_MM));
                                                                          LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                                                      }
                                                                  } else if (type == TYPE.SECOND) {
                                                                      if (lastTimeSent.seconds == now.seconds) {
                                                                          running = false;
                                                                          timer.cancel();
                                                                          startWithDelay(0.1);
                                                                      } else {
                                                                          KTime elapsed = tripToQuery.returnElapsed();
                                                                          lastTimeSent = elapsed;

                                                                          Intent intent = new Intent(broadcastSendId);
                                                                          Log.i(LOG, "sending seconds:" +
                                                                                  KTime.getProperReadable(elapsed, Globals.TIMEFORMAT.H_MM_SS));
                                                                          intent.putExtra("elapsed", KTime.getProperReadable(elapsed, Globals.TIMEFORMAT.MM_SS_plusHoursIfAppl));
                                                                          LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                                                      }
                                                                  }
                                                              } else {
                                                                  if (type == TYPE.MINUTE) {
                                                                      KTime elapsed = tripToQuery.returnElapsed();
                                                                      Log.i(LOG, "sending minute:" +
                                                                              KTime.getProperReadable(elapsed, Globals.TIMEFORMAT.H_MM_SS));
                                                                      Intent intent = new Intent(broadcastSendId);
                                                                      intent.putExtra("minutes", KTime.getProperReadable(elapsed, Globals.TIMEFORMAT.H_MM));
                                                                      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                                                  } else if (type == TYPE.SECOND) {
                                                                      KTime elapsed = tripToQuery.returnElapsed();
                                                                      Log.i(LOG, "sending secondssss:" +
                                                                              KTime.getProperReadable(elapsed, Globals.TIMEFORMAT.H_MM_SS));
                                                                      lastTimeSent = elapsed;
                                                                      Intent intent = new Intent(broadcastSendId);
                                                                      intent.putExtra("elapsed", KTime.getProperReadable(elapsed, Globals.TIMEFORMAT.MM_SS_plusHoursIfAppl));
                                                                      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                                                  }
                                                              }
                                                          }
                                                      },
                                    (int) delayInSec * 1000,
                                    (int) (interval * 1000));

                        }
                    }).start();
                } else if (type == TYPE.MINUTE) {
                    Log.i(LOG, "Minute Alarm Set");
                    AlarmReceiver.activate(context);
                    running = true;
                }
            }
        }
    }

    public void start() {
       if (!receiverRegistered) { LocalBroadcastManager.getInstance(context).registerReceiver(listeningReceiver, intentFilter);
       receiverRegistered = true; }
       Intent intent = new Intent(broadcastSendId);
       intent.putExtra("listeningRequest", true);
       LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    
    public void stop() {
        if (running) {
            if (type == TYPE.SECOND) timer.cancel();
            else if (type == TYPE.MINUTE) AlarmReceiver.stop(context);
            Log.i(LOG, "Stopping timer from helper, interval:" + interval);
            running = false;
        }
    }

    private BroadcastReceiver listeningReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
         //   if (true)return;
            Boolean listening = intent.getBooleanExtra("listening", false);
            Log.i(LOG, "received broadcast int:" + interval + listening);
            if (listening) {
                if (!running) {
                    Log.i(LOG, "Starting timer from ListenerReceiver in helper, interval:" + interval);
                    if (needToSync) {
                        KTime time = new KTime(Globals.ZERO_TIME);
                        time.seconds = (int) interval;
                        KTime diff = KTime.getDiffBetweenTimes(tripToQuery.returnElapsed(), time);
                        Log.i(LOG, "needtosync, interval:" + interval + diff.seconds);
                        startWithDelay(diff.seconds);
                    } else startWithDelay(0);
                } else Log.i(LOG, "Timer already running, interval:" + interval);
            } else {

                stop();
            }
        }
    };

    public void destroy(){
        stop();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(listeningReceiver);
    }

}
