package com.cec.videoplayer.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cec.videoplayer.R;
import com.cec.videoplayer.adapter.CommentAdapter;
import com.cec.videoplayer.adapter.RelateAdapter;
import com.cec.videoplayer.module.User;
import com.cec.videoplayer.module.Comment;
import com.cec.videoplayer.module.ContentInfo;
import com.cec.videoplayer.module.Relate;
import com.cec.videoplayer.module.NetValue;
import com.cec.videoplayer.utils.PlayHitstUtil;
import com.cec.videoplayer.utils.TimeFormatUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A test activity that renders a 360 video using {@link VrVideoView}.
 * It loads the congo video from app's assets by default. User can use it to load any video files
 * using adb shell commands such as:
 *
 * <p>Video from asset folder
 * adb shell am start -a android.intent.action.VIEW \
 * -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 * -d "file:///android_asset/congo.mp4"
 *
 * <p>Video located on the phone's SD card.
 * adb shell am start -a android.intent.action.VIEW \
 * -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 * -d /sdcard/FILENAME.MP4
 *
 * <p>Video hosted on a website:
 * adb shell am start -a android.intent.action.VIEW \
 * -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 * -d "https://EXAMPLE.COM/FILENAME.MP4"
 *
 * <p>To load HLS files add "--ei inputFormat 2" to pass in an integer extra which will set
 * VrVideoView.Options.inputFormat to FORMAT_HLS. e.g.
 * adb shell am start -a android.intent.action.VIEW \
 * -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 * -d "file:///android_asset/hls/iceland.m3u8" \
 * --ei inputFormat 2 --ei inputType 2
 *
 * <p>To load MPEG-DASH files add "--ei inputFormat 3" to pass in an integer extra which will set
 * VrVideoView.Options.inputFormat to FORMAT_DASH. e.g.
 * adb shell am start -a android.intent.action.VIEW \
 * -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 * -d "file:///android_asset/dash/congo_dash.mpd" \
 * --ei inputFormat 3 --ei inputType 2
 *
 * <p>To specify that the video is of type stereo over under (has images for left and right eyes),
 * add "--ei inputType 2" to pass in an integer extra which will set VrVideoView.Options.inputType
 * to TYPE_STEREO_OVER_UNDER. This can be combined with other extras, e.g:
 * adb shell am start -a android.intent.action.VIEW \
 * -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 * -d "https://EXAMPLE.COM/FILENAME.MP4" \
 * --ei inputType 2
 */
