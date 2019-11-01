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
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cec.videoplayer.R;
import com.cec.videoplayer.adapter.CommentAdapter;
import com.cec.videoplayer.adapter.FilesAdapter;
import com.cec.videoplayer.adapter.RelateAdapter;
import com.cec.videoplayer.module.Comment;
import com.cec.videoplayer.module.ContentInfo;
import com.cec.videoplayer.module.NetValue;
import com.cec.videoplayer.module.PlayUrl;
import com.cec.videoplayer.module.Relate;
import com.cec.videoplayer.module.User;
import com.cec.videoplayer.utils.MediaUtils;
import com.cec.videoplayer.utils.NetWorkSpeedUtils;
import com.cec.videoplayer.utils.PlayHitstUtil;
import com.cec.videoplayer.utils.ToastUtils;
import com.dou361.ijkplayer.bean.VideoijkBean;
import com.dou361.ijkplayer.widget.IjkVideoView;
import com.dou361.ijkplayer.widget.PlayStateParams;
import com.dou361.ijkplayer.widget.PlayerView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
    private NetValue netValue = new NetValue();
    private RelativeLayout mostBottom;
    private PlayerView player;
    private Context mContext;
    private ListView lv_relate;
    private ListView lv_comment;
    private RelateAdapter relAdapter;
    private CommentAdapter comAdapter;
    private TextView tv_title;
    private TextView tv_hits;
    private TextView currentTime;
    private TextView tv_updateTime;
    private View view_file;
    private RelativeLayout rl_player;
    private LinearLayout relateLayout;
    private NestedScrollView nestedScrollView;
    private ImageView imageView;
    private TextView contentText;
    private TextView netSpeed;
    private Button relate_btn;
    private LinearLayout bottomMenuLayout;
    private LinearLayout noComment;
    private Handler mHnadler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    netSpeed.setText(msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
        }
    };
    /**
     * 原生的Ijkplayer
     */
    public IjkVideoView videoView;


    private User user;

    private RecyclerView rv_content_files;
    private FilesAdapter mAdapter;
    private LinearLayoutManager layoutManager;


    private PowerManager.WakeLock wakeLock;
    private ContentInfo contentInfo;
    private List<Relate> relateList;
    private List<VideoijkBean> vList = new ArrayList<>();
    private String id;
    private String fileId;
    private String contentInfoStr;

    //弹幕
    private boolean showDanmaku;
    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;
    private ImageView iv_bar_player;
    private ImageView iv_bar_danmu;
    private ImageView iv_back;
    private boolean isDanmu = true;
    private boolean removeTimer = false;
    private String memberName = "";
    private boolean isLogin;
    /**
     * 是否在拖动进度条中，默认为停止拖动，true为在拖动中，false为停止拖动
     */
    private boolean isDragging;

    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };


    // 要申请的权限
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


    //评论测试
    private TextView bt_comment;
    private List<Comment> cList = new ArrayList<>();
    private List<com.dou361.ijkplayer.module.Comment> comList = new ArrayList<>();
    private BottomSheetDialog dialog;


    private OrientationEventListener mScreenOrientation;
    private int oldOrientation = 0;
    private boolean oldOrientationLocked = false;

    public PlayerActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;

        //翻转监听
        mScreenOrientation = new OrientationEventListener(this.mContext, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return;
                }
                if ((orientation > 350 || orientation < 10) && !player.isLocked) {
                    orientation = 0;
                } else if (orientation > 80 && orientation < 100 && !player.isLocked) {
                    orientation = 90;
                } else if (orientation > 260 && orientation < 280 && !player.isLocked) {
                    orientation = 270;
                } else if (orientation > 170 && orientation < 190 && !player.isLocked) {
                    orientation = 180;
                } else {
                    return;
                }
                if (oldOrientationLocked) {
                    if (oldOrientation == 270 && orientation != 0) {
                        oldOrientationLocked = false;
                    }
                    if (oldOrientation == 0 && (orientation == 0 || orientation == 180)) {
                        oldOrientationLocked = false;
                    }
                }
                if (!oldOrientationLocked) {
                    toggleFullScreen(orientation);
                }
            }
        };
        if (mScreenOrientation.canDetectOrientation()) {
            mScreenOrientation.enable();
        } else {
            mScreenOrientation.disable();
        }
        setContentView(R.layout.activity_player);
        new NetWorkSpeedUtils(this, mHnadler).startShowNetSpeed();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.BLACK);
        }
        getSession();
        initView();
        id = getIntent().getStringExtra("id");
        contentInfoStr = getIntent().getStringExtra("contentInfoStr");
        initModel(contentInfoStr, 1);
        initEvent();
        authority();


    }


    @SuppressLint("InvalidWakeLockTag")
    private void initView() {

        //初始化视频播放器
        /**常亮*/
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "liveTAG");
        wakeLock.acquire();

        player = new PlayerView(this)
                .setProcessDurationOrientation(PlayStateParams.PROCESS_PORTRAIT)
                .setScaleType(PlayStateParams.fitparent)
                .hideCenterPlayer(true)
                .hideMenu(true)
                .hideSteam(true);
        mostBottom = findViewById(R.id.player_bg);
        //初始化相关视频lv
        rl_player = findViewById(R.id.rl_player);
        int screenWidthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
        int height = screenWidthPixels / 16 * 9;
        ViewGroup.LayoutParams params = rl_player.getLayoutParams();
        params.height = height;
        rl_player.setLayoutParams(params);
        lv_relate = findViewById(R.id.lv_relate);
        relateList = new ArrayList<>();
        relAdapter = new RelateAdapter(relateList, this, lv_relate);
        lv_relate.setAdapter(relAdapter);

        relateLayout = findViewById(R.id.relate_layout2);
        nestedScrollView = findViewById(R.id.content_ScrollView);
        imageView = findViewById(R.id.locate_content_image);
        imageView.setOnClickListener(v -> {
            nestedScrollView.scrollTo(0, relateLayout.getHeight());
        });
        contentText = findViewById(R.id.locate_content_text);
        contentText.setOnClickListener(v -> {
            nestedScrollView.scrollTo(0, relateLayout.getHeight());
        });
        bottomMenuLayout = findViewById(R.id.bottom_menu);
        noComment = findViewById(R.id.no_comment);


        //初始化视频信息
        netSpeed = findViewById(R.id.app_video_speed);
        tv_title = findViewById(R.id.tv_content_title);
        tv_hits = findViewById(R.id.tv_content_hits);
        tv_updateTime = findViewById(R.id.tv_content_updateTime);
        currentTime = findViewById(R.id.app_video_currentTime);
