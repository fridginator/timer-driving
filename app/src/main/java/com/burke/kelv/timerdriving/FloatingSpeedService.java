package com.burke.kelv.timerdriving;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.burke.kelv.timerdriving.R;

import java.text.DecimalFormat;

/**
 * Created by kelv on 25/06/2015.
 */

public class FloatingSpeedService extends Service {

    private WindowManager windowManager;
    private RelativeLayout chatHead;
    WindowManager.LayoutParams params;

    private TextView speedTextView;
    private Boolean running = false;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    private static final int NOTIFICATION_ID = 1998;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action.equals(Globals.ACTION.STARTFOREGROUND_ACTION)) {
            if (!running) {
                Intent notificationIntent = new Intent(this, MainActivity.class);
                notificationIntent.setAction(Globals.ACTION.MAIN_ACTION);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher);

                builder = new NotificationCompat.Builder(this)
                        .setContentTitle("Speed Watcher")
                        .setTicker("Speed Overlay")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(
                                Bitmap.createScaledBitmap(icon, 256, 256, false))
                        .setContentText("Current Speed: 0.0 km/hr")
                        .setContentIntent(pendingIntent)
                        .setOngoing(true);

                notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                //notificationManager.notify(notificationID, builder.build());

                running = true;
                startForeground(NOTIFICATION_ID,
                        builder.build());
            }
        }

        else if (action.equals(Globals.ACTION.STOPFOREGROUND_ACTION)) {
            builder = null;
            running = false;
            stopForeground(true);
            stopSelf();
        }
        else if (action.equals(Globals.ACTION.UPDATE_SPEED_ACTION)) {
            if (running) updateSpeed(intent.getFloatExtra("speed",0));
        }


        return START_NOT_STICKY;
    }

    private void updateSpeed(float speed) {
        DecimalFormat oneDForm = new DecimalFormat("####.#");
        String speedStr = oneDForm.format((double) speed);
        if (speedTextView != null) speedTextView.setText(speedStr + " kmh");
        if (builder != null) {
            builder.setContentText("Current Speed: " + speedStr + " km/hr");
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        chatHead = (RelativeLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.floating_speed_layout,null);
        speedTextView = (TextView) chatHead.findViewById(R.id.speedTextView);
        Typeface font = Typeface.createFromAsset(getAssets(),"Square.ttf");
        speedTextView.setTypeface(font);
        speedTextView.setTextSize(50);

        params= new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        //this code is for dragging the chat head
        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX
                                + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY
                                + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chatHead, params);
                        return true;
                }
                return false;
            }
        });
        windowManager.addView(chatHead, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null)
            windowManager.removeView(chatHead);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
