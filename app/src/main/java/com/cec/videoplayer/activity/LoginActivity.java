package com.cec.videoplayer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cec.videoplayer.R;
import com.cec.videoplayer.model.NetValue;
import com.cec.videoplayer.model.User;
import com.cec.videoplayer.utils.EditTextClearUtil;
import com.cec.videoplayer.utils.ToastUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
    private EditText e1, e2;
    private ImageView m1, m2;
    private Button btn;
    private User sessionUser;
    private NetValue netValue;
    private Boolean isLogin = false;

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
        netValue = new NetValue();
        //判断当前栈中是否已存在LoginActivity
//       boolean flag =  ActivityManager.isExistActivity(LoginActivity.class);
//       if (!flag) {
//           ActivityManager.getActivityManager().add(this);
//       }
        //关闭除当前activity外的所有activity
//        ActivityManager.finish(this);

        getSession();
//        authority();
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
        btn = findViewById(R.id.loginButton);
        btn.setOnClickListener(this);
        e1 = findViewById(R.id.phonenumber);
        e2 = findViewById(R.id.password);
        m1 = findViewById(R.id.del_phonenumber);
        m2 = findViewById(R.id.del_password);
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
                login();
                break;
        }
    }

    public void result() {
        if (isLogin) {
            User user = new User(e1.getText().toString(), e2.getText().toString(), "f620969ebe7a0634c0aabc1b4fecf1ab");
            saveSession(user);
            this.setResult(RESULT_OK);
            LoginActivity.this.finish();
        }
    }

    //通过网络获取用户登录信息
    private void login() {
        //登录请求接口
        String mName=e1.getText().toString();
        try{
            mName = URLEncoder.encode(mName, "UTF-8");
            mName = URLEncoder.encode(mName, "UTF-8");
        }catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/SystemApi-validationMember.action?loginName="
                + mName + "&password=" + e2.getText().toString();
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
                    Looper.prepare();
                    Toast.makeText(LoginActivity.this, "无网络连接，请检查网络设置。", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response = client.newCall(request).execute();
                    String json = response.body().string(); //请求返回结果
                    response.body().close();
                    isLogin = Boolean.parseBoolean(json);
                    if (!Boolean.parseBoolean(json)) {
                        Looper.prepare();
                        Toast.makeText(LoginActivity.this, "用户名或密码错误。", Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                    result();
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
        editor.putString("name", user.getName());
        editor.putString("password", user.getPassword());
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }



    /**
     * 获取当前用户session信息, 用于默认加载上次登录用户
     */
    private void getSession() {
        SharedPreferences setting = this.getSharedPreferences("User", 0);
        sessionUser = new User(setting.getString("name", ""), setting.getString("password", ""));
    }


}
