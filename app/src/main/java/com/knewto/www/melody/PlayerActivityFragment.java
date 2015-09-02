package com.knewto.www.melody;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends Fragment {

    ArrayList<TopTrack> arrayOfTracks;
    String artistName = "Artist Name";
    String albumName = "Album Name";
    String trackName = "Track Title";
    String imageName = "";
    Uri trackUrl;
    int position = 0;
    SeekBar seekBar;
    int currentTime = 0;
    int maxTime = 30041;
    MediaPlayer mediaPlayer;
    boolean songPlaying = false;
    boolean songPaused = false;
    boolean seekOn = false;
    SeekThread seekThread = new SeekThread();


    public PlayerActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get intent information
        Intent intent = getActivity().getIntent();
        if (intent != null ) {
            // Get data from Array
            arrayOfTracks = intent.getParcelableArrayListExtra("trackData");
            position = intent.getIntExtra("posValue", 0);
            artistName = intent.getStringExtra("artistName");
        }
        // Create receiver for current track time
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(timeMessageReceiver,
                new IntentFilter("current-track-position"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View playerView = inflater.inflate(R.layout.fragment_player, container, false);

        // set Ui to current track
        setTrackUi(playerView, position);

        // Get Seekbar
        seekBar = (SeekBar) playerView.findViewById(R.id.music_seek);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                scrubSong(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Do nothing
            }
        });

        // Handle button events
        ImageButton playButton = (ImageButton) playerView.findViewById(R.id.icon_play_pause);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSong();
            }
        });
        ImageButton prevButton = (ImageButton) playerView.findViewById(R.id.icon_rewind);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevSong();
            }
        });
        ImageButton nextButton = (ImageButton) playerView.findViewById(R.id.icon_forward);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSong();
            }
        });


        return playerView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // set up Seekbar
        seekBar.setMax(maxTime);
        seekBar.setProgress(currentTime);
    }

    @Override
    public void onStop() {
        super.onStop();
        seekOn = false;
        seekThread.close();
        mediaPlayer.release();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(timeMessageReceiver);
        super.onDestroy();
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "current-track-position" is broadcast and update the seekbar
    private BroadcastReceiver timeMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int currentBcTime = intent.getIntExtra("currentPosition", 0);
            seekBar.setProgress(currentBcTime);
        }
    };


    // Set UI to current track
    public void setTrackUi(View currentView, int trackPos){
        if (trackPos >= 0 && trackPos <= arrayOfTracks.size()) {

            // Get Track data from intent
            trackName = arrayOfTracks.get(trackPos).name;
            albumName = arrayOfTracks.get(trackPos).album;
            imageName = arrayOfTracks.get(trackPos).bigImage;
            trackUrl = Uri.parse(arrayOfTracks.get(trackPos).trackUrl);

            // Get layout views to change
            TextView artistView = (TextView) currentView.findViewById(R.id.artist_name);
            TextView albumView = (TextView) currentView.findViewById(R.id.album_name);
            TextView trackView = (TextView) currentView.findViewById(R.id.track_name);

            // Change text in views
            artistView.setText(artistName);
            albumView.setText(albumName);
            trackView.setText(trackName);

            // Load image with Picasso
            if (!imageName.equals("")) {
                ImageView imageView = (ImageView) currentView.findViewById(R.id.album_pic);
                Picasso.with(getActivity())
                        .load(imageName)
                        .into(imageView);
            }

            // Pre load previous and next images
            int prevPos = trackPos - 1;
            if (prevPos < 0)
                prevPos = arrayOfTracks.size() - 1;
            Picasso.with(getActivity()).load(arrayOfTracks.get(prevPos).bigImage).fetch();
            int nextPos = trackPos + 1;
            if (nextPos >= arrayOfTracks.size())
                nextPos = 0;
            Picasso.with(getActivity()).load(arrayOfTracks.get(nextPos).bigImage).fetch();
        }

    }


    // Method to play, pause, and resume track
    public void playSong() {
        final ImageButton playButton = (ImageButton) getActivity().findViewById(R.id.icon_play_pause);
        if (songPlaying && !songPaused) {
            // pause the track
            mediaPlayer.pause();
            songPaused = true;
            playButton.setImageResource(android.R.drawable.ic_media_play);
            Log.v("MUSIC PLAYER", "Track Paused");
            seekThread.pause();
        }
        else if (songPlaying &&     songPaused) {
            // restart song from current position
            mediaPlayer.start();
            songPaused = false;
            playButton.setImageResource(android.R.drawable.ic_media_pause);
            Log.v("MUSIC PLAYER", "Track Restarted");
            seekThread.restart();
        }
        else {
            // load and start new song
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        if (songPaused)
                            mp.pause();
                        else {
                            playButton.setImageResource(android.R.drawable.ic_media_pause);
                        }
                        maxTime = mp.getDuration();
                        seekBar.setMax(maxTime);
                        TextView maxTimeView = (TextView) getActivity().findViewById(R.id.end_time);
                        maxTimeView.setText(maxTime / 60000 + ":" + String.format("%02d", (maxTime / 1000)));
                        if (!seekOn){
                            seekOn = true;
                            seekThread.start();
                        }
                        else
                            seekThread.restart();
                    }
                });
            }
            if (seekOn)
                seekThread.pause();
            mediaPlayer.reset();
            try {
                Log.v("MUSIC PLAYER", trackUrl.toString());
                mediaPlayer.setDataSource(getActivity(), trackUrl);
            } catch (Exception e) {
                Log.e("MUSIC PLAYER", "Error retrieving track", e);
            }
            mediaPlayer.prepareAsync();

            songPlaying = true;
        }
    }

    public void nextSong() {
        // Pick next song, need to send array of tracks not just current
        position++;
        if (position == arrayOfTracks.size())
            position = 0;
        trackUrl = Uri.parse(arrayOfTracks.get(position).trackUrl);
        songPlaying = false;
        playSong();
        setTrackUi(getView(), position);
    }

    public void scrubSong(int seekProgress) {
        mediaPlayer.seekTo(seekProgress);
    }

    public void prevSong() {
        // Pick previous song, need to send top 10 array.
        position--;
        if (position < 0 )
            position = arrayOfTracks.size() - 1;
        trackUrl = Uri.parse(arrayOfTracks.get(position).trackUrl);
        songPlaying = false;
        playSong();
        setTrackUi(getView(), position);
    }


    private class SeekThread extends Thread {
        private boolean seekRun = true;   // Start seekbar updates and end thread by setting to false
        private boolean seekPause = false; // Pause updates to the seekbar

        @Override
        public void run() {
            try {
                while(seekRun) {
                    if (!seekPause) {
                    // Get current track position and broadcast
                    currentTime = mediaPlayer.getCurrentPosition();
                    Intent intent = new Intent("current-track-position");
                    intent.putExtra("currentPosition", currentTime);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                    Log.v("SeekThread", "Max " + maxTime + " Cur: " + currentTime); }
                    sleep(500);
                    }
                }
            catch (InterruptedException e){
                Log.e("SeekThread", "error in incrementing seekbar");  // change to log error
            }
        }

        public void pause(){
            // set pause variable
            seekPause = true;
        }

        public void restart(){
            // set pause variable false
            seekPause = false;
        }

        public void close() {
            // set close variable
            seekRun = false;
        }

    };
}

