package com.burke.kelv.timerdriving;

import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by kelv on 20/02/2015.
 *
 *
 *
 * String.format("%d min, %d sec",
 *TimeUnit.MILLISECONDS.toMinutes(millis),
 *TimeUnit.MILLISECONDS.toSeconds(millis) -
 *TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
 *);
*
*For anyone that wants a leading 0 (e.g. for secs 0-9) use %02d instead of %d in the above example
*http://stackoverflow.com/questions/625433/how-to-convert-milliseconds-to-x-mins-x-seconds-in-java
 */

public class KTime {
    public int id;
    public int hours;
    public int minutes;
    public int seconds;
    public int dayOfWeek;
    public int date;
    public int month;
    public int year;
    private long unreadableIntSecs;
    public String readable;

    //public static long kDifferenceInMillis = 949323600000L;
    public static int TYPE_DATETIME = 1;
    public static int TYPE_TIME_LENGTH = 2;

    public KTime(String type){
        if (type.equals(Globals.NOW)) {
            Calendar c = GregorianCalendar.getInstance();
            this.hours = c.get(Calendar.HOUR_OF_DAY);
            this.minutes = c.get(Calendar.MINUTE);
            this.seconds = c.get(Calendar.SECOND);

            this.dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            this.date = c.get(Calendar.DATE);
            this.month = c.get(Calendar.MONTH);
            this.year = c.get(Calendar.YEAR);

            this.readable = KTime.getProperReadable(this, Globals.TIMEFORMAT.HH_MM_SS);

            long time= System.currentTimeMillis();
            this.unreadableIntSecs = time/1000;
        }
        else if (type.equals(Globals.ZERO_TIME)) {
            this.hours = 0;
            this.minutes = 0;
            this.seconds = 0;
            this.dayOfWeek = 0;
            this.date = 0;
            this.month = 0;
            this.year = 0;

            this.readable = KTime.getProperReadable(this, Globals.TIMEFORMAT.HH_MM_SS);
            this.unreadableIntSecs = 0;
        }
        else Log.w(Globals.LOG, "KTime type was not GLOBALS.NOW or ZERO_TIME");
    }

    public long toIntMillis(){
        if (year != 0) {
            Calendar c = new GregorianCalendar(year, month, date, hours, minutes, seconds);
            return c.getTimeInMillis();
        } else return TimeUnit.HOURS.toMillis(this.hours)
                + TimeUnit.MINUTES.toMillis(this.minutes)
                + TimeUnit.SECONDS.toMillis(this.seconds);

    }

    public long toIntSeconds() {
        return toIntMillis() / 1000;
    }

    public KTime(long seconds, int type) {
        if (type == TYPE_DATETIME) {
            GregorianCalendar c = new GregorianCalendar();
            c.setTimeInMillis(seconds*1000L);

            this.hours = c.get(Calendar.HOUR_OF_DAY);
            this.minutes = c.get(Calendar.MINUTE);
            this.seconds = c.get(Calendar.SECOND);

            this.dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            this.date = c.get(Calendar.DATE);
            this.month = c.get(Calendar.MONTH);
            this.year = c.get(Calendar.YEAR);

            this.readable = KTime.getProperReadable(this, Globals.TIMEFORMAT.HH_MM_SS);

            long time= c.getTimeInMillis();
            this.unreadableIntSecs = (int) (long) time/1000;
        }
        else if (type == TYPE_TIME_LENGTH) {
            this.dayOfWeek = 0;
            this.date = 0;
            this.month = 0;
            this.year = 0;

            this.hours = (int) TimeUnit.SECONDS.toHours(seconds);
            this.minutes = (int) TimeUnit.SECONDS.toMinutes(seconds) - this.hours*60;
            this.seconds = (int) (seconds - this.hours*3600 - this.minutes*60);

            this.readable = KTime.getProperReadable(this, Globals.TIMEFORMAT.HH_MM_SS);

            this.unreadableIntSecs = seconds;
        }
    }

    public KTime(Calendar c) {
        this.hours = c.get(Calendar.HOUR_OF_DAY);
        this.minutes = c.get(Calendar.MINUTE);
        this.seconds = c.get(Calendar.SECOND);

        this.dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        this.date = c.get(Calendar.DATE);
        this.month = c.get(Calendar.MONTH);
        this.year = c.get(Calendar.YEAR);

        this.readable = KTime.getProperReadable(this, Globals.TIMEFORMAT.HH_MM_SS);

        long time= c.getTimeInMillis();
        this.unreadableIntSecs = time/1000;
    }


