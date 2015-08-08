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
        this.elapsedTime = new KTime(Globals.ZERO_TIME);
        this.startTime = new KTime(Globals.NOW);
        this._id = dbHelper.newSubTrip(tripToBePartOf,this.startTime,this.status,this.elapsedTime);
        Log.i(Globals.LOG, "new subtrip with id:"+_id);
    }

    public KSubTrip(KTrip tripToBePartOf, KTime startTime, KTime totalTime,int status) {
        if (status != KTrip.STATUS.FINISHED) throw new RuntimeException("dont make unfinished subtrip with finished constructor");
        this.partOfTrip_id = tripToBePartOf._id;
        this.latestTime = KTime.addTimes(startTime,totalTime);
        this.latestTime.date = startTime.date;
        this.latestTime.month = startTime.month;
        this.latestTime.year = startTime.year;
        this.distance = 0;
        this.status = KTrip.STATUS.FINISHED;

        dbHelper = MyApplication.getStaticDbHelper();
        this.elapsedTime = totalTime;
        this.totalTime = totalTime;
        this.startTime = startTime;
        this.endTime = latestTime;
        this._id = dbHelper.newSubTrip(tripToBePartOf,this.startTime,this.status,this.elapsedTime);
        dbHelper.updateSubTrip((int)_id,partOfTrip_id,startTime.toIntSeconds(),endTime.toIntSeconds(),
                this.totalTime.toIntSeconds(),distance,this.status,elapsedTime.toIntSeconds());
        Log.i(Globals.LOG, "new subtrip with id:"+_id);
    }

    public KSubTrip(int id, int parentTripID, int startTimeID, int endTimeID, int totalTimeID, int distance, int status) {
        // TODO get Times sqlite (idk what this means anymore)

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
        totalTime = elapsedTime;
        endTime = new KTime(Globals.NOW);
        dbHelper.updateSubTrip((int)_id, partOfTrip_id, startTime.toIntSeconds(), endTime.toIntSeconds(),
                totalTime.toIntSeconds(), distance, status, elapsedTime.toIntSeconds());
        dbHelper = null;
        return this;
    }

    public void updateElapsed() {
        if (status == KTrip.STATUS.RUNNING) {
            latestTime = new KTime(Globals.NOW);
        }
        elapsedTime = KTime.getDiffBetweenTimes(latestTime, startTime);
        dbHelper = MyApplication.getStaticDbHelper();
        dbHelper.updateSubTripSingleColumn((int)_id, DBHelper.SUB.KEY_ELAPSED,elapsedTime.toIntSeconds());
    }

    public KTime getElapsedTime() {
        updateElapsed();
        return this.elapsedTime;
    }
}
