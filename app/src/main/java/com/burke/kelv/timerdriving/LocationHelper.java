package com.burke.kelv.timerdriving;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kelv on 18/03/2015.
 */

public class LocationHelper implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
NetworkingHelper.DownloadCallbacks {
    public static final String LOG_TAG = "TIMERDRIVING LC_HELPER";

    private LocationRequest locationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Context context;
    private int interval;
    private int minBroadcastInterval;
    private boolean running;
    public boolean finished;
    private Intent intent;
    private Location bestLocation;
    private DBHelper dbHelper;
    private PowerManager.WakeLock wakeLock;
    private ArrayList<Location> locations;
    private ArrayList<Location> helperLocationArray;
    private int locationNumber;
    private int snappedNumber;

    private NetworkingHelper networkingHelper;

    private static boolean dialogShowing = false;
    private static boolean wifiSearchDisabled = false;

    public static final String API_KEY = "AIzaSyDLoZuZhalGsqENHJegv88qNLI12bq4gVQ";

    public LocationHelper(Context context, int intervalMilli, int broadcastMilli, String action, PowerManager.WakeLock wakeLock){
        this.interval = intervalMilli;
        this.minBroadcastInterval = broadcastMilli;
        this.mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        this.context = context;
        this.intent = new Intent(action);
        this.wakeLock = wakeLock;
        this.dbHelper = MyApplication.getStaticDbHelper();
        this.locations = new ArrayList<>();
        this.locationNumber = 0;
        this.snappedNumber = 0;
        this.networkingHelper = new NetworkingHelper(this,context,"lol");
        this.finished = false;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void start(DBHelper dbHelperr) {
        if (!running) {
            Log.i(LOG_TAG, "starting...");
            mGoogleApiClient.connect();
            running = true;
            dbHelper = dbHelperr;
            checkLocationServices();
        }
    }

    public void stop() {
        //showTurnOnDataDialog();
        if (running) {
            Log.i(LOG_TAG, "Stopping...");
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
                mGoogleApiClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            running = false;
            dbHelper = null;
        }
    }

    public void finish() {
        if (running || locations.size() != 0) {
            Log.i(LOG_TAG, "Finishing...");
            finished = true;
            stop();
            //combineLocationsAndSnap(locations,wakeLock);
            //Location[] locations1 = new Location[locations.size()];
            // locations1 = locations.toArray(locations1);
            if (false) {
                Location location = locations.get(0);
                Log.d(LocationHelper.LOG_TAG, "speed=" + location.getSpeed() + "lat: " + location.getLatitude() + ", long: " + location.getLongitude() +
                        ", accuracy: " + location.getAccuracy());
            }
            DistanceCalculator distanceCalc = new DistanceCalculator();
            distanceCalc.calculateDistanceBySnappingAndMeasuring(locations);
            //int distance = DistanceCalculator.calculateDistanceUsingSpeeds(locations);
            //Globals.currentTrip.addDistance(distance);
        }
    }

    public void restart(int delayMilli) {
        Log.i(LOG_TAG, "RESTARTING in "+delayMilli +"...");
        Handler handler = new Handler(context.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!finished) {
                    stop();
                    start(MyApplication.getStaticDbHelper());
                }
            }
        };
        handler.postDelayed(runnable,delayMilli);
    }

    public void loadLocationsFromDatabase(int tripID){
        if (locations == null) locations = new ArrayList<>();
        ArrayList<Location> oldList = null;
        if (!locations.isEmpty()) {
            oldList = locations;
        }
        locations.clear();
        Cursor locationsCursor = MyApplication.getStaticDbHelper().getDBLocations(tripID,"fused", DBHelper.LOCATION.KEY_NUMBER,DBHelper.ASCENDING);
        if (locationsCursor.moveToFirst()) {
            int latColm = locationsCursor.getColumnIndexOrThrow(DBHelper.LOCATION.KEY_LATITUDE);
            int longColm = locationsCursor.getColumnIndexOrThrow(DBHelper.LOCATION.KEY_LONGITUDE);
            int speedColm = locationsCursor.getColumnIndexOrThrow(DBHelper.LOCATION.KEY_SPEED);
            int accColm = locationsCursor.getColumnIndexOrThrow(DBHelper.LOCATION.KEY_ACCURACY);
            int providerColm = locationsCursor.getColumnIndexOrThrow(DBHelper.LOCATION.KEY_PROVIDER);
            do {
                Location location = new Location(locationsCursor.getString(providerColm));
                location.setLatitude(locationsCursor.getDouble(latColm));
                location.setLongitude(locationsCursor.getDouble(longColm));
                location.setSpeed(locationsCursor.getFloat(speedColm));
                location.setAccuracy(locationsCursor.getFloat(accColm));
                locations.add(location);
                locationNumber++;
            } while (locationsCursor.moveToNext());
        }
    }

    public void checkLocationServices() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        Log.w(Globals.LOG,"context: "+ context);
        if(!gps_enabled || !network_enabled) {
            if (!dialogShowing) {
                dialogShowing = true;

                final MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                        .title("GPS Not Enabled")
                        .content("Pretty pleeaassseeee turn on GPS.\nDolan pls")
                        .positiveText("Settings")
                        .negativeText("Back")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                restart(10000);
                                Intent intent1 = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent1);
                                dialogShowing = false;
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                restart(5000);
                                dialogShowing = false;
                            }
                        })
                        .cancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialogShowing = false;
                                restart(5000);
                            }
                        })
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                dialogShowing = false;
                                restart(1000);
                            }
                        });
                Handler mainHandler = new Handler(context.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MaterialDialog alert = builder.build();
                        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        alert.show();
                    }
                });
            }
        }

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled() && !dialogShowing && !wifiSearchDisabled) {
            dialogShowing = true;
            final MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                    .title("WiFi Not Enabled")
                    .content("Enabling WiFi can help find locations easier and more accurately.")
                    .positiveText("Settings")
                    .negativeText("Cancel")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            dialogShowing = false;
                            wifiSearchDisabled = true;
                        }

                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            Intent myIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(myIntent);
                            restart(5000);
                            dialogShowing = false;
                            wifiSearchDisabled = true;
                        }
                    }).dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            dialogShowing = false;
                            wifiSearchDisabled = true;
                        }
                    }).cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialogShowing = false;
                            wifiSearchDisabled = true;
                        }
                    });

            Handler mainHandler = new Handler(context.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    MaterialDialog alert = builder.build();
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alert.show();
                }
            });
        }
    }

    public Location getLatestLocation(){
        return locations.size() != 0 ? locations.get((locations.size() - 1)) : null;
    }

    public void updateSpeedDialogWithLatestSpeed(){
        Location latestLoc = getLatestLocation();
        if (latestLoc == null) return;
        float speed = ((float) (getLatestLocation().getSpeed() * (3.6)));

        Intent speedIntent = new Intent(context, FloatingSpeedService.class);
        speedIntent.setAction(Globals.ACTION.UPDATE_SPEED_ACTION);
        speedIntent.putExtra("speed", speed );
        context.startService(speedIntent);
    }

    @Deprecated
    private void showTurnOnDataDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your mobile data doesn't appear to be working. \nPlease check your connection or turn on mobile data by going to  settings.")
                .setTitle("No Internet Connection")
                .setPositiveButton("Settings",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(
                                "com.android.settings",
                                "com.android.settings.Settings$DataUsageSummaryActivity"));
                        context.startActivity(intent);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Back",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        builder.show();
    }


    @Override
    public void onLocationChanged(Location location) {
        wakeLock.acquire();
        if (isBetterLocation(location,bestLocation)) {
            if (bestLocation != null) bestLocation.set(location);
            else bestLocation = location;

            Intent speedIntent = new Intent(context, FloatingSpeedService.class);
            speedIntent.setAction(Globals.ACTION.UPDATE_SPEED_ACTION);
            speedIntent.putExtra("speed",((float) (location.getSpeed()*(3.6))) );
            context.startService(speedIntent);

            Log.i(LOG_TAG, "Location got:lat:" + location.getLatitude() + ", long:" + location.getLongitude() +
                    ", accuracy:" + location.getAccuracy() + ", time:" + location.getTime() + ", number:" + locationNumber);
            if (dbHelper == null) dbHelper = MyApplication.getStaticDbHelper();
            dbHelper.insertLocation(location.getLatitude(),location.getLongitude(),location.getProvider(),location.getAccuracy(),
                    location.getSpeed(),Globals.currentTrip._id,locationNumber);
            Location l = new Location("fused");
            l.set(location);
            locations.add(locationNumber,l);
            locationNumber++;

            if (false) {
                int i = 0;
                for (Location location1 : locations) {
                    Log.i(LOG_TAG, "Location:" + i + " lat:" + location1.getLatitude() + ", long:" + location1.getLongitude() +
                            ", accuracy:" + location1.getAccuracy() + ", time:" + location1.getTime());
                    i++;
                }
            }

            if (locations.size() >= 50 && false) { ///remove false for get dist every 50 locs
                Log.i(LOG_TAG, "50+ locations");
                combineLocationsAndSnap(locations,wakeLock);
            }
            else wakeLock.release();
        }

    }

    @Override
    public void onDownloadFinished(String output, String identifier) {

    }

    @Override
    public void onDownloadFailed(Exception e, boolean willRetry) {

    }

    public void combineLocationsAndSnap(final ArrayList<Location> originalLocations, final PowerManager.WakeLock wakeLock) {
        new Thread(new Runnable() {
            public void run() {
                ArrayList<Location> combined = combineNearbyLocations(originalLocations);
                locations = combined;
                getSnappedPoints(combined, wakeLock);
        }}).start();
    }

    private void getSnappedPoints(ArrayList<Location> originalLocations, PowerManager.WakeLock wakeLock) {
        if (originalLocations.size() >= 2) {
            StringBuilder url = new StringBuilder("https://roads.googleapis.com/v1/snapToRoads?path=");
            for (Location location : originalLocations) {
                url.append(location.getLatitude());
                url.append(",");
                url.append(location.getLongitude());
                url.append("|");
            }
            url.deleteCharAt(url.length() - 1);
            url.append("&interpolate=false");
            url.append("&key=");
            url.append(API_KEY);
            Log.e(LOG_TAG, "Url = " + url);


            new SnappedPointsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url.toString());
            locations.clear();
        }
    }

    private class SnappedPointsTask extends AsyncTask<String, Location, ArrayList<Location>> {
        ArrayList<Location> snappedLocations;
        DBHelper dbHelper;
        @Override
        protected void onPreExecute() {
            if (!wakeLock.isHeld()) wakeLock.acquire();
            super.onPreExecute();
            snappedLocations = new ArrayList<>();
            helperLocationArray = new ArrayList<>();
            dbHelper = MyApplication.getStaticDbHelper();
        }

        @Override
        protected ArrayList<Location> doInBackground(String... params) {
            String url = params[0];
            final String resultStr = new String();/// = ///convertStreamToString(getInputStream(url));
            Log.d(LOG_TAG,resultStr);
            try {
                final JSONObject json = new JSONObject(resultStr);
                final JSONArray snappedPoints = json.getJSONArray("snappedPoints");

                for (int i = 0; i < snappedPoints.length(); i++) {
                    final JSONObject locationJson = snappedPoints.getJSONObject(i).getJSONObject("location");
                    Location location = new Location("snapAPI");
                    location.setLatitude(locationJson.getDouble("latitude"));
                    location.setLongitude(locationJson.getDouble("longitude"));
                    snappedLocations.add(location);
                    publishProgress(location);
                }
            } catch (JSONException e) {
                Log.e(e.getMessage(), "Google JSON Parser - " + url);
            }
            return snappedLocations;
        }

        @Override
        protected void onProgressUpdate(Location... values) {
            super.onProgressUpdate(values);
            for (Location location : values) {
                Location original = dbHelper.getDBLocation(Globals.currentTrip._id,snappedNumber,"fused");
                dbHelper.insertLocation(location.getLatitude(),location.getLongitude(),location.getProvider(),
                        original.getAccuracy(),original.getSpeed(),Globals.currentTrip._id,snappedNumber);
                Log.i(LOG_TAG,"Snapped Location got:" + location.getLatitude() + "," + location.getLongitude());
                snappedNumber++;
                helperLocationArray.add(location);
                if (helperLocationArray.size() == 10) {
                    new DistanceTask().execute(helperLocationArray);
                    helperLocationArray = new ArrayList<>();
                    helperLocationArray.clear();
                }
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Location> locations) {
            super.onPostExecute(locations);
            Log.i(LOG_TAG, "SnappedPointsTask finished");
            if (helperLocationArray.size() != 0) {
                if (helperLocationArray.size() != 1) {
                    new DistanceTask().execute(helperLocationArray);
                    helperLocationArray = new ArrayList<>();
                    helperLocationArray = null;
                }
            }

        }
    }

    private class DistanceTask extends AsyncTask<ArrayList<Location>, Void, Integer> {
        Integer distance = 0;
        @Override
        protected void onPreExecute() {
            if (!wakeLock.isHeld()) wakeLock.acquire();
            super.onPreExecute();
            dbHelper = MyApplication.getStaticDbHelper();
        }
        @Override
        protected Integer doInBackground(ArrayList<Location>... arrayLists) {
            ArrayList<Location> locations1 = arrayLists[0];
            Location origin = locations1.get(0);
            String originStr = "origin=" + origin.getLatitude() + "," + origin.getLongitude();
            Location destination = null;

            StringBuilder waypoints = new StringBuilder("waypoints=");
            if (locations1.size() > 2) {
                int i = 0;
                for (Location location : locations1) {
                    if (i == 0) {
                        i++;
                        continue;
                    }
                    if (i == (locations1.size() - 1)) {
                        i++;
                        continue;
                    }
                    waypoints.append("via:");
                    waypoints.append(location.getLatitude());
                    waypoints.append(",");
                    waypoints.append(location.getLongitude());
                    if (i != (locations1.size() - 2)) waypoints.append("|");
                    i++;
                }
            } else waypoints = null;
            destination = locations1.get((locations1.size() - 1));
            String destinationStr = "destination=" + destination.getLatitude() + "," + destination.getLongitude();
            String url;
            if (waypoints == null) {
                url = "https://maps.googleapis.com/maps/api/directions/json?"
                        + originStr + "&"
                        + destinationStr + "&"
                        + "key=" + API_KEY;
            } else {
                url = "https://maps.googleapis.com/maps/api/directions/json?"
                        + originStr + "&"
                        + destinationStr + "&"
                        + waypoints.toString() + "&"
                        + "key=" + API_KEY;
            }

            Log.d(LOG_TAG,"distance url: " + url);
            final String resultStr = new String();/////convertStreamToString(getInputStream(url));
            Log.d(LOG_TAG,"distance result = " + resultStr);

            try {
                final JSONObject json = new JSONObject(resultStr);
                final JSONObject route = json.getJSONArray("routes").getJSONObject(0);
                final JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
                final JSONObject distanceObj = leg.getJSONObject("distance");
                distance = distanceObj.getInt("value");
                Globals.currentTrip.addDistance(distance);
                Log.d(LOG_TAG,"distance distance = " + distance);
            } catch (JSONException e) {
                Log.e(e.getMessage(), "Google JSON Parser - " + url);
            }
            return distance;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }

    public static final int rad = 6371;

    public static double getDistanceBetweenDistances(Location location1, Location location2) {
        double lat1 = Math.toRadians(location1.getLatitude());
        double long1 = Math.toRadians(location1.getLongitude());
        double lat2 = Math.toRadians(location2.getLatitude());
        double long2 = Math.toRadians(location2.getLongitude());

        double x = (long2 - long1) * (float) Math.cos((lat1+lat2)/2);
        double y = lat2 - lat1;
        double dist = Math.sqrt(x*x + y*y) * rad * 1000;
        Log.i(LOG_TAG,"distance = " + dist);
        return dist;
    }

    public static boolean isLocationWithinDistance(Location baseLoc, Location location, double distance) {
        double dist = getDistanceBetweenDistances(baseLoc,location);
        return (dist <= distance);
    }

    public static Location getWeightedAverageLocation(ArrayList<Location> locations) {
        ArrayList<Double> lats = new ArrayList<>(locations.size());
        ArrayList<Double> longs = new ArrayList<>(locations.size());
        ArrayList<Float> accuracies = new ArrayList<>(locations.size());
        for (Location location : locations) {
            lats.add(location.getLatitude());
            longs.add(location.getLongitude());
            accuracies.add(location.getAccuracy());
        }

        double topLat = 0;
        double topLong = 0;
        double bottom = 0;
        float totalAccuracy = 0;
        for (int i = 0; i < locations.size(); i++) {
            double weight = 1/(accuracies.get(i));
            topLat += lats.get(i) * weight;
            topLong += longs.get(i) * weight;
            bottom += weight;
            totalAccuracy += accuracies.get(i);
        }
        double averageLat = topLat / bottom;
        double averageLong = topLong / bottom;
        float averageAccuracy = totalAccuracy / lats.size();

        Location location = new Location("averageAPI");
        location.setLatitude(averageLat);
        location.setLongitude(averageLong);
        location.setAccuracy(averageAccuracy);

        return location;
    }

    public static final int numberOfLocationsToCheck = 5;
    public static final double distanceToCombine = 15.0;

    public ArrayList<Location> combineNearbyLocations(ArrayList<Location> origLocations) {
        SparseArray<Boolean> locationsUsed = new SparseArray<>(origLocations.size());
        ArrayList<Location> combinedLocations = new ArrayList<>(origLocations.size());

        for (int i = 0; i < origLocations.size(); i++) {
            if (locationsUsed.valueAt(i) != null && locationsUsed.valueAt(i)) {
                continue;
            }
            Location baseLocation = origLocations.get(i);
            ArrayList<Location> nearbyLocations = null;

            int numberSinceLastNearby = 0;
            int relIndex = 0;
            Boolean scanning = true;
            while (scanning) {
                numberSinceLastNearby++;
                relIndex++;
                if (numberSinceLastNearby == numberOfLocationsToCheck) {
                    scanning = false;
                    continue;
                }
                int index = relIndex + i;
                if (index >= origLocations.size()) {
                    scanning = false;
                    continue;
                }
                Location location = origLocations.get(index);
                if (isLocationWithinDistance(baseLocation,location,distanceToCombine)) {
                    if (nearbyLocations == null) nearbyLocations = new ArrayList<>();
                    numberSinceLastNearby = 0;
                    locationsUsed.setValueAt(index,true);
                    nearbyLocations.add(location);
                }
            }
            if (nearbyLocations == null) {
                combinedLocations.add(baseLocation);
            }
            else {
                Location combinedLocation = getWeightedAverageLocation(nearbyLocations);
                combinedLocations.add(combinedLocation);
            }
        }
        Log.i(LOG_TAG,"Combined location list of " + origLocations.size() + " locations into " + combinedLocations.size() + " location(s)");
        return combinedLocations;
    }






    @Override
    public void onConnected(Bundle bundle) {
        if (true) {
            Log.i(LOG_TAG, "GoogleApiClient connection has succeeded");
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(interval);

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspended");
    }


    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta >  1000 * 60 * 2; //two minutes
        boolean isSignificantlyOlder = timeDelta < -1000 * 60 * 2;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 50;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}
