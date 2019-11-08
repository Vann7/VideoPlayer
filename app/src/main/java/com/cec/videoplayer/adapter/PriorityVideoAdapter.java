package com.cec.videoplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.cec.videoplayer.activity.PlayerActivity;
import com.cec.videoplayer.activity.VrVideoActivity;
import com.cec.videoplayer.model.ContentInfo;
import com.cec.videoplayer.model.VideoInfo;
import com.cec.videoplayer.model.NetValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PriorityVideoAdapter extends PagerAdapter {
    public List<Object> getmList() {
        return mList;
    }

    public void setmList(List<Object> mList) {
        this.mList = mList;
    }

    private List<Object> mList;
    private Context mContext;
    private String contentInfoStr;
    public PriorityVideoAdapter(Context context, List<Object> list) {
        super();
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position){
        if(mList.size() == 0){
            return null;
        }
        ImageView imageView = new ImageView(mContext);
        container.addView(imageView);
        VideoInfo video = (VideoInfo)mList.get(position);
        NetValue netValue = new NetValue();
        Picasso.with(mContext).load("http://" + netValue.getIp() + ":" + netValue.getPort()+"/powercms/" + video.getImage()).into(imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/api/ContentApi-getContentInfo.action" +
                        "?userName=superuser&token=3dfcacea492d36be4a5b949e291823d9&returnType=json&size=10&" +
                        "contentId=" + video.getId();

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
                            Toast.makeText(mContext, "无网络连接，请检查网络设置。", Toast.LENGTH_LONG).show();
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
                            ContentInfo contentInfo = infos.get(0);
                            if (contentInfo.getVideo360() == 1) {
                                Intent playIntent = new Intent(mContext, VrVideoActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("id", contentInfo.getId());
                                bundle.putString("contentInfoStr", contentInfoStr);
                                playIntent.putExtras(bundle);
                                mContext.startActivity(playIntent);
                            } else {
                                Intent playIntent = new Intent(mContext, PlayerActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("contentInfoStr", contentInfoStr);
                                bundle.putString("id", video.getId());
                                playIntent.putExtras(bundle);
                                mContext.startActivity(playIntent);
                            }
                        }

                    });
                }).start();
            }
        });
        return imageView;
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((View) object);
    }
}
