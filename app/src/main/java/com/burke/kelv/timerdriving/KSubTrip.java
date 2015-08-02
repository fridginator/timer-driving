package com.burke.kelv.timerdriving;

import android.content.Context;
import android.util.Log;

/**
 * Created by kelv on 25/02/2015.
 */

public class KSubTrip implements KTimerHelper.TimerHelperInterface {
    public long _id;
    public int partOfTrip_id;

    public KTime startTime;
    public KTime endTime;
    public KTime totalTime;
    public KTime latestTime;
    public int distance;
    private KTime elapsedTime;
    public int status = KTrip.STATUS.NOT_INITIALISED;
    private Context context;
    DBHelper dbHelper;


    public KSubTrip() {
        //empty
    }

    public KSubTrip(KTrip tripToBePartOf, Context context) {
        this.partOfTrip_id = tripToBePartOf._id;
        this.latestTime = new KTime(Globals.NOW);
        this.distance = 0;
        this.status = KTrip.STATUS.NOT_RUNNING;
        this.context = context;

        dbHelper = MyApplication.getStaticDbHelper();
        this.elapsedTime = KTime.newDBTime(new KTime(Globals.ZERO_TIME),dbHelper);
        this.startTime = KTime.newDBTime(new KTime(Globals.NOW), dbHelper);
        this._id = dbHelper.newSubTrip(tripToBePartOf,this.startTime,this.status,this.elapsedTime);
        Log.i(Globals.LOG, "new subtrip with id:"+_id);
    }

    public KSubTrip(KTrip tripToBePartOf, KTime startTime, KTime totalTime,int status) {
        if (status != KTrip.STATUS.FINISHED) throw new RuntimeException("dont make unfinished subtrip with finished constructor");
        this.partOfTrip_id = tripToBePartOf._id;
        this.latestTime = KTime.addTimes(startTime,totalTime);
        this.distance = 0;
        this.status = KTrip.STATUS.FINISHED;

        dbHelper = MyApplication.getStaticDbHelper();
        this.elapsedTime = KTime.newDBTime(totalTime,dbHelper);
        this.totalTime = KTime.newDBTime(totalTime,dbHelper);
        this.startTime = KTime.newDBTime(startTime, dbHelper);
        this.endTime = KTime.newDBTime(latestTime,dbHelper);
        this._id = dbHelper.newSubTrip(tripToBePartOf,this.startTime,this.status,this.elapsedTime);
        dbHelper.updateSubTrip((int)_id,partOfTrip_id,startTime.id,endTime.id,this.totalTime.id,distance,this.status,elapsedTime.id);
        Log.i(Globals.LOG, "new subtrip with id:"+_id);
    }

    public KSubTrip(int id, int parentTripID, int startTimeID, int endTimeID, int totalTimeID, int distance, int status) {
        // TODO get Times sqlite

    }


    @Override
    public KTime returnElapsed() {
        return getElapsedTime();
    }

    public void start() {
        status = KTrip.STATUS.RUNNING;
        dbHelper.updateSubTripSingleColumn((int)_id,DBHelper.SUB.KEY_STATUS,status);
    }

    public KSubTrip stop() {
        updateElapsed();
        status = KTrip.STATUS.FINISHED;
        totalTime = KTime.newDBTime(elapsedTime, dbHelper);
        endTime = KTime.newDBTime(new KTime(Globals.NOW),dbHelper);
        dbHelper.updateSubTrip((int)_id, partOfTrip_id, startTime.id, endTime.id, totalTime.id, distance, status, elapsedTime.id);
        dbHelper = null;
        return this;
    }

    public void updateElapsed() {
        int id = elapsedTime.id;
        if (status == KTrip.STATUS.RUNNING) {
            latestTime = new KTime(Globals.NOW);
        }
        elapsedTime = KTime.getDiffBetweenTimes(latestTime, startTime);
        elapsedTime.id = id;
        if (dbHelper != null) {
            new Thread(new Runnable() {
                public void run() {
                    dbHelper.updateTime(elapsedTime);
                }
            }).start();
        }
        else {
            dbHelper = MyApplication.getStaticDbHelper();
            new Thread(new Runnable() {
                public void run() {
                    dbHelper.updateTime(elapsedTime);
                }
            }).start();
        }
    }

    public KTime getElapsedTime() {
        updateElapsed();
        return this.elapsedTime;
    }
}
