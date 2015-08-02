package com.burke.kelv.timerdriving;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kelv on 12/05/2015.
 */
public class NetworkingHelper {
    public DownloadCallbacks callbacks;
    private Context context;
    private String id;
    private static boolean waiting;

    public static interface DownloadCallbacks {
        public void onDownloadFinished(String output, String identifier);
        public void onDownloadFailed(Exception e, boolean willRetry);
    }

    public NetworkingHelper(DownloadCallbacks callbacks, Context context, String identifier) {
        this.callbacks = callbacks;
        this.context = context;
        this.id = identifier;
    }

    public void downloadStringFromUrl(String url) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread() && false) throw new RuntimeException(
                "DO NOT NETWORK ON MAIN THREAD\n" +
                        "com.burke.kelv.timerdriving.networkingHelper Line 39: public void downloadStringFromUrl(String url)");
        new SnappedDistanceTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    }

    public class SnappedDistanceTask extends AsyncTask<String, Void, String> {
        String result;
        boolean trying;
        int attempt;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            result = null;
            trying = true;
            waiting = false;
            attempt = 0;
        }
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            int numberOfResets = 0;
            while (trying) {
                if (waiting) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e ) {
                        e.printStackTrace();
                    }
                }
                attempt++;
                Log.d("NETWORKER", "Attempt #" + attempt);
                if (numberOfResets >= 5) {
                    trying = false;
                    continue;
                }
                if (attempt >= 10) {
                    showTurnOnDataDialog();
                    attempt = 0;
                    numberOfResets++;
                    continue;
                } else if (attempt >= 5) {
                    try {
                        Log.d("NETWORKER", "Waiting...");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    result = convertStreamToString(getInputStream(url));
                } catch (NullPointerException e) {
                    callbacks.onDownloadFailed(e, true);
                }
                if (result != null) {
                    trying = false;
                }
            }
            if (result == null) {
                callbacks.onDownloadFailed(null,false);
                return null;
            }
            else return result;
        }

        private String convertStreamToString(final InputStream input) {
            if (input == null) {
                callbacks.onDownloadFailed(null,true);
                return null;
            }
            final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            final StringBuilder sBuf = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sBuf.append(line);
                }
            } catch (IOException e) {
                callbacks.onDownloadFailed(e,true);
                Log.e(e.getMessage(), "Google parser, stream2string");
                return null;
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    callbacks.onDownloadFailed(e,true);
                    Log.e(e.getMessage(), "Google parser, stream2string");
                    return null;
                }
            }
            return sBuf.toString();
        }
        private InputStream getInputStream(String urlStr) {
            InputStream in = null;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (MalformedURLException e) {
                trying = false;
                callbacks.onDownloadFailed(e,false);
            } catch (IOException e) {
                callbacks.onDownloadFailed(e,true);
            }
            return in;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) callbacks.onDownloadFinished(result,id);
        }

        private void showTurnOnDataDialog() {
            if (!waiting && false) { /// uncomment false
                Log.d("NETWORKER", "showing dialog...");
                waiting = true;

                final MaterialDialog.Builder materialBuilder = new MaterialDialog.Builder(context)
                        .title("No Internet Connection")
                        .content("Your mobile data doesn't appear to be working.\nPlease check your connection or turn on mobile data by going to settings.")
                        .positiveText("Settings")
                        .negativeText("Back")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName(
                                        "com.android.settings",
                                        "com.android.settings.Settings$DataUsageSummaryActivity"));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                waiting = false;
                            }

                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                waiting = false;
                            }
                        });
                Handler mainHandler = new Handler(context.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        MaterialDialog alert = materialBuilder.build();
                        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        alert.show();
                    }
                };
                mainHandler.post(myRunnable);
            }
            else if (!waiting) {
                Toast.makeText(context,"Timer Driving: No internet connection!",Toast.LENGTH_LONG).show();
            }
        }
    }

}
