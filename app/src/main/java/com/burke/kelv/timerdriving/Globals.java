package com.burke.kelv.timerdriving;

/**
 * Created by kelv on 20/02/2015.
 */
public class Globals {
    @Deprecated static boolean defaultTimerRunning = false;
    @Deprecated static boolean defaultTimerConstructed = false;
    @Deprecated static int currentTripState = KTrip.STATUS.NOT_INITIALISED;
    @Deprecated static KTime currentTripStartTime = new KTime("zerotime");
    static KTrip currentTrip;

    static int noOfCursors = 0;

    static final String defaultTimerId = "defaultTimer";
    @Deprecated static final Integer defaultTimerServiceId = 12345;
    static final String defaultTimerUpdateBroadcastId = "defaultTimerBroadcast";
    static final String defaultTimerListeningBroadcast = "defaultListening";

    static final String notificationTimerBroadcastId = "notificationTimerBroadcast";
    static final String notificationTimerListeningBroadcast = "notificationListening";

    static final String listeningRequest = "listeningRequest";

    static final String tripChangedStateBroadcast = "tripChangedState";

    static final String numberOfDistanceReqBroadcast = "numberOfDistanceReqBroadcast";
    static final String distanceChangedBroadcast = "distanceChangedBroadcast";

    @Deprecated static final String startTripAction = "tripStarted";
    @Deprecated static final String startTripBroadcast = "tripStartedBroady";
    @Deprecated static final String pauseTripBroadcast = "paused";
    @Deprecated static final String resumeTripBroadcast = "resumer";

    static final String NOW = "now";
    static final String ZERO_TIME = "zerotime";
    static final String LOG = "TimerDriving:LOG";

    public interface ACTION {
        public static final String MAIN_ACTION = "com.burke.kelv.foregroundservice.action.main";
        public static final String STOP_ACTION = "com.burke.kelv.foregroundservice.action.stop";
        public static final String PLAY_ACTION = "com.burke.kelv.foregroundservice.action.play";
        public static final String PAUSE_ACTION = "com.burke.kelv.foregroundservice.action.pause";
        public static final String STARTFOREGROUND_ACTION = "com.burke.kelv.foregroundservice.action.startforeground";
        public static final String STOPFOREGROUND_ACTION = "com.burke.kelv.foregroundservice.action.stopforeground";
        public static final String SHOW_HIDE_SPEED_OVERLAY_ACTION = "com.burke.kelv.foregroundservice.action.updateSpeedOverlay";

        public static final String UPDATE_SPEED_ACTION = "com.burke.kelv.floatingspeedservice.action.updateSpeed";


        public static final String USE_GLOBALS_CURRENT_TRIP = "com.burke.kelv.foregroundservice.action.useGlobals.trip";

        public static final String GET_INPUTSTREAM_FROM_URL = "com.burke.kelv.action.GETINPUTSTREAM";
    }

    public interface TIMEFORMAT {
        public static String H_MM_SS = "default";
        public static String MM_SS_plusHoursIfAppl = "random time";
        public static String H_MM = "notification time";
        public static String HH_MM_SS = "full time";
        public static String WORDED_DATE = "date worded";
    }

    //  public interface DAY {

    //public static int MONDAY = 1;

    //public static int TUESDAY = 2;

    //public static int WEDNESDAY = 3;

    //public static int THURSDAY = 4;

    //public static int FRIDAY = 5;

    //public static int SATURDAY = 6;

    //public static int SUNDAY = 7;

    //}

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
