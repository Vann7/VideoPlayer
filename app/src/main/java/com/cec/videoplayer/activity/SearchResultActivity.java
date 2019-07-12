package com.cec.videoplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.cec.videoplayer.R;

public class SearchResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        TextView textView =(TextView) findViewById(R.id.tv_show);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            textView.setText(bundle.getString("data"));  //bundle.getString("data")为搜索框中输入的文字
        }

    }
}
