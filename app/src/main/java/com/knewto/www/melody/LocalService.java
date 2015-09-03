package com.knewto.www.melody;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Random;

/**
 * Created by willwallis on 8/25/15.
 * Based on Google sample code, testing bound services.
 */
public class LocalService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();
    // Broadcast object
    Intent testIntent;
    private final Handler handler = new Handler();
    int counter = 0;


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocalService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocalService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public void onCreate() {
        super.onCreate();
        // create broadcast
        testIntent = new Intent("test_broadcast");
        handler.removeCallbacks(broadcastUpdates);
        handler.post(broadcastUpdates);

    }

    public Runnable broadcastUpdates = new Runnable() {
        public void run() {
                testIntent.putExtra("test_extra", counter);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(testIntent);
                counter++;
                handler.postDelayed(this, 500);
        }
    };

    /** method for clients */
    public int getRandomNumber() {
        return mGenerator.nextInt(100);
    }

}