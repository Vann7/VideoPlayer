package com.cec.videoplayer.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.module.CategoryInfo;
import com.cec.videoplayer.module.ContentInfo;
import com.cec.videoplayer.module.VideoInfo;
import com.cec.videoplayer.service.NetService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.cec.videoplayer.adapter.VideoListAdapter;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zq on 2017/1/12.
 */

public class TabActivity extends AppCompatActivity {
    private TabLayout tabLayout = null;
    private ViewPager vp_pager;
    private List<CategoryInfo> categoryInfos = new ArrayList<>();
    private List<CategoryInfo> mList = new ArrayList<>();
    private MorePagerAdapter mAdapter = new MorePagerAdapter();
    private NetService netService = new NetService();
    private ContentInfo contentInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tab_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        tabLayout = findViewById(R.id.tablayout);
        vp_pager = findViewById(R.id.tab_viewpager);
        ImageView userLogin = findViewById(R.id.user_msg);
        userLogin.setOnClickListener(v -> {
            Intent intent1 = new Intent(TabActivity.this, LoginActivity.class);
            startActivity(intent1);
        });
        EditText searchEditText = findViewById(R.id.fp_search);
        searchEditText.setOnClickListener(v -> {
            Intent intent2 = new Intent(TabActivity.this, SearchActivity.class);
            startActivity(intent2);
        });
        Bundle bundle = getIntent().getExtras();
        categoryInfos = bundle.getParcelableArrayList("categorys");
        filter(categoryInfos);
        initView();
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
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        vp_pager.setAdapter(mAdapter);
        tabLayout.setupWithViewPager(vp_pager);
    }

    final class MorePagerAdapter extends PagerAdapter {
        public MorePagerAdapter() {

        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //判断当前是否存在gridview
            PullToRefreshGridView gridView = null;
            VideoListAdapter videoListAdapter = null;
            List<VideoInfo> videoList = null;
            LinearLayout layout = null;
            for (int i = 0; i < container.getChildCount(); ++i) {
                layout = (LinearLayout) container.getChildAt(i);
                if (layout.getChildAt(0).getId() == position) {
                    gridView = (PullToRefreshGridView) layout.getChildAt(0);
                    break;
                } else {
                    layout = null;
                }
            }
            if (gridView == null) {
                layout = new LinearLayout(TabActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                layout.setLayoutParams(layoutParams);

                gridView = new PullToRefreshGridView(TabActivity.this);
                gridView.setId(position);
                setVideoGridViewStyle(gridView);
                gridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
                    @Override
                    public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
                        ((VideoListAdapter) (refreshView.getRefreshableView().getAdapter())).clear();
                        if (position != 0) {
                            getVideoListByCateId(mList.get(position).getId(), (PullToRefreshGridView) refreshView, (VideoListAdapter) refreshView.getRefreshableView().getAdapter());
                        } else {
                            getVideoListBySiteId((PullToRefreshGridView) refreshView, (VideoListAdapter) refreshView.getRefreshableView().getAdapter());
                        }
                    }

                    @Override
                    public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
                        if (position != 0) {
                            getVideoListByCateId(mList.get(position).getId(), (PullToRefreshGridView) refreshView, (VideoListAdapter) refreshView.getRefreshableView().getAdapter());
                        } else {
                            getVideoListBySiteId((PullToRefreshGridView) refreshView, (VideoListAdapter) refreshView.getRefreshableView().getAdapter());
                        }
                    }
                });
                gridView.setOnItemClickListener((parent, v, positions, id) -> {
                    VideoInfo videoInfo = (VideoInfo) parent.getAdapter().getItem(positions);
                    String url = "http://" + netService.getIp() + ":" + netService.getPort() + "/powercms/api/ContentApi-getContentInfo.action" +
                            "?userName=demo1&token=f620969ebe7a0634c0aabc1b4fecf1ab&returnType=json&size=10&" +
                            "contentId=" + videoInfo.getId();

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
                                Gson gson = new Gson();
                                List<ContentInfo> infos = gson.fromJson(json,new TypeToken<List<ContentInfo>>() {}.getType());
                                contentInfo=infos.get(0);
                                if(contentInfo.getVideo360()==1){
                                    Intent playIntent = new Intent(TabActivity.this, SimpleVrVideoActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("id", contentInfo.getId());
                                    playIntent.putExtras(bundle);
                                    startActivity(playIntent);
                                }else{
                                    Intent playIntent = new Intent(TabActivity.this, PlayerActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("url", videoInfo.getPlayurl());
                                    bundle.putString("id", videoInfo.getId());
                                    playIntent.putExtras(bundle);
                                    startActivity(playIntent);
                                }
                            }
                        });
                    }).start();
                });
                videoList = new ArrayList<VideoInfo>();
                videoListAdapter = new VideoListAdapter(TabActivity.this, R.layout.video_item, videoList);
                gridView.setAdapter(videoListAdapter);

                layout.addView(gridView);
                container.addView(layout);
            }
            ((VideoListAdapter) gridView.getRefreshableView().getAdapter()).clear();
            if (position != 0) {
                getVideoListByCateId(mList.get(position).getId(), gridView, (VideoListAdapter) gridView.getRefreshableView().getAdapter());
            } else {
                getVideoListBySiteId(gridView, (VideoListAdapter) gridView.getRefreshableView().getAdapter());
            }
            return layout;
        }

        //设置视频列表的样式
        public void setVideoGridViewStyle(PullToRefreshGridView view) {
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(10, 10, 10, 0);

            view.setLayoutParams(layoutParams);
            view.setGravity(Gravity.CENTER);
            view.getRefreshableView().setHorizontalSpacing(10);
            view.getRefreshableView().setVerticalSpacing(5);
            view.getRefreshableView().setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            view.getRefreshableView().setNumColumns(2);
            view.setMode(PullToRefreshBase.Mode.BOTH);
        }

        //根据栏目ID获取视频列表
        public void getVideoListByCateId(String cateId, PullToRefreshGridView gridView, VideoListAdapter adapter) {
            new Thread(() -> {
                String url = "http://" + netService.getIp() + ":" + netService.getPort() + "/powercms/api/ContentApi-getContentList.action?userName=demo1&token=f620969ebe7a0634c0aabc1b4fecf1ab&returnType=json&cateId=" + cateId;
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

        //根据站点id获取视频列表
        public void getVideoListBySiteId(PullToRefreshGridView gridView, VideoListAdapter adapter) {
            new Thread(() -> {
                String url = "http://" + netService.getIp() + ":" + netService.getPort() + "/powercms/api/ContentApi-getContentList.action?userName=demo1&token=f620969ebe7a0634c0aabc1b4fecf1ab&returnType=json&siteId=" + netService.getSiteId();
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
            List<VideoInfo> videoInfoList = gson.fromJson(json, new TypeToken<List<VideoInfo>>() {
            }.getType());
            TabActivity.this.runOnUiThread(() -> {
                for (int i = 0; i < videoInfoList.size(); ++i) {
                    adapter.add(videoInfoList.get(i));
                }
                gridView.onRefreshComplete();
            });
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            (container).removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CategoryInfo categoryInfo = mList.get(position);
            return categoryInfo.getName();
        }
    }

    public void filter(List<CategoryInfo> list) {
        int i = 0;
        int j = 0;
        CategoryInfo fPage = new CategoryInfo("", "", "", "", "首页");
        mList.add(fPage);
        mAdapter.getCount();
        mAdapter.getPageTitle(j);
        while (i < list.size()) {
            CategoryInfo categoryInfo = list.get(i);
            if (categoryInfo.getSiteId().equals(netService.getSiteId())) {
                mList.add(categoryInfo);
                mAdapter.getCount();
                mAdapter.getPageTitle(j + 1);
                ++j;
            }
            i++;
        }
        TabActivity.this.runOnUiThread(() -> mAdapter.notifyDataSetChanged());
    }

}