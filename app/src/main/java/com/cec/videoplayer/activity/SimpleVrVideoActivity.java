package com.cec.videoplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cec.videoplayer.R;
import com.cec.videoplayer.adapter.RelateAdapter;
import com.cec.videoplayer.model.User;
import com.cec.videoplayer.module.ContentInfo;
import com.cec.videoplayer.module.Relate;
import com.cec.videoplayer.utils.PlayHitstUtil;
import com.cec.videoplayer.utils.TimeFormatUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;
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
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d "file:///android_asset/congo.mp4"
 *
 * <p>Video located on the phone's SD card.
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d /sdcard/FILENAME.MP4
 *
 * <p>Video hosted on a website:
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d "https://EXAMPLE.COM/FILENAME.MP4"
 *
 * <p>To load HLS files add "--ei inputFormat 2" to pass in an integer extra which will set
 * VrVideoView.Options.inputFormat to FORMAT_HLS. e.g.
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d "file:///android_asset/hls/iceland.m3u8" \
 *     --ei inputFormat 2 --ei inputType 2
 *
 * <p>To load MPEG-DASH files add "--ei inputFormat 3" to pass in an integer extra which will set
 * VrVideoView.Options.inputFormat to FORMAT_DASH. e.g.
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d "file:///android_asset/dash/congo_dash.mpd" \
 *     --ei inputFormat 3 --ei inputType 2
 *
 * <p>To specify that the video is of type stereo over under (has images for left and right eyes),
 * add "--ei inputType 2" to pass in an integer extra which will set VrVideoView.Options.inputType
 * to TYPE_STEREO_OVER_UNDER. This can be combined with other extras, e.g:
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d "https://EXAMPLE.COM/FILENAME.MP4" \
 *     --ei inputType 2
 */
public class SimpleVrVideoActivity extends Activity  implements View.OnClickListener {
    private static final String TAG = SimpleVrVideoActivity.class.getSimpleName();

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

    /** Tracks the file to be loaded across the lifetime of this app. **/
    private Uri fileUri;

    /** Configuration information for the video. **/
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

    private ImageButton volumeToggle;
    private ImageButton playToggle;
    private boolean isMuted;
    private TextView statusText;

    /**
     * By default, the video will start playing as soon as it is loaded. This can be changed by using
     * {@link VrVideoView#pauseVideo()} after loading the video.
     */
    private boolean isPaused = false;

    private ContentInfo contentInfo;
    private ListView lv_relate;
    private RelateAdapter relAdapter;
    private TextView tv_title;
    private TextView tv_hits;
    private TextView tv_updateTime;
    private ImageView iv_back;
    private List<Relate> mList;
    private String id;
    private String url;
    private User user;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_vr_video);
        id = getIntent().getStringExtra("id");
        getSession();
        initView();
        initEvent();
        getNetData(id);
    }

    private void initView() {
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBarListener());
        statusText = (TextView) findViewById(R.id.tv_vr_time);
        iv_back = findViewById(R.id.iv_vr_back);
        // Make the source link clickable.
