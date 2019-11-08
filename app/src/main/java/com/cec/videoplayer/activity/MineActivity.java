package com.cec.videoplayer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.model.User;
import com.cec.videoplayer.utils.CacheUtil;

import java.lang.reflect.Field;

public class MineActivity extends AppCompatActivity implements View.OnClickListener {

    private User user;
    private TextView cacheSize_tv;
    private TextView name_tv;
    private RelativeLayout clearCache_rl;
    private TextView logout_tv;
    private TextView login_tv;
    private ImageView back_iv;
    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
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
        login_tv.setOnClickListener(this);
        back_iv.setOnClickListener(this);
    }

    /**
     * 初始化界面view
     */
    private void initView() {
        logout_tv = this.findViewById(R.id.mine_out_tv);
        login_tv = this.findViewById(R.id.mine_login_tv);
        clearCache_rl = this.findViewById(R.id.mine_clear_cache_rl);
        cacheSize_tv = this.findViewById(R.id.cache_size);
        name_tv = this.findViewById(R.id.mine_name_tv);
        back_iv = this.findViewById(R.id.mine_back);
        checkLogin();

        try {
            cacheSize_tv.setText(CacheUtil.getTotalCacheSize(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                getSession();
                checkLogin();
                break;
        }
    }


    private void checkLogin() {
        if (isLogin) {
            name_tv.setText(user.getName());
            login_tv.setVisibility(View.GONE);
            logout_tv.setVisibility(View.VISIBLE);
        } else {
            name_tv.setText("游客");
            logout_tv.setVisibility(View.GONE);
            login_tv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mine_out_tv:
                logout();
                break;
            case R.id.mine_clear_cache_rl:
                clean();
                break;
            case R.id.mine_login_tv:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.mine_back:
                MineActivity.this.finish();
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
        user = new User(setting.getString("name", ""), setting.getString("password", ""));
        isLogin = setting.getBoolean("isLogin", false);
    }


    /**
     * 注销当前登录用户
     */
    public void logout() {
        AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.appalertdialog)
                .setTitle("退出")
                .setMessage("退出后不会删除当前账户信息")
                .setPositiveButton("确定", (dialog, which) -> {
                    //设置登录状态为false
                    SharedPreferences setting = getSharedPreferences("User", 0);
                    SharedPreferences.Editor editor = setting.edit();
                    editor.putBoolean("isLogin", false);
                    editor.commit();
                    isLogin = false;
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivityForResult(intent, 1);
                    checkLogin();
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
