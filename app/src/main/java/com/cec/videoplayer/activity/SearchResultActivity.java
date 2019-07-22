package com.cec.videoplayer.activity;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.adapter.VideoListAdapter;
import com.cec.videoplayer.module.VideoInfo;
import com.cec.videoplayer.service.NetService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SearchResultActivity extends AppCompatActivity {
    private NetService netService = new NetService();
    private PullToRefreshGridView gridView;
    private VideoListAdapter madapter;
    List<VideoInfo> videoList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        gridView = new PullToRefreshGridView(SearchResultActivity.this);
//        TextView textView = (TextView) findViewById(R.id.tv_show);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            initView(bundle);
        }

    }


    public void initView(Bundle bundle) {
        getVideoListByKeyword(gridView, madapter, bundle);
//        madapter = new VideoListAdapter(SearchResultActivity.this, R.layout.video_item, videoList);
        gridView.setAdapter(madapter);

//        if (gridView == null) {
//            layout = new LinearLayout(SearchResultActivity.this);
//            layout.setOrientation(LinearLayout.VERTICAL);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//            layout.setLayoutParams(layoutParams);
//
//            gridView = new PullToRefreshGridView(SearchResultActivity.this);
//            gridView.setId(0);
//            setVideoGridViewStyle(gridView);
//            gridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
//                @Override
//                public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
//                    ((VideoListAdapter) (refreshView.getRefreshableView().getAdapter())).clear();
//                    getVideoListByKeyword((PullToRefreshGridView) refreshView, (VideoListAdapter) refreshView.getRefreshableView().getAdapter(), bundle);
//                }
//
//                @Override
//                public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
//                    getVideoListByKeyword((PullToRefreshGridView) refreshView, (VideoListAdapter) refreshView.getRefreshableView().getAdapter(), bundle);
//                }
//            });
//            gridView.setOnItemClickListener((parent, v, positions, id) -> {
//                VideoInfo videoInfo = (VideoInfo) parent.getAdapter().getItem(positions);
//                Intent playIntent = new Intent(SearchResultActivity.this, PlayerActivity.class);
//                Bundle netBundle = new Bundle();
//                netBundle.putString("url", videoInfo.getPlayurl());
//                netBundle.putString("id", videoInfo.getId());
//                playIntent.putExtras(netBundle);
//                startActivity(playIntent);
//            });
//            videoList = new ArrayList<VideoInfo>();
//            videoListAdapter = new VideoListAdapter(SearchResultActivity.this, R.layout.video_item, videoList);
//            gridView.setAdapter(videoListAdapter);
//            layout.addView(gridView);
//        }
//        ((VideoListAdapter) gridView.getRefreshableView().getAdapter()).clear();
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


    //设置视频列表的样式
    public void setVideoGridViewStyle(PullToRefreshGridView view) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(10, 10, 10, 0);

        view.setLayoutParams(layoutParams);
        view.setGravity(Gravity.CENTER);
        view.getRefreshableView().setHorizontalSpacing(10);
        view.getRefreshableView().setVerticalSpacing(5);
        view.getRefreshableView().setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        view.getRefreshableView().setNumColumns(2);
        view.setMode(PullToRefreshBase.Mode.BOTH);
    }

    //根据检索内容获取视频列表
    public void getVideoListByKeyword(PullToRefreshGridView gridView, VideoListAdapter adapter, Bundle bundle) {
        new Thread(() -> {
            String url = "http://" + netService.getIp() + ":" + netService.getPort() + "/powercms/api/ContentApi-searchContent.action?userName=demo1&token=f620969ebe7a0634c0aabc1b4fecf1ab&returnType=json&siteId="
                    + netService.getSiteId() + "keyword=" + bundle.getString("data");
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            //异步加载
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("load", "onFailure");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response = client.newCall(request).execute();
                    //将视频信息json转成类对象
                    convertVideoFromJson(response.body().string(), gridView, adapter);
                    response.body().close();
                }
            });
        }).start();
    }

    //将视频列表由json格式转换成类数组
    public void convertVideoFromJson(String json, PullToRefreshGridView gridView, VideoListAdapter adapter) {
        Gson gson = new Gson();
        videoList = gson.fromJson(json, new TypeToken<List<VideoInfo>>() {
        }.getType());
        SearchResultActivity.this.runOnUiThread(() -> {
            for (int i = 0; i < videoList.size(); ++i) {
                adapter.add(videoList.get(i));
            }
            gridView.onRefreshComplete();
        });
    }

}