public class VrVideoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = VrVideoActivity.class.getSimpleName();
    private NetValue netValue = new NetValue();
    /**
     * Preserve the video's state when rotating the phone.
     */
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    /**
     * The video duration doesn't need to be preserved, but it is saved in this example. This allows
     * the seekBar to be configured during {@link #onRestoreInstanceState(Bundle)} rather than waiting
     * for the video to be reloaded and analyzed. This avoid UI jank.
     */
    private static final String STATE_VIDEO_DURATION = "videoDuration";

    /**
     * Arbitrary constants and variable to track load status. In this example, this variable should
     * only be accessed on the UI thread. In a real app, this variable would be code that performs
     * some UI actions when the video is fully loaded.
     */
    public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    public static final int LOAD_VIDEO_STATUS_ERROR = 2;

    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

    /**
     * Tracks the file to be loaded across the lifetime of this app.
     **/
    private Uri fileUri;

    /**
     * Configuration information for the video.
     **/
    private VrVideoView.Options videoOptions = new VrVideoView.Options();

    private VideoLoaderTask backgroundVideoLoaderTask;

    /**
     * The video view and its custom UI elements.
     */
    protected VrVideoView videoWidgetView;

    /**
     * Seeking UI & progress indicator. The seekBar's progress value represents milliseconds in the
     * video.
     */
    private SeekBar seekBar;
    private NestedScrollView nestedScrollView;

    private ImageButton volumeToggle;
    private ImageButton playToggle;
    private boolean isMuted;
    private TextView statusText;
    private Button relate_btn;
    //    private Button content_btn;  评论查看更多
    private ImageView imageView;
    private LinearLayout videoBottomView;
    private LinearLayout relateLayout;
    private LinearLayout noVrComment;
    private RelativeLayout video;


    /**
     * By default, the video will start playing as soon as it is loaded. This can be changed by using
     * {@link VrVideoView#pauseVideo()} after loading the video.
     */
    private boolean isPaused = false;

    private ContentInfo contentInfo;
    private ListView lv_relate;
    private ListView lv_comment;
    private RelateAdapter relAdapter;
    private CommentAdapter comAdapter;
    private TextView tv_title;
    private TextView tv_hits;
    private TextView tv_updateTime;
    private TextView contentText;
    private ImageView iv_back;
    private List<Relate> mList = new ArrayList<>();
    private List<Comment> cList = new ArrayList<>();
    private String id;
    private String playUrl;
    private String contentInfoStr;
    private User user;
    private Uri uri;
    private Boolean clicked = false;
    private Boolean isLogin;
    private String memberName = "";


    private TextView bt_comment;
    private BottomSheetDialog dialog;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_vr_video);
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
        Gson gson = new Gson();
        List<ContentInfo> infos = gson.fromJson(contentInfoStr, new TypeToken<List<ContentInfo>>() {
        }.getType());
        contentInfo = infos.get(0);
        playUrl = contentInfo.getFiles().get(0).getPlayurl().get(0).getUrl();
        uri=Uri.parse(playUrl);
        initModel(contentInfoStr, 1);
        getCommentData(id);
        initEvent();
    }

    @SuppressLint("InvalidWakeLockTag")
    private void initView() {
        /**常亮*/
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "liveTAG");
        wakeLock.acquire();
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        nestedScrollView = findViewById(R.id.content_ScrollView);
        relateLayout = findViewById(R.id.relate_layout);
        video=findViewById(R.id.video);
        int screenWidthPixels = this.getResources().getDisplayMetrics().widthPixels;
        int height = screenWidthPixels / 16 * 9;
        ViewGroup.LayoutParams params = video.getLayoutParams();
        params.height = height;
        video.setLayoutParams(params);
        noVrComment = findViewById(R.id.no_vr_comment);
        contentText = findViewById(R.id.locate_vr_content_text);
        contentText.setOnClickListener(v -> {
            nestedScrollView.scrollTo(0, relateLayout.getHeight());
        });
        videoBottomView = findViewById(R.id.video_bottom);
        imageView = findViewById(R.id.locate_content_image);
        imageView.setOnClickListener(v -> {
            nestedScrollView.scrollTo(0, relateLayout.getHeight());
        });
//        relate_btn = findViewById(R.id.show_more_relate);
//        relate_btn.setOnClickListener(v -> {
//            if (showMore) {
//                setListViewHeightByItem(lv_relate, 1);
//                relate_btn.setText("查看全部");
//                showMore = false;
//            } else {
//                setListViewHeightByItem(lv_relate, 1);
//                relate_btn.setText("收起");
//                showMore = true;
//            }
//        });
//        content_btn = findViewById(R.id.show_content);
//        content_btn.setOnClickListener(v -> {
//            if (showContent) {
//                setListViewHeightByItem(lv_relate);
//                relate_btn.setText("查看全部");
//                showContent = false;
//            } else {
//                setListViewHeightByItem(lv_relate);
//                relate_btn.setText("收起");
//                showContent = true;
//            }
//        });
        seekBar.setOnSeekBarChangeListener(new SeekBarListener());
        nestedScrollView.setOnScrollChangeListener(new NestedScrollViewListener());
        statusText = (TextView) findViewById(R.id.tv_vr_time);
        iv_back = findViewById(R.id.iv_vr_back);

        //初始化相关视频lv
        lv_relate = findViewById(R.id.lv_vr_relate);
        relAdapter = new RelateAdapter(mList, this, lv_relate);
        lv_relate.setAdapter(relAdapter);

        //初始化评论
        lv_comment = findViewById(R.id.detail_page_lv_comment);
        comAdapter = new CommentAdapter(cList, this, lv_comment);

        lv_comment.setAdapter(comAdapter);
        bt_comment = (TextView) findViewById(R.id.vr_detail_page_do_comment);
        bt_comment.setOnClickListener(this);


        //初始化视频信息
        tv_title = findViewById(R.id.tv_vr_content_title);
        tv_hits = findViewById(R.id.tv_vr_content_hits);
        tv_updateTime = findViewById(R.id.tv_vr_content_updateTime);


        //评论部分测试
