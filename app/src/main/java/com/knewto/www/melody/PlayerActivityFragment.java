package com.knewto.www.melody;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

import java.io.IOException;

import static com.knewto.www.melody.MusicService.*;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends Fragment {

    public PlayerActivityFragment() {
    }


    // pass variables
    String artistName = "Artist Name";
    String albumName = "Album Name";
    String trackName = "Track Title";
    String imageName = "";
    Uri trackUrl;
    MediaPlayer mediaPlayer;


    public void playSong() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        }
        mediaPlayer.reset();
        try {
            Log.v("MUSIC PLAYER", trackUrl.toString());
            mediaPlayer.setDataSource(getActivity(), trackUrl);
        }
        catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error retrieving track", e);
        }
        mediaPlayer.prepareAsync();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get intent information
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("trackName")) {
            // Get Track data from intent
            artistName = intent.getStringExtra("artistName");
            albumName = intent.getStringExtra("albumName");
            trackName = intent.getStringExtra("trackName");
            imageName = intent.getStringExtra("imageName");
            trackUrl = Uri.parse(intent.getStringExtra("trackUrl"));
        }

        View playerView =  inflater.inflate(R.layout.fragment_player, container, false);

        // Get layout views to change
        TextView artistView = (TextView)playerView.findViewById(R.id.artist_name);
        TextView albumView = (TextView)playerView.findViewById(R.id.album_name);
        TextView trackView = (TextView)playerView.findViewById(R.id.track_name);

        // Change text in views
        artistView.setText(artistName);
        albumView.setText(albumName);
        trackView.setText(trackName);

        // Load image with Picasso
        if(!imageName.equals("")){
            ImageView imageView = (ImageView)playerView.findViewById(R.id.album_pic);
            Picasso.with(getActivity())
                    .load(imageName)
                    .into(imageView);
        }

        Button button = (Button) playerView.findViewById(R.id.testButton);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // do something
                int num = 0;
                num = ((PlayerActivity)getActivity()).playSong();
                String message = "number: " + num;
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

            }
        });

        return playerView;
    }


}

