package com.cec.videoplayer.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.module.Comment;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class LiveCommentHolder {
    private Context mContext;
    private TextView tv_user;
    private TextView tv_content;
    private TextView tv_time;

    public LiveCommentHolder(View view, Context context) {
        //对viewHolder的属性进行赋值
        mContext = context;
        tv_user = view.findViewById(R.id.live_comment_item_userName);
        tv_content = view.findViewById(R.id.live_comment_item_content);
        view.setTag(this);
    }

    public void bindData(final Comment comment) {
        tv_user.setText(comment.getUserName()+"：");
        try {
            String content = URLDecoder.decode(comment.getContent(), "UTF-8");
            tv_content.setText(content);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String date = comment.getCreateTime().toString();
        String year = date.substring(date.length() - 4);

        // 显示图片
//        Picasso.with(mContext)
//                .load(comment.getImage())
//                .placeholder(R.mipmap.mis_default_error)
//                .centerCrop()
//                .resize(60, 60)
//                .into(iv_image);

    }


}
