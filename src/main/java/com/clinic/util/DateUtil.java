package com.clinic.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    private static final  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String stdFormat(Date date){
        return sdf.format(date);
    }

    public static String getZero(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date) + " 00:00:00";
    }
    // 两个时间是否是同一天
    public static boolean isSomeDay(Date d1,Date d2){
        Calendar instance1 = Calendar.getInstance();
        instance1.setTime(d1);
        Calendar instance2 = Calendar.getInstance();
        instance2.setTime(d2);
        if (instance1.get(Calendar.DATE) != instance2.get(Calendar.DATE)){
            return false;
        }
        if (instance1.get(Calendar.MONTH)!= instance2.get(Calendar.MONTH)){
            return false;
        }
        if (instance1.get(Calendar.YEAR) != instance2.get(Calendar.YEAR)){
            return false;
        }
        return true;
    }

    public static class DateRange{
        private Date start;
        private Date end;

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }

        public static DateRange of(Date s, Date e){
            DateRange dateRange = new DateRange();
            dateRange.start = s;
            dateRange.end = s;
            return dateRange;
        }

        public static DateRange ofMonth(int m){
            if (m<1 || m > 12){
                return null;
            }
            DateRange dateRange = new DateRange();
            Calendar instance = Calendar.getInstance();
            instance.set(Calendar.MONTH,m-1);
            instance.set(Calendar.SECOND,0);
            instance.set(Calendar.MINUTE,0);
            instance.set(Calendar.DATE,1);
            instance.set(Calendar.HOUR_OF_DAY,0);
            instance.set(Calendar.MILLISECOND,0);
            dateRange.start = instance.getTime();
            instance.set(Calendar.MONTH,m-1);
            instance.set(Calendar.SECOND,59);
            instance.set(Calendar.MINUTE,59);
            instance.set(Calendar.HOUR_OF_DAY,23);
            instance.set(Calendar.MILLISECOND,0);
            instance.roll(Calendar.DATE,-1);
            dateRange.end = instance.getTime();
            return dateRange;
        }
    }
    public static Date getZeroTime(Date date){
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.set(Calendar.SECOND,0);
        instance.set(Calendar.MINUTE,0);
        instance.set(Calendar.HOUR_OF_DAY,0);
        instance.set(Calendar.MILLISECOND,0);
        return instance.getTime();
    }

    public static Date getTailTime(Date date){
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.set(Calendar.SECOND,59);
        instance.set(Calendar.MINUTE,59);
        instance.set(Calendar.HOUR_OF_DAY,23);
        instance.set(Calendar.MILLISECOND,0);
        return instance.getTime();
    }


    public static Date getMonthStartTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE,1);
        return getZeroTime(calendar.getTime());
    }

    public static Date getMonthEndTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE,1);
        calendar.roll(Calendar.DATE,-1);
        return getTailTime(calendar.getTime());
    }



    public static int getMonthDayNum(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE,1);
        calendar.roll(Calendar.DATE,-1);
        return calendar.get(Calendar.DATE);
    }

    public static int getMonth(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static int getDayAtMonth(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DATE);
    }

    public static Date getDateAfter( Date date,int days){
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.add(Calendar.DAY_OF_MONTH,days);
        return  instance.getTime();
    }
}
