package com.cec.videoplayer.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.cec.videoplayer.R;
import com.cec.videoplayer.adapter.VideoListAdapter;
import com.cec.videoplayer.model.User;
import com.cec.videoplayer.model.CategoryInfo;
import com.cec.videoplayer.model.ContentInfo;
import com.cec.videoplayer.model.PlayUrl;
import com.cec.videoplayer.model.VideoInfo;
import com.cec.videoplayer.model.NetValue;
import com.cec.videoplayer.view.VideoListGridView;
import com.cec.videoplayer.view.VideoScrollView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.cec.videoplayer.view.PullToRefreshBase;
import com.cec.videoplayer.view.PullToRefreshScrollView;
import com.tangguna.searchbox.library.cache.HistoryCache;
import com.tangguna.searchbox.library.callback.onSearchCallBackListener;
import com.tangguna.searchbox.library.widget.SearchListLayout;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {
    private User user;
    private SearchListLayout searchLayout;
    private TabLayout tabLayout = null;
    private ViewPager vp_pager;
    private List<CategoryInfo> mList = new ArrayList<>();
    private MorePagerAdapter mAdapter = new MorePagerAdapter();
    private NetValue netValue = new NetValue();
    private String keyWord;
    private LinearLayout noResultLayout;
    private LinearLayout historyLayout;
    private ViewPager viewPager;
    private ContentInfo contentInfo;
    private HashMap<String, VideoScrollView> mScrollViews = new HashMap<>();
    private Boolean isLogin;
    private String memberName = "";
    private List<PlayUrl> playUrls = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.search_main);
        searchLayout = findViewById(R.id.searchlayout);
        noResultLayout = findViewById(R.id.no_search_result);
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
                try {
                    String utf8Str = URLEncoder.encode(str, "UTF-8");
                    utf8Str = URLEncoder.encode(utf8Str, "UTF-8");
                    keyWord = utf8Str;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                tabLayout.setTabMode(TabLayout.MODE_FIXED);
                vp_pager.setAdapter(mAdapter);
                tabLayout.setupWithViewPager(vp_pager);
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
            VideoScrollView scrollView = null;
            for (int i = 0; i < container.getChildCount(); ++i) {
                if (container.getChildAt(i).getId() == position) {
                    scrollView = mScrollViews.get(position + "");
                    break;
                }
            }
            if (scrollView == null) {
                scrollView = new VideoScrollView(SearchActivity.this);
                mScrollViews.put(position + "", scrollView);
                scrollView.initScrollView();
                scrollView.initScrollViewChildren();
                scrollView.getmScrollView().setId(position);
                scrollView.getmScrollView().setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {
                    @Override
                    public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                        ((VideoListAdapter) ((VideoListGridView) ((LinearLayout) refreshView.getRefreshableView().getChildAt(0)).getChildAt(2)).getAdapter()).clear();
                        getVideoListByKeyword((PullToRefreshScrollView) refreshView, (VideoListAdapter) ((VideoListGridView) ((LinearLayout) refreshView.getRefreshableView().getChildAt(0)).getChildAt(2)).getAdapter(), keyWord);
                    }

                    @Override
                    public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
//                        getVideoListByKeyword((PullToRefreshScrollView) refreshView, (VideoListAdapter) ((VideoListGridView) ((LinearLayout) refreshView.getRefreshableView().getChildAt(0)).getChildAt(2)).getAdapter(), keyWord);
                        ((VideoListAdapter) ((VideoListGridView) ((LinearLayout) refreshView.getRefreshableView().getChildAt(0)).getChildAt(2)).getAdapter()).clear();
                        getVideoListByKeyword((PullToRefreshScrollView) refreshView, (VideoListAdapter) ((VideoListGridView) ((LinearLayout) refreshView.getRefreshableView().getChildAt(0)).getChildAt(2)).getAdapter(), keyWord);
                    }
                });
                scrollView.getmVideoListGridView().setOnItemClickListener((parent, v, positions, id) -> {
                    getSession();
                    VideoInfo videoInfo = (VideoInfo) parent.getAdapter().getItem(positions);
                    if (videoInfo.getLimit() == 0) {
                        String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/ContentApi-getContentInfo.action" +
                                "?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&returnType=json&size=10&" +
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
                                    String contentInfoStr = response.body().string();
                                    response.body().close();
                                    Gson gson = new Gson();
                                    List<ContentInfo> infos = gson.fromJson(contentInfoStr, new TypeToken<List<ContentInfo>>() {
                                    }.getType());
                                    contentInfo = infos.get(0);
                                    //直播
                                    if (videoInfo.getFileattr().equals("0")) {
                                        Intent playIntent = new Intent(SearchActivity.this, LiveActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("contentInfoStr", contentInfoStr);
                                        bundle.putString("id", videoInfo.getId());
                                        bundle.putString("url", videoInfo.getAddress());
                                        playIntent.putExtras(bundle);
                                        startActivity(playIntent);
                                    }
                                    //点播
                                    else {
                                        if (contentInfo.getVideo360() == 1) {
                                            Intent playIntent = new Intent(SearchActivity.this, VrVideoActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("id", contentInfo.getId());
                                            bundle.putString("contentInfoStr", contentInfoStr);
                                            playIntent.putExtras(bundle);
                                            startActivity(playIntent);
                                        } else {
                                            Intent playIntent = new Intent(SearchActivity.this, PlayerActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("contentInfoStr", contentInfoStr);
                                            bundle.putString("id", videoInfo.getId());
                                            playIntent.putExtras(bundle);
                                            startActivity(playIntent);
                                        }
                                    }
                                }

                            });
                        }).start();
                    } else {
                        if (isLogin) {
                            Toast.makeText(SearchActivity.this, "当前用户无权限访问该内容。", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SearchActivity.this, "请先登录。", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SearchActivity.this, MineActivity.class);
                            startActivity(intent);
                        }
                    }

                });
                container.addView(scrollView.getmScrollView());
            }
            ((VideoListAdapter) scrollView.getmVideoListGridView().getAdapter()).clear();
            scrollView.getmRotationImageView().getmPriorityAdapter().getmList().clear();
            scrollView.getmRotationImageView().getmPriorityAdapter().notifyDataSetChanged();
            scrollView.getmRotationImageView().getmHandler().removeMessages(0);
            if (!isNetworkAvailable(SearchActivity.this)) {
                scrollView.getmLinearLayout().getChildAt(1).setVisibility(View.GONE);
                scrollView.getmLinearLayout().getChildAt(2).setVisibility(View.GONE);
                scrollView.getmLinearLayout().getChildAt(3).setVisibility(View.VISIBLE);
                ((LinearLayout) (scrollView.getmLinearLayout().getChildAt(3))).getChildAt(2).setOnClickListener(v -> instantiateItem(container, position));
            } else {
                scrollView.getmLinearLayout().getChildAt(1).setVisibility(View.GONE);
                scrollView.getmLinearLayout().getChildAt(2).setVisibility(View.VISIBLE);
                scrollView.getmLinearLayout().getChildAt(3).setVisibility(View.GONE);
                getVideoListByKeyword(scrollView.getmScrollView(), scrollView.getmVideoListAdapter(), keyWord);
            }

            return scrollView.getmScrollView();
        }

        //根据检索內容获取视频列表
        public void getVideoListByKeyword(PullToRefreshScrollView scrollView, VideoListAdapter adapter, String keyWord) {
            new Thread(() -> {
                String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/ContentApi-searchContent.action?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&returnType=json&siteId="
                        + netValue.getSiteId() + "&keyword=" + keyWord + "&memberName=" + memberName;
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
                        convertVideoFromJson(response.body().string(), scrollView, adapter);
                        response.body().close();
                    }
                });
            }).start();
        }

        //将视频列表由json格式转换成类数组
        public void convertVideoFromJson(String json, PullToRefreshScrollView scrollView, VideoListAdapter adapter) {
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
                scrollView.onRefreshComplete();
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

    /**
     * 获取当前用户session信息
     */
    private void getSession() {
        SharedPreferences setting = this.getSharedPreferences("User", 0);
        user = new User(setting.getString("name", ""), setting.getString("password", ""));
        isLogin = setting.getBoolean("isLogin", false);
        if (isLogin) {
            memberName = user.getName();
        }
    }
}
