package com.cec.videoplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cec.videoplayer.R;

import com.tangguna.searchbox.library.cache.HistoryCache;
import com.tangguna.searchbox.library.callback.onSearchCallBackListener;
import com.tangguna.searchbox.library.widget.SearchListLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private SearchListLayout searchLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_main);
        searchLayout = (SearchListLayout)findViewById(R.id.searchlayout);
        initData();
    }

    private void initData() {
        List<String> skills = HistoryCache.toArray(getApplicationContext());
//        String shareHotData ="C++,C,PHP,React";
//        List<String> skillHots = Arrays.asList(shareHotData.split(","));
//        searchLayout.initData(skills, skillHots, new onSearchCallBackListener() {

        searchLayout.initData(skills, new onSearchCallBackListener() { //匿名内部类，不是new一个接口
            @Override
            public void Search(String str) {
                //进行或联网搜索
                Bundle bundle = new Bundle();
                bundle.putString("data",str);
                startActivity(SearchResultActivity.class,bundle);
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
                HistoryCache.saveHistory(getApplicationContext(),HistoryCache.toJsonArray(AlloldDataList));
            }
        },1);
    }



    public void startActivity(Class<?> openClass, Bundle bundle) {
        Intent intent = new Intent(this,openClass);
        if (null != bundle)
            intent.putExtras(bundle);
        startActivity(intent);
    }

}
