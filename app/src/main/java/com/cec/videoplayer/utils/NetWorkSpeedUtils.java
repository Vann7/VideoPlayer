package com.cec.videoplayer.utils;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ricky on 2016/10/13.
 */
public class NetWorkSpeedUtils {
    private Context context;
    private Handler mHandler;

    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;

    public NetWorkSpeedUtils(Context context, Handler mHandler) {
        this.context = context;
        this.mHandler = mHandler;
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            showNetSpeed();
        }
    };

    public void startShowNetSpeed() {
        lastTotalRxBytes = getTotalRxBytes();
        lastTimeStamp = System.currentTimeMillis();
        new Timer().schedule(task, 1000, 1000); // 1s后启动任务，每2s执行一次

    }

    private long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }

    private void showNetSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
        long speed1;
        long speed2;
        if (speed < 1024) {
            speed1 = speed;
            speed2 = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 % (nowTimeStamp - lastTimeStamp));//毫秒转换

        } else {
            speed1 = speed / 1024;
            speed2 = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / 1024 % (nowTimeStamp - lastTimeStamp));
        }
        if (speed2 < 10) {

        } else if (speed2 < 100) {
            speed2 = speed2 / 10;
        } else if (speed2 < 1000) {
            speed2 = speed2 / 100;
        } else if (speed2 < 10000) {
            speed2 = speed2 / 1000;
        }
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;

        Message msg = mHandler.obtainMessage();
        msg.what = 100;
        if (speed < 1024) {
            msg.obj = speed1 + "." + speed2 + " Kb/s";
        } else {
            msg.obj = speed1 + "." + speed2 + " Mb/s";
        }
        mHandler.sendMessage(msg);//更新界面
    }
}