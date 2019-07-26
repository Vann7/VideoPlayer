package com.cec.videoplayer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.cec.videoplayer.R;
import com.cec.videoplayer.model.User;
import com.cec.videoplayer.module.CategoryInfo;
import com.cec.videoplayer.service.NetService;
import com.cec.videoplayer.utils.FileUtil;
import com.cec.videoplayer.utils.PermissionUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.os.Parcelable;
import android.widget.Toast;


public class SplashActivity extends Activity {
    private final int SPLASH_DISPLAY_LENGHT = 1;
    private List<CategoryInfo> tcategoryInfos = new ArrayList<>();
    private int net = 0;
    private NetService netService = new NetService();
    private String json;
    private String filePath = Environment.getExternalStorageDirectory().getPath() + "/VideoPlayer/category.txt";
    private File fileName;
    private boolean isLogin;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        fileName = new File(filePath);
        PermissionUtils.isGrantExternalRW(SplashActivity.this, 1);
//        getSession();
        initView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //检验是否获取权限，如果获取权限，外部存储会处于开放状态，会弹出一个toast提示获得授权
                    if (!isNetworkAvailable(SplashActivity.this)) {
                        String sdCard = Environment.getExternalStorageState();
                        if (sdCard.equals(Environment.MEDIA_MOUNTED)) {
                            FileUtil.createSDCardDir();
                            if (fileName.exists()) {
                                try {
                                    String str = FileUtil.readTxtFile(fileName);
                                    if (str != "") {
                                        Gson gson = new Gson();
                                        tcategoryInfos = gson.fromJson(str, new TypeToken<List<CategoryInfo>>() {
                                        }.getType());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(SplashActivity.this, TabActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelableArrayList("categorys", (ArrayList<? extends Parcelable>) tcategoryInfos);
                                    intent.putExtras(bundle);
                                    SplashActivity.this.startActivity(intent);
                                    SplashActivity.this.finish();
                                }
                            }, SPLASH_DISPLAY_LENGHT);
                        }
                    } else {
                        initView();

                    }

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SplashActivity.this, "为保证正常运行，请授权使用存储权限。", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
//    private void initView() {
//            if (!isNetworkAvailable(SplashActivity.this)) {
//                if (isWifi(SplashActivity.this)) {
//                    net = 1;
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent intent = new Intent(SplashActivity.this, NoNetworkActivity.class);
//                            intent.putExtra("netType", net);
//                            startActivity(intent);
//                            SplashActivity.this.finish();
//                        }
//                    }, SPLASH_DISPLAY_LENGHT);
//                } else if (isMobile(SplashActivity.this)) {
//                    net = 2;
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent intent = new Intent(SplashActivity.this, NoNetworkActivity.class);
//                            intent.putExtra("netType", net);
//                            startActivity(intent);
//                            SplashActivity.this.finish();
//                        }
//                    }, SPLASH_DISPLAY_LENGHT);
//                } else {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent intent = new Intent(SplashActivity.this, NoNetworkActivity.class);
//                            startActivity(intent);
//                            SplashActivity.this.finish();
//                        }
//                    }, SPLASH_DISPLAY_LENGHT);
//                }}}

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

    private void initView() {
//        if (!isNetworkAvailable(SplashActivity.this)) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getNeteData();
            }
        }, SPLASH_DISPLAY_LENGHT);
//        } else {

//            } else {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getNeteData();
            }
        }, SPLASH_DISPLAY_LENGHT);
    }


    private void getNeteData() {
        new Thread(() -> {
            String url = "http://" + netService.getIp() + ":" + netService.getPort() + "/powercms/api/ContentApi-getCategoryInfo.action?userName=demo1&token=f620969ebe7a0634c0aabc1b4fecf1ab&returnType=json";
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            //同步加载
         /* try {
              Response response = client.newCall(request).execute();
              String json =  response.body().string();
              initModel(json);
          } catch (IOException e) {
              e.printStackTrace();
          }*/

            //异步加载
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("load", "onFailure: ");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response = client.newCall(request).execute();
                    json = response.body().string();
                    boolean create = false;
                    try {
                        create = FileUtil.createFile(fileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (create) {
                        try {
                            FileUtil.writeTxtFile(json, fileName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            FileUtil.deleteFile(filePath);
                            FileUtil.createFile(fileName);
                            FileUtil.writeTxtFile(json, fileName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    initModel();
                }
            });
        }).start();
    }

    public void initModel() {
        Gson gson = new Gson();
        tcategoryInfos = gson.fromJson(json, new TypeToken<List<CategoryInfo>>() {
        }.getType());
        Intent intent = new Intent(SplashActivity.this, TabActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("categorys", (ArrayList<? extends Parcelable>) tcategoryInfos);
        intent.putExtras(bundle);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }

    /**
     * 检查是否有网络
     */
    public static boolean isNetworkAvailable(Context context) {

        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isAvailable();
    }


    /**
     * 检查是否是WIFI
     */
    public static boolean isWifi(Context context) {

        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }


    /**
     * 检查是否是移动网络
     */
    public static boolean isMobile(Context context) {

        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            }
        }
        return false;
    }


    private static NetworkInfo getNetworkInfo(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    private void getSession() {
        SharedPreferences setting = this.getSharedPreferences("User", 0);
        user = new User(setting.getString("name", ""), setting.getString("password", ""));
        isLogin = setting.getBoolean("isLogin", false);
    }
}