//        commentListView = (CommentExpandableListView) findViewById(R.id.detail_page_lv_comment);
//        commentsList = generateTestData();
//        initExpandableListView(commentsList);
    }

    private void initEvent() {
        // Bind input and output objects for the view.
        videoWidgetView = (VrVideoView) findViewById(R.id.vr_video_view);
        videoWidgetView.setEventListener(new ActivityEventListener());

        volumeToggle = (ImageButton) findViewById(R.id.vr_volume_toggle);
        volumeToggle.setOnClickListener(v -> setIsMuted(!isMuted));
        playToggle = findViewById(R.id.play_toggle);
        playToggle.setOnClickListener(v -> togglePause());
        iv_back.setOnClickListener(this);

        loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

        // Initial launch of the app or an Activity recreation due to rotation.
        handleIntent(getIntent());

        //切换相关视频
        relAdapter.setOnListClickListener((relate, position) -> {
            if (mList.get(position).getLimit() == 0) {
                if (!mList.get(position).getId().equals(id)) {
                    id = mList.get(position).getId();
                    getNetData(mList.get(position).getId());
                    getCommentData(mList.get(position).getId());
                    clicked = true;
//                relate_btn.setText("查看全部");
                }
            }else{
                if (isLogin) {
                    Toast.makeText(VrVideoActivity.this, "当前用户无权限访问该内容。", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(VrVideoActivity.this, "请先登录。", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(VrVideoActivity.this, MineActivity.class);
                    startActivity(intent);
                }
            }

        });

    }

    private void getCommentData(String id) {
        String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/SystemApi-getCommentsInfo.action" +
                "?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&contentId=" + id + "&returnType=json";
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
        String url = "http://"+ netValue.getIp()+":"+ netValue.getPort()+"/powercms/api/ContentApi-getContentInfo.action" +
                "?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&returnType=json&size=10&" +
                "contentId=" + id;

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
            contentInfo = infos.get(0);
            mList = infos.get(1).getRelates();
            if (clicked) {
                if (contentInfo.getVideo360() == 0) {
                    Intent playIntent = new Intent(VrVideoActivity.this, PlayerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("contentInfoStr", json);
                    bundle.putString("id", contentInfo.getId());
                    playIntent.putExtras(bundle);
                    startActivity(playIntent);
                    this.finish();
                } else {
                    playUrl = contentInfo.getFiles().get(0).getPlayurl().get(0).getUrl();
                    id = contentInfo.getId();
                    uri = Uri.parse(playUrl);
                    clicked = false;
                    initEvent();
                }
            }
            //子线程刷新UI
            VrVideoActivity.this.runOnUiThread(() -> {
                relAdapter.onDateChange(mList);
                tv_title.setText(contentInfo.getTitle());
                tv_hits.setText(PlayHitstUtil.getCount(contentInfo.getHits()));
                tv_updateTime.setText(contentInfo.getUpdateTime() + "发布");
                setListViewHeightByItem(lv_relate, 1);
            });
        } else if (type == 2) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            cList = gson.fromJson(json, new TypeToken<List<Comment>>() {
            }.getType());

            //子线程刷新UI
            VrVideoActivity.this.runOnUiThread(() -> {
                contentText.setText("(" + cList.size() + ")");
                comAdapter.onDateChange(cList);
                setListViewHeightByItem(lv_comment, 0);
                if (cList.size() > 0) {
                    noVrComment.setVisibility(View.GONE);
                }
            });
        }


    }


    /**
     * Called when the Activity is already running and it's given a new intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, this.hashCode() + ".onNewIntent()");
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
        setIntent(intent);
        // Load the new video.
        handleIntent(intent);
    }

    public int getLoadVideoStatus() {
        return loadVideoStatus;
    }

    private void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
        volumeToggle.setImageResource(isMuted ? R.mipmap.volume_off : R.mipmap.volume_on);
        videoWidgetView.setVolume(isMuted ? 0.0f : 1.0f);
    }

    /**
     * Load custom videos based on the Intent or load the default video. See the Javadoc for this
     * class for information on generating a custom intent via adb.
     */
    private void handleIntent(Intent intent) {
        // Determine if the Intent contains a file to load.
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.i(TAG, "ACTION_VIEW Intent received");

            fileUri = intent.getData();
            if (fileUri == null) {
                Log.w(TAG, "No data uri specified. Use \"-d /path/filename\".");
            } else {
                Log.i(TAG, "Using file " + fileUri.toString());
            }

            videoOptions.inputFormat = intent.getIntExtra("inputFormat", VrVideoView.Options.FORMAT_DEFAULT);
            videoOptions.inputType = intent.getIntExtra("inputType", VrVideoView.Options.TYPE_MONO);
        } else {
            Log.i(TAG, "Intent is not ACTION_VIEW. Using the default video.");
            fileUri = null;
        }

        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        if (backgroundVideoLoaderTask != null) {
            // Cancel any task from a previous intent sent to this activity.
            backgroundVideoLoaderTask.cancel(true);
        }
        backgroundVideoLoaderTask = new VideoLoaderTask();
        backgroundVideoLoaderTask.execute(Pair.create(fileUri, videoOptions));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PROGRESS_TIME, videoWidgetView.getCurrentPosition());
        savedInstanceState.putLong(STATE_VIDEO_DURATION, videoWidgetView.getDuration());
        savedInstanceState.putBoolean(STATE_IS_PAUSED, isPaused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
        videoWidgetView.seekTo(progressTime);
        seekBar.setMax((int) savedInstanceState.getLong(STATE_VIDEO_DURATION));
        seekBar.setProgress((int) progressTime);

        isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
        if (isPaused) {
            videoWidgetView.pauseVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        videoWidgetView.pauseRendering();
        // If the video is playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the 3D rendering.
        videoWidgetView.resumeRendering();
        // Update the text to account for the paused video in onPause().
        updateStatusText();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        videoWidgetView.shutdown();
        super.onDestroy();
    }

    private void togglePause() {
        if (isPaused) {
            videoWidgetView.playVideo();
            playToggle.setImageResource(R.mipmap.simple_player_icon_media_pause);
            videoBottomView.setVisibility(View.GONE);
        } else {
            videoWidgetView.pauseVideo();
            playToggle.setImageResource(R.mipmap.simple_player_arrow_white_24dp);
            videoBottomView.setVisibility(View.VISIBLE);
        }
        isPaused = !isPaused;
        updateStatusText();
    }

    private void updateStatusText() {
        StringBuilder status = new StringBuilder();
        String current = TimeFormatUtil.MillisecondToDate(videoWidgetView.getCurrentPosition());
        String duration = TimeFormatUtil.MillisecondToDate(videoWidgetView.getDuration());
        status.append(current);
        status.append(" / ");
        status.append(duration);
        statusText.setText(status.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_vr_back:
                this.finish();
                break;
            case R.id.vr_detail_page_do_comment:
                showCommentDialog();
                break;
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

    /**
     * When the user manipulates the seek bar, update the video position.
     */
    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoWidgetView.seekTo(progress);
                updateStatusText();
            } // else this was from the ActivityEventHandler.onNewFrame()'s seekBar.setProgress update.
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrVideoEventListener {
        private final int DELAY = 4000;
        private boolean flag = true;
        private long one = 0;
        private long two = 0;
        private int interval = 300;
        private Boolean videoBottomShow = true;

        /**
         * Called by video widget on the UI thread when it's done loading the video.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Successfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
            seekBar.setMax((int) videoWidgetView.getDuration());
            updateStatusText();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    videoBottomView.setVisibility(View.GONE);
                    videoBottomShow = false;
                }
            }, DELAY);
        }

        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the video format.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Toast.makeText(
                    VrVideoActivity.this, "Error loading video: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading video: " + errorMessage);
        }

        @Override
        public void onClick() {
            if (!flag) {
                if ((System.currentTimeMillis() - one) > interval) {
                    flag = true;
                    one = 0;
                    two = 0;
                }
            }
            if (flag) {
                one = System.currentTimeMillis();
                flag = false;
            } else {
                two = System.currentTimeMillis();
                if ((two - one) > interval) {
                    flag = true;
                    one = 0;
                    two = 0;
                }
            }
            long t_o = two - one;
            if (t_o > 0 && t_o < interval) {
                togglePause();
                flag = true;
                one = 0;
                two = 0;
            } else {
                if (videoBottomShow) {
                    videoBottomView.setVisibility(View.GONE);
                    videoBottomShow = false;
                } else {
                    videoBottomView.setVisibility(View.VISIBLE);
                    videoBottomShow = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            videoBottomView.setVisibility(View.GONE);
                            videoBottomShow = false;
                        }
                    }, DELAY);
                }
            }
        }

        /**
         * Update the UI every frame.
         */
        @Override
        public void onNewFrame() {
            updateStatusText();
            seekBar.setProgress((int) videoWidgetView.getCurrentPosition());
        }

        /**
         * Make the video play in a loop. This method could also be used to move to the next video in
         * a playlist.
         */
        @Override
        public void onCompletion() {
            videoWidgetView.seekTo(0);
        }
    }

    /**
     * Helper class to manage threading.
     */
    class VideoLoaderTask extends AsyncTask<Pair<Uri, VrVideoView.Options>, Void, Boolean> {
        @SuppressLint("WrongThread")
        @Override
        protected Boolean doInBackground(Pair<Uri, VrVideoView.Options>... fileInformation) {
            try {
                VrVideoView.Options options = new VrVideoView.Options();
                if (playUrl.substring(playUrl.length() - 4).equals("m3u8")) {
                    options.inputFormat = VrVideoView.Options.FORMAT_HLS;
                } else {
                    options.inputType = VrVideoView.Options.TYPE_MONO;
                }
                videoWidgetView.loadVideo(uri, options);
//                if (fileInformation == null || fileInformation.length < 1
//                        || fileInformation[0] == null || fileInformation[0].first == null) {
//                    // No intent was specified, so we default to playing the local stereo-over-under video.
//                    options.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER;
//                    videoWidgetView.loadVideoFromAsset("congo.mp4", options);
////                    videoWidgetView.loadVideoFromAsset("sphericalv2.mp4", options);
//                } else {
//                    videoWidgetView.loadVideo(fileInformation[0].first, fileInformation[0].second);
//                }
            } catch (IOException e) {
                // An error here is normally due to being unable to locate the file.
                loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
                // Since this is a background thread, we need to switch to the main thread to show a toast.
                videoWidgetView.post(() -> Toast
                        .makeText(VrVideoActivity.this, "Error opening file. ", Toast.LENGTH_LONG)
                        .show());
                Log.e(TAG, "Could not open video: " + e);
            }

            return true;
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
//            if (showMore) {
//                if (total >= 3) {
//                    total = 3;
//                    showMore = false;
//                } else {
//                    relate_btn.setVisibility(View.GONE);
//                }
//            }
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
//                Log.e(TAG, "onGroupClick: 当前的评论id>>>" + commentList.get(groupPosition).getId());
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
//                Toast.makeText(SimpleVrVideoActivity.this, "点击了回复", Toast.LENGTH_SHORT).show();
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
     * func:生成测试数据
     *
     * @return 评论数据
     */
//    private List<CommentDetailBean> generateTestData() {
//        Gson gson = new Gson();
//        commentBean = gson.fromJson(testJson, CommentBean.class);
//        List<CommentDetailBean> commentList = commentBean.getData().getList();
//        return commentList;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
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

                String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/SystemApi-addComment.action" +
                        "?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&content=" + commentContent + "&entityId=" + id + "&memberName=" + mName;
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
                            Toast.makeText(VrVideoActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
                            getCommentData(id);
                            Looper.loop();
                        } else {
                            Looper.prepare();
                            Toast.makeText(VrVideoActivity.this, "发表评论失败", Toast.LENGTH_SHORT).show();
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
//                                Toast.makeText(SimpleVrVideoActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
//                                getCommentData(id);
//                                Looper.loop();
//                            } else {
//                                Looper.prepare();
//                                Toast.makeText(SimpleVrVideoActivity.this, "发表评论失败", Toast.LENGTH_SHORT).show();
//                                Looper.loop();
//                            }
//                        }
//
//                    });
                }).start();

                dialog.dismiss();

            } else {
                Toast.makeText(VrVideoActivity.this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
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
//        final Button bt_comment = (Button) commentView.findViewById(R.id.dialog_comment_bt);
//        commentText.setHint("回复 " + commentsList.get(position).getNickName() + " 的评论:");
//        dialog.setContentView(commentView);
//        bt_comment.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String replyContent = commentText.getText().toString().trim();
//                if (!TextUtils.isEmpty(replyContent)) {
//                    dialog.dismiss();
//                    ReplyDetailBean detailBean = new ReplyDetailBean("小红", replyContent);
//                    adapter.addTheReplyData(detailBean, position);
//                    expandableListView.expandGroup(position);
//                    Toast.makeText(SimpleVrVideoActivity.this, "回复成功", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(SimpleVrVideoActivity.this, "回复内容不能为空", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        commentText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (!TextUtils.isEmpty(charSequence) && charSequence.length() > 2) {
//                    bt_comment.setBackgroundColor(Color.parseColor("#FFB568"));
//                } else {
//                    bt_comment.setBackgroundColor(Color.parseColor("#D8D8D8"));
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//        dialog.show();
//    }

}

