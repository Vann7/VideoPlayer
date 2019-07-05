package com.cec.videoplayer.activity;

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
import android.widget.TextView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.module.CategoryInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tab_layout);
        tabLayout = findViewById(R.id.tablayout);
        vp_pager = findViewById(R.id.tab_viewpager);
        getNeteData();
        initView();
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
            TextView tv = new TextView(TabActivity.this);
            tv.setText("布局" + position);
            tv.setTextSize(30.0f);
            tv.setGravity(Gravity.CENTER);
            (container).addView(tv);
            return tv;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((View) object);
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

    private void getNeteData() {
        new Thread(() -> {
            String url = "http://115.28.215.145:8080/powercms/api/ContentApi-getCategoryInfo.action?userName=demo1&token=f620969ebe7a0634c0aabc1b4fecf1ab&returnType=json";
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            //同步加载
         /* try {
              Response response = client.newCall(request).execute();
              String json =  response.body().string();
              initModel(json);
          } catch (IOException e) {
              e.printStackTrace();
          }*/

            //异步加载
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("load", "onFailure: ");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("load", "onResponse: " + response.body().string());
                    response = client.newCall(request).execute();
                    String json = response.body().string();
                    initModel(json);
                }
            });
        }).start();
    }

    public void initModel(String json) {
        Gson gson = new Gson();
        categoryInfos = gson.fromJson(json, new TypeToken<List<CategoryInfo>>() {
        }.getType());
        filter(categoryInfos);
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
            if (categoryInfo.getSiteId().equals(getResources().getString(R.string.siteId))) {
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