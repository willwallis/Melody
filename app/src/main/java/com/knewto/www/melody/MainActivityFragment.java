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
        ArtistList = new ArrayList<String>();
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
                String artistName = ArtistAdapter.getItem(position);
                artistSpotify(artistName);
            }
        });

        return fragmentView;
    }

    public void artistSpotify(String artistName) {
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
                ArtistAdapter.clear();
                for(Artist artist : artistsPager.artists.items){
                    ArtistAdapter.add(artist.name);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Artist search failure", error.toString());
            }
        });
    }
}
