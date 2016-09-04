package com.codecos.gobang;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.codecos.gobang.R;

import java.io.IOException;

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
    public int onStartCommand(Intent intent, int flags, int startId) {
        mp.stop();
        mp.setLooping(true);
        try {
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.seekTo(0);
        mp.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.stop();
    }
 
}