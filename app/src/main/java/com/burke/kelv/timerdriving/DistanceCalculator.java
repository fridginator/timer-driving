package com.burke.kelv.timerdriving;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kelv on 14/05/2015.
 */
public class DistanceCalculator implements NetworkingHelper.DownloadCallbacks {
    public static final String API_KEY = "AIzaSyDLoZuZhalGsqENHJegv88qNLI12bq4gVQ";

    public static int calculateDistanceUsingSpeeds(ArrayList<Location> locations){
        ArrayList<ArrayList<Double>> arrays = prepareSpeedsFromLocations(locations);
        Double[] speeds = new Double[arrays.get(0).size()];
        speeds = arrays.get(0).toArray(speeds);
        Double[] times = new Double[arrays.get(1).size()];
        times = arrays.get(1).toArray(times);
        double distance = 0.0;
        for (int i = 0; i < speeds.length; i++) {
            double dist = speeds[i] * times[i];
            distance += dist;
            Log.d(LocationHelper.LOG_TAG,"distance=" + dist + " for i: " + i);
        }
        Log.w(LocationHelper.LOG_TAG,"total distance=" + distance);
        return (int) Math.round(distance);
    }

    public static final double baseTime = 1.431746E12;
    private static ArrayList<ArrayList<Double>> prepareSpeedsFromLocations(ArrayList<Location> locations) {
        ArrayList<Double> speeds = new ArrayList<>();
        ArrayList<Double> times = new ArrayList<>();
        int i = 0;
        double lastSpeed = 0.0;
        double lastTime =  0.0;
        for (int o = 0;o<locations.size();o++) {
            Location location = locations.get(o);
            double time = ( location.getTime() - baseTime ) / 1000;
            double speed = location.getSpeed();
            if (i==0) {
                lastSpeed = speed;
                lastTime = time;
                speeds.add(speed);
                times.add(0.0);
                Log.d(LocationHelper.LOG_TAG,"i="+i + "o=" + o);
                Log.d(LocationHelper.LOG_TAG,"time="+time);
                Log.d(LocationHelper.LOG_TAG,"speed="+speed + "lat: " + location.getLatitude() + ", long: " + location.getLongitude() +
                        ", accuracy: " + location.getAccuracy());
                i++;
                continue;
            }

            double deltaSpeed = speed - lastSpeed;
            double deltaTime = time - lastTime;
            boolean isSignificantlyFaster = deltaSpeed >= 10;
            boolean isSignificantlyNewer = deltaTime >= 30;
            Log.d(LocationHelper.LOG_TAG,"i="+i);
            Log.d(LocationHelper.LOG_TAG,"time="+time);
            Log.d(LocationHelper.LOG_TAG,"speed="+speed + "lat: " + location.getLatitude() + ", long: " + location.getLongitude() +
                    ", accuracy: " + location.getAccuracy());
            Log.d(LocationHelper.LOG_TAG,"Lasttime="+lastTime);
            Log.d(LocationHelper.LOG_TAG,"Lastspeed="+lastSpeed);
            Log.d(LocationHelper.LOG_TAG,"deltaTime=" + deltaTime);
            Log.d(LocationHelper.LOG_TAG,"deltaSpeed=" + deltaSpeed);
            Log.d(LocationHelper.LOG_TAG,"isALotNewer=" + isSignificantlyNewer);
            Log.d(LocationHelper.LOG_TAG,"isALotFaster=" + isSignificantlyFaster);


            if (isSignificantlyFaster || isSignificantlyNewer) {
                double averageSpeed = (lastSpeed + speed) / 2;
                double averageTime = (lastTime + time) / 2;
                speeds.add(averageSpeed);
                times.add(averageTime - lastTime);

                speeds.add(speed);
                times.add(time - averageTime);
            }
            else {
                speeds.add(speed);
                times.add(deltaTime);
            }
            lastSpeed = speed;
            lastTime = time;
            i++;
        }
        ArrayList<ArrayList<Double>> results = new ArrayList<>(2);
        results.add(speeds);
        results.add(times);
        return results;
    }

    public static final int numberOfLocationsToCheck = 6;
    public static final double distanceToCombine = 20.0;

