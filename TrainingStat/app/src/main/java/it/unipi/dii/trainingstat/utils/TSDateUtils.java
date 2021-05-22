package it.unipi.dii.trainingstat.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

public class TSDateUtils {
    public static Date getCurrentUTCDate(){
        Instant instant = Instant.now();
        return Date.from(instant);
    }

    public static String DateToStringIsoDate(Date date){
        Instant instant = date.toInstant();
        return instant.toString();
    }

    public static Date StringIsoDateToDate(String date){
        Instant instant = Instant.parse(date);
        return Date.from(instant);
    }

    public static String DateInLocalTimezoneHumanReadable(Date date){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getDefault());

        return df.format(date);
    }

    public static String DateInLocalTimezoneHumanReadable(String isoDateString){
        Date date = StringIsoDateToDate(isoDateString);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getDefault());

        return df.format(date);
    }
}
