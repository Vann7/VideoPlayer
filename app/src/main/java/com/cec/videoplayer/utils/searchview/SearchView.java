package com.cec.videoplayer.utils.searchview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cec.videoplayer.R;



public class SearchView extends LinearLayout {

    private Context context;

    // 搜索框组件
    private EditText et_search; // 搜索按键
    private LinearLayout search_block; // 搜索框布局
    private LinearLayout result_ll;
    private TextView search_tv; //检索

    public SearchView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();// ->>关注b
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    /**
     *
     * 初始化搜索框
     */
    private void init(){
        search_tv=findViewById(R.id.search_item);
        search_tv.setOnClickListener(v -> {
            checkAndSearch();
        });

    }

    private void checkAndSearch() {
        String name = et_search.getText().toString();
        if (name.trim().equals("")) {
            return;
        }
        search();
    }

    /**
     * 点击搜索按钮检索信息
     */
    public void search() {
        String name = et_search.getText().toString();
        Log.d("SearchView", "search: "+name);

    }

}
