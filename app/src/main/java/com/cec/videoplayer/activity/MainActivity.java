package com.cec.videoplayer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cec.videoplayer.R;
import com.cec.videoplayer.adapter.VideoListAdapter;
import com.cec.videoplayer.module.User;
import com.cec.videoplayer.module.CategoryInfo;
import com.cec.videoplayer.module.ContentInfo;
import com.cec.videoplayer.module.PagingVideoInfo;
import com.cec.videoplayer.module.VideoInfo;
import com.cec.videoplayer.module.NetValue;
import com.cec.videoplayer.utils.FileUtil;
import com.cec.videoplayer.utils.NetWorkSpeedUtils;
import com.cec.videoplayer.view.RotationImageView;
import com.cec.videoplayer.view.VideoListGridView;
import com.cec.videoplayer.view.VideoScrollView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.cec.videoplayer.view.PullToRefreshBase;
import com.cec.videoplayer.view.PullToRefreshScrollView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zq on 2017/1/12.
 */

public class MainActivity extends Activity {
    private User user;
    private TabLayout tabLayout = null;
    private ViewPager vp_pager;
    private List<CategoryInfo> categoryInfos = new ArrayList<>();
    private List<CategoryInfo> mList = new ArrayList<>();
    private List<Integer> pnList = new ArrayList<>();
    private List<Integer> pcList = new ArrayList<>();
    private MorePagerAdapter mAdapter = new MorePagerAdapter();
    private NetValue netValue = new NetValue();
    private ContentInfo contentInfo;
    private String contentInfoStr;
    private HashMap<String, VideoScrollView> mScrollViews = new HashMap<>();
    private File listFileName;
    private File recommendFileName;
    private String listFilePath = Environment.getExternalStorageDirectory().getPath() + "/VideoPlayer/videoList.txt";
    private String recommendFilePath = Environment.getExternalStorageDirectory().getPath() + "/VideoPlayer/recommendList.txt";
    private TextView tv_speed;
    private Handler mHnadler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    tv_speed.setText("当前网速： " + msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private boolean isLogin;
    private String memberName = "";
    private Integer currentTotalCount;
    private Handler handler;
    private Handler handler1;
    private long waitTime = 2000;
    private long touchTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_layout);
        tv_speed = findViewById(R.id.tv_speed);

