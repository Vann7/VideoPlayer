package com.cec.videoplayer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cec.videoplayer.R;
import com.cec.videoplayer.adapter.CommentAdapter;
import com.cec.videoplayer.adapter.ContentAdapter;
import com.cec.videoplayer.adapter.FilesAdapter;
import com.cec.videoplayer.adapter.RelateAdapter;
import com.cec.videoplayer.module.CategoryInfo;
import com.cec.videoplayer.module.Comment;
import com.cec.videoplayer.module.ContentInfo;
import com.cec.videoplayer.module.Relate;
import com.cec.videoplayer.utlis.MediaUtils;
import com.cec.videoplayer.utlis.PlayHitstUtil;
import com.cec.videoplayer.utlis.ToastUtils;
import com.cec.videoplayer.view.MyListView;
import com.dou361.ijkplayer.bean.VideoijkBean;
import com.dou361.ijkplayer.listener.OnShowThumbnailListener;
import com.dou361.ijkplayer.widget.PlayStateParams;
import com.dou361.ijkplayer.widget.PlayerView;
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


/**
 * Created by Android Studio.
 * User: cec
 * Date: 2019/7/3
 * Time: 9:47 AM
 */
public class PlayerActivity extends AppCompatActivity {

    private PlayerView player;
    private Context mContext;
    private MyListView lv_relate;
    private MyListView lv_comment;
    private RelateAdapter relAdapter;
    private CommentAdapter comAdapter;
    private TextView tv_title;
    private TextView tv_hits;
    private TextView tv_updateTime;
    private View view_file;

    private RecyclerView rv_content_files;
    private FilesAdapter mAdapter;
    private LinearLayoutManager layoutManager;


    private PowerManager.WakeLock wakeLock;
    private ContentInfo contentInfo;
    private List<Relate> relateList;
    private List<Comment> commentList;
    private String id;
    private String title;
    private String url;

    // 要申请的权限
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setContentView(R.layout.activity_player);
        initView();
        initEvent();
        authority();
        getNetData();
    }



    @SuppressLint("InvalidWakeLockTag")
    private void initView() {

        //初始化视频播放器
        /**常亮*/
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "liveTAG");
        wakeLock.acquire();
        String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f30.mp4";
        String h264 = getLocalVideoPath("c2282f525c494dc7ace426cc5c08fa3f.mp4");
        String h265 = getLocalVideoPath("e40651e289f14cbdbc8e425f17cded9b.mp4");

        player = new PlayerView(this)
                .setTitle(title)
                .setProcessDurationOrientation(PlayStateParams.PROCESS_PORTRAIT)
                .setScaleType(PlayStateParams.fitparent)
                .hideCenterPlayer(true)
                .hideMenu(true)
                .hideSteam(true)
                .setPlaySource(url);

        //初始化相关视频lv
        lv_relate = findViewById(R.id.lv_relate);
        relateList = new ArrayList<>();
        relAdapter = new RelateAdapter(relateList, this, lv_relate);
        lv_relate.setAdapter(relAdapter);

        //初始化评论lv
        lv_comment = findViewById(R.id.lv_comment);
        commentList = new ArrayList<>();
        comAdapter = new CommentAdapter(commentList, this, lv_comment);
        lv_comment.setAdapter(comAdapter);

        //初始化视频信息
        tv_title = findViewById(R.id.tv_content_title);
        tv_hits = findViewById(R.id.tv_content_hits);
        tv_updateTime = findViewById(R.id.tv_content_updateTime);

        //初始化视频剧集rv
        rv_content_files = findViewById(R.id.rv_content_files);
        layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_content_files.setLayoutManager(layoutManager);
        List<com.cec.videoplayer.module.File> files = new ArrayList<>();
        mAdapter = new FilesAdapter(rv_content_files, this, files);
        rv_content_files.setAdapter(mAdapter);
        rv_content_files.setVisibility(View.GONE);
        view_file = findViewById(R.id.line_file);
        view_file.setVisibility(View.GONE);

    }

    private void initEvent() {

        relAdapter.setOnListClickListener((relate, position) -> {
            Intent intent = new Intent(PlayerActivity.this, PlayerActivity.class);
            intent.putExtra("title", relateList.get(position).getTitle());
            intent.putExtra("id", relateList.get(position).getId());
            startActivity(intent);
        });

        comAdapter.setOnListClickListener((comment, position) -> {
            ToastUtils.showShort(comment.toString());
        });

        //切换剧集
        mAdapter.setOnListClickListener((v, position) -> {
             contentInfo.getFiles().get(position).getPlayurl();

            String h264 = getLocalVideoPath("c2282f525c494dc7ace426cc5c08fa3f.mp4");
            String h265 = getLocalVideoPath("e40651e289f14cbdbc8e425f17cded9b.mp4");

            player.setPlaySource((position == 0) ? h264 : h265 )
                    .startPlay();
        });

    }

    private void getNetData() {
        id = getIntent().getStringExtra("id");
        title = getIntent().getStringExtra("title");
        url = "http://115.28.215.145:8080/powercms/api/ContentApi-getContentInfo.action" +
                "?userName=demo1&token=f620969ebe7a0634c0aabc1b4fecf1ab&returnType=json&size=10&" +
                "contentId="+ id;

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
                    String json =  response.body().string();
                    initModel(json);
                }
            });
        }).start();
    }

    public void initModel(String json) {
        Gson gson = new Gson();
        List<ContentInfo> infos = gson.fromJson(json,new TypeToken<List<ContentInfo>>() {}.getType());

        contentInfo = infos.get(0);
        relateList = infos.get(1).getRelates();

        //子线程刷新UI
        PlayerActivity.this.runOnUiThread(() -> {
            player.showThumbnail(ivThumbnail -> Glide.with(mContext)
                    .load(contentInfo.getImage())
                    .placeholder(R.color.cl_default)
                    .error(R.color.cl_error)
                    .into(ivThumbnail));
//            String playUrl = contentInfo.getFiles().get(0).getPlayurl();
//            player.setPlaySource(playUrl);

            relAdapter.onDateChange(relateList);

            tv_title.setText(contentInfo.getTitle());
            tv_hits.setText(PlayHitstUtil.getCount(contentInfo.getHits()));
            tv_updateTime.setText( contentInfo.getUpdateTime() + "发布");

            if (contentInfo.getFiles().size() > 1) {
                rv_content_files.setVisibility(View.VISIBLE);
                view_file.setVisibility(View.VISIBLE);
                mAdapter.onDateChange(contentInfo.getFiles());
            } else {
                rv_content_files.setVisibility(View.GONE);
                view_file.setVisibility(View.GONE);
            }

        });

    }


    /**
     * 播放本地视频
     */

    private String getLocalVideoPath(String name) {
        String sdCard = Environment.getExternalStorageDirectory().getPath();
        String uri = sdCard + File.separator + name;
        return uri;
    }





    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.onPause();
        }
        MediaUtils.muteAudioFocus(mContext, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.onResume();
        }
        MediaUtils.muteAudioFocus(mContext, false);
        if (wakeLock != null) {
            wakeLock.acquire();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (player != null) {
            player.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        if (player != null && player.onBackPressed()) {
            return;
        }
        super.onBackPressed();
        if (wakeLock != null) {
            wakeLock.release();
        }
    }


    //权限判断
    private void authority() {
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //先判断有没有权限 ，没有就在这里进行权限的申请
            ActivityCompat.requestPermissions(this,
                    permissions,321);
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                    ) {
                // 开始提交摄像头、存储请求权限
                ActivityCompat.requestPermissions(this, permissions, 321);
            }
        }
    }

}
