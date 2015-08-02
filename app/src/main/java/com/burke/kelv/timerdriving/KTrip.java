package com.burke.kelv.timerdriving;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.burke.kelv.timerdriving.Globals.LOG;

/**
 * Created by kelv on 25/02/2015.
 */
public class KTrip extends Object implements KTimerHelper.TimerHelperInterface {
    public static interface STATUS {
        public final static int NOT_INITIALISED = 1;
        public final static int RUNNING = 2;
        public final static int PAUSED = 3;
        public final static int FINISHED = 4;
        public final static int NOT_RUNNING = 5;
        public final static int PLACEHOLDER_TRIP = 6;
    }

    @Deprecated private Boolean shouldRunSecondTimer;
    @Deprecated private Boolean shouldRunMinuteTimer;
    @Deprecated private Timer secondTimer;
    @Deprecated private Timer minuteTimer;

    private KTimerHelper timerHelper;
    private KTimerHelper timerHelperMinute;

    private DBHelper dbHelper;

    private Context context;
    public int _id;
    public int status = STATUS.NOT_INITIALISED;

    public KTime realStartedTime;
    public KTime realEndTime;
    public KTime realTotalTime;
    public KTime apparentStartTime;
    public KTime apparentEndTime;
    public KTime apparentTotalTime;
    public KTime elapsedTime;
    public KTime totalDrivingBeforeTime;
    public KTime totalDrivingAfterTime;
    public KTime totalNightBeforeTime;
    public KTime totalNightAfterTime;

    public int order;

    public ArrayList<Integer> subTripIDs;
    public KSubTrip currentSubTrip;
    private KTime previousSubTripTotalTime;

    public int odometerStart;
    public int odometerEnd;
    public int distance;
    public int carID;
    public int numOfSubtrips;
    public int traffic;
    public int weather;
    public int roadTypeID;
    public int timeOfDay;
    public int parentID;

    public boolean parking;
    public boolean isNightTrip;

    private LocationHelper lcHelper;

    public KTrip() {}

    public KTrip(Context context, int id, int numOfSubs, int realStartID, int realEndID, int realTotalID, int appaStartID, int appaEndID, int appaTotalID,
                 int odoStart, int odoEnd, int distance, int carID, boolean parking, int traffic, int weather, int roadTypeID,
                 int light, int parentID, boolean isNight, int status, int totalDrivingAfterID, int nightDrivingAfterID, int order) throws Exception {
        if (true) throw new RuntimeException("dont make finished trip");
        DBHelper dbHelp = MyApplication.getStaticDbHelper();
        this._id = id;
        this.numOfSubtrips = numOfSubs;
        this.realStartedTime = dbHelp.getDBTime(realStartID);
        this.realEndTime = dbHelp.getDBTime(realEndID);
        this.realTotalTime = dbHelp.getDBTime(realTotalID);
        this.apparentStartTime = dbHelp.getDBTime(appaStartID);
        this.apparentEndTime = dbHelp.getDBTime(appaEndID);
        this.apparentTotalTime = dbHelp.getDBTime(appaTotalID);
        this.odometerStart = odoStart;
        this.odometerEnd = odoEnd;
        this.distance = distance;
        this.carID = carID;
        this.parking = parking;
        this.traffic = traffic;
        this.weather = weather;
        this.roadTypeID = roadTypeID;
        this.timeOfDay = light;
        this.parentID = parentID;
        this.isNightTrip = isNight;
        this.status = status;
        this.totalDrivingAfterTime = dbHelp.getDBTime(totalDrivingAfterID);
        this.totalNightAfterTime = dbHelp.getDBTime(nightDrivingAfterID);
        this.order = order;

        this.subTripIDs = new ArrayList<Integer>();
    }

