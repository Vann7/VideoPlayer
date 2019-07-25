package com.cec.videoplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.model.User;
import com.cec.videoplayer.utils.ActivityManager;
import com.cec.videoplayer.utils.CacheUtil;

import java.lang.reflect.Field;

public class MineActivity extends AppCompatActivity implements View.OnClickListener {

    private User user;
    private TextView cacheSize_tv;
    private TextView name_tv;
    private RelativeLayout clearCache_rl;
    private TextView logout_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);
        getSession();
        initView();
        initEvent();
    }


    /**
     * 绑定事件
     */
    private void initEvent() {
        clearCache_rl.setOnClickListener(this);
        logout_tv.setOnClickListener(this);

    }

    /**
     * 初始化界面view
     */
    private void initView() {
        getSession();
        logout_tv = (TextView) this.findViewById(R.id.mine_out_tv);
        clearCache_rl = (RelativeLayout) this.findViewById(R.id.mine_clear_cache_rl);
        cacheSize_tv = (TextView) this.findViewById(R.id.cache_size);
        name_tv = (TextView) this.findViewById(R.id.mine_name_tv);
        name_tv.setText(user.getName());
        try {
            cacheSize_tv.setText(CacheUtil.getTotalCacheSize(this));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mine_out_tv :
                logout();
                break;
            case R.id.mine_clear_cache_rl :
                clean();
                break;
        }
    }


    /**
     * 清除缓存
     */
    private void clean() {
        CacheUtil.clearAllCache(this);
        cacheSize_tv.setText("0KB");
    }

    /**
     * 获取当前用户session信息
     */
    private void getSession() {
        SharedPreferences setting = this.getSharedPreferences("User", 0);
        user = new User(setting.getString("name",""),setting.getString("password",""));
    }


    /**
     * 注销当前登录用户
     */
    public void logout() {
        AlertDialog alertDialog = new AlertDialog.Builder(this,R.style.appalertdialog)
                .setTitle("退出")
                .setMessage("退出后不会删除当前账户信息")
                .setPositiveButton("确定", (dialog, which) -> {
                    //设置登录状态为false
                    SharedPreferences setting = getSharedPreferences("User", 0);
                    SharedPreferences.Editor editor = setting.edit();
                    editor.putBoolean("isLogin", false);
                    editor.commit();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//关掉所要到的界面中间的activity
                    startActivity(intent);
                    this.finish();
                })
                .setNegativeButton("取消", (dialog, which) -> {

                })
                .create();

        alertDialog.show();
        //修改Message字体颜色
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(alertDialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(Color.BLACK);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

}
