package com.knewto.www.melody;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

    ArrayAdapter<String> ArtistAdapter;  // Adapter for artist list view
    ArrayList<String> ArtistList; // Array to contains the lists of artist

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create the Array Adapter for the list of artists
        loadTestData();
        ArtistAdapter = new ArrayAdapter<String> (
                getActivity(),  // Context
                R.layout.artist_list_item, // Name of item layout file
                R.id.item_artist_name, // ID of the text view to populate
                ArtistList //Artist Data
        ) ;
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
            ListView listview = (ListView) fragmentView.findViewById(R.id.artist_list);
            listview.setAdapter(ArtistAdapter);

        // Responds to click on list item
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Run Spotify Query
                // Connect to the Spotify API with the wrapper
                SpotifyApi api = new SpotifyApi();
                // Create a SpotifyService object that we can use to get desire data
                SpotifyService spotify = api.getService();
                // Add querymap with limit of 10 options returned
                String artistName = ArtistAdapter.getItem(position);
                Map<String, Object> options = new HashMap<>();
                options.put("limit", 10);

                // Spotify Call back
                spotify.searchArtists(artistName, options, new Callback<ArtistsPager>() {
                    @Override
                    public void success(ArtistsPager artistsPager, Response response) {
                        // Display toast of Spotify result if list item is selected
                        Context context = getActivity(); // the current context
                        int duration = Toast.LENGTH_SHORT; // how long the toast should display
                        String toastText = ""; //what the toast should display
                        int count = 1;
                        for(Artist artist : artistsPager.artists.items){
                            toastText = toastText + count + ": " + artist.name + "\n";
                            count++;
                        }
                        Toast toast = Toast.makeText(context, toastText, duration);  // create the toast
                        toast.show(); // display the toast
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("Artist search failure", error.toString());
                    }
                });
            }
        });

        return fragmentView;
    }



    /**
     *  Method to load test data to test the list view before connecting to Spotify.
     */
    public void loadTestData() {
        ArtistList = new ArrayList<String>();
        ArtistList.add("Kanye");
        ArtistList.add("Beyonce");
        ArtistList.add("Ciara");
        ArtistList.add("Eminem");
        ArtistList.add("Jay-Z");
        ArtistList.add("The Killers");
        ArtistList.add("U2");
        ArtistList.add("Coldplay");
        ArtistList.add("Will Smith");
        ArtistList.add("Bassnecter");
    }

}
