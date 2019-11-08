package com.cec.videoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.holder.LiveCommentHolder;
import com.cec.videoplayer.model.Comment;

import java.util.List;

public class LiveCommentAdapter extends BaseAdapter {
    private List<Comment> mList;
    private LayoutInflater mInflater; //布局装载器对象
    private Context mContext;
    private LiveCommentHolder viewHolder;
    private ListView mListView;
    private LiveCommentAdapter.OnListClickListener mListener;

    public LiveCommentAdapter(List<Comment> rList, Context rContext, ListView rListView) {
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
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // 由于我们只需要将XML转化为View，并不涉及到具体的布局，所以第二个参数通常设置为null
            convertView = mInflater.inflate(R.layout.live_comment_item, null);
            viewHolder = new LiveCommentHolder(convertView, mContext);
            //通过setTag将convertView与viewHolder关联
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (LiveCommentHolder) convertView.getTag();
        }
        if (viewHolder != null) {
            viewHolder.bindData(mList.get(position));
        }
        return convertView;
    }

    public void setOnListClickListener(LiveCommentAdapter.OnListClickListener mListener) {
        this.mListener = mListener;
    }


    public void onDateChange(List<Comment> commentList) {
        mList = commentList;
        this.notifyDataSetChanged();
    }


    /**
     * listView点击Listener接口
     */
    public interface OnListClickListener {
        void onClick(Comment comment, int position);
    }
}
