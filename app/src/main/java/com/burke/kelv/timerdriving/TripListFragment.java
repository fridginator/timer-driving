package com.burke.kelv.timerdriving;

import android.location.Location;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Space;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kelv on 30/06/2015.
 */
public class TripListFragment extends Fragment{
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trip_list_fragment,container,false);
        recyclerView = (RecyclerView) view.findViewById(R.id.cardList);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.w("llsushefi","sefuygsefugsyefugsyefugsyuefgsfye\nsehfuihseifhfuies");
        if (recyclerView == null) Log.e("llsushefi", "noooooooooooooooooooooooooooooo\nooooooooooooooooo");

  //      Space space = (Space) getActivity().findViewById(R.id.space);
 //       RelativeLayout.LayoutParams recyclerParams = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
//        FrameLayout.LayoutParams llParams = (FrameLayout.LayoutParams) getActivity().findViewById(R.id.llayout).getLayoutParams();
  //      AppBarLayout.LayoutParams toolbarParams = (AppBarLayout.LayoutParams) getActivity().findViewById(R.id.toolbar).getLayoutParams();
      //  recyclerParams.height = -1;
       // Log.w("osiejofs",recyclerParams.height + "rec " + llParams.height + " ll " + toolbarParams.height +" heihgt");
 //       recyclerView.setLayoutParams(recyclerParams);
   //     recyclerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
  //      int width = recyclerView.getMeasuredWidth();
   //     int height = recyclerView.getMeasuredHeight();
   //     Log.w("sefse","w,h:"+width+","+height);

        recyclerView.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);

        TripRecyclerAdapter adapter = new TripRecyclerAdapter(getActivity(),MyApplication.getStaticDbHelper().getAllTrips(DBHelper.TRIP.KEY_ORDER,DBHelper.DESCENDING));
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        new RefreshTotalsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //uncomment for re-calc totals on resume
    }

    private class RefreshTotalsTask extends AsyncTask<Void,Void,Void> {
        MaterialDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new MaterialDialog.Builder(getActivity())
                    .title("Processing")
                    .content("Please Wait...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .cancelable(false)
                    .show();
                    ((TripRecyclerAdapter) recyclerView.getAdapter()).changeCursor(MyApplication.getStaticDbHelper().getAllTrips(DBHelper.TRIP.KEY_ORDER, DBHelper.DESCENDING));
        }

        @Override
        protected Void doInBackground(Void... params) {
            //MyApplication.getStaticDbHelper().orderTrips(DBHelper.TRIP.KEY_REAl_START,DBHelper.ASCENDING);
            MyApplication.getStaticDbHelper().orderTripsAndCheckTotals(DBHelper.TRIP.KEY_REAl_START, DBHelper.ASCENDING);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((TripRecyclerAdapter) recyclerView.getAdapter()).changeCursor(MyApplication.getStaticDbHelper().getAllTrips(DBHelper.TRIP.KEY_ORDER, DBHelper.DESCENDING));
            dialog.dismiss();
        }
    }
}
