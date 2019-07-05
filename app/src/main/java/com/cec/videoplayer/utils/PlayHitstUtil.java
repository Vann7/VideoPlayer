package com.cec.videoplayer.utils;

/**
 * User: cec
 * Date: 2019/7/4
 * Time: 2:33 PM
 * 播放次数格式化
 */
public class PlayHitstUtil {

    private static String unit = "万";

    public static String getCount(int hits) {

        if (hits > 9999 && hits < 100000) {
            StringBuilder hitCounts;
            int hit = hits / 10000;
            int hit2 = (hits % 10000) / 1000;
            hitCounts = new StringBuilder().append(String.valueOf(hit))
                    .append(".")
                    .append(String.valueOf(hit2));
            return hitCounts + "万次播放";
        } else if (hits > 99999){
            int hit = hits / 10000;
            return String.valueOf(hit) + "万次播放";
        } else {
            return String.valueOf(hits) + "次播放";
        }


    }
}
