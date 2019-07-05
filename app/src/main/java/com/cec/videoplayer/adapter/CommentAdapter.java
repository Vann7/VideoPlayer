package com.cec.videoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.cec.videoplayer.holder.RelateHolder;
import com.cec.videoplayer.module.Comment;
import com.cec.videoplayer.module.Relate;

import java.util.List;

/**
 * User: cec
 * Date: 2019/7/4
 * Time: 10:09 AM
 */
public class CommentAdapter extends BaseAdapter {
    private List<Comment> mList;
    private LayoutInflater mInflater; //布局装载器对象
    private Context mContext;
    private RelateHolder viewHolder;
    private ListView mListView;
    private OnListClickListener mListener;

    public CommentAdapter(List<Comment> rList, Context rContext, ListView rListView) {
        mInflater = LayoutInflater.from(rContext);
        this.mList = rList;
        this.mContext = rContext;
        this.mListView = rListView;

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            if (mListener != null) {
                mListener.onClick(mList.get(position), position);
            }
        });

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void setOnListClickListener(OnListClickListener mListener) {
        this.mListener = mListener;
    }


    public void onDateChange(List<Comment> relateList) {
        mList = relateList;
        this.notifyDataSetChanged();
    }


    /**
     * listView点击Listener接口
     */
    public interface OnListClickListener {
        void onClick(Comment comment, int position);
    }

}
