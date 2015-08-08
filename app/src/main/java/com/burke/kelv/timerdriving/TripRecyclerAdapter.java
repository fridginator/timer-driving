package com.burke.kelv.timerdriving;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by kelv on 30/06/2015.
 */
public class TripRecyclerAdapter extends RecyclerView.Adapter<TripRecyclerAdapter.TripRecyclerViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;
    private DBHelper dbHelper;
    private int startTimeColumn, stopTimeColumn, totalTimeColumn, isNightColumn, totalDrivingColumn, totalNightColumn;
    private int nightTextColour, dayTextColour;

    public TripRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex(DBHelper.TRIP.KEY_ROWID) : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        dbHelper = MyApplication.getStaticDbHelper();
        if (cursor != null) {
            cursor.moveToFirst();
            //Log.w("logger","TripRecyclerAdapter Constructor p2"+mDataValid+" row:" +mRowIdColumn +" :"+ cursor.getColumnName(mRowIdColumn) + " db:" + dbHelper + " cursor:"+mCursor);
            mCursor.registerDataSetObserver(mDataSetObserver);

            startTimeColumn = cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_APPA_START);
            stopTimeColumn = cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_APPA_END);
            totalTimeColumn = cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_APPA_TOTAL);
            isNightColumn = cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_IS_NIGHT);
            totalDrivingColumn = cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_DRIVING_TOTAL_AFTER);
            totalNightColumn = cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_NIGHT_TOTAL_AFTER);

            nightTextColour = Color.parseColor("#ffffffff");
            dayTextColour = Color.parseColor("#c3010101");
        }
    }

    public void bindViewHolder(TripRecyclerViewHolder viewHolder, Cursor cursor, int position) {
       //Log.w("logger","bindViewHolder position:"+position);
        boolean isNight = ConversionHelper.intToBool(cursor.getInt(isNightColumn));
        int textColour = isNight ? nightTextColour : dayTextColour;


        int startTime = cursor.getInt(startTimeColumn);
        int endTime = cursor.getInt(stopTimeColumn);
        int totalTime = cursor.getInt(totalTimeColumn);
        int totalDrivingTime = cursor.getInt(totalDrivingColumn);
        int totalNightDrivingTime = cursor.getInt(totalNightColumn);

        String start = KTime.getProperReadable(startTime,Globals.TIMEFORMAT.H_MM,KTime.TYPE_DATETIME);
        String end = KTime.getProperReadable(endTime, Globals.TIMEFORMAT.H_MM,KTime.TYPE_DATETIME);
        String total = KTime.getProperReadable(totalTime, Globals.TIMEFORMAT.H_MM,KTime.TYPE_TIME_LENGTH);
        String totalDriving = KTime.getProperReadable(totalDrivingTime, Globals.TIMEFORMAT.H_MM,KTime.TYPE_TIME_LENGTH);
        String totalNightDriving = KTime.getProperReadable(totalNightDrivingTime, Globals.TIMEFORMAT.H_MM,KTime.TYPE_TIME_LENGTH);

        viewHolder.vDate.setText(KTime.getProperReadable(startTime,Globals.TIMEFORMAT.WORDED_DATE,KTime.TYPE_DATETIME));
        viewHolder.vTimes.setText(start + " - " + end + "  (" + total + ")");
        viewHolder.vTotal.setText("Total-- " + totalDriving);
        viewHolder.vTotalNight.setText("Night-- " + totalNightDriving);

        viewHolder.nightIV.setVisibility(isNight ? View.VISIBLE : View.INVISIBLE);
        viewHolder.vDate.setTextColor(textColour);
        viewHolder.vTimes.setTextColor(textColour);
        viewHolder.vTotal.setTextColor(textColour);
        viewHolder.vTotalNight.setTextColor(textColour);

        viewHolder.sunIV.setX(randInt(-50,900)); //-50 to 900
        viewHolder.sunIV.setY(randInt(-50,100)); //-50 to 100
    }

    @Override
    public TripRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.w("logger","onCreateViewHolder");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_list_item, parent, false);
        return new TripRecyclerViewHolder(itemView);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        //Log.w("logger","getItemCount");
        if (mDataValid && mCursor != null) {
           // Log.w("sief",mCursor.getCount()+"");
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        //Log.w("logger","getItemId");
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getInt(mRowIdColumn);
        }
        return 0;
    }


    @Override
    public void onBindViewHolder(TripRecyclerViewHolder viewHolder, int position) {
       // Log.w("logger","onBindViewHolder");
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        bindViewHolder(viewHolder, mCursor, position);
    }


    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");

            startTimeColumn = newCursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_APPA_START);
            stopTimeColumn = newCursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_APPA_END);
            totalTimeColumn = newCursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_APPA_TOTAL);
            isNightColumn = newCursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_IS_NIGHT);
            totalDrivingColumn = newCursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_DRIVING_TOTAL_AFTER);
            totalNightColumn = newCursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_NIGHT_TOTAL_AFTER);

            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            Log.w("sefsefsefswe","onChanged");
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            Log.w("sefsefsefswe","onInvalidated");
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }


    public static class TripRecyclerViewHolder extends RecyclerView.ViewHolder {
        protected TextView vDate;
        protected TextView vTimes;
        protected TextView vTotal;
        protected TextView vTotalNight;
        protected ImageView nightIV;
        protected ImageView sunIV;

        public TripRecyclerViewHolder(final View view) {
            super(view);
            Log.w("logger","TripRecyclerViewHolder constructor");
            this.vDate = (TextView) view.findViewById(R.id.dateTV);
            this.vTimes = (TextView) view.findViewById(R.id.timesTV);
            this.vTotal = (TextView) view.findViewById(R.id.totalTV);
            this.vTotalNight = (TextView) view.findViewById(R.id.nightTotalTV);
            this.nightIV = (ImageView) view.findViewById(R.id.nightImageView);
            this.sunIV = (ImageView) view.findViewById(R.id.sunImageView);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = (int) getItemId();
                    Intent intent = new Intent(view.getContext(),FinishedDetailsActivity.class);
                    intent.putExtra(FinishedDetailsActivity.ORIGINAL_TRIP_EXTRA,id);
                    view.getContext().startActivity(intent);
                }
            });
        }

    }

    public static final Random random = new Random();
    public static int randInt(int min, int max) {
        int randomNum = random.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
