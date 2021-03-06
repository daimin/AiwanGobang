package com.codecos.gobang;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

import com.codecos.gobang.R;


public class NetActivity extends Activity {

    GobangNetView gbv;
    Button restartBtn = null;
    Button menuBtn = null;
    EditText userEditText = null;
    NetClient netClient = null;
    InitNetHandler mInitNetHandler = null;

    public static final String USERINFO = "___userinfo"; // 用户信息文件
    public static final String NAME_NOT_ALLOWEDS = "!|\"'@,.><?\\/:;-#$%^&*()";


    public static final int INIT_NET_WHAT_CHECK_NET = 0;
    public static final int INIT_NET_WHAT_USER_INIT = 1;
    public static final int INIT_NET_WHAT_USER_CHECK= 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_net);

        gbv = (GobangNetView) this.findViewById(R.id.gobangnetview);
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
        mInitNetHandler = new InitNetHandler(this);
        mInitNetHandler.sendEmptyMessage(NetActivity.INIT_NET_WHAT_CHECK_NET);

    }


    private void checkIsNetOn(){
        if(!NetClient.isNetworkAvailable(this)){
                // 提示框方法
            endActivityDialog("提示", "网络不可用，请打开网络再试^_^", "返回");
        }else{
            new Thread(){
                @Override
                public void run() {
                    try {
                        netClient = new NetClient(NetActivity.this, mInitNetHandler);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    netClient.send(NetClient.C2L_VERSION, NetClient.PROTO_VERSION, INIT_NET_WHAT_USER_INIT);
                }
            }.start();
        }

    }

    private void endActivityDialog(String title, String text, String btnText){
        new AlertDialog.Builder(this).setTitle(title)
                .setMessage(text)
                .setPositiveButton(btnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setClass(NetActivity.this, MainActivity.class);
                        startActivity(intent);
                        NetActivity.this.finish();
                    }
                }).setCancelable(false)
                .show();
    }


    private void initUser(){
        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            fis = this.openFileInput(USERINFO);
            fos = this.openFileOutput(USERINFO, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Log.i(this.getClass().getSimpleName(), e.getMessage());
            userEditText = new EditText(this);
            Intent intent = new Intent();
            intent.setClass(NetActivity.this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        }finally {
            try {
                if(fos != null)
                    fos.close();
                if(fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class InitNetHandler extends Handler{
        private NetActivity client = null;
        public InitNetHandler(NetActivity client) {
            this.client = client;
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == INIT_NET_WHAT_CHECK_NET){
                this.client.checkIsNetOn();
            } else if(msg.what == INIT_NET_WHAT_USER_INIT){
                this.client.initUser();
            } else if(msg.what == INIT_NET_WHAT_USER_CHECK){
                Bundle data = msg.getData();
                String username = data.getString("username");

            }

        }
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
