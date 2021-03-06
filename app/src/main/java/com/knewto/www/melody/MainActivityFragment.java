package com.knewto.www.melody;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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

    // Declare arraylist to contain Artist profiles and adapter to bind with list view
    ArrayList<ArtistProfile> arrayOfArtists;
    ArtistsAdapter artistsAdapter;
    int mPosition;

    // Interface to handle item clicks
    OnArtistSelected mCallback;

    public interface OnArtistSelected {
        public void displayTopTen(String id, String name);
    }


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Create list of artists
        arrayOfArtists = new ArrayList<ArtistProfile>();
        // Looked for saved instance and if found retrieve artist info, otherwise do open search
        if(savedInstanceState == null || !savedInstanceState.containsKey("artists")) {
         }
        else {
            arrayOfArtists = savedInstanceState.getParcelableArrayList("artists");
            mPosition = savedInstanceState.getInt("currentPosition");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create Adapter
        artistsAdapter = new ArtistsAdapter(getActivity(), arrayOfArtists);

        // Inflate fragment in bind List Adapter
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
            ListView listview = (ListView) fragmentView.findViewById(R.id.artist_list);
            listview.setAdapter(artistsAdapter);
            listview.setSelection(mPosition);
//            listview.smoothScrollToPosition(mPosition);  // Can't get this to work for me.

        // Responds to click on list item
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                String artistId = arrayOfArtists.get(position).id;
                String artistName = arrayOfArtists.get(position).name;
                mCallback.displayTopTen(artistId, artistName);
            }
        });

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("artists", arrayOfArtists);
        if (mPosition != ListView.INVALID_POSITION)
            outState.putInt("currentPosition", mPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnArtistSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnArtistSelected ");
        }
    }


    /**
     * Method to search the Spotify API. Uses call back to remain off main thread.
     * @param artistName
     */
    public void artistSearch(final String artistName) {
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
                if (artistsPager.artists.items.size() < 1){
                    String toastText = "No artist found for: " + artistName + ", please try again."; // what the toast should display
                    Toast toast = Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT);  // create the toast
                    toast.show(); // display the toast
                }

                arrayOfArtists.clear();
                for(Artist artist : artistsPager.artists.items){
                    arrayOfArtists.add(new ArtistProfile(artist.name, pickArtistImage(artist, 200), artist.id));
                }
                artistsAdapter.notifyDataSetChanged();
                if (arrayOfArtists.size() == 0) {
                    String toastText = "Redo Query"; // what the toast should display
                    Toast toast = Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT);  // create the toast
                    toast.show(); // display the toast
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Artist search failure", error.toString());
                String toastText = "Spotify search failed"; // what the toast should display
                Toast toast = Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT);  // create the toast
                toast.show(); // display the toast
            }
        });
    }

    /**
     * Method to pick the image closest to the requested size from spotify image list.
     * @param artist Artist object returned by query
     * @param size Preferred size of image.
     * @return URL of the closest image match. Returns Spotify logo if none found.
     */
    public String pickArtistImage(Artist artist, int size){
        String defaultURL = "https://s.yimg.com/cd/resizer/2.0/FIT_TO_WIDTH-w200/e4c5009d6b9eefbbda64587d3a49064c22db7821.jpg";
        String imageURL;
        if (artist.images.size() == 0){
            imageURL = defaultURL;
        }
        else {
            int imageDiff = Math.abs(artist.images.get(0).width - size);
            imageURL = artist.images.get(0).url;
            for ( int i = 1 ; i < artist.images.size() ; i++){
                if(Math.abs(artist.images.get(i).width - size) < imageDiff){
                    imageURL = artist.images.get(i).url;
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
