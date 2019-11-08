package com.cec.videoplayer.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.adapter.VideoListAdapter;
import com.cec.videoplayer.model.VideoInfo;

import java.util.ArrayList;
import java.util.List;

import static com.cec.videoplayer.R.drawable.button_shape;

public class VideoScrollView {
    Context mContext;
    PullToRefreshScrollView mScrollView;//滚动控件
    LinearLayout mLinearLayout;
    RelativeLayout mRelativeLayout;
    RotationImageView mRotationImageView;
    VideoListGridView mVideoListGridView;
    VideoListAdapter mVideoListAdapter;
    public VideoScrollView(Context context) {
        mContext = context;
    }

    public void initScrollView() {
        mScrollView = new PullToRefreshScrollView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mScrollView.setLayoutParams(layoutParams);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setPadding(0,0,0,0);
    }

    public void initScrollViewChildren() {
        int screenWidthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
        int height = screenWidthPixels / 16 * 9;
        //初始化LinearLayout
        mLinearLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mLinearLayout.setLayoutParams(layoutParams);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        //初始化RelativeLayout
        mRelativeLayout = new RelativeLayout(mContext);
        LinearLayout.LayoutParams relativeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
        relativeParams.setMargins(0, 0, 0, 20);
        mRelativeLayout.setLayoutParams(relativeParams);
        //初始化轮播图控件即ViewPager和LinearLayout
        //ViewPager
        ViewPager viewPager = new ViewPager(mContext);

        LinearLayout.LayoutParams viewPagerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
        viewPager.setLayoutParams(viewPagerParams);
        //LinearLayout
        LinearLayout linearLayout = new LinearLayout(mContext);
        RelativeLayout.LayoutParams linearParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        linearParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        linearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        linearParams.setMargins(0, 0, 0, 50);
        linearLayout.setLayoutParams(linearParams);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mRotationImageView = new RotationImageView(mContext, viewPager, linearLayout, 5000);
        //初始化视频列表控件
        mVideoListGridView = new VideoListGridView(mContext);
        LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, 0);
        mVideoListGridView.setLayoutParams(videoParams);
        mVideoListGridView.setGravity(Gravity.CENTER);
        mVideoListGridView.setHorizontalSpacing(10);
        mVideoListGridView.setVerticalSpacing(50);
        mVideoListGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        mVideoListGridView.setNumColumns(2);
        //初始化视频列表adapter
        List<VideoInfo> videoList = new ArrayList<>();
        mVideoListAdapter = new VideoListAdapter(mContext, R.layout.video_item, videoList);
        mVideoListGridView.setAdapter(mVideoListAdapter);
        mVideoListGridView.setPadding(20,0,20,0);

        //初始化提示页
        LinearLayout netLinearLayout = new LinearLayout(mContext);
        netLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        netLinearLayout.setOrientation(LinearLayout.VERTICAL);
        netLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        netLinearLayout.setPadding(0,500,0,0);
        ImageView image = new ImageView(mContext);
        image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        image.setImageResource(R.drawable.error);
        TextView text = new TextView(mContext);
        text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setText("无网络连接,请检查网络设置！");
        text.setPadding(0,15,0,60);
        Button button = new Button(mContext);
        button.setLayoutParams(new LinearLayout.LayoutParams(180, 65));
        button.setBackgroundResource(button_shape);
        button.setText("点击重试");
        ColorStateList colorStateList=mContext.getResources().getColorStateList(R.color.seagreen);
        button.setTextColor(colorStateList);
        button.setPadding(0,0,0,0);
        netLinearLayout.addView(image);
        netLinearLayout.addView(text);
        netLinearLayout.addView(button);
        netLinearLayout.setVisibility(View.GONE);

        //初始化占位layout
        LinearLayout placeHolder= new LinearLayout(mContext);
        placeHolder.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        placeHolder.setOrientation(LinearLayout.VERTICAL);
        placeHolder.setPadding(0,10,0,0);
        placeHolder.setVisibility(View.GONE);


        //组织scrollView中孩子们的父子关系
        mRelativeLayout.addView(viewPager);
        mRelativeLayout.addView(linearLayout);
        mLinearLayout.addView(placeHolder);
        mLinearLayout.addView(mRelativeLayout);
        mLinearLayout.addView(mVideoListGridView);
        mLinearLayout.addView(netLinearLayout);
        mScrollView.addView(mLinearLayout);

    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public PullToRefreshScrollView getmScrollView() {
        return mScrollView;
    }

    public void setmScrollView(PullToRefreshScrollView mScrollView) {
        this.mScrollView = mScrollView;
    }

    public LinearLayout getmLinearLayout() {
        return mLinearLayout;
    }

    public void setmLinearLayout(LinearLayout mLinearLayout) {
        this.mLinearLayout = mLinearLayout;
    }

    public RelativeLayout getmRelativeLayout() {
        return mRelativeLayout;
    }

    public void setmRelativeLayout(RelativeLayout mRelativeLayout) {
        this.mRelativeLayout = mRelativeLayout;
    }

    public RotationImageView getmRotationImageView() {
        return mRotationImageView;
    }

    public void setmRotationImageView(RotationImageView mRotationImageView) {
        this.mRotationImageView = mRotationImageView;
    }

    public VideoListGridView getmVideoListGridView() {
        return mVideoListGridView;
    }

    public void setmVideoListGridView(VideoListGridView mVideoListGridView) {
        this.mVideoListGridView = mVideoListGridView;
    }

    public VideoListAdapter getmVideoListAdapter() {
        return mVideoListAdapter;
    }

    public void setmVideoListAdapter(VideoListAdapter mVideoListAdapter) {
        this.mVideoListAdapter = mVideoListAdapter;
    }
}
