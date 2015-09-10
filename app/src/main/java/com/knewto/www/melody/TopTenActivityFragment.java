package com.knewto.www.melody;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTenActivityFragment extends Fragment {

    private static final String LOG_TAG = TopTenActivityFragment.class.getSimpleName();
    // Declare arraylist to contain Track profiles and adapter to bind with list view
    ArrayList<TopTrack> arrayOfTracks;
    TracksAdapter tracksAdapter;

    // Interface to handle item clicks
    OnSongSelected mCallback;

    public interface OnSongSelected {
        public void playSelectedSong(ArrayList<TopTrack> arrayOfTracks, int position);
    }

    public TopTenActivityFragment() {
    }

    String artistId;
    String artistName = "";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Create list of tracks
        arrayOfTracks = new ArrayList<TopTrack>();
        // Looked for saved instance and if found retrieve track info, otherwise query spotify
        if(savedInstanceState == null || !savedInstanceState.containsKey("tracks")) {
            // Get ID from intent
            Intent intent = getActivity().getIntent();
            Log.v(LOG_TAG, "Check Intent");
            if (intent != null && intent.hasExtra("artistId"))
            {
                Log.v(LOG_TAG, "Intent Found");
                artistId = intent.getStringExtra("artistId");
                artistName = intent.getStringExtra("artistName");
                topTenSearch(artistId);
            }
            else {
                Bundle arguments = getArguments();
                Log.v(LOG_TAG, "Check Bundle");
                if (arguments != null) {
                    Log.v(LOG_TAG, "Bundle Found");
                    artistId = arguments.getString("artistId");
                    artistName = arguments.getString("artistName");
                    topTenSearch(artistId);
                }
            }
        }
        else {
            arrayOfTracks = savedInstanceState.getParcelableArrayList("tracks");
            Log.v(LOG_TAG, "Saved Instance");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create Adapter
        tracksAdapter = new TracksAdapter(getActivity(), arrayOfTracks);

        // Inflate fragment in bind List Adapter
        View fragmentView = inflater.inflate(R.layout.fragment_top_ten, container, false);
        ListView listview = (ListView) fragmentView.findViewById(R.id.track_list);
        listview.setAdapter(tracksAdapter);

        // Responds to click on list item
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Intent to open Player
                mCallback.playSelectedSong(arrayOfTracks, position);
//                Intent playerIntent = new Intent(getActivity(), PlayerActivity.class);
//                playerIntent.putExtra("posValue", position);
//                playerIntent.putParcelableArrayListExtra("trackData", arrayOfTracks);
//                getActivity().startActivity(playerIntent);
            }
        });

        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSongSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSongSelected ");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("tracks", arrayOfTracks);
        super.onSaveInstanceState(outState);
    }

    /**
     * Searches Spotify API for Top Ten tracks for an artist.
     * Selects country based on device default.
     * @param artistId
     */
    public void topTenSearch(String artistId) {
        // Connect to the Spotify API with the wrapper
        SpotifyApi api = new SpotifyApi();
        // Create a SpotifyService object that we can use to get desire data
        SpotifyService spotify = api.getService();
        // Spotify Call back
        // Add querymap with default country matching device
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());

        spotify.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                if (tracks.tracks.size() < 1){
                    String toastText = "No tracks found for this artist, sorry."; // what the toast should display
                    Toast toast = Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT);  // create the toast
                    toast.show(); // display the toast
                }
                arrayOfTracks.clear();
                for (Track track : tracks.tracks) {
                    arrayOfTracks.add(new TopTrack(track.name, artistName, pickTrackImage(track, 200), pickTrackImage(track, 1000), track.album.name, track.preview_url));
                }
                tracksAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Top 10 search failure", error.toString());
                String toastText = "Spotify search failed"; // what the toast should display
                Toast toast = Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT);  // create the toast
                toast.show(); // display the toast
            }
        });


    }

    /**
     * Method to pick the image closest to the requested size from spotify image list.
     * @param track Track object returned by query
     * @param size Preferred size of image.
     * @return URL of the closest image match. Returns Spotify logo if none found.
     */
    public String pickTrackImage(Track track, int size){
        String defaultURL = "https://s.yimg.com/cd/resizer/2.0/FIT_TO_WIDTH-w200/e4c5009d6b9eefbbda64587d3a49064c22db7821.jpg";
        String imageURL;
        if (track.album.images.size() == 0){
            imageURL = defaultURL;
        }
        else {
            int imageDiff = Math.abs(track.album.images.get(0).width - size);
            imageURL = track.album.images.get(0).url;
            for ( int i = 1 ; i < track.album.images.size() ; i++){
                if(Math.abs(track.album.images.get(i).width - size) < imageDiff){
                    imageURL = track.album.images.get(i).url;
                }
            }
        }
        // Check that URL is valid before returning
        if(URLUtil.isValidUrl(imageURL))
            return imageURL;
        else
            return defaultURL;
    }

}
