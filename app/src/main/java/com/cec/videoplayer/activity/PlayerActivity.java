package com.cec.videoplayer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cec.videoplayer.R;

import com.cec.videoplayer.adapter.CommentAdapter;
import com.cec.videoplayer.adapter.ContentAdapter;
import com.cec.videoplayer.adapter.FilesAdapter;
import com.cec.videoplayer.adapter.RelateAdapter;
import com.cec.videoplayer.model.User;
import com.cec.videoplayer.module.CategoryInfo;
import com.cec.videoplayer.module.Comment;
import com.cec.videoplayer.module.ContentInfo;
import com.cec.videoplayer.module.Relate;
import com.cec.videoplayer.utils.MediaUtils;
import com.cec.videoplayer.utils.PlayHitstUtil;
import com.cec.videoplayer.utils.ToastUtils;
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
import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;
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
public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {

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
    private RelativeLayout rl_player;

    private User user;

    private RecyclerView rv_content_files;
    private FilesAdapter mAdapter;
    private LinearLayoutManager layoutManager;


    private PowerManager.WakeLock wakeLock;
    private ContentInfo contentInfo;
    private List<Relate> relateList;
    private List<Comment> commentList;
    private String id;
    private String url;

    //弹幕
    private boolean showDanmaku;
    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;
    private ImageView iv_bar_player;
    private ImageView iv_bar_danmu;
    private ImageView iv_back;
    private boolean isDanmu = true;

    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };


    // 要申请的权限
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setContentView(R.layout.activity_player);
        id = getIntent().getStringExtra("id");
        getSession();
        initView();
        initEvent();
        authority();
        getNetData(id);

    }


    @SuppressLint("InvalidWakeLockTag")
    private void initView() {

        //初始化视频播放器
        /**常亮*/
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "liveTAG");
        wakeLock.acquire();
        String h264 = getLocalVideoPath("c2282f525c494dc7ace426cc5c08fa3f.mp4");
        String h265 = getLocalVideoPath("e40651e289f14cbdbc8e425f17cded9b.mp4");

        player = new PlayerView(this)
                .setProcessDurationOrientation(PlayStateParams.PROCESS_PORTRAIT)
                .setScaleType(PlayStateParams.fitparent)
                .hideCenterPlayer(true)
                .hideMenu(true)
                .hideSteam(true);

        //初始化相关视频lv
        rl_player = findViewById(R.id.rl_player);
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


        danmakuView = (DanmakuView) findViewById(R.id.danmaku_view);
        danmakuView.enableDanmakuDrawingCache(true);
        danmakuContext = DanmakuContext.create();
        danmakuView.prepare(parser, danmakuContext);

        iv_bar_player = findViewById(R.id.app_video_play);
        iv_bar_danmu = findViewById(R.id.app_video_danmu);
        iv_back = (ImageView)findViewById(com.dou361.ijkplayer.R.id.app_video_finish);
    }

    private void initEvent() {
        iv_bar_player.setOnClickListener(this);
        iv_bar_danmu.setOnClickListener(this);
        player.iv_fullscreen.setOnClickListener(this);
        iv_back.setOnClickListener(this);

        //切换相关视频
        relAdapter.setOnListClickListener((relate, position) -> {
            getNetData(relateList.get(position).getId());
        });

        comAdapter.setOnListClickListener((comment, position) -> {
            ToastUtils.showShort(comment.toString());
        });

        //切换剧集
        mAdapter.setOnListClickListener((v, position) -> {
             player.setPlaySource(contentInfo.getFiles().get(position).getPlayurl())
                    .startPlay();

//            String h264 = getLocalVideoPath("c2282f525c494dc7ace426cc5c08fa3f.mp4");
//            String h265 = getLocalVideoPath("video-h265.mkv");

//            player.setPlaySource((position == 0) ? h264 : h265 )
//                    .startPlay();
        });


        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku = true;
                danmakuView.start();
                generateSomeDanmaku();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });

    }

    private void getNetData(String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://115.28.215.145:8080/powercms/api/ContentApi-getContentInfo.action" +
                        "?userName=");
        sb.append(user.getName());
        sb.append("&token=");
        sb.append(user.getToken());
        sb.append("&returnType=json&size=20&contentId=");
        sb.append(id);
