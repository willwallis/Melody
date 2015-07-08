package com.knewto.www.melody;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


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
        return fragmentView;
    }

    /**
     *  Method to load test data to test the list view before connecting to Spotify.
     */
    public void loadTestData() {
        ArtistList = new ArrayList<String>();
        ArtistList.add("Kanye");
        ArtistList.add("Kanyeezy");
        ArtistList.add("Ye");
        ArtistList.add("Yeezy");
        ArtistList.add("Yeezus");
        ArtistList.add("Mr West");
        ArtistList.add("Kanye Kardasian");
        ArtistList.add("Kimye");
        ArtistList.add("Kanye West");
        ArtistList.add("That guy in the leather kilt");
    }

}
