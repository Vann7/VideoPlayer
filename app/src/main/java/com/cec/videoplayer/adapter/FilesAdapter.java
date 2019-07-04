package com.cec.videoplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cec.videoplayer.R;
import com.cec.videoplayer.module.CategoryInfo;
import com.cec.videoplayer.module.File;
import com.cec.videoplayer.module.Relate;

import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    private List<File> mList;
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
            tv_name=view.findViewById(R.id.content_file_name);
        }
    }

    public FilesAdapter(RecyclerView recyclerview, Context context, List<File> list) {
        mInflater = LayoutInflater.from(context);
        this.mList = list;
        this.rv_content = recyclerview;
        mContext = context;
    }


    public void onDateChange(List<File> fileList) {
        mList = fileList;
        notifyDataSetChanged();
    }

    public void setOnListClickListener(OnListClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        holder.tv_name.setText("第" + ++position + "集");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int ViewType){
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.content_file,parent,false);
        ViewHolder holder=new ViewHolder(view);
        holder.itemView.setOnClickListener( v -> {
            mListener.onClick(v, holder.getLayoutPosition());
        });
        return holder;
    }

    @Override
    public int getItemCount(){
        return mList.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }



    /**
     * 点击Listener接口
     */
    public interface OnListClickListener {
        void onClick(View v, int position);
    }

}