//        seekBar=findViewById(R.id.app_video_seekBar);
        player.seekBar.setOnSeekBarChangeListener(mSeekListener);
        videoView = findViewById(R.id.video_view);

        //初始化视频剧集rv
        rv_content_files = findViewById(R.id.rv_content_files);
        layoutManager = new LinearLayoutManager(this);
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
        iv_back = (ImageView) findViewById(com.dou361.ijkplayer.R.id.app_video_finish);


        //初始化评论
        lv_comment = findViewById(R.id.detail_page_lv_comment);
        comAdapter = new CommentAdapter(cList, this, lv_comment);
        comAdapter.setOnListClickListener((comment, position) -> {

        });
        lv_comment.setAdapter(comAdapter);
        bt_comment = (TextView) findViewById(R.id.detail_page_do_comment);
        bt_comment.setOnClickListener(this);


        nestedScrollView.setOnScrollChangeListener(new NestedScrollViewListener());


    }

    private void initEvent() {
        iv_bar_player.setOnClickListener(this);
        iv_bar_danmu.setOnClickListener(this);
        player.iv_fullscreen.setOnClickListener(this);
        iv_back.setOnClickListener(this);

        //切换相关视频
        relAdapter.setOnListClickListener((relate, position) -> {
            if (relateList.get(position).getLimit() == 0) {
                if (!relateList.get(position).getId().equals(id)) {
                    id = relateList.get(position).getId();
                    mAdapter.setThisPosition(0);
                    getNetData(id);
                }
            } else {
                if (isLogin) {
                    Toast.makeText(PlayerActivity.this, "当前用户无权限访问该内容。", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PlayerActivity.this, "请先登录。", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(PlayerActivity.this, MineActivity.class);
                    startActivity(intent);
                }
            }
        });


        //切换剧集
        mAdapter.setOnListClickListener((v, position) -> {
            List<PlayUrl> playUrls = contentInfo.getFiles().get(position).getPlayurl();
            vList.clear();
            for (int i = 0; i < playUrls.size(); i++) {
                VideoijkBean videoijkBean = new VideoijkBean();
                switch (playUrls.get(i).getDefinition()) {
                    case "0":
                        videoijkBean.setStream("流畅");
                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                        videoijkBean.setRemarks("流畅");
                        vList.add(videoijkBean);
                        break;
                    case "1":
                        videoijkBean.setStream("流畅");
                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                        videoijkBean.setRemarks("流畅");
                        vList.add(videoijkBean);
                        break;
                    case "2":
                        videoijkBean.setStream("标清");
                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                        videoijkBean.setRemarks("标清");
                        vList.add(videoijkBean);
                        break;
                    case "3":
                        videoijkBean.setStream("高清");
                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                        videoijkBean.setRemarks("高清");
                        vList.add(videoijkBean);
                        break;
                    case "4":
                        videoijkBean.setStream("超清");
                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                        videoijkBean.setRemarks("超清");
                        vList.add(videoijkBean);
                        break;
                    case "5":
                        videoijkBean.setStream("2k");
                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                        videoijkBean.setRemarks("2k");
                        vList.add(videoijkBean);
                        break;
                    case "6":
                        videoijkBean.setStream("4k");
                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                        videoijkBean.setRemarks("4k");
                        vList.add(videoijkBean);
                        break;
                    case "7":
                        videoijkBean.setStream("原画");
                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                        videoijkBean.setRemarks("原画");
                        vList.add(videoijkBean);
                        break;
                }
            }
            player.setPlaySource(vList)
//                    .showThumbnail(ivThumbnail -> Glide.with(mContext)
//                            .load(contentInfo.getImage())
//                            .placeholder(R.color.cl_default)
//                            .error(R.color.cl_error)
//                            .into(ivThumbnail))
                    .startPlay();
            fileId = contentInfo.getFiles().get(position).getId();
            getCommentData(id, fileId);
            mAdapter.setThisPosition(position);
            tv_title.setText(contentInfo.getTitle() + " 第" + (mAdapter.getThisPosition() + 1) + "集");
            mAdapter.notifyDataSetChanged();

        });

        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku = true;
                danmakuView.start();
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
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                //要做的事情
                if (danmakuView != null) {
                    if (player.doubleClick == 2 && player.bgState == 0 && danmakuView.isPaused()) {
                        danmakuView.resume();
                    } else if (player.doubleClick == 1 && player.bgState == 1 && (!danmakuView.isPaused())) {
                        danmakuView.pause();
                    }
                    handler.postDelayed(this, 1000);
                } else {
                    removeTimer = true;
                }
            }
        };

        handler.postDelayed(runnable, 500);
        if (removeTimer) {
            handler.removeCallbacks(runnable);
        }


    }


    private void getCommentData(String id, String fileId) {
        String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/SystemApi-getCommentsInfo.action" +
                "?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&contentId=" + id + "&returnType=json" + "&fileId=" + fileId;
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
                    String json = response.body().string();
                    initModel(json, 2);
                    response.body().close();
                }
            });
        }).start();
    }

    private void getNetData(String id) {
        String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/ContentApi-getContentInfo.action" +
                "?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&returnType=json&size=20" +
                "&contentId=" + id;


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
                    contentInfoStr = response.body().string();
                    initModel(contentInfoStr, 1);
                    response.body().close();
                }
            });
        }).start();
    }


    private void initModel(String json, int type) {
        if (type == 1) {
            Gson gson = new Gson();
            List<ContentInfo> infos = gson.fromJson(json, new TypeToken<List<ContentInfo>>() {
            }.getType());
            if (infos != null && infos.size() >= 2) {
                contentInfo = infos.get(0);
                fileId = contentInfo.getFiles().get(0).getId();
                getCommentData(id, fileId);
                relateList = infos.get(1).getRelates();
                if (contentInfo.getVideo360() == 1) {
                    Intent playIntent = new Intent(PlayerActivity.this, VrVideoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("contentInfoStr", json);
                    bundle.putString("id", contentInfo.getId());
                    playIntent.putExtras(bundle);
                    startActivity(playIntent);
                    this.finish();
                } else {
                    if (contentInfo.getFiles() != null && contentInfo.getFiles().size() > 0) {
                        //子线程刷新UI
                        PlayerActivity.this.runOnUiThread(() -> {
                            List<PlayUrl> playUrls = contentInfo.getFiles().get(0).getPlayurl();
                            vList.clear();
                            for (int i = 0; i < playUrls.size(); i++) {
                                VideoijkBean videoijkBean = new VideoijkBean();
                                switch (playUrls.get(i).getDefinition()) {
                                    case "0":
                                        videoijkBean.setStream("流畅");
                                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                                        videoijkBean.setRemarks("流畅");
                                        vList.add(videoijkBean);
                                        break;
                                    case "1":
                                        videoijkBean.setStream("流畅");
                                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                                        videoijkBean.setRemarks("流畅");
                                        vList.add(videoijkBean);
                                        break;
                                    case "2":
                                        videoijkBean.setStream("标清");
                                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                                        videoijkBean.setRemarks("标清");
                                        vList.add(videoijkBean);
                                        break;
                                    case "3":
                                        videoijkBean.setStream("高清");
                                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                                        videoijkBean.setRemarks("高清");
                                        vList.add(videoijkBean);
                                        break;
                                    case "4":
                                        videoijkBean.setStream("超清");
                                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                                        videoijkBean.setRemarks("超清");
                                        vList.add(videoijkBean);
                                        break;
                                    case "5":
                                        videoijkBean.setStream("2k");
                                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                                        videoijkBean.setRemarks("2k");
                                        vList.add(videoijkBean);
                                        break;
                                    case "6":
                                        videoijkBean.setStream("4k");
                                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                                        videoijkBean.setRemarks("4k");
                                        vList.add(videoijkBean);
                                        break;
                                    case "7":
                                        videoijkBean.setStream("原画");
                                        videoijkBean.setUrl(playUrls.get(i).getUrl());
                                        videoijkBean.setRemarks("原画");
                                        vList.add(videoijkBean);
                                        break;
                                }
                            }
                            player.setPlaySource(vList)
                                    .showThumbnail(ivThumbnail -> Glide.with(mContext)
                                            .load(contentInfo.getImage())
                                            .placeholder(R.color.cl_default)
                                            .error(R.color.cl_error)
                                            .into(ivThumbnail))
                                    .startPlay();
                            relAdapter.onDateChange(relateList);
                            tv_title.setText(contentInfo.getTitle());
                            tv_hits.setText(PlayHitstUtil.getCount(contentInfo.getHits()));
                            tv_updateTime.setText(contentInfo.getUpdateTime() + "发布");

                            if (contentInfo.getFiles().size() > 1) {
                                rv_content_files.setVisibility(View.VISIBLE);
                                view_file.setVisibility(View.VISIBLE);
                                mAdapter.onDateChange(contentInfo.getFiles());
                                tv_title.setText(contentInfo.getTitle() + " 第" + (mAdapter.getThisPosition() + 1) + "集");
                            } else {
                                rv_content_files.setVisibility(View.GONE);
                                view_file.setVisibility(View.GONE);
                                tv_title.setText(contentInfo.getTitle());
                            }
                            setListViewHeightByItem(lv_relate, 1);
                        });
                    } else {
                        PlayerActivity.this.runOnUiThread(() -> {
                            ToastUtils.showLong("无法播放");
                        });

                        this.finish();
                    }
                }
            } else {
                PlayerActivity.this.runOnUiThread(() -> {
                    ToastUtils.showLong("无法播放");
                });
                this.finish();
            }
        } else if (type == 2) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            cList = gson.fromJson(json, new TypeToken<List<Comment>>() {
            }.getType());
            comList = gson.fromJson(json, new TypeToken<List<com.dou361.ijkplayer.module.Comment>>() {
            }.getType());
//            player.setCommentList(comList);
            PlayerActivity.this.runOnUiThread(() -> {
                contentText.setText("(" + cList.size() + ")");
                comAdapter.onDateChange(cList);
                setListViewHeightByItem(lv_comment, 0);
                if (cList.size() > 0) {
                    noComment.setVisibility(View.GONE);
                } else {
                    noComment.setVisibility(View.VISIBLE);
                }
            });
        }

    }


    /**
     * 绑定点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_video_play:
                /**视频播放和暂停*/
                if (player.videoView.isPlaying()) {
                    if (!danmakuView.isPaused()) {
                        danmakuView.pause();
                        player.doubleClick = 0;
                    }
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
                    if (danmakuView.isPaused()) {
                        danmakuView.resume();
                        player.doubleClick = 0;
                    }
                }
                player.updatePausePlay();
                break;
            case R.id.app_video_danmu:
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
            case R.id.app_video_fullscreen:
                /**视频全屏切换*/
                if (player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    toggleFullScreen(270);
                    oldOrientation = 270;
                    oldOrientationLocked = true;
                } else if (player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    toggleFullScreen(270);
                    oldOrientation = 270;
                    oldOrientationLocked = false;
                } else if (player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    toggleFullScreen(90);
                    oldOrientation = 90;
                    oldOrientationLocked = false;
                } else if (!player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    toggleFullScreen(0);
                    oldOrientation = 0;
                    oldOrientationLocked = false;
                } else if (!player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    toggleFullScreen(0);
                    oldOrientation = 0;
                    oldOrientationLocked = true;
                } else if (!player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    toggleFullScreen(0);
                    oldOrientation = 0;
                    oldOrientationLocked = true;
                }
                break;
            case R.id.app_video_finish:
                /**返回*/
                if (!player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    toggleFullScreen(0);
                    oldOrientation = 0;
                    oldOrientationLocked = false;
                } else if (!player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    toggleFullScreen(0);
                    oldOrientation = 0;
                    oldOrientationLocked = true;
                } else if (!player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    toggleFullScreen(0);
                    oldOrientation = 0;
                    oldOrientationLocked = true;
                } else if (player.isPortrait) {
                    this.finish();
                }
                break;
            case R.id.detail_page_do_comment:
                showCommentDialog();
                break;


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
    //SCREEN_ORIENTATION_LANDSCAPE  横屏
    //SCREEN_ORIENTATION_REVERSE_LANDSCAPE  反向横屏
    //SCREEN_ORIENTATION_PORTRAIT  竖屏
    public PlayerView toggleFullScreen(int orientation) {
        //竖屏
        switch (orientation) {
            case 0:
                showBottomUIMenu();
                player.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                int screenWidthPixels = 0;
                if (player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    screenWidthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
                } else {
                    screenWidthPixels = mContext.getResources().getDisplayMetrics().heightPixels;
                }
                int height = screenWidthPixels / 16 * 9;
                params.height = height;
                rl_player.setLayoutParams(params);
                bottomMenuLayout.setVisibility(View.VISIBLE);
                player.isPortrait = true;
                break;
            case 90:
                secondHide();
                RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                rl_player.setLayoutParams(params1);
                player.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                bottomMenuLayout.setVisibility(View.GONE);
                player.isPortrait = false;
                break;
            case 270:
                secondHide();
                RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                rl_player.setLayoutParams(params2);
                player.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                bottomMenuLayout.setVisibility(View.GONE);
                player.isPortrait = false;
                break;
        }
        return player;
    }


    /**
     * 向弹幕View中添加一条弹幕
     *
     * @param content    弹幕的具体内容
     * @param withBorder 弹幕是否有边框
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
//    private void generateSomeDanmaku() {
//        new Thread(() -> {
//            while (showDanmaku) {
//                int time = new Random().nextInt(300);
//                String content = "" + time + time;
//                addDanmaku(content, false);
//                try {
//                    Thread.sleep(time);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }

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
        mScreenOrientation.disable();
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
        mScreenOrientation.enable();
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
        if (!player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            toggleFullScreen(0);
            oldOrientation = 0;
            oldOrientationLocked = false;
        } else if (!player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            toggleFullScreen(0);
            oldOrientation = 0;
            oldOrientationLocked = true;
        } else if (!player.isPortrait && player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            toggleFullScreen(0);
            oldOrientation = 0;
            oldOrientationLocked = true;
        } else if (player.isPortrait) {
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
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            ToastUtils.showShort("portrait");
        }
    }

    @Override
    public void onBackPressed() {
        if (player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || player.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            toggleFullScreen(0);
        } else {
            if (player != null && player.onBackPressed()) {
                return;
            }
            super.onBackPressed();
            if (wakeLock != null) {
                wakeLock.release();
            }
        }
    }


    //权限判断
    private void authority() {
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //先判断有没有权限 ，没有就在这里进行权限的申请
            ActivityCompat.requestPermissions(this,
                    permissions, 321);
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
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
        user = new User(setting.getString("name", ""), setting.getString("password", "")
                , setting.getString("token", ""));
        isLogin = setting.getBoolean("isLogin", false);
        if (isLogin) {
            memberName = user.getName();
        }
    }

    class NestedScrollViewListener implements NestedScrollView.OnScrollChangeListener {

        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

        }
    }


    private void setListViewHeightByItem(ListView listView, int type) {
        if (listView == null) {
            return;
        }
//        ListAdapter listAdapter = listView.getAdapter();
        if (relAdapter == null) {
            return;
        }
        int totalHeight = 0;
        if (type == 1) {
            for (int i = 0; i < relAdapter.getCount(); i++) {
                View item = relAdapter.getView(i, null, listView);
                //item的布局要求是linearLayout，否则measure(0,0)会报错。
                item.measure(0, 0);
                //计算出所有item高度的总和
                totalHeight += item.getMeasuredHeight();
            }
            //获取ListView的LayoutParams,只需要修改高度就可以。
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            //修改ListView高度为item总高度和所有分割线的高度总和。
            //这里的分隔线是指ListView自带的divider
            params.height = totalHeight + (listView.getDividerHeight() * (relAdapter.getCount() - 1));
            //将修改过的参数，重新设置给ListView
            listView.setLayoutParams(params);
        } else {

            //获取listView的宽度
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            int listViewWidth = this.getWindowManager().getDefaultDisplay().getWidth();
            int widthSpec = View.MeasureSpec.makeMeasureSpec(listViewWidth, View.MeasureSpec.AT_MOST);
            int total = cList.size();
            for (int i = 0; i < total; i++) {
                View listItem = relAdapter.getView(i, null, listView);
                //给item的measure设置参数是listView的宽度就可以获取到真正每一个item的高度
                listItem.measure(widthSpec, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            params.height = totalHeight
                    + (listView.getDividerHeight() * (relAdapter.getCount() + 1));
            listView.setLayoutParams(params);

        }
    }

    //评论部分测试

    /**
     * 初始化评论和回复列表
     */
