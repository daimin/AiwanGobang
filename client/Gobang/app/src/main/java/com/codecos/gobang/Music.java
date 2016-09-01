package com.codecos.gobang;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.codecos.gobang.R;

public class Music extends Service {

    private MediaPlayer mp;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mp=MediaPlayer.create(this, R.raw.bj);
 
    }
    @Override  
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mp.start(); 
    } 
    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.stop();
    }
 
}