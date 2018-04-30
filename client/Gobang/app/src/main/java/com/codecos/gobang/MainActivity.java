package com.codecos.gobang;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.umeng.analytics.game.UMGameAgent;

import java.util.ArrayList;
import java.util.List;

import com.codecos.gobang.R;
import com.codecos.gobang.RobotActivity;


public class MainActivity extends Activity {

    Button robotBtn = null;
    private Button aboutBtn;
    private Button musicBtn;
    private Button netBtn;

    private boolean isMusicOpen = true;

    static List<Activity> activityList = new ArrayList<Activity>();
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        robotBtn = (Button) this.findViewById(R.id.robotBtn);
        robotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, RobotActivity.class);
                // 使每次都使用相同的activity，而不是重新创建
                //FLAG_ACTIVITY_SINGLE_TOP :如果当前栈顶的activity就是要启动的activity,则不会再启动一个新的activity
                //FLAG_ACTIVITY_CLEAR_TOP 删除位于当前activity之上的activity，那么当前activity就在栈顶了
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                MainActivity.this.finish();

            }
        });

        mSharedPreferences = getSharedPreferences("GobangSharedPreferences", 0);

        int music = mSharedPreferences.getInt("music", 1);
        if(mSharedPreferences.getInt("music", 1) > 0) {
            isMusicOpen = true;
        }else{
            isMusicOpen = false;
        }
        musicBtn = (Button) this.findViewById(R.id.musicBtn);
        if(isMusicOpen){
            startService(new Intent(MainActivity.this, Music.class));
            musicBtn.setText("音乐关");
        }else{
            musicBtn.setText("音乐开");
        }
        musicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if(isMusicOpen) {
                    stopService(new Intent(MainActivity.this, Music.class));
                    musicBtn.setText("音乐开");
                    editor.putInt("music", 0);
                }else{
                    startService(new Intent(MainActivity.this, Music.class));
                    musicBtn.setText("音乐关");
                    editor.putInt("music", 1);
                }
                isMusicOpen = !isMusicOpen;
                editor.apply();
            }
        });
        aboutBtn = (Button) this.findViewById(R.id.aboutBtn);
        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        netBtn = (Button)this.findViewById(R.id.netBtn);
        netBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            }
        });


        UMGameAgent.setDebugMode(true);//设置输出运行时日志
        UMGameAgent.init( this );
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            for(Activity activity : activityList) {
               activity.finish();
            }
            stopService(new Intent(MainActivity.this, Music.class));
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void onResume() {
        super.onResume();
        UMGameAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        UMGameAgent.onPause(this);
    }

}
