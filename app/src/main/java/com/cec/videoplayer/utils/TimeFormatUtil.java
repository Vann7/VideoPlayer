package com.cec.videoplayer.utils;

/**
 * User: cec
 * 秒数转换为视频时间格式
 * Date: 2019/7/11
 * Time: 11:08 AM
 */
public class TimeFormatUtil {

    public static String MillisecondToDate(long time) {
        String timeStr = null;
        long hour = 0;
        long minute = 0;
        long second = 0;
        if (time <= 0){
            return "00:00";
        } else {
            time = time / 1000;
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99) return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;

    }

    private static String unitFormat(long i) {
        String retStr = null;
        if (i >= 0 && i < 10){
            retStr = "0" + Long.toString(i);
        }else{
            retStr = "" + i;
        }
        return retStr;
    }

}
