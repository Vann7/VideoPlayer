package com.cec.videoplayer.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cec.videoplayer.R;
import com.cec.videoplayer.module.Relate;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * User: cec
 * Date: 2019/7/4
 * Time: 10:10 AM
 */
public class RelateHolder {
    private Context mContext;
    private TextView tv_title;
    private ImageView iv_image;

   public RelateHolder(View view, Context context) {
        //对viewHolder的属性进行赋值
        mContext = context;
        tv_title =  view.findViewById(R.id.tv_relate_title);
        iv_image =  view.findViewById(R.id.iv_relate_image);
        view.setTag(this);
    }

    public void  bindData(final Relate relate) {
        tv_title.setText(relate.getTitle());
        // 显示图片
        Picasso.with(mContext)
                .load(relate.getImage())
                    .placeholder(R.mipmap.mis_default_error)
                .centerCrop()
                .resize(60, 60)
                .into(iv_image);

    }
}
