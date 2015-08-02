package com.burke.kelv.timerdriving;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.widget.GridLayout;

import java.util.ArrayList;
import java.util.List;

public class DBHelper {

    private static final String TAG = "DBHelper"; //used for logging database version changes

    public static final String ASCENDING = "ASC";
    public static final String DESCENDING = "DESC";

    // Field Names:
    public static interface TRIP {
        public static final String KEY_ROWID = "_id";
        public static final String KEY_NUMOFSUBS = "numberOfSubs";
        public static final String KEY_REAl_START = "realStartTimeID";     //contains id of time
        public static final String KEY_REAL_END = "realEndTimeID";         //contains id of time
        public static final String KEY_REAL_TOTAL = "totalRealTime";       //contains id of time
        public static final String KEY_APPA_START = "apparentStartTimeID"; //contains id of time
        public static final String KEY_APPA_END = "apparentEndTimeID";     //contains id of time
        public static final String KEY_APPA_TOTAL = "totalApparentTime";   //contains id of time
        public static final String KEY_ODO_START = "odometerStart";
        public static final String KEY_ODO_END = "odometerEnd";
        public static final String KEY_DISTANCE = "distanceTravelled";
        public static final String KEY_CAR_ID = "carUsedID";
        public static final String KEY_PARKING = "parked";
        public static final String KEY_TRAFFIC = "traffic";
        public static final String KEY_WEATHER = "weather";
        public static final String KEY_ROAD_TYPE = "roadTypeID";
        public static final String KEY_LIGHT = "lightOrTOD";
        public static final String KEY_PARENT = "accompanyingDriverID";
        public static final String KEY_IS_NIGHT = "isNightTrip";
        public static final String KEY_STATUS = "tripStatus";
        public static final String KEY_DRIVING_TOTAL_AFTER = "totalDrivingAfter";
        public static final String KEY_NIGHT_TOTAL_AFTER = "nightDrivingAfter";
        public static final String KEY_ORDER = "tripOrdr";

        public static final String[] ALL_KEYS = new String[] { KEY_ROWID, KEY_NUMOFSUBS, KEY_REAl_START, KEY_REAL_END, KEY_REAL_TOTAL, KEY_APPA_START,
                KEY_APPA_END, KEY_APPA_TOTAL, KEY_ODO_START, KEY_ODO_END, KEY_DISTANCE, KEY_CAR_ID, KEY_PARKING, KEY_TRAFFIC, KEY_WEATHER,
                KEY_ROAD_TYPE, KEY_LIGHT, KEY_PARENT, KEY_IS_NIGHT, KEY_STATUS , KEY_DRIVING_TOTAL_AFTER, KEY_NIGHT_TOTAL_AFTER, KEY_ORDER };

        public static final String DATABASE_TABLE = "Trips";

        public static final String TABLE_CREATE_SQL =
                "CREATE TABLE " + DATABASE_TABLE
                        + " (" + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + KEY_NUMOFSUBS + " INTEGER, "
                        + KEY_REAl_START + " INTEGER, "
                        + KEY_REAL_END + " INTEGER, "
                        + KEY_REAL_TOTAL + " INTEGER, "
                        + KEY_APPA_START + " INTEGER, "
                        + KEY_APPA_END + " INTEGER, "
                        + KEY_APPA_TOTAL + " INTEGER, "
                        + KEY_ODO_START + " INTEGER, "
                        + KEY_ODO_END + " INTEGER, "
                        + KEY_DISTANCE + " INTEGER, "
                        + KEY_CAR_ID + " INTEGER, "
                        + KEY_PARKING + " INTEGER, "
                        + KEY_TRAFFIC + " INTEGER, "
                        + KEY_WEATHER + " INTEGER, "
                        + KEY_ROAD_TYPE + " INTEGER, "
                        + KEY_LIGHT + " INTEGER, "
                        + KEY_PARENT + " INTEGER, "
                        + KEY_IS_NIGHT + " INTEGER, "
                        + KEY_STATUS + " INTEGER, "
                        + KEY_DRIVING_TOTAL_AFTER + " INTEGER, "
                        + KEY_NIGHT_TOTAL_AFTER + " INTEGER, "
                        + KEY_ORDER + " INTEGER"
                        + ");";
    }
    public static interface SUB {
        public static final String KEY_ROWID = "_id";
        public static final String KEY_PARENT_TRIP = "parentTripID";
        public static final String KEY_START = "StartTimeID";     //contains id of time
        public static final String KEY_END = "EndTimeID";         //contains id of time
        public static final String KEY_TOTAL = "totalTimeID";       //contains id of time
        public static final String KEY_DISTANCE = "distance";
        public static final String KEY_STATUS = "subTripStatus";
        public static final String KEY_ELAPSED = "elapsedTimeID";