        new NetWorkSpeedUtils(this, mHnadler).startShowNetSpeed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        tabLayout = findViewById(R.id.tablayout);
        vp_pager = findViewById(R.id.tab_viewpager);
        vp_pager.setPadding(0, 0, 0, 0);
        listFileName = new File(listFilePath);
        recommendFileName = new File(recommendFilePath);
        ImageView userLogin = findViewById(R.id.user_msg);
        userLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MineActivity.class);
            startActivity(intent);
        });
        EditText searchEditText = findViewById(R.id.fp_search);
        searchEditText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
        //已登录状态，通过上一页面加载数据,后期进行修改，都在本页面加载
        Bundle bundle = getIntent().getExtras();
        categoryInfos = bundle.getParcelableArrayList("categorys");
        if (categoryInfos != null) {
            filter(categoryInfos);
            for (int i = 0; i < mList.size(); i++) {
                pnList.add(1);
                pcList.add(0);
            }
        }
        getSession();
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

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - touchTime) >= waitTime) {
            Toast.makeText(this, "再按一次退出", (int) waitTime).show();
            touchTime = currentTime;
        } else {
            super.onBackPressed();
            this.finish();
        }
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
            pnList.set(position, 1);
            VideoScrollView scrollView = null;
            for (int i = 0; i < container.getChildCount(); ++i) {
                if (container.getChildAt(i).getId() == position) {
                    scrollView = mScrollViews.get(position + "");
                    break;
                }
            }
            if (scrollView == null) {
                scrollView = new VideoScrollView(MainActivity.this);
                mScrollViews.put(position + "", scrollView);
                scrollView.initScrollView();
                scrollView.initScrollViewChildren();
                scrollView.getmScrollView().setId(position);
                scrollView.getmScrollView().setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {
                    @Override
                    public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
//                        ((VideoListAdapter) ((VideoListGridView) ((LinearLayout) refreshView.getRefreshableView().getChildAt(0)).getChildAt(2)).getAdapter()).clear();
//                        if (position != 0) {
//                            getVideoListByCateId(mList.get(position).getId(), (PullToRefreshScrollView) refreshView,
//                                    (VideoListAdapter) ((VideoListGridView) ((LinearLayout) refreshView.getRefreshableView().getChildAt(0)).getChildAt(2)).getAdapter());
//                        } else {
//                            getVideoListBySiteId((PullToRefreshScrollView) refreshView,
//                                    (VideoListAdapter) ((VideoListGridView) ((LinearLayout) refreshView.getRefreshableView().getChildAt(0)).getChildAt(2)).getAdapter());
//                        }
                        instantiateItem(container, position);
                    }

                    @Override
                    public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                        if (position != 0) {
                            if (pnList.get(position) < pcList.get(position)) {
                                pnList.set(position, pnList.get(position) + 1);
                                getVideoListByCateId(mList.get(position).getId(), (PullToRefreshScrollView) refreshView,
                                        (VideoListAdapter) ((VideoListGridView) ((LinearLayout) refreshView.getRefreshableView().getChildAt(0)).getChildAt(2)).getAdapter(), true, pnList.get(position), 10);
                            }
                            handler = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 10) {
                                        pcList.set(position, currentTotalCount);
                                        if (pnList.get(position) >= pcList.get(position)) {
                                            refreshView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                                        }
                                    }
                                }
                            };
                        } else {
                            if (pnList.get(position) < pcList.get(position)) {
                                pnList.set(position, pnList.get(position) + 1);
                                getVideoListBySiteId((PullToRefreshScrollView) refreshView,
                                        (VideoListAdapter) ((VideoListGridView) ((LinearLayout) refreshView.getRefreshableView().getChildAt(0)).getChildAt(2)).getAdapter(), true, pnList.get(position), 0);
                            }
                            handler1 = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 0) {
                                        pcList.set(position, currentTotalCount);
                                        if (pnList.get(position) >= pcList.get(position)) {
                                            refreshView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                                        }
                                    }
                                }
                            };
                        }
                    }
                });
                scrollView.getmVideoListGridView().setOnItemClickListener((parent, v, positions, id) ->
                {
                    getSession();
                    VideoInfo videoInfo = (VideoInfo) parent.getAdapter().getItem(positions);
                    if (videoInfo.getLimit() == 0) {
                        String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/ContentApi-getContentInfo.action" +
                                "?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&returnType=json&size=15&" +
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
                                    Looper.prepare();
                                    Toast.makeText(MainActivity.this, "无网络连接，请检查网络设置。", Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    response = client.newCall(request).execute();
                                    contentInfoStr = response.body().string();
                                    response.body().close();
                                    Gson gson = new Gson();
                                    List<ContentInfo> infos = gson.fromJson(contentInfoStr, new TypeToken<List<ContentInfo>>() {
                                    }.getType());
                                    contentInfo = infos.get(0);
                                    //直播
                                    if (videoInfo.getFileattr().equals("0")) {
                                        Intent playIntent = new Intent(MainActivity.this, LiveActivity.class);
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
                                            Intent playIntent = new Intent(MainActivity.this, VrVideoActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("contentInfoStr", contentInfoStr);
                                            bundle.putString("id", videoInfo.getId());
                                            playIntent.putExtras(bundle);
                                            startActivity(playIntent);
                                        } else {
                                            Intent playIntent = new Intent(MainActivity.this, PlayerActivity.class);
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
                            Toast.makeText(MainActivity.this, "当前用户无权限访问该内容。", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "请先登录。", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MainActivity.this, MineActivity.class);
                            startActivity(intent);
                        }
                    }
                });
                container.addView(scrollView.getmScrollView());
            }
            scrollView.getmScrollView().setMode(PullToRefreshBase.Mode.BOTH);
            ((VideoListAdapter) scrollView.getmVideoListGridView().getAdapter()).clear();
            scrollView.getmRotationImageView().getmPriorityAdapter().getmList().clear();
            scrollView.getmRotationImageView().getmPriorityAdapter().notifyDataSetChanged();
            scrollView.getmRotationImageView().getmHandler().removeMessages(0);
            if (position != 0) {
                if (!isNetworkAvailable(MainActivity.this)) {
                    scrollView.getmLinearLayout().getChildAt(1).setVisibility(View.GONE);
                    scrollView.getmLinearLayout().getChildAt(3).setVisibility(View.VISIBLE);
                    ((LinearLayout) (scrollView.getmLinearLayout().getChildAt(3))).getChildAt(2).setOnClickListener(v -> instantiateItem(container, position));
                } else {
                    scrollView.getmLinearLayout().getChildAt(1).setVisibility(View.VISIBLE);
                    scrollView.getmLinearLayout().getChildAt(2).setVisibility(View.VISIBLE);
                    scrollView.getmLinearLayout().getChildAt(3).setVisibility(View.GONE);
                    getVideoListOfPriorityCate(mList.get(position).getId(), scrollView.getmScrollView(), scrollView.getmLinearLayout(), scrollView.getmRelativeLayout(), scrollView.getmRotationImageView());
                    getVideoListByCateId(mList.get(position).getId(), scrollView.getmScrollView(), scrollView.getmVideoListAdapter(), true, pnList.get(position), 11);
                    VideoScrollView finalScrollView = scrollView;
                    handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 11) {
                                pcList.set(position, currentTotalCount);
                                if (pnList.get(position) >= pcList.get(position)) {
                                    finalScrollView.getmScrollView().setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                                }
                            }
                        }
                    };
                }
            } else {
                getVideoListOfPriority(scrollView.getmScrollView(), scrollView.getmLinearLayout(), scrollView.getmRelativeLayout(), scrollView.getmRotationImageView());
                getVideoListBySiteId(scrollView.getmScrollView(), scrollView.getmVideoListAdapter(), true, pnList.get(position), 1);
                VideoScrollView finalScrollView = scrollView;
                handler1 = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            pcList.set(position, currentTotalCount);
                            if (pnList.get(position) >= pcList.get(position)) {
                                finalScrollView.getmScrollView().setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            }
                        }
                    }
                };
            }
            return scrollView.getmScrollView();
        }

        //获取首页推荐的内容
        public void getVideoListOfPriority(PullToRefreshScrollView scrollView, LinearLayout
                linearLayout, RelativeLayout relativeLayout, RotationImageView rotationImageView) {
            if (!isNetworkAvailable(MainActivity.this)) {
                String sdCard = Environment.getExternalStorageState();
                if (sdCard.equals(Environment.MEDIA_MOUNTED)) {
                    FileUtil.createSDCardDir();
                    if (recommendFileName.exists()) {
                        try {
                            String list = FileUtil.readTxtFile(recommendFileName);
                            if (list != "") {
                                List<VideoInfo> videoInfoList = convertVideoFromJson(list);
                                refreshPriorityToViewPager(videoInfoList, scrollView, linearLayout, relativeLayout, rotationImageView);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                new Thread(() -> {
                    String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/ContentApi-getContentList.action?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&returnType=json&orderType=priority&siteId="
                            + netValue.getSiteId() + "&memberName=" + memberName;
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
                            String list = response.body().string();
                            response.body().close();
                            List<VideoInfo> videoInfoList = convertVideoFromJson(list);
                            boolean create = false;
                            try {
                                create = FileUtil.createFile(recommendFileName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (create) {
                                try {
                                    FileUtil.writeTxtFile(list, recommendFileName);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    FileUtil.deleteFile(recommendFilePath);
                                    FileUtil.createFile(recommendFileName);
                                    FileUtil.writeTxtFile(list, recommendFileName);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            refreshPriorityToViewPager(videoInfoList, scrollView, linearLayout, relativeLayout, rotationImageView);

                        }
                    });
                }).start();
            }
        }

        //获取其他栏目推荐的内容
        public void getVideoListOfPriorityCate(String cateId, PullToRefreshScrollView
                scrollView, LinearLayout linearLayout, RelativeLayout relativeLayout, RotationImageView
                                                       rotationImageView) {
            new Thread(() -> {
                String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/ContentApi-getContentList.action?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&returnType=json&orderType=priority&cateId="
                        + cateId + "&memberName=" + memberName;
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
                        List<VideoInfo> videoInfoList = convertVideoFromJson(response.body().string());
                        refreshPriorityToViewPager(videoInfoList, scrollView, linearLayout, relativeLayout, rotationImageView);
                        response.body().close();
                    }
                });
            }).start();
        }

        //根据栏目ID获取视频列表
        public void getVideoListByCateId(String cateId, PullToRefreshScrollView
                scrollView, VideoListAdapter adapter, boolean isPaging, int pageNo, int type) {
            String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/ContentApi-getContentList.action?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&returnType=json&cateId="
                    + cateId + "&memberName=" + memberName + "&isPaging=" + isPaging + "&size=10" + "&pageNo=" + pageNo;
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
                        //将视频信息json转成类对象
                        List<VideoInfo> videoInfoList = convertPagingVideoFromJson(response.body().string());
                        refreshVideoListToGridView(videoInfoList, scrollView, adapter);
                        response.body().close();
                        Message msg = new Message();
                        msg.what = type;
                        handler.sendMessage(msg);
                    }
                });
            }).start();
        }

        //根据站点id获取视频列表
        public void getVideoListBySiteId(PullToRefreshScrollView scrollView, VideoListAdapter
                adapter, boolean isPaging, int pageNo, int type) {
            if (!isNetworkAvailable(MainActivity.this)) {
                String sdCard = Environment.getExternalStorageState();
                if (sdCard.equals(Environment.MEDIA_MOUNTED)) {
                    FileUtil.createSDCardDir();
                    if (listFileName.exists()) {
                        try {
                            String list = FileUtil.readTxtFile(listFileName);
                            List<VideoInfo> videoInfoList = convertPagingVideoFromJson(list);
                            refreshVideoListToGridView(videoInfoList, scrollView, adapter);
                            Message msg = new Message();
                            msg.what = type;
                            handler1.sendMessage(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                new Thread(() -> {
                    String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/ContentApi-getContentList.action?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&returnType=json&siteId="
                            + netValue.getSiteId() + "&memberName=" + memberName + "&isPaging=" + isPaging + "&size=10" + "&pageNo=" + pageNo;
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
                            String list = response.body().string();
                            response.body().close();
                            List<VideoInfo> videoInfoList = convertPagingVideoFromJson(list);
                            boolean create = false;
                            try {
                                create = FileUtil.createFile(listFileName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (create) {
                                try {
                                    FileUtil.writeTxtFile(list, listFileName);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    FileUtil.deleteFile(listFilePath);
                                    FileUtil.createFile(listFileName);
                                    FileUtil.writeTxtFile(list, listFileName);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            refreshVideoListToGridView(videoInfoList, scrollView, adapter);

                            Message msg = new Message();
                            msg.what = type;
                            handler1.sendMessage(msg);
                        }
                    });
                }).start();
            }
        }

        //将视频列表由json格式转换成类数组
        public List<VideoInfo> convertVideoFromJson(String json) {
            Gson gson = new Gson();
            List<VideoInfo> videoInfoList = gson.fromJson(json, new TypeToken<List<VideoInfo>>() {
            }.getType());
            return videoInfoList;
        }

        //将视频列表由json格式转换成类数组（分页）
        public List<VideoInfo> convertPagingVideoFromJson(String json) {
            Gson gson = new Gson();
            PagingVideoInfo pagingVideoInfo = gson.fromJson(json, new TypeToken<PagingVideoInfo>() {
            }.getType());
            currentTotalCount = pagingVideoInfo.getPageCount();
            List<VideoInfo> videoInfoList = pagingVideoInfo.getInfo();
            return videoInfoList;
        }

        //更新视频列表到gridview
        public void refreshVideoListToGridView
        (List<VideoInfo> videoInfoList, PullToRefreshScrollView scrollView, VideoListAdapter
                adapter) {
            MainActivity.this.runOnUiThread(() -> {
                for (int i = 0; i < videoInfoList.size(); ++i) {
                    adapter.add(videoInfoList.get(i));
                }
                scrollView.onRefreshComplete();
            });
        }

        //更新推荐视频到轮播图控件
        public void refreshPriorityToViewPager
        (List<VideoInfo> videoInfoList, PullToRefreshScrollView scrollView, LinearLayout
                linearLayout, RelativeLayout relativeLayout, RotationImageView rotationImageView) {
            if (videoInfoList.size() == 0) {
                View view = scrollView;
                MainActivity.this.runOnUiThread(() -> relativeLayout.setVisibility(View.GONE));
                MainActivity.this.runOnUiThread(() -> linearLayout.getChildAt(0).setVisibility(View.VISIBLE));
                return;
            } else if (videoInfoList.size() == 1) {
                rotationImageView.getmImageList().add(videoInfoList.get(0));
                MainActivity.this.runOnUiThread(() -> linearLayout.getChildAt(0).setVisibility(View.GONE));
            } else {
                MainActivity.this.runOnUiThread(() -> linearLayout.getChildAt(0).setVisibility(View.GONE));
                rotationImageView.getmImageList().add(videoInfoList.get(videoInfoList.size() - 1));
                for (int i = 0; i < videoInfoList.size(); ++i) {
                    rotationImageView.getmImageList().add(videoInfoList.get(i));
                }
                rotationImageView.getmImageList().add(videoInfoList.get(0));
                linearLayout.getChildAt(0).setVisibility(View.GONE);
            }
            MainActivity.this.runOnUiThread(() -> rotationImageView.getmPriorityAdapter().notifyDataSetChanged());
            MainActivity.this.runOnUiThread(() -> rotationImageView.initCircles());
            MainActivity.this.runOnUiThread(() -> scrollView.onRefreshComplete());
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

    public void filter(List<CategoryInfo> list) {
        int i = 0;
        int j = 0;
        CategoryInfo fPage = new CategoryInfo("", "", "", "", "首页");
        mList.add(fPage);
        mAdapter.getCount();
        mAdapter.getPageTitle(j);
        while (i < list.size()) {
            CategoryInfo categoryInfo = list.get(i);
            if (categoryInfo.getSiteId().equals(netValue.getSiteId())) {
                mList.add(categoryInfo);
                mAdapter.getCount();
                mAdapter.getPageTitle(j + 1);
                ++j;
            }
            i++;
        }
        MainActivity.this.runOnUiThread(() -> mAdapter.notifyDataSetChanged());
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