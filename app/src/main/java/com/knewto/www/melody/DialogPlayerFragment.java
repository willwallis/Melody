package com.knewto.www.melody;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.knewto.www.melody.data.TrackContract;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by willwallis on 9/10/15.
 */
public class DialogPlayerFragment extends DialogFragment {

    private String TAG = "DialogPlayerFragment";                // error message tag
    Intent playbackIntent = new Intent("playback-request");     // intent to send playback requests to song service
    boolean isPlaying = false;                                  // receives play/pause from song service
    int currentTime = 0;                                        // current track position
    int maxTime = 30000;                                        // track length
    String stringCurrentTime = "0:00";                          // UI version of the current time
    String stringMaxTime = "0:30";                              // UI version of the maximum time
    boolean dialogTablet = false;                               // used to decide whether to display title on tablet

    // Track variables
    Cursor trackCursor;
    int position = 0;

//    ArrayList<TopTrack> arrayOfTracks;
    String artistName = "Artist Name";
    String artistId = "Artist ID";
    String albumName = "Album Name";
    String trackName = "Track Title";
    String imageName = "";
    String trackUrl;

    // UI elements
    TextView artistNameView;
    TextView albumNameView;
    ImageView bigImage;
    TextView songTitleView;
    SeekBar seekBar;
    TextView startTimeView;
    TextView endTimeView;
    ImageButton prevButton;
    ImageButton playButton;
    ImageButton nextButton;


    public DialogPlayerFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        // Get all the image elements
        artistNameView = (TextView) view.findViewById(R.id.artist_name);
        albumNameView = (TextView) view.findViewById(R.id.album_name);
        bigImage = (ImageView) view.findViewById(R.id.album_pic);
        songTitleView = (TextView) view.findViewById(R.id.track_name);
        seekBar = (SeekBar) view.findViewById(R.id.music_seek);
        startTimeView = (TextView) view.findViewById(R.id.start_time);
        endTimeView = (TextView) view.findViewById(R.id.end_time);
        prevButton = (ImageButton) view.findViewById(R.id.icon_rewind);
        playButton = (ImageButton) view.findViewById(R.id.icon_play_pause);
        nextButton = (ImageButton) view.findViewById(R.id.icon_forward);

        Intent intent = getActivity().getIntent();
        Bundle arguments = getArguments();

        // Retrieve saved instance state if exists
        if (savedInstanceState != null && savedInstanceState.containsKey("isPlaying")) {
//            arrayOfTracks = savedInstanceState.getParcelableArrayList("tracks");
            artistId = savedInstanceState.getString("artistId");
            position = savedInstanceState.getInt("position");
            dialogTablet = savedInstanceState.getBoolean("dialogTablet");
            isPlaying = savedInstanceState.getBoolean("isPlaying");
            currentTime = savedInstanceState.getInt("currentTime");
            maxTime = savedInstanceState.getInt("maxTime");
            stringCurrentTime = savedInstanceState.getString("stringCurrentTime");
            stringMaxTime = savedInstanceState.getString("stringMaxTime");

            if (isPlaying)
                playButton.setImageResource(android.R.drawable.ic_media_pause);
            seekBar.setProgress(currentTime);
            seekBar.setMax(maxTime);
            startTimeView.setText(stringCurrentTime);
            endTimeView.setText(stringMaxTime);
        }
        else if (arguments != null) {
            Log.v(TAG, "Bundle Found");
            artistId = arguments.getString("artistId");
            position = arguments.getInt("position", 0);
            dialogTablet = true;
            loadTrack(100);
        }
        else if (intent != null) {
            Log.v(TAG, "Intent Found");
            artistId = intent.getStringExtra("artistId");
            position = intent.getIntExtra("posValue", 0);
            loadTrack(100);
        }

        // Display title if tablet
        if (dialogTablet)
            getDialog().setTitle("Now Playing");

        // Load cursor
        trackCursor = Utility.topTracksCursor(getActivity(), artistId);

        // set Ui to current track
        setTrackUi(position);

