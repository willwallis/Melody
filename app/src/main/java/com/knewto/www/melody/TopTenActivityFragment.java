package com.knewto.www.melody;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    // Declare arraylist to contain Track profiles and adapter to bind with list view
    ArrayList<TopTrack> arrayOfTracks;
    TracksAdapter tracksAdapter;

    public TopTenActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Create list of tracks
        arrayOfTracks = new ArrayList<TopTrack>();
        // Looked for saved instance and if found retrieve track info, otherwise query spotify
        if(savedInstanceState == null || !savedInstanceState.containsKey("tracks")) {
            // Get ID from intent
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra("artistId"))
            {
                String artistId = intent.getStringExtra("artistId");
                topTenSearch(artistId);
            }
        }
        else {
            arrayOfTracks = savedInstanceState.getParcelableArrayList("tracks");
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
                String trackName = arrayOfTracks.get(position).name;
                // Display toast if track is selected - placeholder until P2
                String toastText = "Playing: " + trackName; // what the toast should display
                Toast toast = Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT);  // create the toast
                toast.show(); // display the toast

            }
        });

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("tracks", arrayOfTracks);
        super.onSaveInstanceState(outState);
    }

    /**
     *  Class defining model for Track to show in search results
     */
    public class TopTrack implements Parcelable {
        public String name;
        public String image;
        public String album;

        public TopTrack(String name, String image, String album) {
            this.name = name;
            this.image = image;
            this.album = album;
        }

        private TopTrack(Parcel in){
            name = in.readString();
            image = in.readString();
            album = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(name);
            parcel.writeString(image);
            parcel.writeString(album);
        }

        public final Parcelable.Creator<TopTrack> CREATOR = new Parcelable.Creator<TopTrack>() {
            @Override
            public TopTrack createFromParcel(Parcel parcel) {
                return new TopTrack(parcel);
            }

            @Override
            public TopTrack[] newArray(int i) {
                return new TopTrack[i];
            }

        };
    }

    /**
     * ArrayAdapter to contain Artist Profiles returned from search
     */
    public class TracksAdapter extends ArrayAdapter<TopTrack> {
        public TracksAdapter(Context context, ArrayList<TopTrack> topTrack) {
            super(context, 0, topTrack);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            TopTrack topTrack = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.track_list_item, parent, false);
            }
            // Lookup view for data population
            TextView trackName = (TextView) convertView.findViewById(R.id.item_track_name);
            TextView albumName = (TextView) convertView.findViewById(R.id.item_album_name);
            // Populate the data into the template view using the data object
            trackName.setText(topTrack.name);
            albumName.setText(topTrack.album);

            // Load image with Picasso
            ImageView trackImage = (ImageView) convertView.findViewById(R.id.item_track_pic);

            Picasso.with(getContext())
                    .load(topTrack.image)
                    .into(trackImage);
            // Return the completed view to render on screen
            return convertView;
        }
    }

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
                arrayOfTracks.clear();
                for (Track track : tracks.tracks) {
                    arrayOfTracks.add(new TopTrack(track.name, pickTrackImage(track, 200), track.album.name));
                }
                tracksAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Top 10 search failure", error.toString());
            }
        });


    }

    public String pickTrackImage(Track track, int size){
        String imageURL;
        if (track.album.images.size() == 0){
            imageURL = "https://s.yimg.com/cd/resizer/2.0/FIT_TO_WIDTH-w200/e4c5009d6b9eefbbda64587d3a49064c22db7821.jpg";
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
        return imageURL;
    }

}
