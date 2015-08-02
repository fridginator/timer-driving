package com.burke.kelv.timerdriving;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

/**
 * Created by kelv on 14/03/2015.
 */

public class MyApplication extends Application {
    private static DBHelper dbHelper;
    private static FragmentActivity mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DBHelper(this);
        dbHelper.open();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        dbHelper.close();
        dbHelper = null;
    }

    public DBHelper getDbHelper(){
        return dbHelper;
    }
    public static DBHelper getStaticDbHelper() { return dbHelper; }
    public static void setActivityContext(FragmentActivity act) { mainActivity = act; }
    public static Context getActivityContext() { return mainActivity; }
}
