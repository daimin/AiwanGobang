package com.codecos.gobang;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.io.IOException;

import static com.codecos.gobang.R.layout;


public class LoginActivity extends Activity {

    private Button loginBtn;
    private EditText nameET;
    private NetClient netClient;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layout.activity_login);
        MainActivity.activityList.add(this);

        if(!NetClient.isNetworkAvailable(LoginActivity.this)){
            showNetDialog("网络无法使用！");
        }



        new Thread(){
            @Override
            public void run() {
                super.run();
                Looper.prepare();
                try {
                    LoginActivity.this.netClient = new NetClient(LoginActivity.this, LoginActivity.this.mHandler);
                    LoginActivity.this.mHandler = new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if(msg.what == 1){
                                Bundle data = msg.getData();
                                LoginActivity.this.netClient.send(NetClient.C2L_LOGIN, data.getString("login_name"), 1);
                            }
                        }
                    };
                } catch (IOException e) {
                    showNetDialog("服务器无法连接！");
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();

        nameET = (EditText) this.findViewById(R.id.nameET);

        loginBtn = (Button) this.findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginName = nameET.getText().toString();
                if("".equals(loginName)){
                    Toast.makeText(LoginActivity.this, "登录名不能为空", Toast.LENGTH_LONG).show();
                }else{
                    Message message = LoginActivity.this.mHandler.obtainMessage(1);
                    Bundle data = new Bundle();
                    data.putString("login_name", loginName);
                    message.setData(data);
                    LoginActivity.this.mHandler.sendMessage(message);
                }
            }


        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Intent intent = new Intent();
            intent.setClass(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showNetDialog(String message){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(LoginActivity.this);
        normalDialog.setIcon(android.R.drawable.ic_dialog_alert);
        normalDialog.setTitle("网络问题");
        normalDialog.setMessage(message);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        LoginActivity.this.finish();
                    }
                });
        // 显示
        normalDialog.show();
    }

}
