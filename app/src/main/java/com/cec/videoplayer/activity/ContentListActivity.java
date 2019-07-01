package com.cec.videoplayer.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.cec.videoplayer.R;
import com.cec.videoplayer.adapter.ContentAdapter;
import com.cec.videoplayer.module.CategoryInfo;
import com.cec.videoplayer.utlis.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ContentListActivity extends AppCompatActivity implements View.OnClickListener{

    private List<CategoryInfo> categoryInfos;
    private RecyclerView rv_content;
    private ContentAdapter mAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        getNeteData();
        initView();
        initEvent();

    }

    private void initEvent() {
        mAdapter.setOnListClickListener((info, position) -> {
            ToastUtils.showShort(info.getName());
            Intent intent = new Intent(ContentListActivity.this, PlayerActivity.class);
            startActivity(intent);
        });
    }

    private void initView() {
        rv_content = findViewById(R.id.rv_content_list);
        layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_content.setLayoutManager(layoutManager);
        categoryInfos = new ArrayList<>();
        mAdapter = new ContentAdapter(rv_content, this, categoryInfos);
        rv_content.setAdapter(mAdapter);
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
                  String json =  response.body().string();
                  initModel(json);
              }
          });

      }).start();

    }

    public void initModel(String json) {
        Gson gson = new Gson();
        categoryInfos = gson.fromJson(json,new TypeToken<List<CategoryInfo>>() {}.getType());
        //子线程刷新UI
        ContentListActivity.this.runOnUiThread(() -> mAdapter.refresh(categoryInfos));
    }


    @Override
    public void onClick(View v) {

    }
}
