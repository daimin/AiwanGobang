package com.codecos.gobang;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class RobotActivity extends Activity {

    GobangView gbv;
    Button restartBtn = null;
    Button menuBtn = null;
    EditText userEditText = null;
    NetClient netClient = null;

    public static final String USERINFO = "__userinfo"; // 用户信息文件
    public static final String NAME_NOT_ALLOWEDS = "!|\"'@,.><?\\/:;-#$%^&*()";


    public static final int INIT_NET_WHAT_CHECK_NET = 0;
    public static final int INIT_NET_WHAT_USER_INIT = 1;
    public static final int INIT_NET_WHAT_USER_CHECK= 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_robot);

        gbv = (GobangView) this.findViewById(R.id.gobangview);
        gbv.setTextView((TextView) this.findViewById(R.id.text));
        restartBtn = (Button) this.findViewById(R.id.restartBtn);
        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gbv.gameRestart();
            }
        });
        menuBtn = (Button) this.findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnMenu();
            }
        });
        MainActivity.activityList.add(this);

    }


    private void endActivityDialog(String title, String text, String btnText){
        new AlertDialog.Builder(this).setTitle(title)
                .setMessage(text)
                .setPositiveButton(btnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setClass(RobotActivity.this, MainActivity.class);
                        startActivity(intent);
                        RobotActivity.this.finish();
                    }
                }).setCancelable(false)
                .show();
    }


    private void returnMenu(){
         Intent intent = new Intent();
         intent.setClass(this, MainActivity.class);
         startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            returnMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        if(netClient != null){
            netClient.closeConnected();
        }
        super.onStop();
    }
}
