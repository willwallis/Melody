package com.knewto.www.melody;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
    Intent shareIntent;                                         // intent to share spotify url

    // Track variables
    int position = 0;
    String artistId = "Artist ID";
    String artistName = "Artist Name";
    String albumName = "Album Name";
    String trackName = "Track Title";
    String bigImage = "";
    String spotifyLink = "http://www.spotify.com";

    // UI elements
    TextView artistNameView;
    TextView albumNameView;
    ImageView bigImageView;
    TextView songTitleView;
    SeekBar seekBar;
    TextView startTimeView;
    TextView endTimeView;
    ImageButton prevButton;
    ImageButton playButton;
    ImageButton nextButton;

    private final int LOADSONG = 100;
    private final int PAUSEPLAY = 200;
    private final int SCRUB = 300;
    private final int PREVSONG = 400;
    private final int NEXTSONG = 500;
    private final int REFRESHUI = 600;

    public DialogPlayerFragment() {
        // Empty constructor required for DialogFragment
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        // Initialise share intent
        shareIntent = new Intent(Intent.ACTION_SEND);

        // Get all the image elements
        artistNameView = (TextView) view.findViewById(R.id.artist_name);
        albumNameView = (TextView) view.findViewById(R.id.album_name);
        bigImageView = (ImageView) view.findViewById(R.id.album_pic);
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
            artistId = savedInstanceState.getString("artistId");
            artistName = savedInstanceState.getString("artistName");
            albumName = savedInstanceState.getString("albumName");
            trackName = savedInstanceState.getString("trackName");
            spotifyLink = savedInstanceState.getString("spotifyLink");
            bigImage = savedInstanceState.getString("bigImage");
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
            setTrackUi();
        } else if (arguments != null) {
            Log.v(TAG, "Bundle Found");
            artistId = arguments.getString("artistId");
            position = arguments.getInt("position", 0);
            dialogTablet = true;
            if (position != 0)
                loadTrack(LOADSONG);
            else
                loadTrack(REFRESHUI);
        } else if (intent != null) {
            Log.v(TAG, "Intent Found");
            artistId = intent.getStringExtra("artistId");
            position = intent.getIntExtra("posValue", 0);
            if (position != 0)
              loadTrack(LOADSONG);
            else
                loadTrack(REFRESHUI);
        }

        // Display title if tablet
        if (dialogTablet)
            getDialog().setTitle("Now Playing");

        // Button click handlers
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTrack(PAUSEPLAY); // Play Song
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTrack(PREVSONG); // Prev Song
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTrack(NEXTSONG); // Next Song
            }
        });

        // Seekbar Handler
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playbackIntent.putExtra("action", SCRUB);
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

        // 4. Create receiver for track UI updates
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(uiUpdateReceiver,
                new IntentFilter("player-ui-update"));

        // 3. Create receiver for maxtime  updates
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(maxTimeReceiver,
                new IntentFilter("max-time"));

        // 2. Create receiver for music playback requests
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(playPauseReceiver,
                new IntentFilter("play-pause-status"));

        // 1. Create receiver for current track position
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(timeMessageReceiver,
                new IntentFilter("current-track-position"));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_dialog, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Set share content
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, spotifyLink);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    // TRACK UPDATES
    private void loadTrack(int action) {
        playbackIntent.putExtra("action", action);
        playbackIntent.putExtra("position", position);
        playbackIntent.putExtra("artistId", artistId);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(playbackIntent);
        if (action > 350) {
            seekBar.setProgress(0);
            startTimeView.setText("0:00");
        }
    }

    // Set UI to current track
    public void setTrackUi() {
            // Change text in views
            artistNameView.setText(artistName);
            albumNameView.setText(albumName);
            songTitleView.setText(trackName);
            seekBar.setMax(maxTime);
            endTimeView.setText(stringMaxTime);

            // Load image with Picasso
            if (!bigImage.equals("")) {
                Picasso.with(getActivity())
                        .load(bigImage)
                        .into(bigImageView);
            }
    }

    // BROADCAST RECEIVERS
    // 1. Update Seekbar with current track position.
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
                Log.v("Seekbar Intent", "Max time set to: " + maxTime);
            }
        }
    };

    // 2. Set Play/Pause button to appropriate status based on SongPlayer broadcast
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

    // 3. Update max time track change
    private BroadcastReceiver maxTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Do Stuff
            if(intent.hasExtra("maxTime")){
                // Get the extras and update UI
                maxTime = intent.getIntExtra("maxTime", 0);
                stringMaxTime = uiTimeFormat(maxTime);
                seekBar.setMax(maxTime);
                endTimeView.setText(stringMaxTime);
            }
        }
    };

    // 4. Update user interface on track change
    private BroadcastReceiver uiUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Do Stuff
            if(intent.hasExtra("trackName")){
                // Get the extras and update UI
                position = intent.getIntExtra("position", 0);
                trackName = intent.getStringExtra("trackName");
                spotifyLink = intent.getStringExtra("spotifyLink");
                artistName = intent.getStringExtra("artistName");
                albumName = intent.getStringExtra("albumName");
                bigImage = intent.getStringExtra("bigImage");
                setTrackUi();
                shareIntent.putExtra(Intent.EXTRA_TEXT, spotifyLink);
            }
        }
    };

    // Method to format milliseconds into mm:ss
    private String uiTimeFormat(int timeIn){
        return (int)((timeIn / (1000) / 60)) +  ":" + String.format("%02d", ((int)(timeIn / 1000) % 60));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("artistId", artistId);
        outState.putString("artistName", artistName);
        outState.putString("trackName", trackName);
        outState.putString("spotifyLink", spotifyLink);
        outState.putString("albumName", albumName);
        outState.putString("bigImage", bigImage);
        outState.putInt("position", position);
        outState.putBoolean("isPlaying", isPlaying);
        outState.putBoolean("dialogTablet", dialogTablet);
        outState.putInt("maxTime", maxTime);
        outState.putInt("currentTime", currentTime);
        outState.putString("stringCurrentTime", stringCurrentTime);
        outState.putString("stringMaxTime", stringMaxTime);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
