package com.cec.videoplayer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cec.videoplayer.R;
import com.cec.videoplayer.model.CategoryInfo;
import com.cec.videoplayer.model.NetValue;
import com.cec.videoplayer.utils.FileUtil;
import com.cec.videoplayer.utils.PermissionUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SplashActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private final int SPLASH_DISPLAY_LENGHT = 1000;
    private List<CategoryInfo> tcategoryInfos = new ArrayList<>();
    private NetValue netValue = new NetValue();
    private String filePath = Environment.getExternalStorageDirectory().getPath() + "/VideoPlayer/category.txt";
    private File fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            //设置让应用主题内容占据状态栏和导航栏
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //设置状态栏和导航栏颜色为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_splash);
        fileName = new File(filePath);
        if (PermissionUtils.isGrantExternalRW(SplashActivity.this, 1)) {
            initView();
        }
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
                                    } else {
                                        Toast.makeText(SplashActivity.this, "当前设备无可用网络，初始化失败。", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
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
                            Toast.makeText(SplashActivity.this, "为保证正常运行，请授权使用存储权限。", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    private void initView() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getNeteData();
            }
        }, SPLASH_DISPLAY_LENGHT);
    }


    private void getNeteData() {
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
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("categorys", (ArrayList<? extends Parcelable>) tcategoryInfos);
                intent.putExtras(bundle);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        } else {
            new Thread(() -> {
                String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/ContentApi-getCategoryInfo.action?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&returnType=json";
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
                        String json = response.body().string();
                        response.body().close();
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
                        initModel(json);
                    }
                });
            }).start();
        }
    }

    public void initModel( String str) {
        Gson gson = new Gson();
        tcategoryInfos = gson.fromJson(str, new TypeToken<List<CategoryInfo>>() {
        }.getType());
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
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

}
