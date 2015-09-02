package com.knewto.www.melody;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by willwallis on 8/24/15.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    // media player
    private MediaPlayer player;
    // song url
    private Uri songUrl;
    // instance variable representing inner binder class
    private final IBinder musicBind = new MusicBinder();

    // BINDING SECTION
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

   // BINDING TEST
   public int getMillionBucks() {
        return 1000000;
    }

   // MUSIC PLAYER SECTION
    // Create service
    public void onCreate() {
        super.onCreate();
        // create player
        player = new MediaPlayer();
        initMusicPlayer();
    }

    // Initialise the mediaplayer
    public void initMusicPlayer(){
        // Lets playback continue when device becomes idle.
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        // Set stream type to music
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // Set class as a listener for when MediaPlayer is prepared, when playback completed, and when error thrown
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    // play a song
    public int playSong(Uri trackUrl) {
        player.reset();
        try {
            player.setDataSource(getApplicationContext(), trackUrl);
        }
        catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error retrieving track", e);
        }
        player.prepareAsync();
        return player.getDuration();
    }



    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // start playback when prepared
        mp.start();
    }
}
