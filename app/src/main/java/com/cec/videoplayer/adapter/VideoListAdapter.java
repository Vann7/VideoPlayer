package com.cec.videoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.module.NetValue;
import com.cec.videoplayer.module.VideoInfo;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoListAdapter extends ArrayAdapter<VideoInfo> {
    private Context mContext;
    private int mResourceId;
    private NetValue netValue = new NetValue();
    private View view1;
    private View view2;

    public VideoListAdapter(Context context, int resourceId, List<VideoInfo> videoList) {
        super(context, resourceId, videoList);
        mContext = context;
        mResourceId = resourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VideoInfo video = getItem(position);
        View view;
        VideoHolder videoHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(mResourceId, parent, false);
            videoHolder = new VideoHolder();
            videoHolder.videoImage = view.findViewById(R.id.video_image);
            videoHolder.videoTitle = view.findViewById(R.id.video_title);
            view1 = view.findViewById(R.id.view1);
            view2 = view.findViewById(R.id.view2);
            int screenWidthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
            ViewGroup.LayoutParams params1 = videoHolder.videoImage.getLayoutParams();
            ViewGroup.LayoutParams params2 = view1.getLayoutParams();
            ViewGroup.LayoutParams params3 = view2.getLayoutParams();
            params1.height = (screenWidthPixels - 50) / 32 * 9;
            params2.height = (screenWidthPixels - 50) / 32 * 9 + 10;
            params3.height = (screenWidthPixels - 50) / 32 * 9 + 10;
            videoHolder.videoImage.setLayoutParams(params1);
            view1.setLayoutParams(params2);
            view2.setLayoutParams(params3);
            view.setTag(videoHolder);
        } else {
            view = convertView;
            videoHolder = (VideoHolder) view.getTag();
        }
        videoHolder.videoTitle.setText(video.getTitle());
        Picasso.with(getContext()).load("http://" + netValue.getIp() + ":" + netValue.getPort() + "/powercms/" + video.getImage()).into(videoHolder.videoImage);
        return view;
    }

    class VideoHolder {
        ImageView videoImage;
        TextView videoTitle;
    }
}
