package com.burke.kelv.timerdriving;


import java.util.ArrayList;

/**
 * Created by kelv on 13/03/2015.
 */
public class ConversionHelper {
    public static int getTrafficFromBools(boolean isLight, boolean isMedium, boolean isHeavy) {
        if (!isLight && !isMedium && !isHeavy) return 0;
        StringBuilder str = new StringBuilder();
        if (isLight) str.append("1");
        if (isMedium) str.append("2");
        if (isHeavy) str.append("3");
        if (str.toString().equals("") || str == null || str.toString() == null || str.length() == 0 || str.toString().isEmpty() || str.toString().length() == 0 ) return 0;
        return Integer.parseInt(str.toString());
    }

    public static int getWeatherFromBools(boolean isWet, boolean isDry) {
        StringBuilder str = new StringBuilder();
        if (isWet) str.append("1");
        if (isDry) str.append("2");
        if (str.toString().equals("") || str == null || str.toString() == null || str.length() == 0 || str.toString().isEmpty() || str.toString().length() == 0 ) return 0;
        return Integer.parseInt(str.toString());
    }

    public static int getTimeOfDayFromBools(boolean day, boolean dawnDusk, boolean night) {
        StringBuilder str = new StringBuilder();
        if (day) str.append("1");
        if (dawnDusk) str.append("2");
        if (night) str.append("3");
        if (str.toString().equals("") || str == null || str.toString() == null || str.length() == 0 || str.toString().isEmpty() || str.toString().length() == 0 ) return 0;
        return Integer.parseInt(str.toString());
    }
    public static int getRoadTypeFromBools(boolean localSt, boolean mainRd, boolean innerCity, boolean freeway, boolean ruralHwy, boolean ruralOth, boolean gravel) {
        StringBuilder str = new StringBuilder();
        if (localSt) str.append("1");
        if (mainRd) str.append("2");
        if (innerCity) str.append("3");
        if (freeway) str.append("4");
        if (ruralHwy) str.append("5");
        if (ruralOth) str.append("6");
        if (gravel) str.append("7");
        if (str.toString().equals("") || str == null || str.toString() == null || str.length() == 0 || str.toString().isEmpty() || str.toString().length() == 0 ) return 0;
        return Integer.parseInt(str.toString());
    }
    public static int getRoadTypeFromBools(Boolean[] bools) {
        if (bools.length >= 7) {
            return getRoadTypeFromBools(bools[0],bools[1],bools[2],bools[3],bools[4],bools[5],bools[6]);
        }
        return 0;
    }

    public static Boolean[] getRoadTypeBoolsFromInt(int rType) {
        return new Boolean[] {isLocalSt(rType),isMainRd(rType),isInnerCity(rType),isFreeway(rType),isRuralHwy(rType),isRuralOth(rType),isGravel(rType)};
    }

    public static boolean isLocalSt(int roadTypeInt) {
        return ("" + roadTypeInt).contains("1");
    }
    public static boolean isMainRd(int roadTypeInt) {
        return ("" + roadTypeInt).contains("2");
    }
    public static boolean isInnerCity(int roadTypeInt) {
        return ("" + roadTypeInt).contains("3");
    }
    public static boolean isFreeway(int roadTypeInt) {
        return ("" + roadTypeInt).contains("4");
    }
    public static boolean isRuralHwy(int roadTypeInt) {
        return ("" + roadTypeInt).contains("5");
    }
    public static boolean isRuralOth(int roadTypeInt) {
        return ("" + roadTypeInt).contains("6");
    }
    public static boolean isGravel(int roadTypeInt) {
        return ("" + roadTypeInt).contains("7");
    }

    public static boolean isWetWeather(int weatherInt) {
        return ("" + weatherInt).contains("1");
    }
    public static boolean isDryWeather(int weatherInt) {
        return ("" + weatherInt).contains("2");
    }

    public static boolean isLightTraffic(int trafficInt) {
        return ("" + trafficInt).contains("1");
    }
    public static boolean isMediumTraffic(int trafficInt) {
        return ("" + trafficInt).contains("2");
    }
    public static boolean isHeavyTraffic(int trafficInt) {
        return ("" + trafficInt).contains("3");
    }

    public static boolean isDayTime(int timeOfDayInt) {
        return ("" + timeOfDayInt).contains("1");
    }
    public static boolean isDawnDuskTime(int timeOfDayInt) {
        return ("" + timeOfDayInt).contains("2");
    }
    public static boolean isNightTime(int timeOfDayInt) {
        return ("" + timeOfDayInt).contains("3");
    }


    public static int boolToInt(boolean bool) {
        return bool ? 1 : 0;
    }
    public static boolean intToBool(int i) {
        return  i==1;
    }

    public static String roadTypeIntToShortString(int roadType) {
        switch (roadType) {
            case 0: return "Local St";
            case 1: return "Main Rd";
            case 2: return "Inner City";
            case 3: return "Freeway";
            case 4: return "Rural Hwy";
            case 5: return "Rural Other";
            case 6: return "Gravel";
        }
        return "";
    }

    public static String changesStringFromList(ArrayList<FinishedDetailsFragment.Change> changes) {
        StringBuilder strBuilder = new StringBuilder();
        for (FinishedDetailsFragment.Change change : changes) {
            strBuilder.append(change.getType());
        }
        return strBuilder.toString();
    }

}
