package com.cec.videoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.holder.RelateHolder;
import com.cec.videoplayer.module.Relate;

import java.util.List;

/**
 * User: cec
 * Date: 2019/7/4
 * Time: 10:09 AM
 */
public class RelateAdapter extends BaseAdapter {
    private List<Relate> mList;
    private LayoutInflater mInflater; //布局装载器对象
    private Context mContext;
    private RelateHolder viewHolder;
    private ListView mListView;
    private OnListClickListener mListener;

    public RelateAdapter(List<Relate> rList, Context rContext, ListView rListView) {
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
            convertView = mInflater.inflate(R.layout.holder_relate, null);
            viewHolder = new RelateHolder(convertView, mContext);
            //通过setTag将convertView与viewHolder关联
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (RelateHolder) convertView.getTag();
        }
        if (viewHolder != null) {
            viewHolder.bindData(mList.get(position));
        }


        return convertView;
    }

    public void setOnListClickListener(OnListClickListener mListener) {
        this.mListener = mListener;
    }


    public void onDateChange(List<Relate> relateList) {
        mList = relateList;
        notifyDataSetChanged();
    }


    /**
     * listView点击Listener接口
     */
    public interface OnListClickListener {
        void onClick(Relate relate, int position);
    }

}
