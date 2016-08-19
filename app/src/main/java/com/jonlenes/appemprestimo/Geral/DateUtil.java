package com.jonlenes.appemprestimo.Geral;

import java.util.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by asus on 21/07/2016.
 */
public class DateUtil {
    static public Date parseDate(String sDate) {
        try {
            return new Date(new SimpleDateFormat("dd/MM/yyyy").parse(sDate).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    static public Time parseTime(String sTime) throws ParseException {
        return new Time(new SimpleDateFormat("HH:mm").parse(sTime).getTime());
    }

    static public Time sumTime(Time time, Long minutsAdd) throws ParseException {
        return new Time(time.getTime() + minutsAdd * 60 * 1000);
    }

    static public String formatTime(Time time) {
        return new SimpleDateFormat("HH:mm").format(time);
    }

    static public String formatDate(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    static public String formatDateBd(Date date) {
        if (date == null)
            return null;

        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    static public Date parseDateBd(String sDate) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