    public boolean isAfter(KTime compare) {
        if (compare == null) return false;
        if (this.year > compare.year) return true;
        if (this.year < compare.year) return false;

        if (this.month > compare.month) return true;
        if (this.month < compare.month) return false;

        if (this.date > compare.date) return true;
        if (this.date < compare.date) return false;

        if (this.hours > compare.hours) return true;
        if (this.hours < compare.hours) return false;

        if (this.minutes > compare.minutes) return true;
        if (this.minutes < compare.minutes) return false;

        if (this.seconds > compare.seconds) return true;
        if (this.seconds < compare.seconds) return false;

        return true;
    }

    public boolean isBefore(KTime compare) {
        if (compare == null) return false;
        if (this.year < compare.year) return true;
        if (this.year > compare.year) return false;

        if (this.month < compare.month) return true;
        if (this.month > compare.month) return false;

        if (this.date < compare.date) return true;
        if (this.date > compare.date) return false;

        if (this.hours < compare.hours) return true;
        if (this.hours > compare.hours) return false;

        if (this.minutes < compare.minutes) return true;
        if (this.minutes > compare.minutes) return false;

        if (this.seconds < compare.seconds) return true;
        if (this.seconds > compare.seconds) return false;

        return true;
    }

    static public KTime newDBTime(KTime time, DBHelper dbHelper) {
        String properReadable = getProperReadable(time,Globals.TIMEFORMAT.HH_MM_SS);
        long id = dbHelper.insertTime(time.hours, time.minutes, time.seconds, time.dayOfWeek, time.date, time.month, time.year, properReadable);
        time.id = (int) id;
        time.readable = properReadable;
        //Log.i(Globals.LOG, "New db time:"+ id + time.hours +time.minutes+ time.seconds + time.dayOfWeek+ time.date+ time.month+ time.year+ time.readable);
        return time;
    }


    static public KTime copyTime(KTime time) {
        KTime newTime = new KTime(Globals.ZERO_TIME);
        newTime.hours = time.hours;
        newTime.minutes = time.minutes;
        newTime.seconds = time.seconds;
        newTime.dayOfWeek = time.dayOfWeek;
        newTime.date = time.date;
        newTime.month = time.month;
        newTime.year = time.year;
        newTime.readable = time.readable;
        newTime.unreadableIntSecs = time.unreadableIntSecs;
        newTime.id = time.id;
        return newTime;
    }

    @Deprecated
    static public String getReadableTime(Integer unreadable){
        if (unreadable == null) {
            Calendar c = Calendar.getInstance();
            int hours = c.get(Calendar.HOUR_OF_DAY);
            int minutes = c.get(Calendar.MINUTE);
            int seconds = c.get(Calendar.SECOND);
            return (hours+":"+minutes+":"+seconds);
        }
        else {
            Log.i("LOG", "KTime still need to implement unreadable to readable time");
            return null;
        }

    }

    static public Integer getIntTime(String readable) {
        Log.i("LOG", "KTime gettingIntTime");
        if (readable == null) {
            long time= System.currentTimeMillis();
            Integer timeInSec = (int) (long) time/1000;
            return timeInSec;
        }
        else {
            Log.i("LOG", "KTime still need to implement readable to unreadable time");
            return null;
        }
    }

    static public KTime getDiffBetweenTimes(KTime subtractFrom, KTime subtract) {
        KTime answer = new KTime(Globals.ZERO_TIME);
        KTime subFrom = copyTime(subtractFrom);
        KTime sub = copyTime(subtract);

        answer.hours = subFrom.hours - sub.hours;
        answer.minutes = subFrom.minutes - sub.minutes;
        answer.seconds = subFrom.seconds - sub.seconds;
        if (answer.seconds < 0) {
            answer.minutes -= 1;
            answer.seconds += 60;
        }
        if (answer.minutes < 0 ) {
            answer.hours -= 1;
            answer.minutes += 60;
        }
        if (answer.hours < 0) {
            if (subFrom.date == 1) {
                Log.w(Globals.LOG, "Subtracting from time with date 1... trying our best...");
                if (subFrom.dayOfWeek == (sub.dayOfWeek + 1) || subtractFrom.dayOfWeek == (sub.dayOfWeek - 6)) {
                    if (subFrom.month == (sub.month + 1)) {
                        Log.i(Globals.LOG, "Difference seems to be one day");
                        answer.hours += 24;
                    } else return null;
                } else return null;
            } else if (sub.date != subFrom.date) {
                int diff = subFrom.date - sub.date;
                if (diff > 0) {
                    answer.hours += diff * 24;
                } else Log.w(Globals.LOG, "Difference seems to be negative");
            }
        }


        //

        //if (subtractFrom.seconds < sub.seconds ) {

        //if (subFrom.minutes == 0) {

        //subFrom.hours -= 1;

        //subFrom.minutes += 60;

        //}

        //subFrom.minutes -= 1;

        //subFrom.seconds += 60;

        //}

        //if (subtractFrom.minutes < sub.minutes ) {

        //subFrom.hours -= 1;

        //subFrom.minutes += 60;

        //}

        return answer;
    }

