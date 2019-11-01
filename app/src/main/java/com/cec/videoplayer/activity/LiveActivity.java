package com.cec.videoplayer.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cec.videoplayer.R;
import com.cec.videoplayer.adapter.LiveCommentAdapter;
import com.cec.videoplayer.module.Comment;
import com.cec.videoplayer.module.ContentInfo;
import com.cec.videoplayer.module.NetValue;
import com.cec.videoplayer.module.User;
import com.cec.videoplayer.utils.ToastUtils;
import com.cec.videoplayer.view.LivePlayerView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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

public class LiveActivity extends Activity {
    private NetValue netValue = new NetValue();
    private User user;

    private String mVideoPath;
    private TextView bt_comment;
    private BottomSheetDialog dialog;
    private ListView lv_comment;
    private ImageView iv_back;
    private LivePlayerView player;


    private String memberName = "";
    private String id;
    private String contentInfoStr;
    private String url;
    private List<Comment> cList = new ArrayList<>();
    private List<com.dou361.ijkplayer.module.Comment> comList = new ArrayList<>();
    private LiveCommentAdapter liveComAdapter;
    private boolean isLogin;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        setContentView(R.layout.activity_live);
        id = getIntent().getStringExtra("id");
        url = getIntent().getStringExtra("url");
        contentInfoStr = getIntent().getStringExtra("contentInfoStr");
        initView();
        initEvent();
        getSession();
        initModel(contentInfoStr, 1);
        getCommentData(id);
    }

    @SuppressLint("InvalidWakeLockTag")
    public void initView() {
        /**常亮*/
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "liveTAG");
        wakeLock.acquire();
        player = new LivePlayerView(this);
        bt_comment = findViewById(R.id.live_detail_page_do_comment);
        lv_comment = findViewById(R.id.live_detail_page_lv_comment);
        liveComAdapter = new LiveCommentAdapter(cList, this, lv_comment);
        lv_comment.setAdapter(liveComAdapter);
        iv_back = (ImageView) findViewById(R.id.live_app_video_finish);
    }

    public void initEvent() {
        bt_comment.setOnClickListener(v -> {
            showCommentDialog();
        });
        iv_back.setOnClickListener(v -> {
            this.finish();
        });
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
                        "?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&content=" + commentContent + "&entityId=" + id
                        + "&memberName=" + mName;
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
                            Toast.makeText(LiveActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
                            getCommentData(id);
                            Looper.loop();
                        } else {
                            Looper.prepare();
                            Toast.makeText(LiveActivity.this, "发表评论失败", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LiveActivity.this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
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

    private void initModel(String json, int type) {
        if (type == 1) {
            Gson gson = new Gson();
            List<ContentInfo> infos = gson.fromJson(json, new TypeToken<List<ContentInfo>>() {
            }.getType());
            if (url == null) {
                LiveActivity.this.runOnUiThread(() -> {
                    ToastUtils.showLong("获取不到直播源");
                });
            } else {
                if (infos != null && infos.size() > 0) {
                    getCommentData(id);

                    //子线程刷新UI
                    LiveActivity.this.runOnUiThread(() -> {
                        mVideoPath = url;
                        player.setPlaySource("",mVideoPath).startPlay();

                    });

                } else {
                    LiveActivity.this.runOnUiThread(() -> {
                        ToastUtils.showLong("无直播活动或直播已结束");
                    });
                    this.finish();
                }
            }
        } else if (type == 2) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            cList = gson.fromJson(json, new TypeToken<List<Comment>>() {
            }.getType());
            comList = gson.fromJson(json, new TypeToken<List<com.dou361.ijkplayer.module.Comment>>() {
            }.getType());
//            player.setCommentList(comList);
            LiveActivity.this.runOnUiThread(() -> {
                liveComAdapter.onDateChange(cList);
            });
        }

    }


}
