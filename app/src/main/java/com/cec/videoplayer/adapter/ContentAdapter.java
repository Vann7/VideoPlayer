package com.cec.videoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.module.CategoryInfo;

import java.util.List;

public class ContentAdapter extends BaseAdapter {

    private List<CategoryInfo> mList;
    private ViewHolder viewHolder;
    private LayoutInflater mInflater; //布局装载器对象
    private ListView lv_content;
    private Context mContext;

    private OnListClickListener mListener;


    public ContentAdapter(ListView lv, Context context, List<CategoryInfo> list) {
        mInflater = LayoutInflater.from(context);
        this.mList = list;
        this.lv_content = lv;
        mContext = context;

        lv_content.setOnItemClickListener((parent, view, position, id) -> {
            if (mListener != null) {
                mListener.onClick(mList.get(position), position);
            }
        });
    }

    public void refresh(List<CategoryInfo> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setOnListClickListener(OnListClickListener mListener) {
        this.mListener = mListener;
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
        CategoryInfo info = mList.get(position);
        View itemView = null;
        if (convertView == null) {
            itemView = mInflater.inflate(R.layout.content_item, null);
            viewHolder = new ViewHolder(itemView);

            itemView.setTag(viewHolder);
            convertView = itemView;
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.bindData(info);

        return convertView;
    }

    /**
     * listView点击Listener接口
     */
    public interface OnListClickListener {
        void onClick(CategoryInfo info, int position);
    }



    /**
     * 自定义装备信息view 缓存控件
     */
    class ViewHolder {
        private TextView tv_name;
        private TextView tv_id;
        private TextView tv_siteId;


        public ViewHolder(View view) {
            tv_name = view.findViewById(R.id.content_item_name);
            view.setTag(this);
        }

        public void  bindData(CategoryInfo info) {
            tv_name.setText(info.getName());
        }

    }


}
