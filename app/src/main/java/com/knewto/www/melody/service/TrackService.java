package com.knewto.www.melody.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.knewto.www.melody.data.TrackContract.TrackEntry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by willwallis on 9/14/15.
 */
public class TrackService extends IntentService {
    private final String LOG_TAG = TrackService.class.getSimpleName();

    public TrackService() {
        super("TrackService");
    }

    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "Track Service Called");
        String artistId = intent.getStringExtra("artistId"); // Artist Id from intent
        SpotifyApi api = new SpotifyApi(); // Connect to the Spotify API with the wrapper
        SpotifyService spotify = api.getService();  // Create a SpotifyService object

        // Set Country to preference or default to Locale
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String prefCountry = preferences.getString("country", Locale.getDefault().getCountry());
        Map<String, Object> options = new HashMap<>(); // Add querymap with default country matching device
        options.put(SpotifyService.COUNTRY, prefCountry);

        // Call Spotify Artist Top Tracks Query
        spotify.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                if (tracks.tracks.size() < 1) {
                    // ACTION IF NO TRACKS FOUND
                    String toastText = "No tracks found for this artist, sorry.";
                    Toast toast = Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    // ACTION IF TRACKS FOUND - INSERT RECORDS INTO
                    storeTracks(tracks);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                // ACTION IF SPOTIFY FAILS
                Log.d("Top 10 search failure", error.toString());
                String toastText = "Spotify search failed";
                Toast toast = Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT);
                toast.show();
            }
        });


    }


    private void storeTracks(Tracks tracks){
        // Vector to store returned data
        Vector<ContentValues> cVVector = new Vector<ContentValues>(tracks.tracks.size());

        for(int i = 0; i < tracks.tracks.size(); i++) {
            ContentValues trackValues = new ContentValues();
            trackValues.put(TrackEntry.COLUMN_ARTIST_NAME, tracks.tracks.get(i).artists.get(0).name);
            trackValues.put(TrackEntry.COLUMN_ARTIST_ID, tracks.tracks.get(i).artists.get(0).id);
            trackValues.put(TrackEntry.COLUMN_ALBUM_NAME, tracks.tracks.get(i).album.name);
            trackValues.put(TrackEntry.COLUMN_ALBUM_IMAGE, pickTrackImage(tracks.tracks.get(i), 200));
            trackValues.put(TrackEntry.COLUMN_ALBUM_BIGIMAGE, pickTrackImage(tracks.tracks.get(i), 1000));
            trackValues.put(TrackEntry.COLUMN_TRACK_NAME, tracks.tracks.get(i).name);
            trackValues.put(TrackEntry.COLUMN_TRACK_ID, tracks.tracks.get(i).id);
            trackValues.put(TrackEntry.COLUMN_TRACK_PREVIEW, tracks.tracks.get(i).preview_url);
            trackValues.put(TrackEntry.COLUMN_TRACK_LINK, tracks.tracks.get(i).external_urls.get("spotify"));

            cVVector.add(trackValues);
        }

        int inserted = 0;
        // add to database
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = this.getContentResolver().bulkInsert(TrackEntry.CONTENT_URI, cvArray);
        }
    }


    /**
     * Method to pick the image closest to the requested size from spotify image list.
     * @param track Track object returned by query
     * @param size Preferred size of image.
     * @return URL of the closest image match. Returns Spotify logo if none found.
     */
    private String pickTrackImage(Track track, int size){
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