//        url = "http://115.28.215.145:8080/powercms/api/ContentApi-getContentInfo.action" +
//                "?userName=demo1&token=f620969ebe7a0634c0aabc1b4fecf1ab&returnType=json&size=10&" +
//                "contentId="+ id;
        url = sb.toString();

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
        if (infos != null && infos.size() >= 2) {
            contentInfo = infos.get(0);
            relateList = infos.get(1).getRelates();
            if (contentInfo.getFiles() != null && contentInfo.getFiles().size() > 0) {
                //子线程刷新UI
                PlayerActivity.this.runOnUiThread(() -> {
                    String url = contentInfo.getFiles().get(0).getPlayurl();
                    player.setPlaySource(url)
                            .showThumbnail(ivThumbnail -> Glide.with(mContext)
                                    .load(contentInfo.getImage())
                                    .placeholder(R.color.cl_default)
                                    .error(R.color.cl_error)
                                    .into(ivThumbnail))
                            .startPlay();


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
            } else {
                PlayerActivity.this.runOnUiThread(() -> {
                    ToastUtils.showLong("无法播放");
                });

                this.finish();
            }
        } else {
            PlayerActivity.this.runOnUiThread(() -> {
                ToastUtils.showLong("无法播放");
            });
            this.finish();
        }


    }


    /**
     * 绑定点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_video_play :
                /**视频播放和暂停*/
                if (player.videoView.isPlaying()) {
                    danmakuView.pause();
                    if (player.isLive) {
                        player.videoView.stopPlayback();
                    } else {
                        player.pausePlay();
                    }

                } else {
                    player.startPlay();
                    if (player.videoView.isPlaying()) {
                        player.status = PlayStateParams.STATE_PREPARING;
                        player.hideStatusUI();
                    }
                    danmakuView.resume();
                }
                player.updatePausePlay();
                break;
            case R.id.app_video_danmu :
                /**隐藏或显示弹幕*/
                if (isDanmu) {
                    danmakuView.setVisibility(View.GONE);
                    iv_bar_danmu.setImageResource(R.drawable.danmuguan);
                    isDanmu = false;
                } else {
                    danmakuView.setVisibility(View.VISIBLE);
                    iv_bar_danmu.setImageResource(R.drawable.danmukai);
                    isDanmu = true;
                }
                break;
            case R.id.app_video_fullscreen :
                /**视频全屏切换*/
                toggleFullScreen();
                break;
            case R.id.app_video_finish :
                /**返回*/
                if ( !player.isPortrait) {
                    toggleFullScreen();
                } else {
                    this.finish();
                }

        }
    }

    /**
     * 播放本地视频
     */
    private String getLocalVideoPath(String name) {
        String sdCard = Environment.getExternalStorageDirectory().getPath();
        String uri = sdCard + File.separator + name;
        return uri;
    }

    /**
     * 全屏切换
     */
    public PlayerView toggleFullScreen() {

        if (player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
            params.weight = 3;
            rl_player.setLayoutParams(params);
            player.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            rl_player.setLayoutParams(params);
            player.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        player.isPortrait = (player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        player.updateFullScreenButton();
        return player;
    }

    /**
     * 向弹幕View中添加一条弹幕
     * @param content
     *          弹幕的具体内容
     * @param  withBorder
     *          弹幕是否有边框
     */
    private void addDanmaku(String content, boolean withBorder) {
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;
        danmaku.padding = 5;
        danmaku.textSize = sp2px(20);
        danmaku.textColor = Color.WHITE;
        danmaku.setTime(danmakuView.getCurrentTime());
        if (withBorder) {
            danmaku.borderColor = Color.GREEN;
        }
        danmakuView.addDanmaku(danmaku);
    }

    /**
     * 随机生成一些弹幕内容以供测试
     */
    private void generateSomeDanmaku() {
        new Thread(() -> {
            while(showDanmaku) {
                int time = new Random().nextInt(300);
                String content = "" + time + time;
                addDanmaku(content, false);
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * sp转px的方法。
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }




    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.onPause();
        }
        MediaUtils.muteAudioFocus(mContext, true);

        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
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
        if (danmakuView != null && danmakuView.isPrepared() && danmakuView.isPaused()) {
            danmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.onDestroy();
        }
        showDanmaku = false;
        if (danmakuView != null) {
            danmakuView.release();
            danmakuView = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        if (player != null) {
//            player.onConfigurationChanged(newConfig);
//        }
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            ToastUtils.showShort("landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            ToastUtils.showShort("portrait");
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

    /**
     * 获取当前用户session信息, 用于默认加载上次登录用户
     */
    private void getSession() {
        SharedPreferences setting = this.getSharedPreferences("User", 0);
        user = new User(setting.getString("name", ""), setting.getString("password", "")
        , setting.getString("token", ""));
    }


}