//    private void initExpandableListView(final List<CommentDetailBean> commentList) {
//        expandableListView.setGroupIndicator(null);
//        //默认展开所有回复
//        adapter = new CommentExpandAdapter(this, commentList);
//        expandableListView.setAdapter(adapter);
//        for (int i = 0; i < commentList.size(); i++) {
//            expandableListView.expandGroup(i);
//        }
//        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//            @Override
//            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
//                boolean isExpanded = expandableListView.isGroupExpanded(groupPosition);
////                if(isExpanded){
////                    expandableListView.collapseGroup(groupPosition);
////                }else {
////                    expandableListView.expandGroup(groupPosition, true);
////                }
//                showReplyDialog(groupPosition);
//                return true;
//            }
//        });
//
//        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
//                Toast.makeText(PlayerActivity.this, "点击了回复", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
//
//        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//            @Override
//            public void onGroupExpand(int groupPosition) {
//                //toast("展开第"+groupPosition+"个分组");
//
//            }
//        });
//
//    }


    /**
     * by moos on 2018/04/20
     * func:弹出评论框
     */
    private void showCommentDialog() {
        dialog = new BottomSheetDialog(this, R.style.BottomSheetStyle);
        View commentView = LayoutInflater.from(this).inflate(R.layout.comment_dialog_layout, null);
        final EditText commentText = (EditText) commentView.findViewById(R.id.dialog_comment_et);
        final Button btn_comment = (Button) commentView.findViewById(R.id.dialog_comment_bt);
        dialog.setContentView(commentView);
        /**
         * 解决bsd显示不全的情况
         */
        View parent = (View) commentView.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        commentView.measure(0, 0);
        behavior.setPeekHeight(commentView.getMeasuredHeight());

        btn_comment.setOnClickListener(view -> {
            String commentContent = commentText.getText().toString().trim();
            String mName = memberName;
            try {
                commentContent = URLEncoder.encode(commentContent, "UTF-8");
                commentContent = URLEncoder.encode(commentContent, "UTF-8");
                mName = URLEncoder.encode(mName, "UTF-8");
                mName = URLEncoder.encode(mName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(commentContent)) {
                String commentTime = currentTime.getText().toString();
                String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/SystemApi-addComment.action" +
                        "?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&content=" + commentContent + "&entityId=" + id
                        + "&currentTime=" + commentTime + "&memberName=" + mName + "&fileId=" + fileId;
                new Thread(() -> {
                    OkHttpClient client = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                    //同步加载
                    try {
                        Response response = client.newCall(request).execute();
                        String json = response.body().string();
                        response.body().close();
                        if (json != "" || json != "no permission") {
                            Looper.prepare();
                            Toast.makeText(PlayerActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
                            getCommentData(id, fileId);
                            addDanmaku(commentText.getText().toString().trim(), false);
                            Looper.loop();
                        } else {
                            Looper.prepare();
                            Toast.makeText(PlayerActivity.this, "发表评论失败", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //异步加载
//                    client.newCall(request).enqueue(new Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//                            Log.d("addContent", "onFailure: ");
//                        }
//
//                        @Override
//                        public void onResponse(Call call, Response response) throws IOException {
//                            response = client.newCall(request).execute();
//                            String json = response.body().string();
//                            response.body().close();
//                            if (json != "" || json != "no permission") {
//                                Looper.prepare();
//                                Toast.makeText(PlayerActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
//                                Looper.loop();
//                            } else {
//                                Looper.prepare();
//                                Toast.makeText(PlayerActivity.this, "发表评论失败", Toast.LENGTH_SHORT).show();
//                                Looper.loop();
//                            }
//                        }
//
//                    });
                }).start();
                dialog.dismiss();

            } else {
                Toast.makeText(PlayerActivity.this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
            }
        });
        commentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence) && charSequence.length() > 2) {
                    btn_comment.setBackgroundColor(Color.parseColor("#0AB454"));
                } else {
                    btn_comment.setBackgroundColor(Color.parseColor("#0AB454"));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog.show();
    }

    /**
     * by moos on 2018/04/20
     * func:弹出回复框
     */
//    private void showReplyDialog(final int position) {
//        dialog = new BottomSheetDialog(this);
//        View commentView = LayoutInflater.from(this).inflate(R.layout.comment_dialog_layout, null);
//        final EditText commentText = (EditText) commentView.findViewById(R.id.dialog_comment_et);
//        final Button btn_comment = (Button) commentView.findViewById(R.id.dialog_comment_bt);
////        commentText.setHint("回复 " + commentsList.get(position).getUserName() + " 的评论:");
//        dialog.setContentView(commentView);
////        bt_comment.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                String replyContent = commentText.getText().toString().trim();
////                if (!TextUtils.isEmpty(replyContent)) {
////                    dialog.dismiss();
////                    ReplyDetailBean detailBean = new ReplyDetailBean("小红", replyContent);
////                    adapter.addTheReplyData(detailBean, position);
////                    expandableListView.expandGroup(position);
////                    Toast.makeText(PlayerActivity.this, "回复成功", Toast.LENGTH_SHORT).show();
////                } else {
////                    Toast.makeText(PlayerActivity.this, "回复内容不能为空", Toast.LENGTH_SHORT).show();
////                }
////            }
////        });
////        commentText.addTextChangedListener(new TextWatcher() {
////            @Override
////            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
////
////            }
////
////            @Override
////            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
////                if (!TextUtils.isEmpty(charSequence) && charSequence.length() > 2) {
////                    bt_comment.setBackgroundColor(Color.parseColor("#FFB568"));
////                } else {
////                    bt_comment.setBackgroundColor(Color.parseColor("#D8D8D8"));
////                }
////            }
////
////            @Override
////            public void afterTextChanged(Editable editable) {
////
////            }
////        });
//        dialog.show();
//    }


    /**
     * 进度条滑动监听
     */
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

        /**数值的改变*/
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                long duration = player.getDuration();
                int position = (int) ((duration * progress * 1.0) / 1000);
                String time = player.generateTime(position);
                for (int i = 0; i < cList.size(); i++) {
                    if (cList.get(i).getCurrentTime().equals(time)) {
                        addDanmaku(cList.get(i).getContent(), false);
                    }
                }
                return;
            } else {
                long duration = player.getDuration();
                int position = (int) ((duration * progress * 1.0) / 1000);
                String time = player.generateTime(position);
                player.query.id(com.dou361.ijkplayer.R.id.app_video_currentTime).text(time);
                player.query.id(com.dou361.ijkplayer.R.id.app_video_currentTime_full).text(time);
                player.query.id(com.dou361.ijkplayer.R.id.app_video_currentTime_left).text(time);
            }

        }

        /**开始拖动*/
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDragging = true;
            mHandler.removeMessages(PlayerView.MESSAGE_SHOW_PROGRESS);
        }

        /**停止拖动*/
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            long duration = player.getDuration();
            videoView.seekTo((int) ((duration * seekBar.getProgress() * 1.0) / 1000));
            mHandler.removeMessages(PlayerView.MESSAGE_SHOW_PROGRESS);
            isDragging = false;
            mHandler.sendEmptyMessageDelayed(PlayerView.MESSAGE_SHOW_PROGRESS, 1000);
        }
    };


    /**
     * 消息处理
     */
    @SuppressWarnings("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /**滑动完成，隐藏滑动提示的box*/
                case PlayerView.MESSAGE_HIDE_CENTER_BOX:
                    player.query.id(com.dou361.ijkplayer.R.id.app_video_volume_box).gone();
                    player.query.id(com.dou361.ijkplayer.R.id.app_video_brightness_box).gone();
                    player.query.id(com.dou361.ijkplayer.R.id.app_video_fastForward_box).gone();
                    break;
                /**滑动完成，设置播放进度*/
                case PlayerView.MESSAGE_SEEK_NEW_POSITION:
                    if (!player.isLive && player.newPosition >= 0) {
                        videoView.seekTo((int) player.newPosition);
                        player.newPosition = -1;
                    }
                    break;
                /**滑动中，同步播放进度*/
                case PlayerView.MESSAGE_SHOW_PROGRESS:
                    long pos = player.syncProgress();
                    if (!isDragging && player.isShowControlPanl) {
                        msg = obtainMessage(PlayerView.MESSAGE_SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                        player.updatePausePlay();
                    }
                    break;
                /**重新去播放*/
                case PlayerView.MESSAGE_RESTART_PLAY:
                    player.status = PlayStateParams.STATE_ERROR;
                    player.startPlay();
                    player.updatePausePlay();
                    break;
            }
        }
    };

    public void secondHide() {
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        getWindow().getDecorView().setSystemUiVisibility(flags | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mostBottom.setBackgroundColor(Color.parseColor("#000000"));
    }

    protected void showBottomUIMenu() {
        int flags;
        int curApiVersion = android.os.Build.VERSION.SDK_INT;
        // This work only for android 4.4+
        if (curApiVersion >= Build.VERSION_CODES.KITKAT) {
            // This work only for android 4.4+
            // hide navigation bar permanently in android activity
            // touch the screen, the navigation bar will not show
            flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        } else {
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        // must be executed in main thread :)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(flags);
        mostBottom.setBackgroundColor(Color.parseColor("#F2F2F2"));
    }
}
