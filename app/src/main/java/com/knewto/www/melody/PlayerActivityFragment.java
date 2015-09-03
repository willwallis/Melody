package com.knewto.www.melody;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    // Track variables
    ArrayList<TopTrack> arrayOfTracks;
    int position = 0;
    String artistName = "Artist Name";
    String albumName = "Album Name";
    String trackName = "Track Title";
    String imageName = "";
    Uri trackUrl;

    // Variables used by playServiceSong method
    boolean newSong = true;  // indicates code should load new song
    boolean songPlaying = true;  // is a song currently playing
    boolean songPaused = false;   // is the song paused

    // UI elements
    ImageButton playButton;

    // Seekbar element and position
    SeekBar seekBar;
    int currentTime = 0;
    int maxTime = 20041;


    public PlayerActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get intent information
        Intent intent = getActivity().getIntent();
        if(savedInstanceState != null && savedInstanceState.containsKey("tracks")) {
            arrayOfTracks = savedInstanceState.getParcelableArrayList("tracks");
            position = savedInstanceState.getInt("position");
            newSong = savedInstanceState.getBoolean("newSong");
            songPaused = savedInstanceState.getBoolean("songPaused");
            songPlaying = savedInstanceState.getBoolean("songPlaying");}
            // Get ID from intent
        else if (intent != null) {
            // Get data from Array
            arrayOfTracks = intent.getParcelableArrayListExtra("trackData");
            position = intent.getIntExtra("posValue", 0);
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
        playButton = (ImageButton) playerView.findViewById(R.id.icon_play_pause);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSongService();
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

        // Set to paused if songPaused true after rotation


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
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(timeMessageReceiver);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        outState.putParcelableArrayList("tracks", arrayOfTracks);
        outState.putInt("position", position);
        outState.putBoolean("newSong", newSong);
        outState.putBoolean("songPaused", songPaused);
        outState.putBoolean("songPlaying", songPlaying);
        super.onSaveInstanceState(outState);
    }



    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "current-track-position" is broadcast and update the seekbar
    private BroadcastReceiver timeMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("currentPosition")) {
                // Get extra data included in the Intent
                currentTime = intent.getIntExtra("currentPosition", 0);
                seekBar.setProgress(currentTime);
            }
            else if (intent.hasExtra("maxTime")){
                maxTime = intent.getIntExtra("maxTime", 0);
                seekBar.setMax(maxTime);
                Log.v("Seekbar Intent", "Max time set to: " + maxTime);
            }
        }
    };


    // Set UI to current track
    public void setTrackUi(View currentView, int trackPos) {
        if (trackPos >= 0 && trackPos <= arrayOfTracks.size()) {

            // Get Track data from intent
            trackName = arrayOfTracks.get(trackPos).name;
            artistName = arrayOfTracks.get(trackPos).artist;
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


    // Method to load and play song with service
    public void playSongService() {
        // If new song indicated load song
        if (newSong) {
            ((PlayerActivity) getActivity()).loadSong(trackUrl);
            newSong = false;
        }
        // If already playing then pause track
        else if (songPlaying && !songPaused) {
            if (((PlayerActivity) getActivity()).pauseSong(true)){
            songPlaying = false;
            songPaused = true;}
        }
        // If already paused then restart track
        else if (!songPlaying && songPaused) {
            if(((PlayerActivity) getActivity()).pauseSong(false)){
            songPlaying = true;
            songPaused = false;}
        }
        // If the song is paused set button to play, otherwise set to pause
        if(songPaused)
            playButton.setImageResource(android.R.drawable.ic_media_play);
        else
            playButton.setImageResource(android.R.drawable.ic_media_pause);

    }

    // method to move to next song
    public void nextSong() {
        // Pick next song, need to send array of tracks not just current
        position++;
        if (position == arrayOfTracks.size())
            position = 0;
        trackUrl = Uri.parse(arrayOfTracks.get(position).trackUrl);
        newSong = true;
        playSongService();
        setTrackUi(getView(), position);
    }

    // method to move to previous song
    public void prevSong() {
        // Pick previous song, need to send top 10 array.
        position--;
        if (position < 0)
            position = arrayOfTracks.size() - 1;
        trackUrl = Uri.parse(arrayOfTracks.get(position).trackUrl);
        newSong = true;
        playSongService();
        setTrackUi(getView(), position);
    }

    public void scrubSong(int seekProgress) {
        ((PlayerActivity) getActivity()).scrubSong(seekProgress);
    }

}

