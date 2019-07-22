package com.cec.videoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import com.cec.videoplayer.R;
import com.cec.videoplayer.activity.TabActivity;
import com.cec.videoplayer.module.VideoInfo;
import com.cec.videoplayer.service.NetService;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class VideoListAdapter extends ArrayAdapter<VideoInfo> {
    private Context mContext;
    private int mResourceId;
    private NetService netService=new NetService();
    public VideoListAdapter(Context context, int resourceId, List<VideoInfo> videoList){
        super(context, resourceId, videoList);
        mContext = context;
        mResourceId = resourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        VideoInfo video = getItem(position);
        View view;
        VideoHolder videoHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(mResourceId, parent, false);
            videoHolder = new VideoHolder();
            videoHolder.videoImage = (ImageView) view.findViewById(R.id.video_image);
            videoHolder.videoTitle = (TextView) view.findViewById(R.id.video_title);
            view.setTag(videoHolder);
        }
        else{
            view = convertView;
            videoHolder = (VideoHolder) view.getTag();
        }
        videoHolder.videoTitle.setText(video.getTitle());
        Picasso.with(getContext()).load("http://"+netService.getIp()+":"+netService.getPort()+"/powercms/" + video.getImage()).into(videoHolder.videoImage);
        return view;
    }
    class VideoHolder{
        ImageView videoImage;
        TextView videoTitle;
    }
}