//        TextView sourceText = (TextView) findViewById(R.id.source);
////        sourceText.setText(Html.fromHtml(getString(R.string.source)));
//        sourceText.setMovementMethod(LinkMovementMethod.getInstance());

        //初始化相关视频lv
        lv_relate = findViewById(R.id.lv_vr_relate);
        mList = new ArrayList<>();
        relAdapter = new RelateAdapter(mList, this, lv_relate);
        lv_relate.setAdapter(relAdapter);

        //初始化视频信息
        tv_title = findViewById(R.id.tv_vr_content_title);
        tv_hits = findViewById(R.id.tv_vr_content_hits);
        tv_updateTime = findViewById(R.id.tv_vr_content_updateTime);

    }

    private void initEvent() {
        // Bind input and output objects for the view.
        videoWidgetView = (VrVideoView) findViewById(R.id.video_view);
        videoWidgetView.setEventListener(new ActivityEventListener());

        volumeToggle = (ImageButton) findViewById(R.id.volume_toggle);
        volumeToggle.setOnClickListener(v -> setIsMuted(!isMuted));
        playToggle = findViewById(R.id.play_toggle);
        playToggle.setOnClickListener(v -> togglePause());
        iv_back.setOnClickListener(this);

        loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

        // Initial launch of the app or an Activity recreation due to rotation.
        handleIntent(getIntent());

        //切换相关视频
        relAdapter.setOnListClickListener((relate, position) -> {
            getNetData(mList.get(position).getId());

        });
    }

    private void getNetData(String id) {

     /*   StringBuilder sb = new StringBuilder();
        sb.append("http://115.28.215.145:8080/powercms/api/ContentApi-getContentInfo.action" +
                "?userName=");
        sb.append(user.getName());
        sb.append("&token=");
        sb.append(user.getToken());
        sb.append("&returnType=json&size=20&contentId=");
        sb.append(id);
        url = sb.toString();*/

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

    private void initModel(String json) {
        Gson gson = new Gson();
        List<ContentInfo> infos = gson.fromJson(json,new TypeToken<List<ContentInfo>>() {}.getType());
        contentInfo = infos.get(0);
        mList = infos.get(1).getRelates();


        //子线程刷新UI
        SimpleVrVideoActivity.this.runOnUiThread(() -> {
            relAdapter.onDateChange(mList);
            tv_title.setText(contentInfo.getTitle());
            tv_hits.setText(PlayHitstUtil.getCount(contentInfo.getHits()));
            tv_updateTime.setText( contentInfo.getUpdateTime() + "发布");
        });
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

    public boolean isMuted() {
        return isMuted;
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

        } else {
            videoWidgetView.pauseVideo();
            playToggle.setImageResource(R.mipmap.simple_player_arrow_white_24dp);
        }
        isPaused = !isPaused;
        updateStatusText();
    }

    private void updateStatusText() {
        StringBuilder status = new StringBuilder();
        String current = TimeFormatUtil.MillisecondToDate(videoWidgetView.getCurrentPosition());
        String duration =  TimeFormatUtil.MillisecondToDate(videoWidgetView.getDuration());
        status.append(current);
        status.append(" / ");
        status.append(duration);
        statusText.setText(status.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_vr_back :
                this.finish();
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
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrVideoEventListener {
        /**
         * Called by video widget on the UI thread when it's done loading the video.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Successfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
            seekBar.setMax((int) videoWidgetView.getDuration());
            updateStatusText();
        }

        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the video format.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Toast.makeText(
                    SimpleVrVideoActivity.this, "Error loading video: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading video: " + errorMessage);
        }

        @Override
        public void onClick() {
            togglePause();
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
        @Override
        protected Boolean doInBackground(Pair<Uri, VrVideoView.Options>... fileInformation) {
            try {
                if (fileInformation == null || fileInformation.length < 1
                        || fileInformation[0] == null || fileInformation[0].first == null) {
                    // No intent was specified, so we default to playing the local stereo-over-under video.
                    VrVideoView.Options options = new VrVideoView.Options();
                    options.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER;
                    videoWidgetView.loadVideoFromAsset("congo.mp4", options);
//                    videoWidgetView.loadVideoFromAsset("sphericalv2.mp4", options);
                } else {
                    videoWidgetView.loadVideo(fileInformation[0].first, fileInformation[0].second);
                }
            } catch (IOException e) {
                // An error here is normally due to being unable to locate the file.
                loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
                // Since this is a background thread, we need to switch to the main thread to show a toast.
                videoWidgetView.post(() -> Toast
                        .makeText(SimpleVrVideoActivity.this, "Error opening file. ", Toast.LENGTH_LONG)
                        .show());
                Log.e(TAG, "Could not open video: " + e);
            }

            return true;
        }
    }

}

