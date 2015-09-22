package com.knewto.www.melody.service;

/**
 * Created by willwallis on 9/10/15.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;

import com.knewto.www.melody.EmbeddedPlayerActivity;
import com.knewto.www.melody.MainActivity;
import com.knewto.www.melody.Utility;
import com.knewto.www.melody.data.TrackContract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class SongService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private final int LOADSONG = 100;
    private final int PAUSEPLAY = 200;
    private final int SCRUB = 300;
    private final int PREVSONG = 400;
    private final int NEXTSONG = 500;
    private final int REFRESHUI = 600;

    private MediaPlayer player;             // media player
    private String TAG = "SongService";     // error message tag
    private int playbackReq = 0;            // code used to determine playback request
    private boolean isPaused = false;       // indicates song is paused, so play can restart rather than reload
    private boolean isReady = false;        // indicates song is ready and can be acted on
    Intent trackIntent = new Intent("current-track-position"); // Intent to communicate current track position
    Intent playPauseIntent = new Intent("play-pause-status");
    Intent maxTimeIntent = new Intent("max-time");
    Intent uiUpdateIntent = new Intent("player-ui-update");
    private final Handler handler = new Handler();             // Supports continuous publishing of track position
    SharedPreferences preferences;
    boolean mTwoPane = false;                                           // determines if in tablet mode

    private Cursor trackCursor;                                 // Cursor to hold track list
    private String artistId = "";                               // artist Id used to refresh cursor when changes
    private int position = 0;                                   // position in cursor
    private String trackName = "track_name";
    private String artistName = "artist_name";
    private String albumName = "album_name";
    private String bigImage = "";
    private String trackUrl = "";
    private String spotifyLink = "";


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
        // Get preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setNowPlaying(false);  // initiate as false

        // Create receiver for music playback requests
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(musicMessageReceiver,
                new IntentFilter("playback-request"));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand executed");

        if(intent != null && intent.getAction() != null) {
            String intentAction = intent.getAction();
            Log.v(TAG, "IntentAction: " + intentAction);
            if (intentAction.equals("NEXT_TRACK")) {
                nextSong();
            } else if (intentAction.equals("PREV_TRACK")){
                prevSong();
            }else if (intentAction.equals("PAUSE_TRACK")) {
                playPauseSong();
            }
        }
        if(intent != null && intent.hasExtra("twoPane")) {
            mTwoPane = intent.getBooleanExtra("twoPane", false);
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        setNowPlaying(false);
        if (player != null) player.release();
    }

    // PLAYBACK COMMAND HANDLERS

    private BroadcastReceiver musicMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playbackReq = intent.getIntExtra("action", 0);
            String newArtistId = intent.getStringExtra("artistId");
            int newPosition = intent.getIntExtra("position", 0);
            switch(playbackReq){
                case LOADSONG:{
                    Log.v(TAG, "LOADSONG: " + playbackReq);
                    position = newPosition;
                    loadSong(newArtistId, position);
                    break;
                }
                case PAUSEPLAY:{
                    Log.v(TAG, "PAUSEPLAY: " + playbackReq);
                    playPauseSong();
                    break;
                }
                case SCRUB: {
                    Log.v(TAG, "SCRUB: " + playbackReq);
                    if (player.isPlaying() || isPaused == true) {
                        player.seekTo(intent.getIntExtra("progress", player.getCurrentPosition()));
                        // Update track immediately as handler won't work if paused.
                        trackIntent.putExtra("currentPosition", player.getCurrentPosition());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(trackIntent);
                    }
                    break;
                }
                case PREVSONG:{
                    Log.v(TAG, "PREVSONG: " + playbackReq);
                    prevSong();
                    break;
                }
                case NEXTSONG: {
                    Log.v(TAG, "NEXTSONG: " + playbackReq);
                    nextSong();
                    break;
                }
                case REFRESHUI: {
                    broadcastUiUpdate(position);
                break;
                }

            }
        }
    };

    // Load Song
    private void loadSong(String newArtistId, int position) {
        Log.v(TAG, "Position: " + position);
        // Reload cursor if new artist provided, move to cursor position, and get track url
        if(!artistId.equals(newArtistId)){
            trackCursor = Utility.topTracksCursor(getApplicationContext(), newArtistId);
            artistId = newArtistId;
        }
        // Load data
        loadTrackData(position);

        // Load the song and prepare player
        isReady = false;
        player.reset();
        try {
            player.setDataSource(getApplicationContext(), Uri.parse(trackUrl));
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving track", e);
        }
        player.prepareAsync();
        // Publish UI Updates
        broadcastUiUpdate(position);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isReady = true;
        if (!isPaused) {
            startSong();
        }
        setNowPlaying(true);
    }

    // Start Song
    private void startSong() {
        player.start();
        broadcastMaxTime(player.getDuration());         // Update max time
        isPaused = false;
        broadcastPlayPause(true);                       // update pause play button
        handler.removeCallbacks(broadcastUpdates);
        handler.post(broadcastUpdates);                 // start broadcast of current time
        boolean notifOn = preferences.getBoolean("enable_notifications", false);
        if (notifOn)
            new sendNotification().execute();
    }

    // Previous Song
    private void prevSong(){
        position--;
        if (position < 0)
            position = trackCursor.getCount() - 1;
        loadSong(artistId, position);
    }


    // Next Song
    private void nextSong(){
        position++;
        if (position == trackCursor.getCount())
            position = 0;
        loadSong(artistId, position);
    }

    private void playPauseSong() {
        if (player.isPlaying()) {
            player.pause();
            isPaused = true;
            broadcastPlayPause(false);
            boolean notifOn = preferences.getBoolean("enable_notifications", false);
            if (notifOn)
                new sendNotification().execute();
        } else if (isReady){
            startSong();
        }
    }

    // MEDIAPLAYER METHODS
    @Override
    public void onCompletion(MediaPlayer mp) {
        broadcastPlayPause(false);
        stopForeground(true);
        setNowPlaying(false);
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }


    // BROADCASTS
    // 1. Broadcast Current Position
    public Runnable broadcastUpdates = new Runnable() {
        public void run() {
            if (player.isPlaying()) {
                trackIntent.putExtra("currentPosition", player.getCurrentPosition());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(trackIntent);
                handler.postDelayed(this, 500);
            }
        }
    };

    // 2. Broadcast play/pause button in UI based on state
    private void broadcastPlayPause(boolean isPlaying) {
        playPauseIntent.putExtra("isPlaying", isPlaying);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(playPauseIntent);
    }

    // 3. Broadcast Max Time
    private void broadcastMaxTime(int maximumTime) {
        maxTimeIntent.putExtra("maxTime", maximumTime);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(maxTimeIntent);
    }

    // 4. Update User Interface in Player
    private void broadcastUiUpdate (int posValue) {
        uiUpdateIntent.putExtra("position", position);
        uiUpdateIntent.putExtra("trackName", trackName);
        uiUpdateIntent.putExtra("spotifyLink", spotifyLink);
        uiUpdateIntent.putExtra("artistName", artistName);
        uiUpdateIntent.putExtra("albumName", albumName);
        uiUpdateIntent.putExtra("bigImage", bigImage);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(uiUpdateIntent);
        // Required when player is reopened from Now Playing
        broadcastPlayPause(!isPaused);
        if(player.isPlaying())
            broadcastMaxTime(player.getDuration());
    }


    // NOTIFICATION ASYNC TASK
    private class sendNotification extends AsyncTask<String, String, String>{
        // Define notification variables
        String trackName;
        String artistName;
        String imageUrl;
        int prevImage;
        int playPauseImage;
        int nextImage;
        Bitmap albumImage;

        @Override
        protected void onPreExecute() {
            // Get all the inputs
            trackName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_TRACK_NAME));
            artistName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_ARTIST_NAME));
            imageUrl = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_ALBUM_BIGIMAGE));
            prevImage = android.R.drawable.ic_media_previous;
            nextImage = android.R.drawable.ic_media_next;
            if(isPaused)
                playPauseImage = android.R.drawable.ic_media_play;
            else
                playPauseImage = android.R.drawable.ic_media_pause;
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            // Post the notification
            int NOTIFY_ID = 1;

            // Button intents
            Intent prevIntent = new Intent(getApplicationContext(), SongService.class);
            prevIntent.setAction("PREV_TRACK");
            PendingIntent piPrev = PendingIntent.getService(getApplicationContext(), 0, prevIntent, 0);

            Intent pauseIntent = new Intent(getApplicationContext(), SongService.class);
            pauseIntent.setAction("PAUSE_TRACK");
            PendingIntent piPause = PendingIntent.getService(getApplicationContext(), 0, pauseIntent, 0);

            Intent nextIntent = new Intent(getApplicationContext(), SongService.class);
            nextIntent.setAction("NEXT_TRACK");
            PendingIntent piNext = PendingIntent.getService(getApplicationContext(), 0, nextIntent, 0);

            // Open player intent
            Intent resultIntent;
            if (mTwoPane)
                resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            else
                //resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent = new Intent(getApplicationContext(), EmbeddedPlayerActivity.class);
            resultIntent.putExtra("artistId", "");
            resultIntent.putExtra("posValue", 0);
            resultIntent.putExtra("openPlayer", true);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Create Notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

            builder//.setContentIntent(pendInt)
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setLargeIcon(albumImage)
                    .setTicker(trackName)
                    .setOngoing(true)
                    .setContentTitle(trackName)
                    .setContentText(artistName)
                    .setContentIntent(resultPendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(artistName))
                    .addAction(prevImage, "Previous", piPrev)
                    .addAction(playPauseImage, "Pause", piPause)
                    .addAction(nextImage, "Next", piNext)
                    .setStyle(new NotificationCompat.MediaStyle())
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
            Notification not = builder.build();

            startForeground(NOTIFY_ID, not);

            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... params) {
            // Load the image
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                albumImage = BitmapFactory.decodeStream(input);
                return null;
            } catch (IOException e) {
                // Log exception
                return null;
            }
        }
    }

private void setNowPlaying(boolean nowPlaying) {
    SharedPreferences.Editor editor = preferences.edit();
    editor.putBoolean("now_playing", nowPlaying);
    editor.commit();
    Log.v(TAG, "TopTenMenu: " + nowPlaying);
}

    private void loadTrackData(int position){
        // check position not bigger than count
        if (position == trackCursor.getCount())
            position = 0;
        // check position not les than zero
        if (position < 0)
            position = trackCursor.getCount() - 1;
        // Load new data
        trackCursor.moveToPosition(position);

        trackName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_TRACK_NAME));
        artistName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_ARTIST_NAME));
        albumName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_ALBUM_NAME));
        bigImage = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_ALBUM_BIGIMAGE));
        trackUrl = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_TRACK_PREVIEW));
        spotifyLink = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_TRACK_LINK));

    }
}


