package com.cec.videoplayer.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.db.DatabaseHelper;
import com.cec.videoplayer.model.User;
import com.cec.videoplayer.utils.ActivityManager;
import com.cec.videoplayer.utils.EditTextClearUtil;
import com.cec.videoplayer.utils.ToastUtils;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 登录活动页
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences setting;
    private Boolean is_first;
    private DatabaseHelper helper;
    private EditText e1, e2;
    private ImageView m1, m2;
    private Button btn;
    // 要申请的权限
    private String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private User sessionUser;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //判断当前栈中是否已存在LoginActivity
       boolean flag =  ActivityManager.isExistActivity(LoginActivity.class);
       if (!flag) {
           ActivityManager.getActivityManager().add(this);
       }
       //关闭除当前activity外的所有activity
        ActivityManager.finish(this);

        getSession();
        authority();
        init();
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

    private void init() {
        btn = (Button) findViewById(R.id.loginButton);
        btn.setOnClickListener(this);
        e1 = (EditText) findViewById(R.id.phonenumber);
        e2 = (EditText) findViewById(R.id.password);
        m1 = (ImageView) findViewById(R.id.del_phonenumber);
        m2 = (ImageView) findViewById(R.id.del_password);
        EditTextClearUtil.addclerListener(e1, m1);
        EditTextClearUtil.addclerListener(e2, m2);
        if (sessionUser != null && sessionUser.getName() != "") {
            e1.setText(sessionUser.getName());
            e2.setText(sessionUser.getPassword());
        }

    }

    @Override
    public void onClick(View v) {

        User user = new User(e1.getText().toString(), e2.getText().toString());
        switch (v.getId()) {
            case R.id.loginButton:
                if (user.getName().equals("") || user.getPassword().equals("")) {
                    ToastUtils.showShort("用户名和密码不能为空");
                    return;
                }


                //接口提供后使用
//                login();
                user = new User("demo1", "123456","f620969ebe7a0634c0aabc1b4fecf1ab" );
                saveSession(user);
                Intent intent = new Intent(LoginActivity.this, TabActivity.class);
                startActivity(intent);
                this.finish();
                break;
        }

    }

    //通过网络获取用户登录信息
    private void login() {
        //登录请求接口
        url = "";
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            //异步加载
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("load", "onFailure: ");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response = client.newCall(request).execute();
                    String json =  response.body().string(); //请求返回结果
                }
            });
        }).start();
    }



    /**
     * 保存当前用户session用于"我的"界面显示用户信息
     *
     * @param user
     */
    private void saveSession(User user) {
        SharedPreferences setting = getSharedPreferences("User", 0);
        SharedPreferences.Editor editor = setting.edit();
        //判断当前用户是否已登录
        editor.putBoolean("isLogin", true);
        //用户登录接口提供后使用
        editor.putString("token", String.valueOf(user.getToken()));
        editor.putString("name", user.getName());
        editor.putString("password", user.getPassword());
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //权限判断
    private void authority() {
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //先判断有没有权限 ，没有就在这里进行权限的申请
            ActivityCompat.requestPermissions(this,
                    permissions, 321);
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                // 开始提交存储请求权限
                ActivityCompat.requestPermissions(this, permissions, 321);
            }
        }
    }

    /**
     * 获取当前用户session信息, 用于默认加载上次登录用户
     */
    private void getSession() {
        SharedPreferences setting = this.getSharedPreferences("User", 0);
        sessionUser = new User(setting.getString("name", ""), setting.getString("password", ""));
    }



}