        public static final String[] ALL_KEYS = new String[] { KEY_ROWID, KEY_PARENT_TRIP, KEY_START, KEY_END, KEY_TOTAL, KEY_DISTANCE, KEY_STATUS, KEY_ELAPSED };

        public static final String DATABASE_TABLE = "SubTrips";

        public static final String TABLE_CREATE_SQL =
                "CREATE TABLE " + DATABASE_TABLE
                        + " (" + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + KEY_PARENT_TRIP + " INTEGER, "
                        + KEY_START + " INTEGER, "
                        + KEY_END + " INTEGER, "
                        + KEY_TOTAL + " INTEGER, "
                        + KEY_DISTANCE + " INTEGER, "
                        + KEY_STATUS + " INTEGER, "
                        + KEY_ELAPSED + " INTEGER"
                        + ");";
    }
    public static interface TIME {
        public static final String KEY_ROWID = "_id";
        public static final String KEY_HOURS = "hours";
        public static final String KEY_MINUTES = "minutes";     //contains id of time
        public static final String KEY_SECONDS = "seconds";         //contains id of time
        public static final String KEY_DAYOFWEEK = "dayOfWeek";       //contains id of time
        public static final String KEY_DATE = "daate";
        public static final String KEY_MONTH = "month";
        public static final String KEY_YEAR = "year";
        public static final String KEY_READABLE = "readableStr";


        public static final String[] ALL_KEYS = new String[] { KEY_ROWID, KEY_HOURS, KEY_MINUTES, KEY_SECONDS, KEY_DAYOFWEEK, KEY_DATE, KEY_MONTH, KEY_YEAR, KEY_READABLE };

        public static final String DATABASE_TABLE = "Times";

        public static final String TABLE_CREATE_SQL =
                "CREATE TABLE " + DATABASE_TABLE
                        + " (" + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + KEY_HOURS + " INTEGER, "
                        + KEY_MINUTES + " INTEGER, "
                        + KEY_SECONDS + " INTEGER, "
                        + KEY_DAYOFWEEK + " INTEGER, "
                        + KEY_DATE + " INTEGER, "
                        + KEY_MONTH + " INTEGER, "
                        + KEY_YEAR + " INTEGER, "
                        + KEY_READABLE + " TEXT"
                        + ");";
    }
    public static interface LOCATION {
        public static final String KEY_ROWID = "_id";
        public static final String KEY_LATITUDE = "lat";
        public static final String KEY_LONGITUDE = "longitude";     //contains id of time
        public static final String KEY_PROVIDER = "provider";
        public static final String KEY_ACCURACY = "accuracy"; //contains id of time
        public static final String KEY_SPEED = "speed";       //contains id of time
        public static final String KEY_TRIP = "trip";
        public static final String KEY_NUMBER = "number";

        public static final String[] ALL_KEYS = new String[] { KEY_ROWID, KEY_LATITUDE, KEY_LONGITUDE, KEY_PROVIDER, KEY_ACCURACY, KEY_SPEED, KEY_TRIP, KEY_NUMBER};

        public static final String DATABASE_TABLE = "Locations";

        public static final String TABLE_CREATE_SQL =
                "CREATE TABLE " + DATABASE_TABLE
                        + " (" + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + KEY_LATITUDE + " REAL, "
                        + KEY_LONGITUDE + " REAL, "
                        + KEY_PROVIDER + " TEXT, "
                        + KEY_ACCURACY + " REAL, "
                        + KEY_SPEED + " REAL, "
                        + KEY_TRIP + " INTEGER, "
                        + KEY_NUMBER + " INTEGER"
                        + ");";
    }

    public long insertLocation(double latitude, double longitude, String provider, float accuracy,
                               float speed, int tripId, int number){
        ContentValues initialValues = new ContentValues();
        initialValues.put(LOCATION.KEY_LATITUDE, latitude);
        initialValues.put(LOCATION.KEY_LONGITUDE, longitude);
        initialValues.put(LOCATION.KEY_PROVIDER, provider);
        initialValues.put(LOCATION.KEY_ACCURACY, accuracy);
        initialValues.put(LOCATION.KEY_SPEED, speed);
        initialValues.put(LOCATION.KEY_TRIP, tripId);
        initialValues.put(LOCATION.KEY_NUMBER, number);

        return db.insert(LOCATION.DATABASE_TABLE, null, initialValues);
    }

    // DataBase info:
    public static final String DATABASE_NAME = "DefaultSavesDatabase";

    public static final int DATABASE_VERSION = 7; // The version number must be incremented each time a change to DB structure occurs.

    private final Context context;
    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;


