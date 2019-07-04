package com.cec.videoplayer.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cec.videoplayer.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn;
    private Button btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.player);
        btn2 = findViewById(R.id.player2);
        btn.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player :
                Intent intent = new Intent(MainActivity.this, ContentListActivity.class);
                startActivity(intent);
                break;
            case R.id.player2 :
                Intent intent2 = new Intent(MainActivity.this, PlayerActivity.class);
                intent2.putExtra("title", "港珠澳大桥是怎样建成的");
                intent2.putExtra("id", "f39c5711667f95430166a9cc32ea0114");
                startActivity(intent2);
                break;
        }
    }
}