    public KTrip(Context context, int odoStart, int odoEnd, int distance, int carID, boolean parking, int traffic, int weather, int roadTypeID,
                 int light, int parentID, boolean isNight, int status, int order,KTime totalDrivingBeforeTime, KTime totalNightBeforeTime) {
        if (status == STATUS.FINISHED) throw new RuntimeException("used short constructor with finished trip");
        try {
            this.dbHelper = ((MyApplication)((Service)context).getApplication()).getDbHelper();
        } catch (ClassCastException e) {
            Log.e(e.getMessage(), " from KTrip");
            this.dbHelper = MyApplication.getStaticDbHelper();
        }


        this.odometerStart = odoStart;
        this.odometerEnd = odoEnd;
        this.distance = distance;
        this.carID = carID;
        this.parking = parking;
        this.traffic = traffic;
        this.weather = weather;
        this.roadTypeID = roadTypeID;
        this.timeOfDay = light;
        this.parentID = parentID;
        this.isNightTrip = isNight;
        this.status = status;
        this.subTripIDs = new ArrayList<>();
        this.order = order;

        this.totalDrivingBeforeTime = totalDrivingBeforeTime;
        this.totalNightBeforeTime = totalNightBeforeTime;

        this.context = context;
        this.timerHelper = new KTimerHelper(context, KTimerHelper.TYPE.SECOND,this);
        this.timerHelperMinute = new KTimerHelper(context, KTimerHelper.TYPE.MINUTE,this);

        this.elapsedTime = new KTime(Globals.ZERO_TIME);
        this.previousSubTripTotalTime = new KTime(Globals.ZERO_TIME);

        this._id = (int) dbHelper.newTrip(context, odoStart, odoEnd, distance, carID, parking, traffic, weather, roadTypeID,
                light, parentID, isNight, status,order);

        this.lcHelper = new LocationHelper(context,500,1000,"test action",
                ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"TimerDrivingWakelock"));
    }

    public KTrip(Context context,KTime realStartTime, KTime elapsedTime, boolean isNightTrip, int id){
        this.dbHelper = MyApplication.getStaticDbHelper();

        dbHelper.deleteAllSubtripsForTrip(id);

        this._id = id;
        Cursor c = dbHelper.getTripCursor(this._id);

        this.context = context;

        this.odometerStart = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ODO_START));
        this.odometerEnd = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ODO_END));
        this.distance = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_DISTANCE));
        this.carID = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_CAR_ID));
        this.parking = ConversionHelper.intToBool(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_PARKING)));
        this.traffic = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_TRAFFIC));
        this.weather = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_WEATHER));
        this.roadTypeID = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ROAD_TYPE));
        this.timeOfDay = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_LIGHT));
        this.parentID = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_PARENT));
        this.isNightTrip = isNightTrip;
        this.status = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_STATUS));
        this.order = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ORDER));

        this.totalDrivingBeforeTime = dbHelper.getDBTime(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_DRIVING_TOTAL_AFTER)));
        this.totalDrivingBeforeTime = this.totalDrivingBeforeTime != null ? this.totalDrivingBeforeTime : new KTime(Globals.ZERO_TIME);
        this.totalDrivingBeforeTime.id = 0;

        this.totalNightBeforeTime = dbHelper.getDBTime(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_NIGHT_TOTAL_AFTER)));
        this.totalNightBeforeTime = this.totalNightBeforeTime != null ? this.totalNightBeforeTime : new KTime(Globals.ZERO_TIME);
        this.totalNightBeforeTime.id = 0;

        c.close();

        this.timerHelper = new KTimerHelper(context, KTimerHelper.TYPE.SECOND,this);
        this.timerHelperMinute = new KTimerHelper(context, KTimerHelper.TYPE.MINUTE,this);

        this.lcHelper = new LocationHelper(context,500,1000,"test action",
                ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"TimerDrivingWakelock"));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lcHelper.loadLocationsFromDatabase(_id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        this.subTripIDs = new ArrayList<Integer>();
        this.elapsedTime = elapsedTime;
        this.previousSubTripTotalTime = elapsedTime;
        this.numOfSubtrips = 1;

        KSubTrip subTrip = new KSubTrip(this,realStartTime,elapsedTime,STATUS.FINISHED);
        this.subTripIDs.add((int)subTrip._id);


        this.realStartedTime = KTime.newDBTime(realStartTime,dbHelper);
        this.apparentStartTime = KTime.newDBTime(KTime.roundTimeToMinute(realStartedTime), dbHelper);

        this.status = STATUS.PAUSED;

        dbHelper.updateRunningTrip(_id, numOfSubtrips, realStartedTime.id, apparentStartTime.id, odometerStart, odometerEnd, distance, carID, parking, traffic,
                weather, roadTypeID, timeOfDay, parentID, isNightTrip, status, order);
    }

    @Deprecated
    public KTrip(ArrayList<KTime> subTrips, int status,
                 Boolean containsNightTrip, Boolean dayTime, Boolean dawnDusk,
                 int traffic, Boolean dryWeather, Boolean wetWeather, int id) {
        this.status = status;
       // this.containsNightTrip = containsNightTrip;
      //  this.dayTime = dayTime;
      //  this.dawnDusk = dawnDusk;
        this.traffic = traffic;
      //  this.dryWeather = dryWeather;
      //  this.wetWeather = wetWeather;
        this._id = id;
        Log.i(Globals.LOG, "creating ktrip with id:" + this._id + " status:" + this.status);
        if (status != 4) Log.i(Globals.LOG, "!!!! Should use long constructor with finished trips");
    }
    @Deprecated
    public KTrip(int status, Boolean shouldRunSecondTimer, Boolean shouldRunMinuteTimer, Context context) {
        this.realStartedTime = new KTime(Globals.NOW);
        this.status = status;
        this.shouldRunSecondTimer = shouldRunSecondTimer;
        this.shouldRunMinuteTimer = shouldRunMinuteTimer;
        this.elapsedTime = new KTime(Globals.ZERO_TIME);
        this.previousSubTripTotalTime = new KTime(Globals.ZERO_TIME);
        this.context = context;
        this.timerHelper = new KTimerHelper(context, KTimerHelper.TYPE.SECOND,this);
        this.timerHelperMinute = new KTimerHelper(context, KTimerHelper.TYPE.MINUTE,this);
        Log.i(Globals.LOG, "creating ktrip" + " with status:" + this.status);
    }
    @Deprecated
    public void setShouldRunSecondTimer(Boolean shouldRun) {
        shouldRunSecondTimer = shouldRun;
        if (secondTimer != null && !shouldRun) secondTimer.cancel();
        if (shouldRun) startSecondTimer();
    }
    @Deprecated
    public void setShouldRunMinuteTimer(Boolean shouldRun) {
        shouldRunMinuteTimer = shouldRun;
        if (minuteTimer != null && !shouldRun) minuteTimer.cancel();
        if (shouldRun) startMinuteTimer();
    }

    @Override
    public KTime returnElapsed() {
        updateElapsed();
        return elapsedTime;
    }


    public LocationHelper getLocationHelper() {
        return lcHelper;
    }


    public void start() {
        Log.i(Globals.LOG, "Starting trip from status:" + status);
        if (status == STATUS.NOT_RUNNING) {
            realStartedTime = KTime.newDBTime(new KTime(Globals.NOW), dbHelper);
            apparentStartTime = KTime.newDBTime(KTime.roundTimeToMinute(realStartedTime), dbHelper);

            currentSubTrip = new KSubTrip(this,context);
            subTripIDs.add((int)currentSubTrip._id);
            numOfSubtrips = subTripIDs.size();
            status = STATUS.RUNNING;
            currentSubTrip.start();

            timerHelper.subToQuery = currentSubTrip;

            dbHelper.updateTripSingleColumn(_id, DBHelper.TRIP.KEY_NUMOFSUBS, numOfSubtrips); //numOfSubs realStartID appaStartID status
            dbHelper.updateTripSingleColumn(_id, DBHelper.TRIP.KEY_REAl_START, realStartedTime.id);
            dbHelper.updateTripSingleColumn(_id, DBHelper.TRIP.KEY_APPA_START, apparentStartTime.id);
            dbHelper.updateTripSingleColumn(_id, DBHelper.TRIP.KEY_STATUS, status);
            Globals.currentTrip = this;
            Intent intent = new Intent(Globals.tripChangedStateBroadcast);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
        if (status == STATUS.PAUSED) {
            currentSubTrip = new KSubTrip(this, context);
            subTripIDs.add((int)currentSubTrip._id);
            numOfSubtrips = subTripIDs.size();
            status = STATUS.RUNNING;
            currentSubTrip.start();

            dbHelper.updateTripSingleColumn(_id, DBHelper.TRIP.KEY_NUMOFSUBS, numOfSubtrips);
            dbHelper.updateTripSingleColumn(_id, DBHelper.TRIP.KEY_STATUS, status);
            Intent intent = new Intent(Globals.tripChangedStateBroadcast);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        new Thread(new Runnable() {
            public void run() {
                timerHelper.subToQuery = currentSubTrip;
                timerHelperMinute.subToQuery = currentSubTrip;
                timerHelper.start();
                timerHelperMinute.start();

                lcHelper.finished = false;
                lcHelper.start(dbHelper);
            }
        }).start();
    }

    public void pause() {
        Log.i(Globals.LOG, "Pausing trip from status:" + status);
        if (status == STATUS.RUNNING) {
            currentSubTrip.stop();
            previousSubTripTotalTime = KTime.addTimes(previousSubTripTotalTime, currentSubTrip.totalTime);

            currentSubTrip = null;
            Intent intent = new Intent(Globals.tripChangedStateBroadcast);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
        timerHelper.stop();
        timerHelperMinute.stop();

        lcHelper.stop();

        status = STATUS.PAUSED;

    }

    public void stop() {
        if (status == STATUS.RUNNING) {
            currentSubTrip.stop();
            currentSubTrip = null;
            lcHelper.stop();
        }
        if (status == STATUS.RUNNING || status == STATUS.PAUSED) {
            timerHelper.stop();
            timerHelperMinute.stop();


            status = STATUS.FINISHED;
            dbHelper.updateTripSingleColumn(_id, DBHelper.TRIP.KEY_STATUS,status);
            Intent intent = new Intent(Globals.tripChangedStateBroadcast);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public void prepareForStop(){
        pause();

        new Thread(new Runnable() {
            @Override
            public void run() {
                lcHelper.finish();
            }
        }).start();

        realEndTime = KTime.newDBTime(new KTime(Globals.NOW), dbHelper);
        realTotalTime = new KTime(Globals.ZERO_TIME);
        Cursor cursor = dbHelper.getSubtripsFromTripID(_id);
        int totalTimeColumn = cursor.getColumnIndexOrThrow(DBHelper.SUB.KEY_TOTAL);
        Log.w("prep", "1");
        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                int totalTimeID = cursor.getInt(totalTimeColumn);
                Log.w("prep","id = " + totalTimeID);
                KTime total = dbHelper.getDBTime(totalTimeID);
                realTotalTime = KTime.addTimes(realTotalTime, total);
                i++;
            } while (cursor.moveToNext());
            numOfSubtrips = i;
            realTotalTime = KTime.newDBTime(realTotalTime,dbHelper);
        }
        cursor.close();

        apparentTotalTime = KTime.newDBTime(KTime.roundTimeToMinute(realTotalTime), dbHelper);
        apparentEndTime = KTime.addTimes(apparentStartTime, apparentTotalTime);
        if (apparentEndTime.hours > 24) {
            apparentEndTime.hours -= 24;
            apparentEndTime.date += 1;
        }
        apparentEndTime = KTime.newDBTime(apparentEndTime,dbHelper);

        updateTotalDrivingAfterTime();
        updateTotalNightAfterTime();

        Intent intent = new Intent(Globals.tripChangedStateBroadcast);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        if (dbHelper == null) throw new RuntimeException("dbhelper = null");
        Log.i(LOG, "stopping trip with total time:" + KTime.getProperReadable(realTotalTime, Globals.TIMEFORMAT.H_MM_SS));
        dbHelper.updateTrip(_id, numOfSubtrips, realStartedTime.id, realEndTime.id, realTotalTime.id, apparentStartTime.id, apparentEndTime.id, apparentTotalTime.id,
                odometerStart, odometerEnd, distance, carID, parking, traffic, weather,
                roadTypeID, timeOfDay, parentID, isNightTrip, status, totalDrivingAfterTime.id, totalNightAfterTime.id, order); ////TODO TOTALDRIVIGNAFTER
    }

    public void updateTotalDrivingAfterTime() {
        if (apparentTotalTime != null) {
            totalDrivingAfterTime = KTime.addTimes(totalDrivingBeforeTime, apparentTotalTime);
            totalDrivingAfterTime.seconds = 0;
            totalDrivingAfterTime = KTime.newDBTime(totalDrivingAfterTime, dbHelper);
        }
    }

    public void updateTotalNightAfterTime() {
        if (isNightTrip && apparentTotalTime != null) {
            totalNightAfterTime = KTime.addTimes(totalNightBeforeTime,apparentTotalTime);
            totalNightAfterTime.seconds = 0;
            totalNightAfterTime = KTime.newDBTime(totalNightAfterTime, dbHelper);
        }
        else {
            totalNightAfterTime = KTime.newDBTime(totalNightBeforeTime,dbHelper);
        }
    }

    public void reprepareForResume(){
        apparentEndTime = null;
        apparentTotalTime = null;
        realEndTime = null;
        realTotalTime = null;
        totalDrivingAfterTime = null;
        totalNightAfterTime = null;
        distance = 0;
        dbHelper.updateTripForResume(this._id, status);
    }

    public void updateAllColumnsInDatabase(){
        Log.w("updateAllColumns",""+_id+ numOfSubtrips+ realStartedTime.id+ realEndTime.id+ realTotalTime.id+ apparentStartTime.id+
                apparentEndTime.id+ apparentTotalTime.id+ odometerStart+ odometerEnd+ distance+ carID+ parking+
                traffic+ weather+ roadTypeID+ timeOfDay+ parentID+ isNightTrip+ status+ totalDrivingAfterTime.id+ totalNightAfterTime.id+ order);
        Boolean b = dbHelper.updateTrip(_id, numOfSubtrips, realStartedTime.id, realEndTime.id, realTotalTime.id, apparentStartTime.id,
                apparentEndTime.id, apparentTotalTime.id, odometerStart, odometerEnd, distance, carID, parking,
                traffic, weather, roadTypeID, timeOfDay, parentID, isNightTrip, status, totalDrivingAfterTime.id, totalNightAfterTime.id, order); //TODO TOTALDRIVIGNAFTER
        Log.e("e","b:"+b);
    }

    public void updateAllRunningColumnsInDB(){
        dbHelper.updateRunningTrip(_id, numOfSubtrips, realStartedTime.id, apparentStartTime.id, odometerStart, odometerEnd, distance, carID,
                parking, traffic, weather, roadTypeID, timeOfDay, parentID, isNightTrip, status, order);
    }

    public void addDistance(int dist) {
        distance += dist;
        MyApplication.getStaticDbHelper().updateTripSingleColumn(_id, DBHelper.TRIP.KEY_DISTANCE, distance);
        if (currentSubTrip != null) {
            currentSubTrip.distance += dist;
            MyApplication.getStaticDbHelper().updateSubTripSingleColumn((int) currentSubTrip._id, DBHelper.SUB.KEY_DISTANCE, currentSubTrip.distance);
        }

        Intent sendIntent = new Intent(Globals.distanceChangedBroadcast);
        LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent);
    }

    @Deprecated
    private void startSecondTimer() {
        secondTimer = new Timer();
        new Thread(new Runnable() {
            public void run() {
                secondTimer.scheduleAtFixedRate(new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        KTime now = new KTime(Globals.NOW);
                                                        if (currentSubTrip.latestTime.seconds != now.seconds) {
                                                            updateElapsed();
                                                            Log.i(LOG, "sending seconds:" +
                                                                    KTime.getProperReadable(elapsedTime, Globals.TIMEFORMAT.MM_SS_plusHoursIfAppl));

                                                            Intent intent = new Intent(Globals.defaultTimerUpdateBroadcastId);
                                                            intent.putExtra("elapsed", KTime.getProperReadable(elapsedTime, Globals.TIMEFORMAT.MM_SS_plusHoursIfAppl));
                                                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                                        }
                                                    }
                                                },
                        0,
                        500);

                }
            }).start();
    }

    @Deprecated
    private void startMinuteTimer() {
        Log.i(LOG, "starting minute timer");
        minuteTimer = new Timer();
        minuteTimer.scheduleAtFixedRate(new TimerTask() {
                         @Override
                         public void run() {
                         new Thread(new Runnable() {
                              public void run() {
                                  updateElapsed();
                                  Log.i(LOG, "sending minutes notification:" +
                                          KTime.getProperReadable(elapsedTime, Globals.TIMEFORMAT.H_MM_SS));
                                  Intent intent = new Intent(Globals.notificationTimerBroadcastId);
                                  intent.putExtra("minutes", KTime.getProperReadable(elapsedTime, Globals.TIMEFORMAT.H_MM));
                                  LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                               }
                         }).start();
                     }
                  },
                0,
                60000);
    }

    private void updateElapsed() {
        if (currentSubTrip != null) {
            KTime current = currentSubTrip.getElapsedTime();
            elapsedTime = KTime.addTimes(previousSubTripTotalTime, current);
        }
    }

    public KTime getCurrentTotalDriving() {
        if (totalDrivingBeforeTime != null) {
            KTime total = KTime.addTimes(totalDrivingBeforeTime, returnElapsed());
            total.seconds = 0;
            return total;
        }
        return null;
    }

    public KTime getCurrentTotalNightDriving() {
        if (totalNightBeforeTime != null) {
            if (isNightTrip) {
                KTime total = KTime.addTimes(totalNightBeforeTime, returnElapsed());
                total.seconds = 0;
                return total;
            }
            else return totalNightBeforeTime;
        }
        return null;
    }

    public void setContext(Context context){
        this.context = context;
        if (this.lcHelper != null) lcHelper.setContext(this.context);
        if (this.timerHelper != null) timerHelper.setContext(this.context);
        if (this.timerHelperMinute != null) timerHelperMinute.setContext(this.context);
    }

}
