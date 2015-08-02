package com.burke.kelv.timerdriving;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.Inflater;

import static com.burke.kelv.timerdriving.Globals.tripChangedStateBroadcast;

/**
 * Created by kelv on 25/04/2015.
 */
public class DetailsFragment extends Fragment implements View.OnClickListener {
    ViewGroup container;
    LayoutInflater inflater;

    public DetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containerGiven, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = containerGiven;

        ViewGroup overlayView = (ViewGroup) inflater.inflate(R.layout.no_trip_overlay, container, false);
        ((ViewGroup)overlayView.findViewById(R.id.container)).addView(inflater.inflate(R.layout.trip_details_fragment,container,false));

        return setupViews(overlayView,Globals.currentTrip);
    }

    private BroadcastReceiver changeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupViews(getView(),Globals.currentTrip);
            //saveDetails(getView(),Globals.currentTrip);
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupViews(getView(),Globals.currentTrip);

        IntentFilter intentFilter = new IntentFilter(tripChangedStateBroadcast);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(changeReceiver, intentFilter );
    }

    // TODO ADD AUTO SAVE AND ODO

    public View setupViews(final View view, final KTrip trip){
        if (trip == null || trip.status == KTrip.STATUS.FINISHED) {
            View overlayTextView = view.findViewById(R.id.overlayTV);
            if (overlayTextView != null) {
                overlayTextView.setVisibility(View.VISIBLE);
                overlayTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                overlayTextView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            }
            return view;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View overlayTextView = view.findViewById(R.id.overlayTV);
                if (overlayTextView != null) overlayTextView.setVisibility(View.GONE);

                refreshNightSwitch(trip,view);
                ((Switch) view.findViewById(R.id.parkingSwitch)).setChecked(trip.parking);

                int traffic = trip.traffic;
                ((Switch) view.findViewById(R.id.lightTrafficSwitch)).setChecked(ConversionHelper.isLightTraffic(traffic));
                ((Switch) view.findViewById(R.id.mediumTrafficSwitch)).setChecked(ConversionHelper.isMediumTraffic(traffic));
                ((Switch) view.findViewById(R.id.heavyTrafficSwitch)).setChecked(ConversionHelper.isHeavyTraffic(traffic));

                int weather = trip.weather;
                ((Switch) view.findViewById(R.id.dryWeatherSwitch)).setChecked(ConversionHelper.isDryWeather(weather));
                ((Switch) view.findViewById(R.id.wetWeatherSwitch)).setChecked(ConversionHelper.isWetWeather(weather));

                int timeOfDay = trip.timeOfDay;
                ((Switch) view.findViewById(R.id.daySwitch)).setChecked(ConversionHelper.isDayTime(timeOfDay));
                ((Switch) view.findViewById(R.id.dawnSwitch)).setChecked(ConversionHelper.isDawnDuskTime(timeOfDay));
                ((Switch) view.findViewById(R.id.nightLightSwitch)).setChecked(ConversionHelper.isNightTime(timeOfDay));

                if (trip.odometerStart != 0) ((EditText) view.findViewById(R.id.odoStartET)).setText(""+trip.odometerStart);
                if (trip.odometerEnd != 0) ((EditText) view.findViewById(R.id.odoEndET)).setText(""+trip.odometerEnd);
                else ((EditText) view.findViewById(R.id.odoEndET)).setText("");

                loadRoadType(trip,view);
                setupOnClickListeners(view,trip);
            }
        });
        return view;
    }

    public void setupOnClickListeners(final View view, final KTrip trip) {
        view.findViewById(R.id.roadTypeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRoadTypeDialog(trip);
            }
        });
        ((Switch)view.findViewById(R.id.nightSwitch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showChangeNightDialog(isChecked,trip);
            }
        });
        CompoundButton.OnCheckedChangeListener saveSwitchListener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveDetails(getView(),Globals.currentTrip);
            }
        };
        int[] switchIds = { R.id.parkingSwitch , R.id.lightTrafficSwitch, R.id.mediumTrafficSwitch, R.id.heavyTrafficSwitch,
                R.id.dryWeatherSwitch,R.id.wetWeatherSwitch,R.id.daySwitch,R.id.nightLightSwitch,R.id.dawnSwitch};
        for (int id : switchIds) {
            ((Switch) view.findViewById(id)).setOnCheckedChangeListener(saveSwitchListener);
        }
    }

    public void showRoadTypeDialog(final KTrip trip) {
        final Dialog roadTypeDialog = new Dialog(getActivity());
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
                saveRoadTypes(view.getRootView(),trip);
                loadRoadType(trip,getView());
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

    public void saveRoadTypes(View dialogContainer, KTrip trip) {
        Integer[] ids = new Integer[] { R.id.localStTick,R.id.mainRdTick,R.id.innerCityTick,R.id.freewayTick,R.id.ruralHwyTick,R.id.ruralOthTick,R.id.gravelTick };
        ArrayList<Boolean> bools = new ArrayList<>(8);
        for (Integer id : ids) {
            bools.add(dialogContainer.findViewById(id).getVisibility()==View.VISIBLE);
        }
        int roadType = ConversionHelper.getRoadTypeFromBools(bools.toArray(new Boolean[bools.size()]));
        trip.roadTypeID = roadType;
        MyApplication.getStaticDbHelper().updateTripSingleColumn(trip._id,DBHelper.TRIP.KEY_ROAD_TYPE,trip.roadTypeID);
    }

    public void loadRoadType(KTrip trip, View view){
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
        ((TextView) view.findViewById(R.id.roadTypeTV)).setText(strBuilder.toString());
    }

    public void showChangeNightDialog(final boolean newIsNight, final KTrip trip) {
        String message = newIsNight ? "Are you sure you want to make this a night trip?" :
                "Are you sure you want to make this a day trip?";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle("Confirm Change")
                .setPositiveButton("SAVE",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveIsNightTrip(newIsNight,trip);
                        dialogInterface.dismiss();

                        Intent intent = new Intent(Globals.tripChangedStateBroadcast);
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                    }
                })
                .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        refreshNightSwitch(trip,getView());
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

    public void saveIsNightTrip(boolean isNight,KTrip trip) {
        trip.isNightTrip = isNight;
        MyApplication.getStaticDbHelper().updateTripSingleColumn(trip._id,DBHelper.TRIP.KEY_IS_NIGHT,ConversionHelper.boolToInt(trip.isNightTrip));
    }

    public void refreshNightSwitch(final KTrip trip, View view) {
        Switch nightSwitch = ((Switch) view.findViewById(R.id.nightSwitch));
        nightSwitch.setOnCheckedChangeListener(null);

        ((Switch) view.findViewById(R.id.nightSwitch)).setChecked(trip.isNightTrip);

        nightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showChangeNightDialog(isChecked,trip);
            }
        });
    }

    public void saveDetails(View view, KTrip trip){
        Log.w(Globals.LOG,"SAVE");
        if (trip != null) {
            boolean odometerStartEmpty = ((TextView) view.findViewById(R.id.odoStartET)).getText().length() == 0;
            boolean odometerEndEmpty = ((TextView) view.findViewById(R.id.odoEndET)).getText().length() == 0;

            if (!odometerStartEmpty)
                trip.odometerStart = Integer.parseInt("" + ((TextView) view.findViewById(R.id.odoStartET)).getText());
            if (!odometerEndEmpty)
                trip.odometerEnd = Integer.parseInt("" + ((TextView) view.findViewById(R.id.odoEndET)).getText());
            trip.parking = ((Switch) view.findViewById(R.id.parkingSwitch)).isChecked();

            boolean isLightTraffic = ((Switch) view.findViewById(R.id.lightTrafficSwitch)).isChecked();
            boolean isMediumTraffic = ((Switch) view.findViewById(R.id.mediumTrafficSwitch)).isChecked();
            boolean isHeavyTraffic = ((Switch) view.findViewById(R.id.heavyTrafficSwitch)).isChecked();
            trip.traffic = ConversionHelper.getTrafficFromBools(isLightTraffic, isMediumTraffic, isHeavyTraffic);

            boolean isDryWeather = ((Switch) view.findViewById(R.id.dryWeatherSwitch)).isChecked();
            boolean isWetWeather = ((Switch) view.findViewById(R.id.wetWeatherSwitch)).isChecked();
            trip.weather = ConversionHelper.getWeatherFromBools(isWetWeather, isDryWeather);

            boolean isDayTime = ((Switch) view.findViewById(R.id.daySwitch)).isChecked();
            boolean isDawnDusk = ((Switch) view.findViewById(R.id.dawnSwitch)).isChecked();
            boolean isNightTime = ((Switch) view.findViewById(R.id.nightLightSwitch)).isChecked();
            trip.timeOfDay = ConversionHelper.getTimeOfDayFromBools(isDayTime, isDawnDusk, isNightTime);

            trip.updateAllRunningColumnsInDB();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //saveDetails(getView(),Globals.currentTrip);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(changeReceiver);
    }

    public static class CarSelectorDialogFragment extends DialogFragment {

        public static CarSelectorDialogFragment newInstance(int title) {
            CarSelectorDialogFragment frag = new CarSelectorDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_launcher)
                    .setTitle(title)
                    .setPositiveButton("Save",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //  ((FragmentAlertDialog)getActivity()).doPositiveClick();
                                }
                            }
                    )
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //    ((FragmentAlertDialog)getActivity()).doNegativeClick();
                                }
                            }
                    )
                    .create();
        }
    }
}