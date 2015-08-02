package com.burke.kelv.timerdriving;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.List;

import static com.burke.kelv.timerdriving.Globals.*;


public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
           // MainFragment fragment = new MainFragment();
           // getSupportFragmentManager().beginTransaction()
           //         .add(R.id.container, new PlaceholderFragment())
           //         .commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //MyApplication.setActivityContext(this);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final Context context = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //context.startActivity(new Intent(context,));
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Globals.currentTrip == null) {
                    checkLastTrip();
                    MyApplication.getStaticDbHelper().orderTripsAndCheckTotals();
                }
            }
        }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    public void checkLastTrip() {
        DBHelper dbHelper = MyApplication.getStaticDbHelper();
        Cursor cursor = dbHelper.getLastTrip();
        int id = 0;
        int startTime = 0;
        int numberOfSubs = 0;
        ArrayList<Integer> elapsedTimes = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ROWID));
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_STATUS));
                int apparentEndID = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_APPA_END));
                Log.w("drhgdrg","hehrere");
                if (status == KTrip.STATUS.FINISHED) return;
                if (apparentEndID != 0) { dbHelper.updateTripSingleColumn(id, DBHelper.TRIP.KEY_STATUS,KTrip.STATUS.FINISHED); return; }

                startTime = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_REAl_START));

                numberOfSubs = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TRIP.KEY_NUMOFSUBS));

                Cursor subtrips = dbHelper.getSubtripsFromTripID(id);
                int subStatusColumn = subtrips.getColumnIndexOrThrow(DBHelper.SUB.KEY_STATUS);
                int subTotalTimeClm = subtrips.getColumnIndexOrThrow(DBHelper.SUB.KEY_TOTAL);
                int subElapsdClm = subtrips.getColumnIndexOrThrow(DBHelper.SUB.KEY_ELAPSED);

                elapsedTimes = new ArrayList<>();
                for (int i = 0; i < numberOfSubs; i++) {
                    subtrips.moveToPosition(i);
                    int subStatus = subtrips.getInt(subStatusColumn);
                    if (subStatus == KTrip.STATUS.FINISHED) {
                        elapsedTimes.add(subtrips.getInt(subTotalTimeClm));
                    }
                    else if (subStatus == KTrip.STATUS.RUNNING) {
                        elapsedTimes.add(subtrips.getInt(subElapsdClm));
                    }

                }
                KTime totalElapsedTime = new KTime(Globals.ZERO_TIME);
                for (Integer time : elapsedTimes) {
                    KTime elapsed = dbHelper.getDBTime(time);
                    totalElapsedTime = KTime.addTimes(totalElapsedTime,elapsed);
                }
                subtrips.close();
                showUnfinishedLastTripDialog(dbHelper.getDBTime(startTime),totalElapsedTime,dbHelper.tripIsNightTrip(id),id);
            }
            catch (Exception e ) {
                e.printStackTrace();
            }
            finally {
                cursor.close();
            }
        }
    }

    public void showUnfinishedLastTripDialog(final KTime startTime, final KTime elapsedTime, final boolean isNightTrip, final int id) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.lasttrip_notfinished_layout);
                ((Toolbar)dialog.findViewById(R.id.toolbar)).setTitle("Unfinished Trip");
                ((TextView)dialog.findViewById(R.id.dateTV)).setText("Date: " + KTime.getProperReadable(startTime, TIMEFORMAT.WORDED_DATE));
                ((TextView)dialog.findViewById(R.id.startTimeTV)).setText("Start Time: " + KTime.getProperReadable(startTime, TIMEFORMAT.H_MM));
                ((TextView)dialog.findViewById(R.id.elapsedTimeTV)).setText("Running for: " + KTime.getProperReadable(elapsedTime, TIMEFORMAT.H_MM_SS));
                String dayNightStr = isNightTrip ? "Night Trip" : "Day Trip";
                ((TextView)dialog.findViewById(R.id.dayNightTV)).setText(dayNightStr);

                dialog.findViewById(R.id.resumePreviousButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        KTrip trip = new KTrip(context,startTime,elapsedTime,isNightTrip,id);
                        Globals.currentTrip = trip;

                        Intent startIntent = new Intent(context, TimerService.class);
                        startIntent.setAction(Globals.ACTION.STARTFOREGROUND_ACTION);
                        Intent startIntent2 = new Intent(context, TimerService.class);
                        startIntent2.setAction(ACTION.USE_GLOBALS_CURRENT_TRIP);
                        context.startService(startIntent);
                        context.startService(startIntent2);

                        showStartPauseStopTripDialog();
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.deleteTripButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DBHelper dbHelper = MyApplication.getStaticDbHelper();
                        dbHelper.deleteTrip(id);
                        Cursor c = dbHelper.getSubtripsFromTripID(id);
                        int idColumn = c.getColumnIndexOrThrow(DBHelper.SUB.KEY_ROWID);
                        if (c.moveToFirst()) {
                            for (int i = 0; i < c.getCount(); i++) {
                                c.moveToPosition(i);
                                dbHelper.deleteSubTrip(c.getInt(idColumn));
                            }
                        }
                        dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });
    }

    public void showStartPauseStopTripDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.cancel_finish_dialog_layout);
        Toolbar toolbar = (Toolbar)dialog.findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_launcher);
        toolbar.setTitle("Choose your action");

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TimerService.class);
                intent.putExtra("night",Globals.currentTrip.isNightTrip);
                switch (view.getId()) {
                    case R.id.playButton:
                        intent.setAction(Globals.ACTION.PLAY_ACTION);
                        break;
                    case R.id.pauseButton:
                        intent.setAction(Globals.ACTION.PAUSE_ACTION);
                        break;
                    case R.id.stopButton:
                        intent.setAction(Globals.ACTION.STOP_ACTION);
                        break;
                    default:
                        intent = null;
                }
                if (intent != null) startService(intent);
                dialog.dismiss();
            }
        };

        dialog.findViewById(R.id.playButton).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.pauseButton).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.stopButton).setOnClickListener(onClickListener);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
    }


    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new PlaceholderFragment(), "Control Panel");
        adapter.addFragment(new DetailsFragment(), "Details");
       // adapter.addFragment(new SupportMapFragment(),"Map");
        adapter.addFragment(new Fragment(),"Map");
        adapter.addFragment(new TripListFragment(), "Trip List");

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            final int listPosition = 3;
            FloatingActionButton fab;
            Integer screenWidth = null;
            Float original = null;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (fab == null) fab = (FloatingActionButton) findViewById(R.id.fab);
                if (fab.getX() != 0 && original == null) original = fab.getX();
                if (position == 2 && positionOffsetPixels > 0 ) fab.setVisibility(View.VISIBLE);
                if (position > 1 && original != null){
                    if (screenWidth == null && positionOffsetPixels != 0 && positionOffset != 0) {
                        screenWidth = Math.round(positionOffsetPixels / positionOffset);
                    }
                    if (screenWidth != null && positionOffsetPixels != 0) {
                        fab.setX(original + (screenWidth - positionOffsetPixels));
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {

                if (fab == null) fab = (FloatingActionButton) findViewById(R.id.fab);
                if (position < 2) fab.setVisibility(View.GONE);
                if (position == listPosition) {
                    if (fab.getX() != 0 && original == null) original = fab.getX();
                    if (original != null)fab.setX(original);
                    fab.setVisibility(View.VISIBLE);
                }
                //else fab.setVisibility(View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    static class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

    }

    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {
        ViewGroup container;
        ListView listView;
        TextView elapsedTextView;
        TextView nightElapsedTextView;
        TextView totalDrivingTV;
        TextView totalNightDrivingTV;
        TextView startStopTextView;
        TextView tripStatusTextView;
        TextView lastSubElapsedTextView;
        TextView lastSubSSTextView;
        Switch showSpeedOverlaySwitch;
        Boolean defaultTimerServiceIsRunning = false;
        DBHelper dbHelper;

        public PlaceholderFragment() {  }

        public void onClick(View view) {
            boolean shouldUpdateView = true;
            Log.i("LOG", "Clicked " + view.getId());
            switch (view.getId()) {
                case R.id.startButton:
                    if (showSpeedOverlaySwitch == null ) showSpeedOverlaySwitch = (Switch) getActivity().findViewById(R.id.showSpeed);
                     Intent startIntent = new Intent(getActivity(), TimerService.class);
                     startIntent.setAction(Globals.ACTION.STARTFOREGROUND_ACTION);
                     Intent startIntent2 = new Intent(getActivity(), TimerService.class);
                     startIntent2.setAction(Globals.ACTION.PLAY_ACTION);
                    startIntent2.putExtra("speedOverlay",showSpeedOverlaySwitch.isChecked());
                    startIntent2.putExtra("night",false);
                     getActivity().startService(startIntent);
                     getActivity().startService(startIntent2);

                    break;
                case R.id.pauseButton:
                    Intent pauseIntent = new Intent(getActivity(), TimerService.class);
                    pauseIntent.setAction(Globals.ACTION.PAUSE_ACTION);
                    pauseIntent.putExtra("night",false);
                    getActivity().startService(pauseIntent);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getActivity(), TimerService.class);
                            intent.setAction(ACTION.SHOW_HIDE_SPEED_OVERLAY_ACTION);
                            intent.putExtra("speedOverlay",false);
                            getActivity().startService(intent);
                        }
                    }).start();
                    break;
                case R.id.stopButton:
                    Intent stopIntent = new Intent(getActivity(), TimerService.class);
                    stopIntent.setAction(Globals.ACTION.STOP_ACTION);
                    stopIntent.putExtra("night",false);
                   // Intent stopServiceIntent = new Intent(getActivity(), TimerService.class);
                   // stopServiceIntent.setAction(Globals.ACTION.STOPFOREGROUND_ACTION);
                    getActivity().startService(stopIntent);
                  //  getActivity().startService(stopServiceIntent);

                    //FinishTripDialog finishTripDialog = new FinishTripDialog();
                    //finishTripDialog.show(getActivity().getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN),
                     //       "finishTrip");
                    //new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    //    @Override
                    //    public void run() {
                    //        Intent dialogIntent = new Intent(getActivity(), FinishTripActivity.class);
                     //       dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     //       startActivity(dialogIntent);
                    //    }
                  //  }, 5000);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getActivity(), TimerService.class);
                            intent.setAction(ACTION.SHOW_HIDE_SPEED_OVERLAY_ACTION);
                            intent.putExtra("speedOverlay",false);
                            getActivity().startService(intent);
                        }
                    }).start();
                    break;
                case R.id.nightStart:
                    if (showSpeedOverlaySwitch == null ) showSpeedOverlaySwitch = (Switch) getActivity().findViewById(R.id.showSpeed);
                    Intent startI = new Intent(getActivity(), TimerService.class);
                    startI.setAction(Globals.ACTION.STARTFOREGROUND_ACTION);
                    Intent startNight = new Intent(getActivity(), TimerService.class);
                    startNight.setAction(ACTION.PLAY_ACTION);
                    startNight.putExtra("speedOverlay",showSpeedOverlaySwitch.isChecked());
                    startNight.putExtra("night",true);
                    getActivity().startService(startI);
                    getActivity().startService(startNight);
                    break;
                case R.id.nightPause:
                    Intent pauseNight = new Intent(getActivity(), TimerService.class);
                    pauseNight.setAction(ACTION.PAUSE_ACTION);
                    pauseNight.putExtra("night",true);
                    getActivity().startService(pauseNight);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getActivity(), TimerService.class);
                            intent.setAction(ACTION.SHOW_HIDE_SPEED_OVERLAY_ACTION);
                            intent.putExtra("speedOverlay",false);
                            getActivity().startService(intent);
                        }
                    }).start();
                    break;
                case R.id.nightStop:
                    Intent stopNight = new Intent(getActivity(), TimerService.class);
                    stopNight.setAction(ACTION.STOP_ACTION);
                    stopNight.putExtra("night",true);
                    getActivity().startService(stopNight);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getActivity(), TimerService.class);
                            intent.setAction(ACTION.SHOW_HIDE_SPEED_OVERLAY_ACTION);
                            intent.putExtra("speedOverlay",false);
                            getActivity().startService(intent);
                        }
                    }).start();
                    break;
                case R.id.detailsButton:
                    //Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    //startActivity(intent);
                    ((MainActivity)getActivity()).viewPager.setCurrentItem(1,true);
                    break;
                case R.id.tripListButton:
                    shouldUpdateView = false;
                   // Fragment listFragment = new TripListFragment();
                  //  FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    //transaction.replace(R.id.container, listFragment);
                  //  transaction.addToBackStack(null);
                  //  transaction.commit();
                    break;
            }
            if (shouldUpdateView) {
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateView();
                    }
                }, 20);
            }
        }



        private BroadcastReceiver timerDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
                String time = intent.getStringExtra("elapsed");
                Boolean listeningRequest = intent.getBooleanExtra(Globals.listeningRequest, false);
                if (time !=null && !time.equals("")) {
                    try {
                        updateTotalDrivingTextViews();

                        updateTimerView(time);
                        if (Globals.currentTrip.isNightTrip) {
                            updateNightTimerView(time);
                        }
                    } catch (Exception e ) {
                        e.printStackTrace();
                    }
                } if (listeningRequest) {
                    Intent sendIntent = new Intent(Globals.defaultTimerListeningBroadcast);
                    sendIntent.putExtra("listening", true);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent);
                }
            }
        };

        private BroadcastReceiver tripInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateView();
            }
        };




        public void updateNightTimerView(String elapsedTime) {
            if (nightElapsedTextView == null) nightElapsedTextView = (TextView) getActivity().findViewById(R.id.nightElapsed);
            nightElapsedTextView.setText(elapsedTime);
        }
        public void updateTimerView(String elapsedTime) {
            if (elapsedTextView == null) elapsedTextView = (TextView) getActivity().findViewById(R.id.elapsedTime);
            elapsedTextView.setText(elapsedTime);
            if (lastSubElapsedTextView != null) {
                if (Globals.currentTrip != null && Globals.currentTrip.currentSubTrip != null) lastSubElapsedTextView.setText(KTime.getProperReadable(
                        Globals.currentTrip.currentSubTrip.returnElapsed(), TIMEFORMAT.HH_MM_SS));
            }
        }

        public void updateTotalDrivingTextViews() {
            if (currentTrip != null) {
                if (totalDrivingTV == null)
                    totalDrivingTV = (TextView) getActivity().findViewById(R.id.totalTotal);
                if (totalNightDrivingTV == null)
                    totalNightDrivingTV = (TextView) getActivity().findViewById(R.id.nightTotal);

                totalDrivingTV.setText(KTime.getProperReadable(currentTrip.getCurrentTotalDriving(), TIMEFORMAT.H_MM));
                totalNightDrivingTV.setText(KTime.getProperReadable(currentTrip.getCurrentTotalNightDriving(), TIMEFORMAT.H_MM));
            }
            else {
                DBHelper dbHelper = MyApplication.getStaticDbHelper();
                Cursor c = dbHelper.getLastTrip();
                if (c != null && c.moveToFirst()) {
                    if (totalDrivingTV == null)
                        totalDrivingTV = (TextView) getActivity().findViewById(R.id.totalTotal);
                    if (totalNightDrivingTV == null)
                        totalNightDrivingTV = (TextView) getActivity().findViewById(R.id.nightTotal);
                    totalDrivingTV.setText(KTime.getProperReadable( dbHelper.getDBTime(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_DRIVING_TOTAL_AFTER)))
                            , TIMEFORMAT.H_MM));
                    totalNightDrivingTV.setText(KTime.getProperReadable( dbHelper.getDBTime(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_NIGHT_TOTAL_AFTER)))
                            , TIMEFORMAT.H_MM));
                }
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup containerGiven,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, containerGiven, false);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            Button startButton= (Button) getActivity().findViewById(R.id.startButton);
            Button pauseButton= (Button) getActivity().findViewById(R.id.pauseButton);
            Button stopButton= (Button) getActivity().findViewById(R.id.stopButton);
            Button nstartButton= (Button) getActivity().findViewById(R.id.nightStart);
            Button npauseButton= (Button) getActivity().findViewById(R.id.nightPause);
            Button nstopButton= (Button) getActivity().findViewById(R.id.nightStop);
            Button detailsButton = (Button) getActivity().findViewById(R.id.detailsButton);
            Button locationsButton = (Button) getActivity().findViewById(R.id.tripListButton);
            if (startButton !=null) startButton.setOnClickListener(this);
            if (pauseButton !=null) pauseButton.setOnClickListener(this);
            if (stopButton !=null) stopButton.setOnClickListener(this);
            if (nstartButton !=null) nstartButton.setOnClickListener(this);
            if (npauseButton !=null) npauseButton.setOnClickListener(this);
            if (nstopButton !=null) nstopButton.setOnClickListener(this);
            if (detailsButton !=null) detailsButton.setOnClickListener(this);
            if (locationsButton !=null) locationsButton.setOnClickListener(this);

            showSpeedOverlaySwitch = (Switch) getActivity().findViewById(R.id.showSpeed);
            showSpeedOverlaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Intent intent = new Intent(getActivity(), TimerService.class);
                    intent.setAction(ACTION.SHOW_HIDE_SPEED_OVERLAY_ACTION);
                    intent.putExtra("speedOverlay",isChecked);
                    getActivity().startService(intent);
                }
            });

            updateView();
            super.onActivityCreated(savedInstanceState);
        }

        public void updateView() {
            if (Globals.currentTrip != null) {
                Log.i(LOG, "Updating main act/frag view");
                if (elapsedTextView == null) elapsedTextView = (TextView) getActivity().findViewById(R.id.elapsedTime);
                if (nightElapsedTextView == null) nightElapsedTextView = (TextView) getActivity().findViewById(R.id.nightElapsed);
                if (tripStatusTextView == null) tripStatusTextView = (TextView) getActivity().findViewById(R.id.tripStatus);
                if (startStopTextView == null) startStopTextView = (TextView) getActivity().findViewById(R.id.startedTime);
                if (listView == null) listView = (ListView) getActivity().findViewById(R.id.listView);

                elapsedTextView.setText(KTime.getProperReadable(currentTrip.returnElapsed(), TIMEFORMAT.MM_SS_plusHoursIfAppl));

                if (Globals.currentTrip.isNightTrip) nightElapsedTextView.setText(KTime.getProperReadable(currentTrip.returnElapsed(), TIMEFORMAT.MM_SS_plusHoursIfAppl));

                updateTotalDrivingTextViews();
                // get total time


                switch (Globals.currentTrip.status) {
                    case KTrip.STATUS.RUNNING:
                        tripStatusTextView.setText("RUNNING");
                        ((Button) getActivity().findViewById(R.id.startButton)).setEnabled(false);
                        ((Button) getActivity().findViewById(R.id.nightStart)).setEnabled(false);
                        ((Button) getActivity().findViewById(R.id.pauseButton)).setEnabled(true);
                        ((Button) getActivity().findViewById(R.id.nightPause)).setEnabled(true);
                        ((Button) getActivity().findViewById(R.id.stopButton)).setEnabled(true);
                        ((Button) getActivity().findViewById(R.id.nightStop)).setEnabled(true);
                        break;
                    case KTrip.STATUS.PAUSED:
                        ((Button) getActivity().findViewById(R.id.startButton)).setEnabled(true);
                        ((Button) getActivity().findViewById(R.id.nightStart)).setEnabled(true);
                        ((Button) getActivity().findViewById(R.id.pauseButton)).setEnabled(false);
                        ((Button) getActivity().findViewById(R.id.nightPause)).setEnabled(false);
                        ((Button) getActivity().findViewById(R.id.stopButton)).setEnabled(true);
                        ((Button) getActivity().findViewById(R.id.nightStop)).setEnabled(true);
                        tripStatusTextView.setText("PAUSED");
                        break;
                    case KTrip.STATUS.FINISHED:
                        ((Button) getActivity().findViewById(R.id.startButton)).setEnabled(true);
                        ((Button) getActivity().findViewById(R.id.nightStart)).setEnabled(true);
                        ((Button) getActivity().findViewById(R.id.pauseButton)).setEnabled(false);
                        ((Button) getActivity().findViewById(R.id.nightPause)).setEnabled(false);
                        ((Button) getActivity().findViewById(R.id.stopButton)).setEnabled(false);
                        ((Button) getActivity().findViewById(R.id.nightStop)).setEnabled(false);
                        tripStatusTextView.setText("FINISHED");
                        break;
                    default:
                        tripStatusTextView.setText("UNKNOWN");
                        break;
                }

                if (Globals.currentTrip.isNightTrip && Globals.currentTrip.status != KTrip.STATUS.FINISHED) {
                    Button start = (Button) getActivity().findViewById(R.id.startButton);
                    Button pause = (Button) getActivity().findViewById(R.id.pauseButton);
                    Button stop = (Button) getActivity().findViewById(R.id.stopButton);
                    start.setEnabled(false);
                    pause.setEnabled(false);
                    stop.setEnabled(false);
                } else if (!Globals.currentTrip.isNightTrip && Globals.currentTrip.status != KTrip.STATUS.FINISHED) {
                    Button start = (Button) getActivity().findViewById(R.id.nightStart);
                    Button pause = (Button) getActivity().findViewById(R.id.nightPause);
                    Button stop = (Button) getActivity().findViewById(R.id.nightStop);
                    start.setEnabled(false);
                    pause.setEnabled(false);
                    stop.setEnabled(false);
                }

                if (currentTrip.status == KTrip.STATUS.RUNNING || currentTrip.status == KTrip.STATUS.PAUSED) {
                    startStopTextView.setText(KTime.getProperReadable(currentTrip.apparentStartTime, TIMEFORMAT.H_MM) + " -");
                } else if (currentTrip.status == KTrip.STATUS.FINISHED) {
                    if (currentTrip.apparentEndTime != null) {
                        String start = KTime.getProperReadable(currentTrip.apparentStartTime, TIMEFORMAT.H_MM);
                        String end = KTime.getProperReadable(currentTrip.apparentEndTime, TIMEFORMAT.H_MM);
                        startStopTextView.setText(start + " - " + end);
                    }
                }

                //gridLayout.removeAllViews();
               // new SubGridLayoutSetup().execute(gridLayout);
//                populateListView();
            } else {
                if (elapsedTextView == null) elapsedTextView = (TextView) getActivity().findViewById(R.id.elapsedTime);
                if (nightElapsedTextView == null) nightElapsedTextView = (TextView) getActivity().findViewById(R.id.nightElapsed);
                if (tripStatusTextView == null) tripStatusTextView = (TextView) getActivity().findViewById(R.id.tripStatus);
                if (startStopTextView == null) startStopTextView = (TextView) getActivity().findViewById(R.id.startedTime);
                if (listView == null) listView = (ListView) getActivity().findViewById(R.id.listView);

                elapsedTextView.setText("00:00");
                startStopTextView.setText("-------");
                nightElapsedTextView.setText("00:00");

                updateTotalDrivingTextViews();

                ((Button) getActivity().findViewById(R.id.startButton)).setEnabled(true);
                ((Button) getActivity().findViewById(R.id.nightStart)).setEnabled(true);
                ((Button) getActivity().findViewById(R.id.pauseButton)).setEnabled(true);
                ((Button) getActivity().findViewById(R.id.nightPause)).setEnabled(true);
                ((Button) getActivity().findViewById(R.id.stopButton)).setEnabled(true);
                ((Button) getActivity().findViewById(R.id.nightStop)).setEnabled(true);
            }
        }

        @Deprecated
        private void populateListView() {
            final DBHelper dbHelper1 = MyApplication.getStaticDbHelper();
            Cursor listCursor = dbHelper1.getSubtripsFromTripID(Globals.currentTrip._id);
            String[] fromFieldNames = new String[] {DBHelper.SUB.KEY_START, DBHelper.SUB.KEY_END, DBHelper.SUB.KEY_ELAPSED};
            int[] toViewIDs = new int[] {R.id.freewayTick, R.id.freewayButton, R.id.elapsedTextView};
            SimpleCursorAdapter myCursorAdapter;
            myCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.subtrip_list_item, listCursor, fromFieldNames, toViewIDs, 0);

            SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (view.getId() == R.id.freewayTick || view.getId() == R.id.elapsedTextView || view.getId() == R.id.freewayTick ) {
                        String timeStr = "";
                        int timeID = cursor.getInt(columnIndex);
                        if (timeID != 0) {

                            timeStr = KTime.getProperReadable(dbHelper.getDBTime(timeID), TIMEFORMAT.HH_MM_SS);
                        }
                        ((TextView) view).setText(timeStr);
                        return true;
                    }
                    return false;
                }
            };

            myCursorAdapter.setViewBinder(viewBinder);
            listView.setAdapter(myCursorAdapter);
            setListViewHeightBasedOnChildren(listView);
           // myCursorAdapter.notifyDataSetChanged();
        }

        public  void setListViewHeightBasedOnChildren(ListView listView) {
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) {
                // pre-condition
                return;
            }

            int totalHeight = 0;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
            listView.requestLayout();
        }




        @Override
        public void onResume() {
            Log.i(LOG, "Main fragment resuming.... registering receiver....");

            Intent sendIntent = new Intent(Globals.defaultTimerListeningBroadcast);
            sendIntent.putExtra("listening", true);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(sendIntent);

            IntentFilter iF = new IntentFilter(tripChangedStateBroadcast);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(timerDataReceiver, new IntentFilter(defaultTimerUpdateBroadcastId));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(tripInfoReceiver, iF );

            if (dbHelper == null) dbHelper = ((MyApplication)getActivity().getApplication()).getDbHelper();

            updateView();

            super.onResume();
        }

        @Override
        public void onPause() {
            Log.i(LOG, "Main fragment pausing.... unregistering receiver....");

            Intent sendIntent = new Intent(Globals.defaultTimerListeningBroadcast);
            sendIntent.putExtra("listening", false);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(sendIntent);

            elapsedTextView = null;
            nightElapsedTextView = null;
            tripStatusTextView = null;
            startStopTextView = null;
            totalDrivingTV = null;
            totalNightDrivingTV = null;
            showSpeedOverlaySwitch = null;
            lastSubElapsedTextView = null;
            listView = null;


            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(timerDataReceiver);
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(tripInfoReceiver);
            super.onPause();

        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.i(Globals.LOG, "Settings clicked");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
