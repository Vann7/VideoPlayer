package com.cec.videoplayer.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cec.videoplayer.module.VideoInfo;
import com.squareup.picasso.Picasso;
import com.cec.videoplayer.service.NetService;

import java.util.List;

public class PriorityVideoAdapter extends PagerAdapter {
    public List<Object> getmList() {
        return mList;
    }

    public void setmList(List<Object> mList) {
        this.mList = mList;
    }

    private List<Object> mList;
    private Context mContext;
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
        NetService netService = new NetService();
        Picasso.with(mContext).load("http://" + netService.getIp() + ":" + netService.getPort()+"/powercms/" + video.getImage()).into(imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
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
