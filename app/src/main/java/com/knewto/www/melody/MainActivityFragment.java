package com.knewto.www.melody;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }
    // Declare arraylist to contain Artist profiles and adapter to bind with list view
    ArrayList<ArtistProfile> arrayOfArtists;
    ArtistsAdapter artistsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create list of artists
        arrayOfArtists = new ArrayList<ArtistProfile>();
        // Add temporary entry
        ArtistProfile tempProfile = new ArtistProfile("Tiny Tim", "Gone");
        arrayOfArtists.add(tempProfile);
        // Create Adapter
        artistsAdapter = new ArtistsAdapter(getActivity(), arrayOfArtists);

        // Inflate fragment in bind List Adapter
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
            ListView listview = (ListView) fragmentView.findViewById(R.id.artist_list);
            listview.setAdapter(artistsAdapter);

        // Responds to click on list item
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String artistName = artistsAdapter.getItem(position).name;
                artistSearch(artistName);
            }
        });

        return fragmentView;
    }

    /**
     *  Class defining model for Artist Profile to show in search results
     */
    public class ArtistProfile {
        public String name;
        public String image;

        public ArtistProfile(String name, String image) {
            this.name = name;
            this.image = image;
        }
    }

    /**
     * ArrayAdapter to contain Artist Profiles returned from search
     */
    public class ArtistsAdapter extends ArrayAdapter<ArtistProfile> {
        public ArtistsAdapter(Context context, ArrayList<ArtistProfile> artistProfile) {
            super(context, 0, artistProfile);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            ArtistProfile artistProfile = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.artist_list_item, parent, false);
            }
            // Lookup view for data population
            TextView artistName = (TextView) convertView.findViewById(R.id.item_artist_name);
            // Populate the data into the template view using the data object
            artistName.setText(artistProfile.name);
            // Return the completed view to render on screen
            return convertView;
        }
    }

    public void artistSearch(String artistName) {
        // Run Spotify Query
        // Connect to the Spotify API with the wrapper
        SpotifyApi api = new SpotifyApi();
        // Create a SpotifyService object that we can use to get desire data
        SpotifyService spotify = api.getService();
        // Add querymap with limit of 10 options returned
        Map<String, Object> options = new HashMap<>();
        options.put("limit", 10);

        // Spotify Call back
        spotify.searchArtists(artistName, options, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                arrayOfArtists.clear();
                for(Artist artist : artistsPager.artists.items){
                    arrayOfArtists.add(new ArtistProfile(artist.name, "TEMP"));
                }
                artistsAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Artist search failure", error.toString());
            }
        });
    }
}
