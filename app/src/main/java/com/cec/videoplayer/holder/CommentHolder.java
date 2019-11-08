package com.cec.videoplayer.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.model.Comment;
import com.cec.videoplayer.model.Relate;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class CommentHolder {
    private Context mContext;
    private TextView tv_user;
    private TextView tv_content;
    private TextView tv_time;

    public CommentHolder(View view, Context context) {
        //对viewHolder的属性进行赋值
        mContext = context;
        tv_user = view.findViewById(R.id.comment_item_userName);
        tv_content = view.findViewById(R.id.comment_item_content);
        tv_time = view.findViewById(R.id.comment_create_time);
        view.setTag(this);
    }

    public void bindData(final Comment comment) {
        tv_user.setText(comment.getUserName());
        try {
            String content = URLDecoder.decode(comment.getContent(), "UTF-8");
            tv_content.setText(content);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String date = comment.getCreateTime().toString();
        String year = date.substring(date.length() - 4);
        String month = getMonth(date);
        String createTime = year + "-" + month + "-" + date.substring(8, 16);
        tv_time.setText(createTime);

        // 显示图片
//        Picasso.with(mContext)
//                .load(comment.getImage())
//                .placeholder(R.mipmap.mis_default_error)
//                .centerCrop()
//                .resize(60, 60)
//                .into(iv_image);

    }

    public String getMonth(String date) {
        String a = date.substring(4, 7);
        String month = "";
        if (a.equals("Jan")) {
            month = "01";
        } else if (a.equals("Feb")) {
            month = "02";
        } else if (a.equals("Mar")) {
            month = "03";
        } else if (a.equals("Apr")) {
            month = "04";
        } else if (a.equals("May")) {
            month = "05";
        } else if (a.equals("Jun")) {
            month = "06";
        } else if (a.equals("Jul")) {
            month = "07";
        } else if (a.equals("Aug")) {
            month = "08";
        } else if (a.equals("Sep")) {
            month = "09";
        } else if (a.equals("Oct")) {
            month = "10";
        } else if (a.equals("Nov")) {
            month = "11";
        } else if (a.equals("Dec")) {
            month = "12";
        }
        return month;
    }
}
