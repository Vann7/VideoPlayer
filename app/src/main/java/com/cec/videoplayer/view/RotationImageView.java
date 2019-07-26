package com.cec.videoplayer.view;

import android.content.Context;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cec.videoplayer.R;
import com.cec.videoplayer.adapter.PriorityVideoAdapter;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

public class RotationImageView {
    private Context mContext;
    private ViewPager mViewPager;//轮播图view
    private LinearLayout mLinearLayout;//用来添加小圆圈

    private PriorityVideoAdapter mPriorityAdapter;//ViewPager对应的adapter
    private Handler mHandler;//用于循环播放
    private List<Object> mImageList;//存储图片列表
    private List<ImageView> mCircleImageViews;//存储小圆圈图片的view


    int mIntervalMS;//播放间隔，单位ms

    //构造函数
    public RotationImageView(Context context, ViewPager viewPager, LinearLayout linearLayout, int intervalMS){
        mContext = context;
        mViewPager = viewPager;
        mLinearLayout = linearLayout;
        mIntervalMS = intervalMS;
        initAdapter();
        initHandler();
        addOnImageChangeListener();
    }
    //初始化adapter
    private void initAdapter(){
        mImageList = new ArrayList<>();
        mPriorityAdapter = new PriorityVideoAdapter(mContext, mImageList);
        mViewPager.setAdapter(mPriorityAdapter);
    }
    //添加图片改变监听事件
    public void addOnImageChangeListener(){
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int currentPosition;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            //当选中某个页面的时候,把当前的小圆点背景变成绿色
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                for(int i = 0; i < mCircleImageViews.size(); ++i){
                    if(i == (position - 1) % mCircleImageViews.size()){
                        mCircleImageViews.get(i).setImageResource(R.drawable.select_circle);
                    }
                    else{
                        mCircleImageViews.get(i).setImageResource(R.drawable.un_select_circle);
                    }
                }
                if(position == 0){
                    mCircleImageViews.get(mCircleImageViews.size() - 1).setImageResource(R.drawable.select_circle);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state != ViewPager.SCROLL_STATE_IDLE) return;

                // 当视图在第一个时，将页面号设置为图片的最后一张。
                if (currentPosition == 0) {
                    mViewPager.setCurrentItem(mImageList.size() - 2, false);

                } else if (currentPosition == mImageList.size() - 1) {
                    // 当视图在最后一个是,将页面号设置为图片的第一张。
                    mViewPager.setCurrentItem(1, false);
                }
            }
        });
    }
    //设置定时播放
    private void initHandler(){
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0){
                    //viewPager显示下一页
                    int currentItem;
                    if(mViewPager.getCurrentItem() >= mImageList.size() - 2){
                        currentItem = 1;
                    }
                    else{
                        currentItem = mViewPager.getCurrentItem() + 1;
                    }
                    mViewPager.setCurrentItem(currentItem);
                    //再次发送延时消息
                    mHandler.sendEmptyMessageDelayed(0, mIntervalMS);
                }
            }
        };
    }
    //初始化小圆圈
    public void initCircles(){
        //1.需要一个集合记录一下小圆点的imageView控件
        mCircleImageViews = new ArrayList<ImageView>();
        //2...linearLayout上面的视图清空一下再去添加
        mLinearLayout.removeAllViews();
        if(mImageList.size() == 1){
            mViewPager.setCurrentItem(0);
        }
        else if(mImageList.size() >= 2){
            for (int i = 0;i < mImageList.size() - 2; ++i){
                ImageView imageView = new ImageView(mContext);
                if (i == 0){
                    imageView.setImageResource(R.drawable.select_circle);
                }else {
                    imageView.setImageResource(R.drawable.un_select_circle);
                }
                //添加到集合去
                mCircleImageViews.add(imageView);
                //添加到线性布局上
                //这是布局参数,,刚开始小圆点之间没有距离,所以使用java代码指定宽度高度,并且指定小圆点之间的距离
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20,20);
                params.setMargins(5,0,5,0);
                mLinearLayout.addView(imageView, params);
            }
            mViewPager.setCurrentItem(1);
            mHandler.sendEmptyMessageDelayed(0, mIntervalMS);
        }
    }

    public ViewPager getmViewPager() {
        return mViewPager;
    }

    public void setmViewPager(ViewPager mViewPager) {
        this.mViewPager = mViewPager;
    }

    public LinearLayout getmLinearLayout() {
        return mLinearLayout;
    }

    public void setmLinearLayout(LinearLayout mLinearLayout) {
        this.mLinearLayout = mLinearLayout;
    }

    public Handler getmHandler() {
        return mHandler;
    }

    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public List<Object> getmImageList() {
        return mImageList;
    }

    public void setmImageList(List<Object> mImageList) {
        this.mImageList = mImageList;
    }

    public List<ImageView> getmCircleImageViews() {
        return mCircleImageViews;
    }

    public void setmCircleImageViews(List<ImageView> mCircleImageViews) {
        this.mCircleImageViews = mCircleImageViews;
    }

    public int getmIntervalMS() {
        return mIntervalMS;
    }

    public void setmIntervalMS(int mIntervalMS) {
        this.mIntervalMS = mIntervalMS;
    }

    public PriorityVideoAdapter getmPriorityAdapter() {
        return mPriorityAdapter;
    }

    public void setmPriorityAdapter(PriorityVideoAdapter mPriorityAdapter) {
        this.mPriorityAdapter = mPriorityAdapter;
    }
}
