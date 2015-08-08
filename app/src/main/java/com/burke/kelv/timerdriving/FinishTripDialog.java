package com.burke.kelv.timerdriving;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

/**
 * Created by kelv on 25/04/2015.
 */
public class FinishTripDialog extends DialogFragment {
    private ProgressDialog loadingDialog;
    private Dialog dialog;
    private FinishTripActivity callbacks;
    private KTrip trip;
    private Dialog roadTypeDialog;

    private int distance;

    private Boolean odoStartSetFirst = null;

    public static final int CLICKED_SAVE_BUTTON = 1;
    public static final int CLICKED_CANCEL_BUTTON = 2;
    public static final int DIALOG_CANCELLED = 3;
    public static final int TRIP_DELETE = 4;

    public void setCallbacks(FinishTripActivity callbacks) {
        this.callbacks = callbacks;
    }
    public void setTrip(KTrip trip) { this.trip = trip; }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.finish_trip_layout);


        setupViews(dialog);
        setupOnClickListeners(dialog);

        Toolbar toolbar = (Toolbar) dialog.findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_launcher);
        toolbar.setTitle("Confirm Trip Details");

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    public void setupOnClickListeners(final Dialog dialog) {
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callbacks != null) {
                    callbacks.finishTripDialogCallbacks(CLICKED_SAVE_BUTTON);
                }
            }
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callbacks != null) {
                    callbacks.finishTripDialogCallbacks(CLICKED_CANCEL_BUTTON);
                }
            }
        });
        dialog.findViewById(R.id.editTimesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("llolol","logged");
            }
        });
        dialog.findViewById(R.id.roadTypeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRoadTypeDialog();
            }
        });
        ((SwitchCompat)dialog.findViewById(R.id.nightSwitch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showChangeNightDialog(isChecked);
            }
        });
        dialog.findViewById(R.id.deleteTripButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteTripDialog();
            }
        });
        ((EditText) dialog.findViewById(R.id.odoStartET)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    if (((EditText) getDialog().findViewById(R.id.odoEndET)).getText().toString().equals("")) {
                        odoStartSetFirst = null;
                        return;
                    }
                    else {
                        odoStartSetFirst = false;
                        setDistance(distance);
                    }
                }
                if (odoStartSetFirst == null || odoStartSetFirst) {
                    odoStartSetFirst = true;
                    try {
                        int start1 = Integer.parseInt(s.toString());
                        int end = start1 + distance;
                        ((EditText) getDialog().findViewById(R.id.odoEndET)).setText(""+end);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        ((EditText) dialog.findViewById(R.id.odoEndET)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    if (((EditText) getDialog().findViewById(R.id.odoStartET)).getText().toString().equals("")) {
                        odoStartSetFirst = null;
                        return;
                    }
                    else {
                        odoStartSetFirst = true;
                        setDistance(distance);
                    }
                }
                if (odoStartSetFirst == null || !odoStartSetFirst) {
                    odoStartSetFirst = false;
                    try {
                        int end2 = Integer.parseInt(s.toString());
                        int start2 = end2 - distance;
                        if (start2 > -1)
                            ((EditText) getDialog().findViewById(R.id.odoStartET)).setText(""+start2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    public void setupViews(final Dialog dialog){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)dialog.findViewById(R.id.dateTV)).setText(KTime.getProperReadable(trip.realStartedTime,Globals.TIMEFORMAT.WORDED_DATE));

                String start = KTime.getProperReadable(trip.apparentStartTime,Globals.TIMEFORMAT.H_MM);
                String end = KTime.getProperReadable(trip.apparentEndTime,Globals.TIMEFORMAT.H_MM);
                String total = KTime.getProperReadable(trip.apparentTotalTime,Globals.TIMEFORMAT.H_MM);
                ((TextView)dialog.findViewById(R.id.timeTV)).setText(start + " - " + end + "  (" + total + ")");

                updateTotalDrivingTextViews();

                ((SwitchCompat) dialog.findViewById(R.id.nightSwitch)).setChecked(trip.isNightTrip);
                ((SwitchCompat) dialog.findViewById(R.id.parkingSwitch)).setChecked(trip.parking);

                int traffic = trip.traffic;
                ((SwitchCompat) dialog.findViewById(R.id.lightTrafficSwitch)).setChecked(ConversionHelper.isLightTraffic(traffic));
                ((SwitchCompat) dialog.findViewById(R.id.mediumTrafficSwitch)).setChecked(ConversionHelper.isMediumTraffic(traffic));
                ((SwitchCompat) dialog.findViewById(R.id.heavyTrafficSwitch)).setChecked(ConversionHelper.isHeavyTraffic(traffic));

                int weather = trip.weather;
                ((SwitchCompat) dialog.findViewById(R.id.dryWeatherSwitch)).setChecked(ConversionHelper.isDryWeather(weather));
                ((SwitchCompat) dialog.findViewById(R.id.wetWeatherSwitch)).setChecked(ConversionHelper.isWetWeather(weather));

                int timeOfDay = trip.timeOfDay;
                ((SwitchCompat) dialog.findViewById(R.id.daySwitch)).setChecked(ConversionHelper.isDayTime(timeOfDay));
                ((SwitchCompat) dialog.findViewById(R.id.dawnSwitch)).setChecked(ConversionHelper.isDawnDuskTime(timeOfDay));
                ((SwitchCompat) dialog.findViewById(R.id.nightLightSwitch)).setChecked(ConversionHelper.isNightTime(timeOfDay));

                if (trip.odometerStart != 0) ((EditText) dialog.findViewById(R.id.odoStartET)).setText("" + trip.odometerStart);
                if (trip.odometerEnd != 0)((EditText) dialog.findViewById(R.id.odoEndET)).setText(""+trip.odometerEnd);

                loadRoadType();
            }
        });
    }
    // TODO ODOMETER AND DISTANCE STUFF

    public void setDistance(int kms) {
        distance = kms;
        ((TextView)dialog.findViewById(R.id.distanceTV)).setText("Distance: " + kms + " kms");

        try {
            if (odoStartSetFirst == null) {
                if (trip.odometerStart != 0) {
                    ((EditText) getDialog().findViewById(R.id.odoStartET)).setText(""+trip.odometerStart);
                    ((EditText) getDialog().findViewById(R.id.odoEndET)).setText(""+(trip.odometerStart+kms));
                    odoStartSetFirst = null;
                }
            } else if (odoStartSetFirst) {
                int start = Integer.parseInt(((EditText) getActivity().findViewById(R.id.odoStartET)).getText().toString());
                ((EditText) getActivity().findViewById(R.id.odoEndET)).setText(""+(start + kms));
            } else {
                int end = Integer.parseInt(((EditText) getActivity().findViewById(R.id.odoEndET)).getText().toString());
                ((EditText) getActivity().findViewById(R.id.odoStartET)).setText(""+(end - kms));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTotalDrivingTextViews() {
        trip.updateTotalDrivingAfterTime();
        trip.updateTotalNightAfterTime();
        String totalDriving = KTime.getProperReadable(trip.totalDrivingAfterTime,Globals.TIMEFORMAT.H_MM);
        String totalNightDriving = KTime.getProperReadable(trip.totalNightAfterTime,Globals.TIMEFORMAT.H_MM);
        ((TextView)dialog.findViewById(R.id.totalDrivingTV)).setText("Total: " +totalDriving);
        ((TextView)dialog.findViewById(R.id.totalNightTV)).setText("( Night: " + totalNightDriving + " )");
    }
    public void saveDetails(){
        boolean odometerStartEmpty = ((TextView) dialog.findViewById(R.id.odoStartET)).getText().length() == 0;
        boolean odometerEndEmpty = ((TextView) dialog.findViewById(R.id.odoEndET)).getText().length() == 0;

        if (!odometerStartEmpty) trip.odometerStart = Integer.parseInt(""+((TextView) dialog.findViewById(R.id.odoStartET)).getText());
        if (!odometerEndEmpty )trip.odometerEnd = Integer.parseInt(""+((TextView) dialog.findViewById(R.id.odoEndET)).getText());
        trip.parking = ((SwitchCompat) dialog.findViewById(R.id.parkingSwitch)).isChecked();

        boolean isLightTraffic = ((SwitchCompat) dialog.findViewById(R.id.lightTrafficSwitch)).isChecked();
        boolean isMediumTraffic = ((SwitchCompat) dialog.findViewById(R.id.mediumTrafficSwitch)).isChecked();
        boolean isHeavyTraffic = ((SwitchCompat) dialog.findViewById(R.id.heavyTrafficSwitch)).isChecked();
        trip.traffic = ConversionHelper.getTrafficFromBools(isLightTraffic,isMediumTraffic,isHeavyTraffic);

        boolean isDryWeather = ((SwitchCompat) dialog.findViewById(R.id.dryWeatherSwitch)).isChecked();
        boolean isWetWeather = ((SwitchCompat) dialog.findViewById(R.id.wetWeatherSwitch)).isChecked();
        trip.weather = ConversionHelper.getWeatherFromBools(isWetWeather,isDryWeather);
        Log.w("weather","dry: "+isDryWeather + " wet: " + isWetWeather + " int:" + trip.weather);

        boolean isDayTime = ((SwitchCompat) dialog.findViewById(R.id.daySwitch)).isChecked();
        boolean isDawnDusk = ((SwitchCompat) dialog.findViewById(R.id.dawnSwitch)).isChecked();
        boolean isNightTime = ((SwitchCompat) dialog.findViewById(R.id.nightLightSwitch)).isChecked();
        trip.timeOfDay = ConversionHelper.getTimeOfDayFromBools(isDayTime,isDawnDusk,isNightTime);

        trip.updateAllColumnsInDatabase();
    }

    public void loadRoadType(){
        Boolean[] roadBools = ConversionHelper.getRoadTypeBoolsFromInt(trip.roadTypeID);
        int i = 0;
        StringBuilder strBuilder = new StringBuilder();
        for (int l = 0; l < roadBools.length; l++) {
            if (roadBools[l]) {
                String type = ConversionHelper.roadTypeIntToShortString(l);
                if (i == 0) {
                    strBuilder.append(type);
                    i++;
                    continue;
                }
                if (i==1) {
                    strBuilder.append(", ");
                    strBuilder.append(type);
                    i++;
                    continue;
                }
                if (i==2) {
                    strBuilder.append(",\n");
                    strBuilder.append(type);
                    i=1;
                }
            }
        }
        ((TextView) dialog.findViewById(R.id.roadTypeTV)).setText(strBuilder.toString());
    }
    public void showRoadTypeDialog() {
        roadTypeDialog = new Dialog(getActivity());
        roadTypeDialog.setContentView(R.layout.road_type_dialog_layout);
        roadTypeDialog.setTitle("Road Types");

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                switch (id) {
                    case R.id.localStButton:
                        View tickViewa = view.getRootView().findViewById(R.id.localStTick);
                        tickViewa.setVisibility(tickViewa.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                        break;
                    case R.id.mainRdButton:
                        View tickViewb = view.getRootView().findViewById(R.id.mainRdTick);
                        tickViewb.setVisibility(tickViewb.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                        break;
                    case R.id.innerCityButton:
                        View tickViewc = view.getRootView().findViewById(R.id.innerCityTick);
                        tickViewc.setVisibility(tickViewc.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                        break;
                    case R.id.freewayButton:
                        View tickViewd = view.getRootView().findViewById(R.id.freewayTick);
                        tickViewd.setVisibility(tickViewd.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                        break;
                    case R.id.ruralHwyButton:
                        View tickViewe = view.getRootView().findViewById(R.id.ruralHwyTick);
                        tickViewe.setVisibility(tickViewe.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                        break;
                    case R.id.ruralOthButton:
                        View tickViewf = view.getRootView().findViewById(R.id.ruralOthTick);
                        tickViewf.setVisibility(tickViewf.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                        break;
                    case R.id.gravelButton:
                        View tickViewg = view.getRootView().findViewById(R.id.gravelTick);
                        tickViewg.setVisibility(tickViewg.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                        break;
                }
            }
        };

        roadTypeDialog.findViewById(R.id.localStButton).setOnClickListener(onClickListener);
        roadTypeDialog.findViewById(R.id.mainRdButton).setOnClickListener(onClickListener);
        roadTypeDialog.findViewById(R.id.innerCityButton).setOnClickListener(onClickListener);
        roadTypeDialog.findViewById(R.id.freewayButton).setOnClickListener(onClickListener);
        roadTypeDialog.findViewById(R.id.ruralHwyButton).setOnClickListener(onClickListener);
        roadTypeDialog.findViewById(R.id.ruralOthButton).setOnClickListener(onClickListener);
        roadTypeDialog.findViewById(R.id.gravelButton).setOnClickListener(onClickListener);

        roadTypeDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveRoadTypes(view.getRootView());
                loadRoadType();
                roadTypeDialog.dismiss();
            }
        });
        roadTypeDialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roadTypeDialog.dismiss();
            }
        });
        final Boolean[] roadTypeBools = ConversionHelper.getRoadTypeBoolsFromInt(trip.roadTypeID);
        Integer[] ids = new Integer[] { R.id.localStTick,R.id.mainRdTick,R.id.innerCityTick,R.id.freewayTick,R.id.ruralHwyTick,R.id.ruralOthTick,R.id.gravelTick };
        for (int i=0; i<ids.length; i++) {
            roadTypeDialog.findViewById(ids[i]).setVisibility(roadTypeBools[i] ? View.VISIBLE : View.INVISIBLE);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roadTypeDialog.show();
            }
        });

        roadTypeDialog.show();
    }
    public void saveRoadTypes(View dialogContainer) {
        Integer[] ids = new Integer[] { R.id.localStTick,R.id.mainRdTick,R.id.innerCityTick,R.id.freewayTick,R.id.ruralHwyTick,R.id.ruralOthTick,R.id.gravelTick };
        ArrayList<Boolean> bools = new ArrayList<>(8);
        for (Integer id : ids) {
            bools.add(dialogContainer.findViewById(id).getVisibility()==View.VISIBLE);
        }
        int roadType = ConversionHelper.getRoadTypeFromBools(bools.toArray(new Boolean[bools.size()]));
        trip.roadTypeID = roadType;
        MyApplication.getStaticDbHelper().updateTripSingleColumn(trip._id,DBHelper.TRIP.KEY_ROAD_TYPE,trip.roadTypeID);
    }

    public void showChangeNightDialog(final boolean newIsNight) {
        String message = newIsNight ? "Are you sure you want to make this a night trip?" :
                "Are you sure you want to make this a day trip?";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle("Confirm Change")
                .setPositiveButton("SAVE",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveIsNightTrip(newIsNight);
                        updateTotalDrivingTextViews();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        refreshNightSwitch();
                        updateTotalDrivingTextViews();
                        dialogInterface.dismiss();
                    }
                });

        final AlertDialog.Builder builder1 = builder;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder1.show();
            }
        });
    }
    public void saveIsNightTrip(boolean isNight) {
        trip.isNightTrip = isNight;
        MyApplication.getStaticDbHelper().updateTripSingleColumn(trip._id,DBHelper.TRIP.KEY_IS_NIGHT,ConversionHelper.boolToInt(trip.isNightTrip));
    }
    public void refreshNightSwitch() {
        SwitchCompat nightSwitch = ((SwitchCompat) dialog.findViewById(R.id.nightSwitch));
        nightSwitch.setOnCheckedChangeListener(null);

        ((SwitchCompat) dialog.findViewById(R.id.nightSwitch)).setChecked(trip.isNightTrip);

        nightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showChangeNightDialog(isChecked);
            }
        });
    }

    public void showDeleteTripDialog() {
        final String message = "Are you sure you want to delete this trip?";
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MaterialDialog.Builder(getActivity())
                        .title("Confirm Delete")
                        .content(message)
                        .positiveText("Delete")
                        .negativeText("Cancel")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                if (callbacks != null) {
                                    callbacks.finishTripDialogCallbacks(TRIP_DELETE);
                                }
                            }
                        })
                        .show();
            }
        });
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(Globals.LOG,"onDismiss");
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(Globals.LOG,"onCancel");
        if (callbacks != null) callbacks.finishTripDialogCallbacks(DIALOG_CANCELLED);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() == null) { Log.e(Globals.LOG, "errorrr"); }
        else {

        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }


}
