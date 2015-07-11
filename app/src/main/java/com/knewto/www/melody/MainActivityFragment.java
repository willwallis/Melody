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

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create list of artists
        arrayOfArtists = new ArrayList<ArtistProfile>();
        // Add temporary entry
        ArtistProfile tempProfile = new ArtistProfile("Tiny Tim", "https://i.scdn.co/image/18141db33353a7b84c311b7068e29ea53fad2326", "6vWDO969PvNqNYHIOW5v0m");
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
                Intent detailIntent = new Intent(getActivity(), TopTenActivity.class);
                String artistId = arrayOfArtists.get(position).id;
                String artistName = arrayOfArtists.get(position).name;
                detailIntent.putExtra("artistId", artistId);
                detailIntent.putExtra("artistName", artistName);
                getActivity().startActivity(detailIntent);
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
        public String id;

        public ArtistProfile(String name, String image, String id) {
            this.name = name;
            this.image = image;
            this.id = id;
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

            // Load image with Picasso
            ImageView artistImage = (ImageView) convertView.findViewById(R.id.item_artist_pic);

            Picasso.with(getContext())
                    .load(artistProfile.image)
                    .into(artistImage);
            // Return the completed view to render on screen
            return convertView;
        }
    }

    public void artistSearch(String artistName) {
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
                    arrayOfArtists.add(new ArtistProfile(artist.name, pickArtistImage(artist, 200), artist.id));
                }
                artistsAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Artist search failure", error.toString());
            }
        });
    }

    public String pickArtistImage(Artist artist, int size){
        String imageURL;
        if (artist.images.size() == 0){
            imageURL = "https://s.yimg.com/cd/resizer/2.0/FIT_TO_WIDTH-w200/e4c5009d6b9eefbbda64587d3a49064c22db7821.jpg";
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
        return imageURL;
    }
}
