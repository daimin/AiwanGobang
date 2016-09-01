package com.codecos.gobang;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import static com.codecos.gobang.R.layout;


public class AboutActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layout.activity_about);
        MainActivity.activityList.add(this);

    }


}
