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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.db.DatabaseHelper;
import com.cec.videoplayer.model.User;
import com.cec.videoplayer.utils.EditTextClearUtil;
import com.cec.videoplayer.utils.StatusBarUtils;
import com.cec.videoplayer.utils.ToastUtils;
import com.cec.videoplayer.service.UserService;


import java.util.List;
import java.util.UUID;

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
    private UserService userService;
    // 要申请的权限
    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private String[] permission_camera = {Manifest.permission.CAMERA};
    private String[] permission_write_storage = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private byte[] images;
    private User sessionUser;

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
        saveConfig();
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
        if (sessionUser != null && sessionUser.getName() == "") {
            e1.setText("root");
        } else {
            e1.setText(sessionUser.getName());
        }

        e2.setText("admin"); //todo 正式上线时,取消
//        e2.setText(sessionUser.getPassword());


//        ImageUtil imageUtil = new ImageUtil(this);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.default_image);
//        images = imageUtil.imageToByte(bitmap);
    }

    @Override
    public void onClick(View v) {
        userService = new UserService(this);

        User user = new User(e1.getText().toString(), e2.getText().toString());
        user.setmId(UUID.randomUUID().toString());
        switch (v.getId()) {
            case R.id.loginButton:
                if (user.getName().equals("") || user.getPassword().equals("")) {
                    ToastUtils.showShort("用户名和密码不能为空");
                    return;
                }
                List<User> list = userService.checkUser(user);
                if (list.size() > 0) {
                    saveSession(list.get(0));
                    Intent intent = new Intent(LoginActivity.this, TabActivity.class);
                    intent.putExtra("user", user);
                    setResult(RESULT_OK, intent);
                    startActivityForResult(intent, 1);
//                    startActivity(intent,ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                    this.finish();
                } else {
                    ToastUtils.showShort("当前用户名或密码错误");
                }
                break;
        }

    }


    /**
     * 第一次登录系统 保存root用户信息
     */
    private void saveConfig() {
        setting = getSharedPreferences("setting", MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();
        is_first = setting.getBoolean("FIRST", true);
        if (is_first) {
            editor.putString("name", "root");
            editor.putString("password", "admin");
            editor.putBoolean("FIRST", false);
            editor.putBoolean("appUpdate", false);
            boolean flag = editor.commit();
            if (flag) {
                userService = new UserService(this);
                userService.insert(new User("root", "admin", false));
            }
        }
    }

    /**
     * 保存当前用户session用于"我的"界面显示用户信息
     *
     * @param user
     */
    private void saveSession(User user) {
        SharedPreferences setting = getSharedPreferences("User", 0);
        SharedPreferences.Editor editor = setting.edit();
        editor.putString("id", String.valueOf(user.getId()));
        editor.putString("name", user.getName());
        editor.putString("mid", user.getmId());
        editor.putString("password", user.getPassword());
        editor.putBoolean("appUpdate", user.isAppUpdate());
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
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                // 开始提交摄像头、存储请求权限
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
        sessionUser.setId(Integer.valueOf(setting.getString("id", "0")));
        sessionUser.setAppUpdate(Boolean.valueOf(setting.getBoolean("appUpdate", false)));
    }

}