    public static ArrayList<Location> combineNearbyLocations(ArrayList<Location> origLocations) {
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
                if (LocationHelper.isLocationWithinDistance(baseLocation, location, distanceToCombine)) {
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
                Location combinedLocation = LocationHelper.getWeightedAverageLocation(nearbyLocations);
                combinedLocations.add(combinedLocation);
            }
        }
        Log.i(LocationHelper.LOG_TAG,"Combined location list of " + origLocations.size() + " locations into " + combinedLocations.size() + " location(s)");
        return combinedLocations;
    }

    public  void calculateDistanceBySnappingAndMeasuring(final ArrayList<Location> locations) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Location> combinedLocations = combineNearbyLocations(locations);

                sendNumberOfDistancesBroadcast((int) Math.floor(combinedLocations.size() / 100));

                ArrayList<Location> helperArray = new ArrayList<>();
                boolean iterating = true;
                int i = 0;
                int index = 0;
                while (iterating) {
                    i++;
                    if (index == combinedLocations.size()) {
                        snapAndMeasure(helperArray);
                        iterating = false;
                        continue;
                    }
                    if (i == 100) {
                        snapAndMeasure(helperArray);
                        helperArray = new ArrayList<>();
                        i = 0;
                        continue;
                    }
                    helperArray.add(combinedLocations.get(index));
                    index++;
                }
            }
        }).start();
    }
    private int snapAndMeasure(ArrayList<Location> originalLocations) {
        NetworkingHelper networkHelper = new NetworkingHelper(this,MyApplication.getActivityContext(),"snapAndMeasure");
        Log.w("gesg","segseg");
        if (originalLocations.size() >= 2) {
            Log.w("gesg","segseg2");
            StringBuilder url = new StringBuilder("https://roads.googleapis.com/v1/snapToRoads?path=");
            for (Location location : originalLocations) {
                url.append(location.getLatitude());
                url.append(",");
                url.append(location.getLongitude());
                url.append("|");
            }
            url.deleteCharAt(url.length() - 1);
            url.append("&interpolate=true");
            url.append("&key=");
            url.append(API_KEY);
            Log.e(LocationHelper.LOG_TAG, "Url = " + url);

            networkHelper.downloadStringFromUrl(url.toString());
        } else { Globals.currentTrip.addDistance(0); Log.w("gesg","segseg3"); }
        return 0;
    }

    public void sendNumberOfDistancesBroadcast(int number){
        Log.w("sefs",number + " numb of  thuhigb");
        Intent sendIntent = new Intent(Globals.numberOfDistanceReqBroadcast);
        sendIntent.putExtra(Globals.numberOfDistanceReqBroadcast, number);
        LocalBroadcastManager.getInstance(MyApplication.getActivityContext()).sendBroadcast(sendIntent);
    }

    @Override
    public void onDownloadFinished(String output, String identifier) {
        if (identifier.equals("snapAndMeasure")) {
            new SnappedDistanceTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, output);
        }
    }

    @Override
    public void onDownloadFailed(Exception e, boolean willRetry) {
    }

    private static class SnappedDistanceTask extends AsyncTask<String, Void, Integer> {
        double distance;
        Location lastLocation;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lastLocation = null;
        }

        @Override
        protected Integer doInBackground(String... params) {
            final String resultStr = params[0];/// = ///convertStreamToString(getInputStream(url));
            Log.d(LocationHelper.LOG_TAG,resultStr);
            try {
                final JSONObject json = new JSONObject(resultStr);
                final JSONArray snappedPoints = json.getJSONArray("snappedPoints");

                for (int i = 0; i < snappedPoints.length(); i++) {
                    final JSONObject locationJson = snappedPoints.getJSONObject(i).getJSONObject("location");
                    Location location = new Location("snapAPI");
                    location.setLatitude(locationJson.getDouble("latitude"));
                    location.setLongitude(locationJson.getDouble("longitude"));
                    distance += distanceToNewLocation(location);
                }
            } catch (JSONException e) {
                Log.e(e.getMessage(), "Google JSON Parser");
            }
            return (int) Math.round(distance);
        }


        protected double distanceToNewLocation(Location location) {
            if (lastLocation == null) {
                lastLocation = location;
                return 0.0;
            }
            double dist = lastLocation.distanceTo(location);
            lastLocation.set(location);
            return dist;
        }

        @Override
        protected void onPostExecute(Integer distance) {
            super.onPostExecute(distance);
            Log.i(LocationHelper.LOG_TAG, "SnappedDistanceTask finished");
            Globals.currentTrip.addDistance(distance);
        }
    }
}
