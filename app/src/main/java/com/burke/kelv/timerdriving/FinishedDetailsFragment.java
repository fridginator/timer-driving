package com.burke.kelv.timerdriving;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

/**
 * Created by kelv on 7/07/2015.
 */

public class FinishedDetailsFragment extends Fragment {
    private int tripId;
    private ArrayList<Change> changes;
    private Dialog roadTypeDialog;
    private LinearLayout linearLayout;
    private Space space;
    private View divider;

    static FinishedDetailsFragment newInstance(int tripID) {
        FinishedDetailsFragment f = new FinishedDetailsFragment();

        Bundle args = new Bundle();
        args.putInt("TRIP_ID", tripID);
        f.setArguments(args);

        return f;
    }

    public FinishedDetailsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripId = 1;
        changes = new ArrayList<>();
        if (getArguments() != null) {
            Bundle arguments = getArguments();
            tripId = arguments.getInt("TRIP_ID");

            String changesTypes = arguments.getString("CHANGES_TYPES","");
            if (changesTypes.contains("1")) changes.add(new Change(Change.PARKING,arguments.getInt("1")));
            if (changesTypes.contains("2")) changes.add(new Change(Change.SUPERVISOR,arguments.getInt("2")));
            if (changesTypes.contains("3")) changes.add(new Change(Change.CAR,arguments.getInt("3")));
            if (changesTypes.contains("4")) changes.add(new Change(Change.ODO_START,arguments.getInt("4")));
            if (changesTypes.contains("5")) changes.add(new Change(Change.TRAFFIC,arguments.getInt("5")));
            if (changesTypes.contains("6")) changes.add(new Change(Change.WEATHER,arguments.getInt("6")));
            if (changesTypes.contains("7")) changes.add(new Change(Change.ODO_END,arguments.getInt("7")));
            if (changesTypes.contains("8")) changes.add(new Change(Change.LIGHT,arguments.getInt("8")));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.finish_trip_layout,container,false);
        setupViews(view, MyApplication.getStaticDbHelper().getTripCursor(tripId), MyApplication.getStaticDbHelper());
        return view;
    }

    public void setupViews(final View view,final Cursor c,final DBHelper dbHelper){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (c != null && c.moveToFirst()) {
                    linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout2);
                    divider = view.findViewById(R.id.divider8);
                    space = (Space) view.findViewById(R.id.space);

                    updateSaveBar();

                    ((TextView) view.findViewById(R.id.dateTV)).setText(KTime.getProperReadable(
                            dbHelper.getDBTime(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_REAl_START))), Globals.TIMEFORMAT.WORDED_DATE));

                    String start = KTime.getProperReadable(
                            dbHelper.getDBTime(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_APPA_START))), Globals.TIMEFORMAT.H_MM);
                    String end = KTime.getProperReadable(
                            dbHelper.getDBTime(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_APPA_END))), Globals.TIMEFORMAT.H_MM);
                    String total = KTime.getProperReadable(
                            dbHelper.getDBTime(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_APPA_TOTAL))), Globals.TIMEFORMAT.H_MM);
                    ((TextView) view.findViewById(R.id.timeTV)).setText(start + " - " + end + "  (" + total + ")");

                    updateTotalDrivingTextViews(c,dbHelper,view);


                    ((SwitchCompat) view.findViewById(R.id.nightSwitch)).setChecked(
                            dbHelper.tripIsNightTrip(tripId));

                    boolean parking = getChangeForType(Change.PARKING) == null ?
                            ConversionHelper.intToBool(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_PARKING)))
                            : ConversionHelper.intToBool(getChangeForType(Change.PARKING).getNewValue());
                    Log.w("parking","parking:"+parking+" isChangenull:"+(getChangeForType(Change.PARKING) == null)+" dbh:"+ConversionHelper.intToBool(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_PARKING))));
                    ((SwitchCompat) view.findViewById(R.id.parkingSwitch)).setChecked(parking);

                    int traffic = getChangeForType(Change.TRAFFIC) == null ? c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_TRAFFIC))
                            : getChangeForType(Change.TRAFFIC).getNewValue();
                    ((SwitchCompat) view.findViewById(R.id.lightTrafficSwitch)).setChecked(ConversionHelper.isLightTraffic(traffic));
                    ((SwitchCompat) view.findViewById(R.id.mediumTrafficSwitch)).setChecked(ConversionHelper.isMediumTraffic(traffic));
                    ((SwitchCompat) view.findViewById(R.id.heavyTrafficSwitch)).setChecked(ConversionHelper.isHeavyTraffic(traffic));

                    int weather = getChangeForType(Change.WEATHER) == null ? c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_WEATHER))
                            : getChangeForType(Change.WEATHER).getNewValue();
                    ((SwitchCompat) view.findViewById(R.id.dryWeatherSwitch)).setChecked(ConversionHelper.isDryWeather(weather));
                    ((SwitchCompat) view.findViewById(R.id.wetWeatherSwitch)).setChecked(ConversionHelper.isWetWeather(weather));

                    int timeOfDay = getChangeForType(Change.LIGHT) == null ? c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_LIGHT))
                            : getChangeForType(Change.LIGHT).getNewValue();
                    ((SwitchCompat) view.findViewById(R.id.daySwitch)).setChecked(ConversionHelper.isDayTime(timeOfDay));
                    ((SwitchCompat) view.findViewById(R.id.dawnSwitch)).setChecked(ConversionHelper.isDawnDuskTime(timeOfDay));
                    ((SwitchCompat) view.findViewById(R.id.nightLightSwitch)).setChecked(ConversionHelper.isNightTime(timeOfDay));

                    int metres = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_DISTANCE));
                    ((TextView)view.findViewById(R.id.distanceTV)).setText("Distance: " +
                            Math.round(metres/1000) + " kms");

                    int originalOdoStart = 0;
                    int originalOdoEnd = 0;
                    try {
                        originalOdoStart = Integer.parseInt(((EditText) view.findViewById(R.id.odoStartET)).getText().toString());
                        originalOdoEnd = Integer.parseInt(((EditText) view.findViewById(R.id.odoEndET)).getText().toString());
                    } catch (Exception e ) {}

                    int odometerStart = getChangeForType(Change.ODO_START) == null ? c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ODO_START))
                            : getChangeForType(Change.ODO_START).getNewValue();
                    int odometerEnd = getChangeForType(Change.ODO_END) == null ? c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ODO_END))
                            : getChangeForType(Change.ODO_END).getNewValue();
                    if (odometerStart != 0 && odometerStart != originalOdoStart)
                        ((EditText) view.findViewById(R.id.odoStartET)).setText(""+odometerStart);
                    if (odometerEnd != 0 && odometerEnd != originalOdoEnd)
                        ((EditText) view.findViewById(R.id.odoEndET)).setText(""+odometerEnd);

                    loadRoadType(c,view);

                    setupOnClickListeners(view,c);

                    c.close();
                }
            }
        });
    }

    public void setupOnClickListeners(final View view, final Cursor c) {
        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDetails();
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelSave();
            }
        });
        view.findViewById(R.id.editTimesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        view.findViewById(R.id.roadTypeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRoadTypeDialog(MyApplication.getStaticDbHelper().getTripCursor(tripId));
            }
        });
        ((SwitchCompat)view.findViewById(R.id.nightSwitch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showChangeNightDialog(isChecked);
            }
        });
        final CompoundButton.OnCheckedChangeListener trafficListener = new CompoundButton.OnCheckedChangeListener() {
            int originalTraffic = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_TRAFFIC));
            SwitchCompat lightSwitch = (SwitchCompat) view.findViewById(R.id.lightTrafficSwitch);
            SwitchCompat mediumSwitch = (SwitchCompat) view.findViewById(R.id.mediumTrafficSwitch);
            SwitchCompat heavySwitch = (SwitchCompat) view.findViewById(R.id.heavyTrafficSwitch);
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int traffic = ConversionHelper.getTrafficFromBools(lightSwitch.isChecked(),mediumSwitch.isChecked(),heavySwitch.isChecked());
                deleteAllChangesOfType(Change.TRAFFIC);
                if (traffic == originalTraffic)  {updateSaveBar(); return; }
                changes.add(new Change(Change.TRAFFIC,traffic));
                updateSaveBar();
            }
        };
        ((SwitchCompat)view.findViewById(R.id.lightTrafficSwitch)).setOnCheckedChangeListener(trafficListener);
        ((SwitchCompat)view.findViewById(R.id.mediumTrafficSwitch)).setOnCheckedChangeListener(trafficListener);
        ((SwitchCompat)view.findViewById(R.id.heavyTrafficSwitch)).setOnCheckedChangeListener(trafficListener);

        CompoundButton.OnCheckedChangeListener parkingListener = new CompoundButton.OnCheckedChangeListener() {
            int originalParking = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_PARKING));
            SwitchCompat parkingSwitch = (SwitchCompat) view.findViewById(R.id.parkingSwitch);
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int parking = ConversionHelper.boolToInt(parkingSwitch.isChecked());
                deleteAllChangesOfType(Change.PARKING);
                if (parking == originalParking)  {updateSaveBar(); return; }
                changes.add(new Change(Change.PARKING,parking));
                updateSaveBar();
            }
        };
        ((SwitchCompat)view.findViewById(R.id.parkingSwitch)).setOnCheckedChangeListener(parkingListener);

        CompoundButton.OnCheckedChangeListener weatherListener = new CompoundButton.OnCheckedChangeListener() {
            int originalWeather = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_WEATHER));
            SwitchCompat wetSwitch = (SwitchCompat) view.findViewById(R.id.wetWeatherSwitch);
            SwitchCompat drySwitch = (SwitchCompat) view.findViewById(R.id.dryWeatherSwitch);
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int weather = ConversionHelper.getWeatherFromBools(wetSwitch.isChecked(),drySwitch.isChecked());
                deleteAllChangesOfType(Change.WEATHER);
                if (weather == originalWeather)  {updateSaveBar(); return; }
                changes.add(new Change(Change.WEATHER,weather));
                updateSaveBar();
            }
        };
        ((SwitchCompat)view.findViewById(R.id.wetWeatherSwitch)).setOnCheckedChangeListener(weatherListener);
        ((SwitchCompat)view.findViewById(R.id.dryWeatherSwitch)).setOnCheckedChangeListener(weatherListener);

        CompoundButton.OnCheckedChangeListener lightListener = new CompoundButton.OnCheckedChangeListener() {
            int originalLight = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_LIGHT));
            SwitchCompat daySwitch = (SwitchCompat) view.findViewById(R.id.daySwitch);
            SwitchCompat dawnSwitch = (SwitchCompat) view.findViewById(R.id.dawnSwitch);
            SwitchCompat nightSwitch = (SwitchCompat) view.findViewById(R.id.nightLightSwitch);
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int light = ConversionHelper.getTimeOfDayFromBools(daySwitch.isChecked(),dawnSwitch.isChecked(),nightSwitch.isChecked());
                deleteAllChangesOfType(Change.LIGHT);
                if (originalLight == light)  {updateSaveBar(); return; }
                changes.add(new Change(Change.LIGHT,light));
                updateSaveBar();
            }
        };
        ((SwitchCompat)view.findViewById(R.id.daySwitch)).setOnCheckedChangeListener(lightListener);
        ((SwitchCompat)view.findViewById(R.id.dawnSwitch)).setOnCheckedChangeListener(lightListener);
        ((SwitchCompat)view.findViewById(R.id.nightLightSwitch)).setOnCheckedChangeListener(lightListener);

        view.findViewById(R.id.deleteTripButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteTripDialog();
            }
        });

        ((EditText) view.findViewById(R.id.odoStartET)).addTextChangedListener(new TextWatcher() {
            int originalStart = c.getInt(c.getColumnIndex(DBHelper.TRIP.KEY_ODO_START));
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int newStart = Integer.parseInt(s.toString());
                    deleteAllChangesOfType(Change.ODO_START);
                    if (originalStart == newStart) {
                        updateSaveBar();
                        return;
                    }
                    changes.add(new Change(Change.ODO_START,newStart));
                    updateSaveBar();
                } catch (Exception e) {
                    e.printStackTrace();
                    deleteAllChangesOfType(Change.ODO_START);
                    updateSaveBar();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ((EditText) view.findViewById(R.id.odoEndET)).addTextChangedListener(new TextWatcher() {
            int originalEnd = c.getInt(c.getColumnIndex(DBHelper.TRIP.KEY_ODO_END));
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int newEnd = Integer.parseInt(s.toString());
                    deleteAllChangesOfType(Change.ODO_END);
                    if (originalEnd == newEnd) {
                        updateSaveBar();
                        return;
                    }
                    changes.add(new Change(Change.ODO_END,newEnd));
                    updateSaveBar();
                } catch (Exception e) {
                    e.printStackTrace();
                    deleteAllChangesOfType(Change.ODO_END);
                    updateSaveBar();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
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
                                MyApplication.getStaticDbHelper().deleteAllSubtripsForTrip(tripId);
                                MyApplication.getStaticDbHelper().deleteTrip(tripId);
                                MyApplication.getStaticDbHelper().orderTripsAndCheckTotals();
                                if (Globals.currentTrip != null && tripId == Globals.currentTrip._id) {
                                    Globals.currentTrip.stop();
                                    Globals.currentTrip = null;
                                    Intent stopIntent = new Intent(getActivity(), TimerService.class);
                                    stopIntent.setAction(Globals.ACTION.STOPFOREGROUND_ACTION);
                                    getActivity().startService(stopIntent);
                                }

                                Cursor c = MyApplication.getStaticDbHelper().getLastFinishedTrip(tripId);
                                if (c != null && c.moveToFirst()) {
                                    int id = c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ROWID));
                                    Intent intent = new Intent(getActivity().getApplicationContext(), FinishedDetailsActivity.class);
                                    intent.putExtra(FinishedDetailsActivity.ORIGINAL_TRIP_EXTRA, id);
                                    getActivity().startActivity(intent);
                                }
                                getActivity().finish();
                            }
                        })
                        .show();
            }
        });
    }

    public void deleteAllChangesOfType(int type) {
        ArrayList<Change> newChanges = new ArrayList<>();
        for (Change change : changes) {
            if (change.getType() != type) newChanges.add(change);
        }
        changes.clear();
        changes = newChanges;
    }
    public Change getChangeForType(int type) {
        if (changes.isEmpty()) return null;
        for (Change change : changes) {
            Log.w("change","change type:"+change.getType()+ ", value:"+change.getNewValue());
            if (change.getType() == type) return change;
        }
        return null;
    }
    public void updateSaveBar() {
        if (changes.isEmpty()) hideSaveBar();
        else showSaveBar();
    }
    public void showSaveBar() {
        space.setVisibility(View.VISIBLE);
        if (linearLayout != null) {
            linearLayout.animate().translationY(0).alpha(1.0f);
            divider.animate().translationY(0).alpha(1.0f);
        }
    }
    public void hideSaveBar() {
        space.setVisibility(View.GONE);
        if (linearLayout != null) {
            linearLayout.animate().translationY(linearLayout.getHeight()).alpha(0.0f);
            divider.animate().translationY(linearLayout.getHeight()).alpha(0.0f);
        }
    }

    public void updateTotalDrivingTextViews(Cursor c,DBHelper dbHelper,View view) {
        String totalDriving = KTime.getProperReadable(
                dbHelper.getDBTime(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_DRIVING_TOTAL_AFTER))), Globals.TIMEFORMAT.H_MM);
        String totalNightDriving = KTime.getProperReadable(
                dbHelper.getDBTime(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_DRIVING_TOTAL_AFTER))), Globals.TIMEFORMAT.H_MM);
        ((TextView)view.findViewById(R.id.totalDrivingTV)).setText("Total: " +totalDriving);
        ((TextView)view.findViewById(R.id.totalNightTV)).setText("( Night: " + totalNightDriving + " )");
    }

    public void loadRoadType(Cursor c, View view){
        c.moveToFirst();
        Boolean[] roadBools = ConversionHelper.getRoadTypeBoolsFromInt(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ROAD_TYPE)));
        int i = 0;
        final StringBuilder strBuilder = new StringBuilder();
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
        TextView textView = ((TextView) view.findViewById(R.id.roadTypeTV));
        if (textView == null) {
            try {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = ((TextView) getActivity().findViewById(R.id.roadTypeTV));
                        textView.setText(strBuilder.toString());
                    }
                },200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else textView.setText(strBuilder.toString());
    }

    public void showRoadTypeDialog(Cursor c) {
        c.moveToFirst();
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
                roadTypeDialog.dismiss();
                loadRoadType(MyApplication.getStaticDbHelper().getTripCursor(tripId),view.getRootView());
            }
        });
        roadTypeDialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roadTypeDialog.dismiss();
            }
        });
        final Boolean[] roadTypeBools = ConversionHelper.getRoadTypeBoolsFromInt(c.getInt(c.getColumnIndexOrThrow(DBHelper.TRIP.KEY_ROAD_TYPE)));
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
        MyApplication.getStaticDbHelper().updateTripSingleColumn(tripId,DBHelper.TRIP.KEY_ROAD_TYPE,roadType);
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
                        updateTotalDrivingTextViews(MyApplication.getStaticDbHelper().getTripCursor(tripId),MyApplication.getStaticDbHelper(),getView());
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        refreshNightSwitch();
                        updateTotalDrivingTextViews(MyApplication.getStaticDbHelper().getTripCursor(tripId),MyApplication.getStaticDbHelper(),getView());
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
        MyApplication.getStaticDbHelper().updateTripSingleColumn(tripId,DBHelper.TRIP.KEY_IS_NIGHT,ConversionHelper.boolToInt(isNight));
    }
    public void refreshNightSwitch() {
        SwitchCompat nightSwitch = ((SwitchCompat) getActivity().findViewById(R.id.nightSwitch));
        nightSwitch.setOnCheckedChangeListener(null);

        ((SwitchCompat) getActivity().findViewById(R.id.nightSwitch)).setChecked(MyApplication.getStaticDbHelper().tripIsNightTrip(tripId));

        nightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showChangeNightDialog(isChecked);
            }
        });
    }

    public void saveDetails(){
        boolean odometerStartEmpty = ((TextView) getView().findViewById(R.id.odoStartET)).getText().length() == 0;
        boolean odometerEndEmpty = ((TextView) getView().findViewById(R.id.odoEndET)).getText().length() == 0;

        int odoStart = 0;
        int odoEnd = 0;
        if (!odometerStartEmpty) odoStart = Integer.parseInt(""+((TextView) getView().findViewById(R.id.odoStartET)).getText());
        if (!odometerEndEmpty ) odoEnd = Integer.parseInt(""+((TextView) getView().findViewById(R.id.odoEndET)).getText());
        boolean parking = ((SwitchCompat) getView().findViewById(R.id.parkingSwitch)).isChecked();

        boolean isLightTraffic = ((SwitchCompat) getView().findViewById(R.id.lightTrafficSwitch)).isChecked();
        boolean isMediumTraffic = ((SwitchCompat) getView().findViewById(R.id.mediumTrafficSwitch)).isChecked();
        boolean isHeavyTraffic = ((SwitchCompat) getView().findViewById(R.id.heavyTrafficSwitch)).isChecked();
        int traffic = ConversionHelper.getTrafficFromBools(isLightTraffic,isMediumTraffic,isHeavyTraffic);

        boolean isDryWeather = ((SwitchCompat) getView().findViewById(R.id.dryWeatherSwitch)).isChecked();
        boolean isWetWeather = ((SwitchCompat) getView().findViewById(R.id.wetWeatherSwitch)).isChecked();
        int weather = ConversionHelper.getWeatherFromBools(isWetWeather,isDryWeather);

        boolean isDayTime = ((SwitchCompat) getView().findViewById(R.id.daySwitch)).isChecked();
        boolean isDawnDusk = ((SwitchCompat) getView().findViewById(R.id.dawnSwitch)).isChecked();
        boolean isNightTime = ((SwitchCompat) getView().findViewById(R.id.nightLightSwitch)).isChecked();
        int timeOfDay = ConversionHelper.getTimeOfDayFromBools(isDayTime,isDawnDusk,isNightTime);

        DBHelper dbHelper = MyApplication.getStaticDbHelper();
        dbHelper.updateTripSingleColumn(tripId, DBHelper.TRIP.KEY_ODO_START,odoStart);
        dbHelper.updateTripSingleColumn(tripId, DBHelper.TRIP.KEY_ODO_END,odoEnd);
        dbHelper.updateTripSingleColumn(tripId, DBHelper.TRIP.KEY_PARKING,ConversionHelper.boolToInt(parking));
        dbHelper.updateTripSingleColumn(tripId, DBHelper.TRIP.KEY_TRAFFIC,traffic);
        dbHelper.updateTripSingleColumn(tripId, DBHelper.TRIP.KEY_WEATHER,weather);
        dbHelper.updateTripSingleColumn(tripId, DBHelper.TRIP.KEY_LIGHT,timeOfDay);

        changes.clear();
        setupViews(getView(),MyApplication.getStaticDbHelper().getTripCursor(tripId),MyApplication.getStaticDbHelper());
        updateSaveBar();
    }

    public void cancelSave() {
        changes.clear();
        updateSaveBar();
        setupViews(getView(),MyApplication.getStaticDbHelper().getTripCursor(tripId),MyApplication.getStaticDbHelper());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("CHANGES_TYPES",ConversionHelper.changesStringFromList(changes));
        Log.e("gdrg","onSaveInstanceState + trip:" + tripId + " chnage tyeps:" + ConversionHelper.changesStringFromList(changes));
        for (Change change : changes) {
            outState.putInt("" + change.getType(), change.getNewValue());
        }
        super.onSaveInstanceState(outState);
    }

    public static class Change {
        public static final int PARKING = 1;
        public static final int SUPERVISOR = 2;
        public static final int CAR = 3;
        public static final int ODO_START = 4;
        public static final int ODO_END = 7;
        public static final int TRAFFIC = 5;
        public static final int WEATHER = 6;
        public static final int LIGHT = 8;

        protected int type;
        protected int newValue;

        public Change(int type,int newValue) {
            this.type = type;
            this.newValue = newValue;
        }

        public void setNewValue(int newValue) {
            this.newValue = newValue;
        }

        public int getType() {  return this.type;   }
        public int getNewValue() {  return this.newValue;   }

        public static String typeToDBColumn(int type) {
            return type == PARKING ? DBHelper.TRIP.KEY_PARKING :
                    type == SUPERVISOR ? DBHelper.TRIP.KEY_PARENT :
                            type == CAR ? DBHelper.TRIP.KEY_CAR_ID :
                                    type == ODO_START ? DBHelper.TRIP.KEY_ODO_START :
                                            type == ODO_END ? DBHelper.TRIP.KEY_ODO_END :
                                                    type == TRAFFIC ? DBHelper.TRIP.KEY_TRAFFIC :
                                                            type == WEATHER ? DBHelper.TRIP.KEY_WEATHER :
                                                                    type == LIGHT? DBHelper.TRIP.KEY_LIGHT : "";
        }
    }
}
