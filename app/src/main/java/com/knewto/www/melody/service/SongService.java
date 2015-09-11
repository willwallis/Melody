package com.knewto.www.melody.service;

/**
 * Created by willwallis on 9/10/15.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.os.Handler;


public class SongService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player;             // media player
    private Uri songUrl;                    // song url
    private String TAG = "SongService";     // error message tag
    private int playbackReq = 0;            // code used to determine playback request
    private boolean isPaused = false;       // indicates song is paused, so play can restart rather than reload
    private boolean isReady = false;        // indicates song is ready and can be acted on
    Intent playPauseIntent = new Intent("play-pause-status");  // Intent to communicate playback status
    Intent trackIntent = new Intent("current-track-position"); // Intent to communicate current track position
    private final Handler handler = new Handler();             // Supports continuous publishing of track position

    public SongService() {
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate called");
        // Create Media Player
        if (player == null) {
            player = new MediaPlayer();
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
        }
        // Create receiver for music playback requests
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(musicMessageReceiver,
                new IntentFilter("playback-request"));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand executed");
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (player != null) player.release();
    }

    // PLAYBACK COMMAND HANDLERS

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "current-track-position" is broadcast and update the seekbar
    private BroadcastReceiver musicMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Playback command received");
            playbackReq = intent.getIntExtra("action", 0);
            Log.v(TAG, "Case # is: " + playbackReq);
            if (playbackReq == 100) {  // Load and play.
                songUrl = Uri.parse(intent.getStringExtra("trackUrl"));
                loadSong(songUrl);
            } else if (playbackReq == 200) {  // Pause and restart
                if (player.isPlaying()) {
                    player.pause();
                    isPaused = true;
                    broadcastPlayPause(false);
                } else if (isReady){
                    startSong();
                }
            } else if (playbackReq == 300) {  // Scrub track
                Log.v(TAG, "300 case");
                if (player.isPlaying() || isPaused == true) {
                    player.seekTo(intent.getIntExtra("progress", player.getCurrentPosition()));
                    // Update track immediately as handler won't work if paused.
                    trackIntent.putExtra("currentPosition", player.getCurrentPosition());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(trackIntent);
                }
            }
        }
    };

    // Load Song
    private void loadSong(Uri trackUrl) {
        // load the song
        isReady = false;
        player.reset();
        try {
            player.setDataSource(getApplicationContext(), trackUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving track", e);
        }
        player.prepareAsync();
    }

    // Start Song
    private void startSong() {
        player.start();
        if(!isPaused){
            trackIntent.removeExtra("currentPosition");
            trackIntent.putExtra("maxTime", player.getDuration());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(trackIntent);
        }
        isPaused = false;
        broadcastPlayPause(true);
        handler.removeCallbacks(broadcastUpdates);
        handler.post(broadcastUpdates);
    }

    // Method to broadcast play/pause button in UI based on state
    private void broadcastPlayPause(boolean isPlaying) {
        playPauseIntent.putExtra("isPlaying", isPlaying);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(playPauseIntent);
    }

    // MEDIAPLAYER METHODS
    @Override
    public void onCompletion(MediaPlayer mp) {
        broadcastPlayPause(false);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // need to start in order to get duration
        if (!isPaused) {
            startSong();
        }
        isReady = true;
    }


    public Runnable broadcastUpdates = new Runnable() {
        public void run() {
            if (player.isPlaying()) {
                trackIntent.putExtra("currentPosition", player.getCurrentPosition());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(trackIntent);
                handler.postDelayed(this, 500);
            }
        }
    };

}
