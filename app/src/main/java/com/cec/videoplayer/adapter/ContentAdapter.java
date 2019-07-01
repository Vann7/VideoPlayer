package com.cec.videoplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cec.videoplayer.R;
import com.cec.videoplayer.module.CategoryInfo;

import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

    private List<CategoryInfo> mList;
//    private ViewHolder viewHolder;
    private LayoutInflater mInflater; //布局装载器对象
    private RecyclerView rv_content;
    private Context mContext;
    private OnListClickListener mListener;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_name;
        View contentView;
        public ViewHolder(View view){
            super(view);
            contentView=view;
            tv_name=view.findViewById(R.id.content_item_name);
        }
    }

    public ContentAdapter(RecyclerView recyclerview, Context context, List<CategoryInfo> list) {
        mInflater = LayoutInflater.from(context);
        this.mList = list;
        this.rv_content = recyclerview;
        mContext = context;
    }

    public void refresh(List<CategoryInfo> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setOnListClickListener(OnListClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        CategoryInfo categoryInfo=mList.get(position);
        holder.tv_name.setText(categoryInfo.getName());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int ViewType){
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.content_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        holder.contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getAdapterPosition();
                CategoryInfo categoryInfo=mList.get(position);
                Toast.makeText(view.getContext(),"你选择"+categoryInfo.getId(),Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public int getItemCount(){
        return mList.size();
    }

//    @Override
//    public int getCount() {
//        return mList.size();
//    }

//    @Override
//    public Object getItem(int position) {
//        return mList.get(position);
//    }

    @Override
    public long getItemId(int position) {
        return position;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        CategoryInfo info = mList.get(position);
//        View itemView = null;
//        if (convertView == null) {
//            itemView = mInflater.inflate(R.layout.content_item, null);
//            viewHolder = new ViewHolder(itemView);
//
//            itemView.setTag(viewHolder);
//            convertView = itemView;
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
//        viewHolder.bindData(info);
//
//        return convertView;
//    }

    /**
     * listView点击Listener接口
     */
    public interface OnListClickListener {
        void onClick(CategoryInfo info, int position);
    }

}
