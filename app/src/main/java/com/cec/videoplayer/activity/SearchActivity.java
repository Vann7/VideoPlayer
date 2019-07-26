package com.cec.videoplayer.activity;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cec.videoplayer.R;

import com.cec.videoplayer.adapter.VideoListAdapter;
import com.cec.videoplayer.module.CategoryInfo;
import com.cec.videoplayer.module.ContentInfo;
import com.cec.videoplayer.module.VideoInfo;
import com.cec.videoplayer.service.NetService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.tangguna.searchbox.library.cache.HistoryCache;
import com.tangguna.searchbox.library.callback.onSearchCallBackListener;
import com.tangguna.searchbox.library.widget.SearchListLayout;

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

public class SearchActivity extends AppCompatActivity {
    private SearchListLayout searchLayout;
    private TabLayout tabLayout = null;
    private ViewPager vp_pager;
    private List<CategoryInfo> mList = new ArrayList<>();
    private MorePagerAdapter mAdapter = new MorePagerAdapter();
    private NetService netService = new NetService();
    private String keyWord;
    private LinearLayout noResultLayout;
    private LinearLayout netErrorLayout;
    private LinearLayout historyLayout;
    private ViewPager viewPager;
    private ContentInfo contentInfo;
    private int net = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.search_main);
        searchLayout = findViewById(R.id.searchlayout);
        noResultLayout = findViewById(R.id.no_search_result);
        netErrorLayout = findViewById(R.id.net_error_layout);
        historyLayout = (LinearLayout) findViewById(R.id.history_list);
        viewPager = findViewById(R.id.search_tab_viewpager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


//        获取屏幕尺寸
//        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics dm = new DisplayMetrics();
//        wm.getDefaultDisplay().getMetrics(dm);
//        int width = dm.widthPixels;         // 屏幕宽度（像素）
//        int height = dm.heightPixels;       // 屏幕高度（像素）
//        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
//        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
//        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
//        int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
//        int screenHeight = (int) (height / density);// 屏幕高度(dp)

        tabLayout = findViewById(R.id.search_tablayout);
        vp_pager = findViewById(R.id.search_tab_viewpager);
        CategoryInfo fPage = new CategoryInfo("", "", "", "", "首页");
        mList.add(fPage);
        mAdapter.getCount();
        mAdapter.getPageTitle(0);
        SearchActivity.this.runOnUiThread(() -> mAdapter.notifyDataSetChanged());
        initData();
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

    private void initData() {
        List<String> skills = HistoryCache.toArray(getApplicationContext());
//        String shareHotData ="C++,C,PHP,React";
//        List<String> skillHots = Arrays.asList(shareHotData.split(","));
//        searchLayout.initData(skills, skillHots, new onSearchCallBackListener() {

        searchLayout.initData(skills, new onSearchCallBackListener() { //匿名内部类，不是new一个接口
            @Override
            public void Search(String str) {
                noResultLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                historyLayout.setVisibility(View.GONE);
                MorePagerAdapter morePagerAdapter = new MorePagerAdapter();
                if (!morePagerAdapter.isNetworkAvailable(SearchActivity.this)) {
                    netErrorLayout.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                    TextView textView = findViewById(R.id.net_error_text);
                    textView.setText("无网络连接" + "\n" + "请检查网络设置！");
                } else {
                    //进行或联网搜索
                    keyWord = str;
                    tabLayout.setTabMode(TabLayout.MODE_FIXED);
                    vp_pager.setAdapter(mAdapter);
                    tabLayout.setupWithViewPager(vp_pager);
                }
            }

            @Override
            public void Back() {
                finish();
            }

            @Override
            public void ClearOldData() {
                //清除历史搜索记录  更新记录原始数据
                HistoryCache.clear(getApplicationContext());
            }

            @Override
            public void SaveOldData(ArrayList<String> AlloldDataList) {
                //保存所有的搜索记录
                HistoryCache.saveHistory(getApplicationContext(), HistoryCache.toJsonArray(AlloldDataList));
            }
        }, 1);
    }


//    public void startActivity(Class<?> openClass, Bundle bundle) {
//        Intent intent = new Intent(this, openClass);
//        if (null != bundle)
//            intent.putExtras(bundle);
//        startActivity(intent);
//    }

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
                layout = new LinearLayout(SearchActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                layout.setLayoutParams(layoutParams);

                gridView = new PullToRefreshGridView(SearchActivity.this);
                gridView.setId(position);
                setVideoGridViewStyle(gridView);
                gridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
                    @Override
                    public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
                        ((VideoListAdapter) (refreshView.getRefreshableView().getAdapter())).clear();

                        getVideoListByKeyword((PullToRefreshGridView) refreshView, (VideoListAdapter) refreshView.getRefreshableView().getAdapter(), keyWord);

                    }

                    @Override
                    public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {

                        getVideoListByKeyword((PullToRefreshGridView) refreshView, (VideoListAdapter) refreshView.getRefreshableView().getAdapter(), keyWord);

                    }
                });
                gridView.setOnItemClickListener((parent, v, positions, id) -> {
                    if (!isNetworkAvailable(SearchActivity.this)) {
                        if (isWifi(SearchActivity.this)) {
                            net = 1;
                            Intent intent = new Intent(SearchActivity.this, NoNetworkActivity.class);
                            intent.putExtra("netType", net);
                            startActivity(intent);
                        } else if (isMobile(SearchActivity.this)) {
                            net = 2;
                            Intent intent = new Intent(SearchActivity.this, NoNetworkActivity.class);
                            intent.putExtra("netType", net);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(SearchActivity.this, NoNetworkActivity.class);
                            startActivity(intent);
                        }
                    } else {
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
                                    List<ContentInfo> infos = gson.fromJson(json, new TypeToken<List<ContentInfo>>() {
                                    }.getType());
                                    contentInfo = infos.get(0);
                                    if (contentInfo.getVideo360() == 1) {
                                        Intent playIntent = new Intent(SearchActivity.this, SimpleVrVideoActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("id", contentInfo.getId());
                                        playIntent.putExtras(bundle);
                                        startActivity(playIntent);
                                    } else {
                                        Intent playIntent = new Intent(SearchActivity.this, PlayerActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("url", videoInfo.getPlayurl());
                                        bundle.putString("id", videoInfo.getId());
                                        playIntent.putExtras(bundle);
                                        startActivity(playIntent);
                                    }
                                }
                            });
                        }).start();
                    }
                });
                videoList = new ArrayList<VideoInfo>();
                videoListAdapter = new VideoListAdapter(SearchActivity.this, R.layout.video_item, videoList);
                gridView.setAdapter(videoListAdapter);

                layout.addView(gridView);
                container.addView(layout);
            }
            ((VideoListAdapter) gridView.getRefreshableView().getAdapter()).clear();

            getVideoListByKeyword(gridView, (VideoListAdapter) gridView.getRefreshableView().getAdapter(), keyWord);

            return layout;
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

        //根据檢索內容获取视频列表
        public void getVideoListByKeyword(PullToRefreshGridView gridView, VideoListAdapter adapter, String keyWord) {
            new Thread(() -> {
                String url = "http://" + netService.getIp() + ":" + netService.getPort() + "/powercms/api/ContentApi-searchContent.action?userName=demo1&token=f620969ebe7a0634c0aabc1b4fecf1ab&returnType=json&siteId="
                        + netService.getSiteId() + "&keyword=" + keyWord;
                String s = url;
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
            SearchActivity.this.runOnUiThread(() -> {
                if (videoInfoList != null) {
                    for (int i = 0; i < videoInfoList.size(); ++i) {
                        adapter.add(videoInfoList.get(i));
                    }
                } else {
                    viewPager.setVisibility(View.GONE);
                    noResultLayout.setVisibility(View.VISIBLE);
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

        /**
         * 检查是否有网络
         */
        public boolean isNetworkAvailable(Context context) {

            NetworkInfo info = getNetworkInfo(context);
            return info != null && info.isAvailable();
        }


        /**
         * 检查是否是WIFI
         */
        public boolean isWifi(Context context) {

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
        public boolean isMobile(Context context) {

            NetworkInfo info = getNetworkInfo(context);
            if (info != null) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return true;
                }
            }
            return false;
        }


        private NetworkInfo getNetworkInfo(Context context) {

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo();
        }
    }

}
