package com.burke.kelv.timerdriving;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by kelv on 7/07/2015.
 */
public class FinishedDetailsActivity extends AppCompatActivity {
    public static final String ORIGINAL_TRIP_EXTRA = "originalTrip";

    private ViewPager viewPager;
    int tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finished_details_activity);
        tripId = getIntent().getIntExtra(ORIGINAL_TRIP_EXTRA,0);

        Log.w("tripID","tripId = " + tripId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager,tripId);
        }
    }

    private void setupViewPager(ViewPager viewPager, int tripId) {
        int position = 0;
        Cursor c = MyApplication.getStaticDbHelper().getTripCursor(tripId);
        if (c != null && c.moveToFirst()) {
            position = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ORDER));
            c.close();
        }
        viewPager.setAdapter(new DetailsAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(position,false);
    }

    public static class DetailsAdapter extends FragmentStatePagerAdapter {
        public Cursor cursor;
        int maxPosition;

        public DetailsAdapter(FragmentManager fm) {
            super(fm);
            this.cursor = MyApplication.getStaticDbHelper().getAllTrips(DBHelper.TRIP.KEY_ORDER,DBHelper.ASCENDING);
            if (cursor.moveToFirst()) {
            }
        }

        @Override
        public int getCount() {
            return cursor != null ? cursor.getCount() : 0;
        }

        @Override
        public Fragment getItem(int position) {
            Cursor c = MyApplication.getStaticDbHelper().getTripCursor(DBHelper.TRIP.KEY_ORDER,position);
            if (c != null && c.moveToFirst()) {
                int id = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ROWID));
                c.close();
                return FinishedDetailsFragment.newInstance(id);
            }
            throw new IllegalStateException("public Fragment getItem(int position) RETURN NULL TRIPS PROBS SHOULD BE ORDERED");
        }
    }

}
