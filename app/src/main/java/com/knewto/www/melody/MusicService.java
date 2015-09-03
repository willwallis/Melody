package com.knewto.www.melody;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.os.Handler;

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
    // song playing and paused flags
    private boolean songReady = false; // used to stop calls while preparing
    private boolean songPaused = false;
    // Intent and handler to broadcast track position
    Intent trackIntent;
    private final Handler handler = new Handler();

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
    public boolean onUnbind(Intent intent) {
//        player.stop();
//        player.release();
        return false;
    }

    // MUSIC PLAYER SECTION
    // Create service
    public void onCreate() {
        super.onCreate();
        // create player
        if (player == null) {
            player = new MediaPlayer();
        initMusicPlayer();}
        // create intent to send current position
        trackIntent = new Intent("current-track-position");
    }

    // Initialise the mediaplayer
    public void initMusicPlayer() {
        // Lets playback continue when device becomes idle.
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        // Set stream type to music
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // Set class as a listener for when MediaPlayer is prepared, when playback completed, and when error thrown
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    // load a song
    public void loadSong(Uri trackUrl) {
        songReady = false;
        player.reset();
        try {
            player.setDataSource(getApplicationContext(), trackUrl);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error retrieving track", e);
        }
        playSong();
    }

    // play a song
    public void playSong() {
        player.prepareAsync();
    }

    // pause and restart a song
    public boolean pauseSong(boolean onOff) {
        if (songReady) {
            if (onOff) {
                player.pause();
                songPaused = true;
            } else {
                player.start();
                songPaused = false;
            }
            return true;
        } else
            return false;
    }

    // play a song
    public void scrubSong(int position) {
        if (songReady)
            player.seekTo(position);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        songReady = false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        songReady = true;
        // start playback when prepared
        if (!songPaused) {
            mp.start();
        }
        // publish max length
        trackIntent.putExtra("maxTime", mp.getDuration());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(trackIntent);
        // start current position runnable
        handler.removeCallbacks(broadcastUpdates);
        handler.post(broadcastUpdates);
    }

    public Runnable broadcastUpdates = new Runnable() {
        public void run() {
            if (songReady) {
                trackIntent.putExtra("currentPosition", player.getCurrentPosition());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(trackIntent);
                handler.postDelayed(this, 500);
            }
        }
    };

}
