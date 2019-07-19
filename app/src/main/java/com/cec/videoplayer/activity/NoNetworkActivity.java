package com.cec.videoplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.cec.videoplayer.R;

public class NoNetworkActivity extends Activity {
    private int type = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_error);
        Intent intent = getIntent();
        type = intent.getIntExtra("netType", 0);
        initView();
    }
    @Override
    protected void onResume() {
        /**
         * 设置为竖屏
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onResume();
    }
    public void initView() {
        switch (type) {
            case 0: {
                TextView textView = findViewById(R.id.textView);
                textView.setText("无网络连接" + "\n" + "请检查网络设置！");
                break;
            }
            case 1: {
                TextView textView = findViewById(R.id.textView);
                textView.setText("当前Wifi不可用" + "\n" + "请检查网络设置！");
                break;
            }
            case 2: {
                TextView textView = findViewById(R.id.textView);
                textView.setText("当前移动网络不可用" + "\n" + "请检查网络设置！");
                break;
            }
        }
    }
}