    static public KTime roundTimeToMinute(KTime t) {
        KTime time1 = copyTime(t);
        if (time1.seconds > 0) {
            if (time1.seconds < 30) time1.seconds = 0;
            else if (time1.seconds >= 30) {
                time1.minutes += 1;
                time1.seconds = 0;
                if (time1.minutes == 60) {
                    time1.hours++;
                    time1.minutes = 0;
                }
            }
        }
        return time1;
    }

    static public KTime addTimes(KTime time1, KTime time2) {
        KTime answer = new KTime(Globals.ZERO_TIME);
        answer.seconds = time1.seconds + time2.seconds;
        answer.minutes = time1.minutes + time2.minutes;
        answer.hours = time1.hours + time2.hours;
        if (answer.seconds > 59) {
            answer.seconds -= 60;
            answer.minutes += 1;
        } if (answer.minutes > 59) {
            answer.minutes -= 60;
            answer.hours += 1;
        }
        return answer;
    }

    static public String getProperReadable(KTime time, String format) {
        if (time == null) return "ERROR";
        String hours;
        String minutes;
        String seconds;
        if (format == Globals.TIMEFORMAT.H_MM_SS) {
            hours = String.valueOf(time.hours);
            if (time.minutes < 10) minutes = "0" + time.minutes;
            else minutes = String.valueOf(time.minutes);
            if (time.seconds < 10) seconds = "0" + time.seconds;
            else seconds = String.valueOf(time.seconds);

            return hours + ":" + minutes + ":" + seconds;
        }
        if (format == Globals.TIMEFORMAT.MM_SS_plusHoursIfAppl) {
            if (time.minutes < 10) minutes = "0" + time.minutes;
            else minutes = String.valueOf(time.minutes);
            if (time.seconds < 10) seconds = "0" + time.seconds;
            else seconds = String.valueOf(time.seconds);
            if (time.hours > 0) {
                hours = String.valueOf(time.hours);
                return hours + ":" + minutes + ":" + seconds;
            }
            else return minutes + ":" + seconds;
        }
        if (format == Globals.TIMEFORMAT.H_MM) {
            hours = String.valueOf(time.hours);
            if (time.minutes < 10) minutes = "0" + time.minutes;
            else minutes = String.valueOf(time.minutes);
            return hours + ":" + minutes;
        } if (format == Globals.TIMEFORMAT.HH_MM_SS) {
            if (time.hours < 10) hours = "0" + time.hours;
            else hours = String.valueOf(time.hours);
            if (time.minutes < 10) minutes = "0" + time.minutes;
            else minutes = String.valueOf(time.minutes);
            if (time.seconds < 10) seconds = "0" + time.seconds;
            else seconds = String.valueOf(time.seconds);

            return hours + ":" + minutes + ":" + seconds;
        } if (format == Globals.TIMEFORMAT.WORDED_DATE) {
            String month = intMonthToShortString(time.month);
            return time.date + " " + month + " " + time.year;
        }
        return null;
    }

    public static String intMonthToShortString(int month) {
        switch (month + 1) { ////note the + 1 bc jan = 0
            case 1: return "Jan";
            case 2: return "Feb";
            case 3: return "March";
            case 4: return "April";
            case 5: return "May";
            case 6: return "June";
            case 7: return "July";
            case 8: return "Aug";
            case 9: return "Sept";
            case 10: return "Oct";
            case 11: return "Nov";
            case 12: return "Dec";
        }
        return null;
    }

}
