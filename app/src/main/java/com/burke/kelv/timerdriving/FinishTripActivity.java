package com.burke.kelv.timerdriving;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.support.v7.widget.Toolbar;

import static com.burke.kelv.timerdriving.Globals.defaultTimerUpdateBroadcastId;
import static com.burke.kelv.timerdriving.Globals.tripChangedStateBroadcast;

/**
 * Created by kelv on 19/06/2015.
 */

public class FinishTripActivity extends Activity {
    private FinishTripDialog finishDialog;
    private Dialog cancelledFinishDialog;
    private KTrip trip;
    private static Boolean dialogShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_activity);
        trip = Globals.currentTrip;
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter iF = new IntentFilter(Globals.distanceChangedBroadcast);
        iF.addAction(Globals.numberOfDistanceReqBroadcast);
        LocalBroadcastManager.getInstance(this).registerReceiver(distanceReceiver, iF );

        if (!dialogShowing) {
            trip.prepareForStop();
            showDialog();
            dialogShowing = true;

        }



      //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // toolbar.setLogo(R.drawable.ic_launcher);
      //  toolbar.setTitle("Confirm Trip Details");
    }

    BroadcastReceiver distanceReceiver = new BroadcastReceiver() {
        int numberAwaiting = 0;
        int numberReceived = 0;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Globals.numberOfDistanceReqBroadcast)) {
                numberAwaiting = intent.getIntExtra(Globals.numberOfDistanceReqBroadcast,0);
                Log.w("here",numberAwaiting+" uifse");
            }
            else if (intent.getAction().equals(Globals.distanceChangedBroadcast)) {
                numberReceived++;
                if (true) {
                    int kms = Math.round(trip.distance/1000);
                    finishDialog.setDistance(kms);
                }
            }
        }
    };

    private void showDialog(){
        finishDialog = new FinishTripDialog();
        finishDialog.setCallbacks(this);
        finishDialog.setTrip(trip);
        finishDialog.show(getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN),
                "finishTrip");
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w("stop","stop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(distanceReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        finishDialog.dismiss();
        dialogShowing = false;
     //   new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
     //       @Override
    //        public void run() {
    //            Intent dialogIntent = new Intent(getApplicationContext(), FinishTripActivity.class);
    //            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //            startActivity(dialogIntent);
    //        }
    //    }, 50);
        trip.reprepareForResume();

        this.finish();
    }

    public void finishTripDialogCallbacks(int response) {
        switch (response) {
            case FinishTripDialog.CLICKED_SAVE_BUTTON:
                Log.d("se","save");
                finishDialog.saveDetails();
                finishDialog.dismiss();
                dialogShowing = false;
                trip.stop();

                Intent stopServiceIntent = new Intent(this, TimerService.class);
                stopServiceIntent.setAction(Globals.ACTION.STOPFOREGROUND_ACTION);
                startService(stopServiceIntent);

                this.finish();
                break;
            case FinishTripDialog.CLICKED_CANCEL_BUTTON:
                finishDialog.dismiss();
                dialogShowing = false;
                trip.reprepareForResume();
                showFinishCancelledDialog();
                break;
            case FinishTripDialog.DIALOG_CANCELLED:
                dialogShowing = false;
                trip.reprepareForResume();
                showFinishCancelledDialog();
                break;
            case FinishTripDialog.TRIP_DELETE:
                dialogShowing = false;
                finishDialog.dismiss();

                MyApplication.getStaticDbHelper().deleteAllSubtripsForTrip(trip._id);
                MyApplication.getStaticDbHelper().deleteTrip(trip._id);
                Globals.currentTrip = null;
                trip = null;
                Intent stopIntent = new Intent(this, TimerService.class);
                stopIntent.setAction(Globals.ACTION.STOPFOREGROUND_ACTION);
                startService(stopIntent);

                this.finish();
                break;
        }
    }

    public void showFinishCancelledDialog() {
        cancelledFinishDialog = new Dialog(this);
        cancelledFinishDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cancelledFinishDialog.setContentView(R.layout.cancel_finish_dialog_layout);
        Toolbar toolbar = (Toolbar)cancelledFinishDialog.findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_launcher);
        toolbar.setTitle("Finish Cancelled");

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TimerService.class);
                intent.putExtra("night",trip.isNightTrip);
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
                cancelledFinishDialog.dismiss();
                finish();
            }
        };

        cancelledFinishDialog.findViewById(R.id.playButton).setOnClickListener(onClickListener);
        cancelledFinishDialog.findViewById(R.id.pauseButton).setOnClickListener(onClickListener);
        cancelledFinishDialog.findViewById(R.id.stopButton).setOnClickListener(onClickListener);

        cancelledFinishDialog.setCancelable(false);
        cancelledFinishDialog.setCanceledOnTouchOutside(false);

        cancelledFinishDialog.show();
    }
/*
loadingDialog = new ProgressDialog(getActivity());
            loadingDialog.setTitle("Calculating trip");
            loadingDialog.setMessage("Please Wait....");
            loadingDialog.setCancelable(false);
            loadingDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    loadingDialog.dismiss();
                    // TODO add cancel STOP and STOP BROADCASTS
                }
            });
            this.dismiss();
            loadingDialog.show();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.dismiss();
                    dialog.show();
                }
            }, 3000);
 */

    @Override
    protected void onDestroy() {
        if (finishDialog != null) finishDialog.dismiss();
        if (cancelledFinishDialog != null) cancelledFinishDialog.dismiss();
        finishDialog = null;
        cancelledFinishDialog = null;
        super.onDestroy();
    }
}