        // Button click handlers
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTrack(200);
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevSong();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSong();
            }
        });

        // Seekbar Handler
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playbackIntent.putExtra("action", 300);
                playbackIntent.putExtra("progress", seekBar.getProgress());
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(playbackIntent);
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


        // Create receiver for music playback requests
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(playPauseReceiver,
                new IntentFilter("play-pause-status"));

        // Create receiver for current track position
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(timeMessageReceiver,
                new IntentFilter("current-track-position"));

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        outState.putParcelableArrayList("tracks", arrayOfTracks);
        outState.putString("artistId", artistId);
        outState.putInt("position", position);
        outState.putBoolean("isPlaying", isPlaying);
        outState.putBoolean("dialogTablet", dialogTablet);
        outState.putInt("maxTime", maxTime);
        outState.putInt("currentTime", currentTime);
        outState.putString("stringCurrentTime", stringCurrentTime);
        outState.putString("stringMaxTime", stringMaxTime);
        super.onSaveInstanceState(outState);
    }

    // TRACK UPDATES
    private void loadTrack(int action) {
        playbackIntent.putExtra("action", action);
        playbackIntent.putExtra("position", position);
        playbackIntent.putExtra("artistId", artistId);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(playbackIntent);
    }

    // method to move to next song
    public void nextSong() {
        // Pick next song, need to send array of tracks not just current
        position++;
        if (position == trackCursor.getCount())
            position = 0;
        loadTrack(100);
        seekBar.setProgress(0);
        startTimeView.setText("0:00");
        setTrackUi(position);
    }

    // method to move to previous song
    public void prevSong() {
        // Pick previous song, need to send top 10 array.
        position--;
        if (position < 0)
            position = trackCursor.getCount() - 1;
        loadTrack(100);
        seekBar.setProgress(0);
        startTimeView.setText("0:00");
        setTrackUi(position);
    }

    // UI UPDATES
    // Set Play/Pause button to appropriate status based on SongPlayer broadcast
    private BroadcastReceiver playPauseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isPlaying = intent.getBooleanExtra("isPlaying", false);
            if (isPlaying)
                playButton.setImageResource(android.R.drawable.ic_media_pause);
            else
                playButton.setImageResource(android.R.drawable.ic_media_play);
        }
    };

    // Set UI to current track
    public void setTrackUi(int trackPos) {
        if (trackPos >= 0 && trackPos <= trackCursor.getCount()) {

            trackCursor.moveToPosition(trackPos);

            // Get Track data from intent
            trackName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_TRACK_NAME));
            artistName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_ARTIST_NAME));
            albumName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_ALBUM_NAME));
            imageName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_ALBUM_BIGIMAGE));
            trackUrl = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_TRACK_PREVIEW));

            // Change text in views
            artistNameView.setText(artistName);
            albumNameView.setText(albumName);
            songTitleView.setText(trackName);

            // Load image with Picasso
            if (!imageName.equals("")) {
                Picasso.with(getActivity())
                        .load(imageName)
                        .into(bigImage);
            }

            // Pre load previous and next images
            int prevPos = trackPos - 1;
            if (prevPos < 0)
                prevPos = trackCursor.getCount() - 1;
            trackCursor.moveToPosition(prevPos);
            String imagePrev = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_ALBUM_BIGIMAGE));;
            Picasso.with(getActivity()).load(imagePrev).fetch();
            int nextPos = trackPos + 1;
            if (nextPos >= trackCursor.getCount())
                nextPos = 0;
            trackCursor.moveToPosition(nextPos);
            String imageNext = trackCursor.getString(trackCursor.getColumnIndexOrThrow(TrackContract.TrackEntry.COLUMN_ALBUM_BIGIMAGE));;
            Picasso.with(getActivity()).load(imageNext).fetch();
        }

    }

    // Update Seekbar with current track position.
    private BroadcastReceiver timeMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("currentPosition")) {
                // Get extra data included in the Intent
                currentTime = intent.getIntExtra("currentPosition", 0);
                seekBar.setProgress(currentTime);
                stringCurrentTime = uiTimeFormat(currentTime);
                startTimeView.setText(stringCurrentTime);
            } else if (intent.hasExtra("maxTime")) {
                maxTime = intent.getIntExtra("maxTime", 0);
                seekBar.setMax(maxTime);
                stringMaxTime = uiTimeFormat(maxTime);
                endTimeView.setText(stringMaxTime);
                Log.v("Seekbar Intent", "Max time set to: " + maxTime);
            }
        }
    };

    private String uiTimeFormat(int timeIn){
        return (int)((timeIn / (1000) / 60)) +  ":" + String.format("%02d", ((int)(timeIn / 1000) % 60));
    }

    @Override
    public void onPause(){
        trackCursor.close();
        super.onPause();
    }
}