    public DBHelper(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public DBHelper open() {
        Log.w(Globals.LOG, "Opened new database connection");
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        Log.w(Globals.LOG, "Closed database connection");
        myDBHelper.close();
    }

    public long newTrip(Context context, int odoStart, int odoEnd, int distance, int carID, boolean parking, int traffic, int weather, int roadTypeID,
                        int light, int parentID, boolean isNight, int status, int order) {
        int park = ConversionHelper.boolToInt(parking);
        int night = ConversionHelper.boolToInt(isNight);
        Log.i(Globals.LOG,"night:" + night);

        ContentValues initialValues = new ContentValues();
        initialValues.put(TRIP.KEY_ODO_START, odoStart);
        initialValues.put(TRIP.KEY_ODO_END, odoEnd);
        initialValues.put(TRIP.KEY_DISTANCE, distance);
        initialValues.put(TRIP.KEY_CAR_ID, carID);
        initialValues.put(TRIP.KEY_PARKING, park);
        initialValues.put(TRIP.KEY_TRAFFIC, traffic);
        initialValues.put(TRIP.KEY_WEATHER, weather);
        initialValues.put(TRIP.KEY_ROAD_TYPE, roadTypeID);
        initialValues.put(TRIP.KEY_LIGHT, light);
        initialValues.put(TRIP.KEY_PARENT, parentID);
        initialValues.put(TRIP.KEY_IS_NIGHT, night);
        initialValues.put(TRIP.KEY_STATUS, status);
        initialValues.put(TRIP.KEY_ORDER, order);

        // Insert the data into the database.
        return db.insert(TRIP.DATABASE_TABLE, null, initialValues);
    }

    // Add a new set of values to be inserted into the database.
    public long insertTrip(int numOfSubs, int realStartID, int realEndID, int realTotalID, int appaStartID, int appaEndID, int appaTotalID,
                           int odoStart, int odoEnd, int distance, int carID, boolean parking, int traffic, int weather, int roadTypeID,
                           int light, int parentID, boolean isNight, int status, int drivingTotalAfterID, int nightTotalAfterID, int order) {
        int park = ConversionHelper.boolToInt(parking);
        int night = ConversionHelper.boolToInt(isNight);

        ContentValues initialValues = new ContentValues();
        initialValues.put(TRIP.KEY_NUMOFSUBS, numOfSubs);
        initialValues.put(TRIP.KEY_REAl_START, realStartID);
        initialValues.put(TRIP.KEY_REAL_END, realEndID);
        initialValues.put(TRIP.KEY_REAL_TOTAL, realTotalID);
        initialValues.put(TRIP.KEY_APPA_START, appaStartID);
        initialValues.put(TRIP.KEY_APPA_END, appaEndID);
        initialValues.put(TRIP.KEY_APPA_TOTAL, appaTotalID);
        initialValues.put(TRIP.KEY_ODO_START, odoStart);
        initialValues.put(TRIP.KEY_ODO_END, odoEnd);
        initialValues.put(TRIP.KEY_DISTANCE, distance);
        initialValues.put(TRIP.KEY_CAR_ID, carID);
        initialValues.put(TRIP.KEY_PARKING, park);
        initialValues.put(TRIP.KEY_TRAFFIC, traffic);
        initialValues.put(TRIP.KEY_WEATHER, weather);
        initialValues.put(TRIP.KEY_ROAD_TYPE, roadTypeID);
        initialValues.put(TRIP.KEY_LIGHT, light);
        initialValues.put(TRIP.KEY_PARENT, parentID);
        initialValues.put(TRIP.KEY_IS_NIGHT, night);
        initialValues.put(TRIP.KEY_STATUS, status);
        initialValues.put(TRIP.KEY_DRIVING_TOTAL_AFTER, drivingTotalAfterID);
        initialValues.put(TRIP.KEY_NIGHT_TOTAL_AFTER, nightTotalAfterID);
        initialValues.put(TRIP.KEY_ORDER, order);

        // Insert the data into the database.
        return db.insert(TRIP.DATABASE_TABLE, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteTrip(long tripId) {
        String where = TRIP.KEY_ROWID + "=" + tripId;
        return db.delete(TRIP.DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAllTrips() {
        Cursor c = getAllTrips();
        long rowId = c.getColumnIndexOrThrow(TRIP.KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteTrip(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Return all data in the database.
    public Cursor getAllTrips() {
        String where = null;
        Cursor c = 	db.query(true, TRIP.DATABASE_TABLE, TRIP.ALL_KEYS, where, null, null, null, null, null);
        Globals.noOfCursors++; Log.i(Globals.LOG, "New Cursor A:"+Globals.noOfCursors);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Get a specific row (by rowId)
    public Cursor getTripCursor(long rowId) {
        String where = TRIP.KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, TRIP.DATABASE_TABLE, TRIP.ALL_KEYS,
                where, null, null, null, null, null);
        Globals.noOfCursors++; Log.i(Globals.LOG, "New Cursor:"+Globals.noOfCursors);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getTripCursor(String columnName, int value) {
        String where = columnName + "=" + value;
        Cursor c = 	db.query(true, TRIP.DATABASE_TABLE, TRIP.ALL_KEYS,
                where, null, null, null, null, null);
        Globals.noOfCursors++; Log.i(Globals.LOG, "New Cursor:"+Globals.noOfCursors);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Change an existing row to be equal to new data.
    public boolean updateTrip(long rowId, int numOfSubs, int realStartID, int realEndID, int realTotalID, int appaStartID, int appaEndID, int appaTotalID,
                             int odoStart, int odoEnd, int distance, int carID, boolean parking, int traffic, int weather, int roadTypeID,
                             int light, int parentID, boolean isNight, int status, int drivingTotalAfterID, int nightTotalAfterID, int order) {
        String where = TRIP.KEY_ROWID + "=" + rowId;
        int park = ConversionHelper.boolToInt(parking);
        int night = ConversionHelper.boolToInt(isNight);

        ContentValues newValues = new ContentValues();
        newValues.put(TRIP.KEY_NUMOFSUBS, numOfSubs);
        newValues.put(TRIP.KEY_REAl_START, realStartID);
        newValues.put(TRIP.KEY_REAL_END, realEndID);
        newValues.put(TRIP.KEY_REAL_TOTAL, realTotalID);
        newValues.put(TRIP.KEY_APPA_START, appaStartID);
        newValues.put(TRIP.KEY_APPA_END, appaEndID);
        newValues.put(TRIP.KEY_APPA_TOTAL, appaTotalID);
        newValues.put(TRIP.KEY_ODO_START, odoStart);
        newValues.put(TRIP.KEY_ODO_END, odoEnd);
        newValues.put(TRIP.KEY_DISTANCE, distance);
        newValues.put(TRIP.KEY_CAR_ID, carID);
        newValues.put(TRIP.KEY_PARKING, park);
        newValues.put(TRIP.KEY_TRAFFIC, traffic);
        newValues.put(TRIP.KEY_WEATHER, weather);
        newValues.put(TRIP.KEY_ROAD_TYPE, roadTypeID);
        newValues.put(TRIP.KEY_LIGHT, light);
        newValues.put(TRIP.KEY_PARENT, parentID);
        newValues.put(TRIP.KEY_IS_NIGHT, night);
        newValues.put(TRIP.KEY_STATUS, status);
        newValues.put(TRIP.KEY_DRIVING_TOTAL_AFTER, drivingTotalAfterID);
        newValues.put(TRIP.KEY_NIGHT_TOTAL_AFTER, nightTotalAfterID);
        newValues.put(TRIP.KEY_ORDER, order);

        // Insert it into the database.
        return db.update(TRIP.DATABASE_TABLE, newValues, where, null) != 0;
    }

    public boolean updateRunningTrip(long rowId, int numOfSubs, int realStartID, int appaStartID,
                              int odoStart, int odoEnd, int distance, int carID, boolean parking, int traffic, int weather, int roadTypeID,
                              int light, int parentID, boolean isNight, int status, int order) {
        String where = TRIP.KEY_ROWID + "=" + rowId;
        int park = ConversionHelper.boolToInt(parking);
        int night = ConversionHelper.boolToInt(isNight);

        ContentValues newValues = new ContentValues();
        newValues.put(TRIP.KEY_NUMOFSUBS, numOfSubs);
        newValues.put(TRIP.KEY_REAl_START, realStartID);
        newValues.put(TRIP.KEY_APPA_START, appaStartID);
        newValues.put(TRIP.KEY_ODO_START, odoStart);
        newValues.put(TRIP.KEY_ODO_END, odoEnd);
        newValues.put(TRIP.KEY_DISTANCE, distance);
        newValues.put(TRIP.KEY_CAR_ID, carID);
        newValues.put(TRIP.KEY_PARKING, park);
        newValues.put(TRIP.KEY_TRAFFIC, traffic);
        newValues.put(TRIP.KEY_WEATHER, weather);
        newValues.put(TRIP.KEY_ROAD_TYPE, roadTypeID);
        newValues.put(TRIP.KEY_LIGHT, light);
        newValues.put(TRIP.KEY_PARENT, parentID);
        newValues.put(TRIP.KEY_IS_NIGHT, night);
        newValues.put(TRIP.KEY_STATUS, status);
        newValues.put(TRIP.KEY_ORDER, order);

        // Insert it into the database.
        return db.update(TRIP.DATABASE_TABLE, newValues, where, null) != 0;
    }

    public boolean updateTripForResume(long tripID, int status) {
        String where = TRIP.KEY_ROWID + "=" + tripID;
        Integer nuller = null;

        ContentValues newValues = new ContentValues();
        newValues.put(TRIP.KEY_REAL_END, nuller);
        newValues.put(TRIP.KEY_REAL_TOTAL, nuller);
        newValues.put(TRIP.KEY_APPA_END, nuller);
        newValues.put(TRIP.KEY_APPA_TOTAL, nuller);
        newValues.put(TRIP.KEY_DISTANCE, nuller);
        newValues.put(TRIP.KEY_DRIVING_TOTAL_AFTER, nuller);
        newValues.put(TRIP.KEY_NIGHT_TOTAL_AFTER, nuller);
        newValues.put(TRIP.KEY_STATUS, status);

        return db.update(TRIP.DATABASE_TABLE, newValues, where, null) != 0;
    }

    public boolean updateTripSingleColumn(long rowID, String columnKey, int value) {
        //Log.i(Globals.LOG, "updateTripSingleColumn:" + rowID +", " + columnKey + ", " + value);
        String where = TRIP.KEY_ROWID + "=" + rowID;
        ContentValues newValues = new ContentValues();
        newValues.put(columnKey, value);

        return db.update(TRIP.DATABASE_TABLE, newValues, where, null) != 0;
    }

    public Boolean tripIsNightTrip(int tripID) {
        Cursor c = getTripCursor(tripID);
        Globals.noOfCursors++; Log.i(Globals.LOG, "New Cursor:"+Globals.noOfCursors);
        Boolean isNight;
        if (c.moveToFirst()) {
            isNight = ConversionHelper.intToBool(c.getInt(c.getColumnIndexOrThrow(TRIP.KEY_IS_NIGHT)));
        } else return null;
        c.close();
        return isNight;
    }

    public long newSubTrip(KTrip parentTrip, KTime startTime, int status, KTime elapsedTime) {
        ContentValues values = new ContentValues();
        values.put(SUB.KEY_PARENT_TRIP, parentTrip._id);
        values.put(SUB.KEY_START, startTime.id);
        values.put(SUB.KEY_STATUS, status);
        values.put(SUB.KEY_ELAPSED, elapsedTime.id);

        return db.insert(SUB.DATABASE_TABLE, null, values);
    }


    public long insertSubTrip(int parentTripId, int startTimeId, int endTimeId, int totalTimeId, int distance, int status, int elapsedTimeId) {
        ContentValues values = new ContentValues();
        values.put(SUB.KEY_PARENT_TRIP, parentTripId);
        values.put(SUB.KEY_START, startTimeId);
        values.put(SUB.KEY_END, endTimeId);
        values.put(SUB.KEY_TOTAL, totalTimeId);
        values.put(SUB.KEY_DISTANCE, distance);
        values.put(SUB.KEY_STATUS, status);
        values.put(SUB.KEY_ELAPSED, elapsedTimeId);

        return db.insert(SUB.DATABASE_TABLE, null, values);
    }

    public boolean updateSubTrip(int id, int parentTripId, int startTimeId, int endTimeId, int totalTimeId, int distance, int status, int elapsedTimeId) {
        String where = SUB.KEY_ROWID + "=" + id;
        ContentValues values = new ContentValues();
        values.put(SUB.KEY_PARENT_TRIP, parentTripId);
        values.put(SUB.KEY_START, startTimeId);
        values.put(SUB.KEY_END, endTimeId);
        values.put(SUB.KEY_TOTAL, totalTimeId);
        values.put(SUB.KEY_DISTANCE, distance);
        values.put(SUB.KEY_STATUS, status);
        values.put(SUB.KEY_ELAPSED, elapsedTimeId);

        return db.update(SUB.DATABASE_TABLE, values, where, null) != 0;
    }

    public boolean updateSubTripSingleColumn(int subTripID, String columnKey, int value) {
        ContentValues newValues = new ContentValues();
        String where = SUB.KEY_ROWID + "=" + subTripID;
        newValues.put(columnKey, value);

        return db.update(SUB.DATABASE_TABLE, newValues, where, null) != 0;
    }

    public boolean deleteSubTrip(long subtripId) {
        String where = SUB.KEY_ROWID + "=" + subtripId;
        return db.delete(SUB.DATABASE_TABLE, where, null) != 0;
    }

    public Cursor getLastTrip() {
        Cursor c = getAllTrips(TRIP.KEY_ROWID,DESCENDING);
        if (c != null) {
            if (!c.moveToFirst()) return null;
        }
        return c;
    }

    public Cursor getAllTrips(String orderByColumn, String ascDesc){
        String where = null;
        String orderBy = orderByColumn + " " + ascDesc;
        Cursor c = db.query(true,TRIP.DATABASE_TABLE,TRIP.ALL_KEYS,where,null,null,null,orderBy,null,null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getSubtripsFromTripID(long tripID) {
        String where = SUB.KEY_PARENT_TRIP + "=" + tripID;
        Cursor c = db.query(true, SUB.DATABASE_TABLE, SUB.ALL_KEYS,
                where, null, null, null, null, null);
        Globals.noOfCursors++; Log.i(Globals.LOG, "New Cursor B:"+Globals.noOfCursors);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getSubTripCursor(long subtripId) {
        String where = SUB.KEY_ROWID + "=" + subtripId;
        Cursor c = db.query(true, SUB.DATABASE_TABLE, SUB.ALL_KEYS,
                where, null, null, null, null, null);
        Globals.noOfCursors++; Log.i(Globals.LOG, "New Cursor:"+Globals.noOfCursors);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public long insertTime(int hours, int minutes, int seconds, int dow, int date, int month, int year, String readableString) {
        ContentValues values = new ContentValues();
        values.put(TIME.KEY_HOURS, hours);
        values.put(TIME.KEY_MINUTES, minutes);
        values.put(TIME.KEY_SECONDS, seconds);
        values.put(TIME.KEY_DAYOFWEEK, dow);
        values.put(TIME.KEY_DATE, date);
        values.put(TIME.KEY_MONTH, month);
        values.put(TIME.KEY_YEAR, year);
        values.put(TIME.KEY_READABLE, readableString);

        return db.insert(TIME.DATABASE_TABLE, null, values);
    }

    public boolean updateTime(KTime time) {
        String where = TIME.KEY_ROWID + "=" + time.id;
        ContentValues values = new ContentValues();
        values.put(TIME.KEY_HOURS, time.hours);
        values.put(TIME.KEY_MINUTES, time.minutes);
        values.put(TIME.KEY_SECONDS, time.seconds);
        values.put(TIME.KEY_DAYOFWEEK, time.dayOfWeek);
        values.put(TIME.KEY_DATE, time.date);
        values.put(TIME.KEY_MONTH, time.month);
        values.put(TIME.KEY_YEAR, time.year);
        values.put(TIME.KEY_READABLE, KTime.getProperReadable(time,Globals.TIMEFORMAT.HH_MM_SS));
        return db.update(TIME.DATABASE_TABLE, values, where, null) != 0;
    }

    public Cursor getTimeCursor(long rowId) {
        if (rowId != 0) {
            String where = TIME.KEY_ROWID + "=" + rowId;
            Cursor c = db.query(true, TIME.DATABASE_TABLE, TIME.ALL_KEYS,
                    where, null, null, null, null, null);
            //Globals.noOfCursors++; Log.i(Globals.LOG, "New Cursor C:"+Globals.noOfCursors);
            if (c != null) {
                c.moveToFirst();
            }
            return c;
        }
        return null;
    }

    public KTime getDBTime(long id) {
        KTime time = new KTime(Globals.ZERO_TIME);
        Cursor c = getTimeCursor(id);
        if (c != null && c.moveToFirst()) {
            time.id = (int) id;
            time.hours = c.getInt(c.getColumnIndex(TIME.KEY_HOURS));
            time.minutes = c.getInt(c.getColumnIndex(TIME.KEY_MINUTES));
            time.seconds = c.getInt(c.getColumnIndex(TIME.KEY_SECONDS));
            time.dayOfWeek = c.getInt(c.getColumnIndex(TIME.KEY_DAYOFWEEK));
            time.date = c.getInt(c.getColumnIndex(TIME.KEY_DATE));
            time.month = c.getInt(c.getColumnIndex(TIME.KEY_MONTH));
            time.year = c.getInt(c.getColumnIndex(TIME.KEY_YEAR));
            time.readable = c.getString(c.getColumnIndex(TIME.KEY_READABLE));
            c.close();
            return time;
        }
        return null;
    }

    public Location getDBLocation(long tripID, int number, String provider){
        Location location = null;

        String where = LOCATION.KEY_TRIP + "=" + tripID +
                " AND " + LOCATION.KEY_NUMBER + "=" + number +
                " AND " + LOCATION.KEY_PROVIDER + "='" + provider + "'";
        Cursor c = 	db.query(true, LOCATION.DATABASE_TABLE, LOCATION.ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                c.moveToFirst();
                location = new Location(provider);
                location.setLatitude(c.getDouble(c.getColumnIndexOrThrow(LOCATION.KEY_LATITUDE)));
                location.setLongitude(c.getDouble(c.getColumnIndexOrThrow(LOCATION.KEY_LONGITUDE)));
                location.setAccuracy(c.getFloat(c.getColumnIndexOrThrow(LOCATION.KEY_ACCURACY)));
                location.setSpeed(c.getFloat(c.getColumnIndexOrThrow(LOCATION.KEY_SPEED)));
            }
            c.close();
        }
        return location;
    }

    public Cursor getDBLocations(long tripID, String provider, String orderByColumn, String ascDesc){
        String where = LOCATION.KEY_TRIP + "=" + tripID +
                " AND " + LOCATION.KEY_PROVIDER + "='" + provider + "'";
        String orderBy = orderByColumn + " " + ascDesc;
        Cursor c = 	db.query(true, LOCATION.DATABASE_TABLE, LOCATION.ALL_KEYS,
                where, null, null, orderBy, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }


    public KTime getTotalAllDrivingTime() {
        KTime time = new KTime(Globals.ZERO_TIME);
        Cursor c = db.query(true, TRIP.DATABASE_TABLE, TRIP.ALL_KEYS, null, null, null, null, null, null);
        Globals.noOfCursors++; Log.i(Globals.LOG, "New Cursor:"+Globals.noOfCursors);
        int index = c.getColumnIndexOrThrow(TRIP.KEY_APPA_TOTAL);
        if (c.moveToFirst()) {
            do {
                Integer id = (int) c.getLong(index);
                if (id != null && id != 0) {
                    KTime time1 = getDBTime(id);
                    time = KTime.addTimes(time, time1);
                }
            } while (c.moveToNext());
        }
        c.close();
        return time;
    }

    public Cursor getLastFinishedTrip(int currentTripID) {
        int id = currentTripID;
        Cursor cursor = null;
        boolean looping = true;
        while (looping) {
            id = id - 1;
            if (id > 0) {
                cursor = getTripCursor(id);
                if (cursor != null && cursor.moveToFirst()) looping = false;
            }
            else return null;
        }
        return cursor;
    }

    public void deleteAllSubtripsForTrip(int tripID) {
        Cursor c = getSubtripsFromTripID(tripID);
        if (c != null) {
            if (c.moveToFirst()) {
                int idColumn = c.getColumnIndexOrThrow(SUB.KEY_ROWID);
                do {
                    Log.w("deleting","subtrip id = " + c.getInt(idColumn));
                    deleteSubTrip(c.getInt(idColumn));
                } while (c.moveToNext());
            }
            c.close();
        }
    }

    public Cursor getAllTimes() {
        String where = null;
        Cursor c = 	db.query(true, TIME.DATABASE_TABLE, TIME.ALL_KEYS, where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Always ASCENDING
    /*
    public void orderTrips(String column) {
        Cursor cursor = getAllTrips(TRIP.KEY_ORDER,ASCENDING);
        Cursor cursor1 = cursor;
        List<Integer> addToEndIds = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            int order = 0;
            int columnIndex = cursor.getColumnIndexOrThrow(column);
            int idIndex = cursor.getColumnIndexOrThrow(TRIP.KEY_ROWID);
            do {
                int currentOrder = order;
                int position = cursor.getPosition();
                int comparePos = position - 1;
                KTime time = getDBTime(cursor.getInt(columnIndex));
                int id = cursor.getInt(idIndex);
                if (time == null) {
                    addToEndIds.add(cursor.getInt(idIndex));
                    continue;
                }
                boolean looping = true;
                while (looping) {
                    if (comparePos < 0) {
                        looping = false;
                        updateTripSingleColumn(id,TRIP.KEY_ORDER,currentOrder);
                        order++;
                        continue;
                    }
                    cursor1.moveToPosition(comparePos);
                    KTime compareTime = getDBTime(cursor1.getInt(columnIndex));
                    int compareId = cursor1.getInt(idIndex);
                    if (compareTime == null) {
                        looping = false;
                        updateTripSingleColumn(id,TRIP.KEY_ORDER,currentOrder);
                        order++;
                        continue;
                    }
                    if (time.isAfter(compareTime) && !time.isBefore(compareTime)) {
                        looping = false;
                        updateTripSingleColumn(id,TRIP.KEY_ORDER,currentOrder);
                        order++;
                        continue;
                    }
                    if (!time.isAfter(compareTime) && time.isBefore(compareTime)) {
                        updateTripSingleColumn(compareId,TRIP.KEY_ORDER,currentOrder);
                        currentOrder = currentOrder - 1;
                        comparePos = comparePos - 1;
                    }
                }
                //don't add anything
            } while (cursor.moveToNext());
        }
    }
    */
    public void orderTrips(String column) {
        Cursor cursor = getAllTrips(TRIP.KEY_ORDER,ASCENDING);

        if (cursor != null && cursor.moveToFirst()) {
            int lastTimeID = 0;
            int lastID = 0;
            int count = cursor.getCount();
            if (count < 2) return;
            Cursor timeCursor = getAllTimes();
            if (timeCursor.moveToLast() && cursor.moveToLast()) {
                lastTimeID = timeCursor.getInt(timeCursor.getColumnIndexOrThrow(TIME.KEY_ROWID));
                lastID = cursor.getInt(cursor.getColumnIndexOrThrow(TRIP.KEY_ROWID));
            }
            ArrayList<Integer> tripIDsFromCPosi = new ArrayList<>(count);
            SparseArray<Integer> timeIDsFromTripID = new SparseArray<>(lastTimeID);
            SparseArray<KTime> timeFromID = new SparseArray<>(lastTimeID);

            ArrayList<Integer> orderedListOfIDs = new ArrayList<>(count);

            SparseArray<Integer> originalOrdersFromID = new SparseArray<>(lastID + 1);

            int idIndex = cursor.getColumnIndexOrThrow(TRIP.KEY_ROWID);
            int columnIndex = cursor.getColumnIndexOrThrow(column);
            int orderIndex = cursor.getColumnIndexOrThrow(TRIP.KEY_ORDER);
            cursor.moveToFirst();
            do {
                int id = cursor.getInt(idIndex);
                tripIDsFromCPosi.add(cursor.getPosition(),id);
                int timeId = cursor.getInt(columnIndex);
                timeIDsFromTripID.setValueAt(id,timeId);
                timeFromID.setValueAt(timeId,getDBTime(timeId));

                originalOrdersFromID.setValueAt(id,cursor.getInt(orderIndex));
            } while (cursor.moveToNext());

            cursor.moveToFirst();
            int position;
            do {
                position = cursor.getPosition();
                int id = tripIDsFromCPosi.get(position);
                orderedListOfIDs.add(position,id);
                while (!isTripInRightPosition(position,orderedListOfIDs,timeIDsFromTripID,timeFromID)) {
                    orderedListOfIDs.remove(position);
                    position = position - 1;
                    orderedListOfIDs.add(position,id);
                }
            } while (cursor.moveToNext());
            int order = 0;
            Integer currentTripId = null;
            if (Globals.currentTrip != null) currentTripId = Globals.currentTrip._id;
            for (Integer tripId : orderedListOfIDs) {
                if (originalOrdersFromID.keyAt(tripId) != order) updateTripSingleColumn(tripId,TRIP.KEY_ORDER,order);
                if (currentTripId != null && ((int)tripId) == ((int)currentTripId)) Globals.currentTrip.order = order;
                order++;
            }
        }
        if (cursor != null) cursor.close();
    }

    public boolean isTripInRightPosition(int position,
                                         ArrayList<Integer> orderedListOfIDs,
                                         SparseArray<Integer> timeIDsFromTripID,
                                         SparseArray<KTime> timeFromID) {
        try {
            int tripId = orderedListOfIDs.get(position);
            int compareId = orderedListOfIDs.get(position - 1);
            KTime time = timeFromID.valueAt(timeIDsFromTripID.valueAt(tripId));
            KTime compareTime = timeFromID.valueAt(timeIDsFromTripID.valueAt(compareId));
            if (compareTime == null) return true;
            return time.isAfter(compareTime);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }


    public void orderTripsAndCheckTotals() {
        Log.w(Globals.LOG,"Starting complete re-calculate totals");
        orderTrips(TRIP.KEY_APPA_START);
        if (false) { Log.w(Globals.LOG,"Finishing complete re-calculate totals"); return; }
        Cursor c = getAllTrips(TRIP.KEY_ORDER,ASCENDING);

        KTime totalDriving = new KTime(Globals.ZERO_TIME);
        KTime totalNightDriving = new KTime(Globals.ZERO_TIME);
        if (c != null && c.moveToFirst()) {
            int totalColumn = c.getColumnIndexOrThrow(TRIP.KEY_APPA_TOTAL);
            int isNightColumn = c.getColumnIndexOrThrow(TRIP.KEY_IS_NIGHT);
            int idColumn = c.getColumnIndexOrThrow(TRIP.KEY_ROWID);
            do {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {}
                int id = c.getInt(idColumn);
                if (Globals.currentTrip != null && id == Globals.currentTrip._id) {
                    Globals.currentTrip.totalDrivingBeforeTime = totalDriving;
                    Globals.currentTrip.totalNightBeforeTime = totalNightDriving;
                    continue;
                }
                KTime totalTime = getDBTime(c.getInt(totalColumn));
                if (totalTime == null) continue;
                totalDriving = KTime.addTimes(totalDriving, totalTime);
                if (ConversionHelper.intToBool(c.getInt(isNightColumn))) {
                    totalNightDriving = KTime.addTimes(totalNightDriving, totalTime);
                }
                updateTripSingleColumn(id,TRIP.KEY_DRIVING_TOTAL_AFTER, KTime.newDBTime(totalDriving,this).id);
                updateTripSingleColumn(id,TRIP.KEY_NIGHT_TOTAL_AFTER, KTime.newDBTime(totalNightDriving,this).id);
                Thread.yield();
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        Log.w(Globals.LOG,"Finishing complete re-calculate totals");
    }




    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(TRIP.TABLE_CREATE_SQL);
            _db.execSQL(SUB.TABLE_CREATE_SQL);
            _db.execSQL(TIME.TABLE_CREATE_SQL);
            _db.execSQL(LOCATION.TABLE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            _db.execSQL("DROP TABLE IF EXISTS " + TRIP.DATABASE_TABLE);
            _db.execSQL("DROP TABLE IF EXISTS " + SUB.DATABASE_TABLE);
            _db.execSQL("DROP TABLE IF EXISTS " + TIME.DATABASE_TABLE);
            _db.execSQL("DROP TABLE IF EXISTS " + LOCATION.DATABASE_TABLE);

            onCreate(_db);
        }
    }


}