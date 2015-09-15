package com.knewto.www.melody;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import com.knewto.www.melody.data.TrackContract;
import com.knewto.www.melody.service.TrackService;
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
public class TopTenActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private TrackCursorAdapter mTrackAdapter;
    private ListView mListView;
    private static final int FORECAST_LOADER = 0;
    private int mPosition = ListView.INVALID_POSITION;

    private static final String[] TRACK_COLUMNS = {
            TrackContract.TrackEntry._ID,
            TrackContract.TrackEntry.COLUMN_ARTIST_NAME,
            TrackContract.TrackEntry.COLUMN_ARTIST_ID,
            TrackContract.TrackEntry.COLUMN_ALBUM_NAME,
            TrackContract.TrackEntry.COLUMN_ALBUM_IMAGE,
            TrackContract.TrackEntry.COLUMN_ALBUM_BIGIMAGE,
            TrackContract.TrackEntry.COLUMN_TRACK_NAME,
            TrackContract.TrackEntry.COLUMN_TRACK_ID,
            TrackContract.TrackEntry.COLUMN_TRACK_PREVIEW,
            TrackContract.TrackEntry.COLUMN_TRACK_LINK,
    };

    // These indices are tied to TRACK_COLUMNS.
    static final int COL_TRACKTABLE_ID = 0;
    static final int COL_ARTIST_NAME = 1;
    static final int COL_ARTIST_ID = 2;
    static final int COL_ALBUM_NAME = 3;
    static final int COL_ALBUM_IMAGE = 4;
    static final int COL_ALBUM_BIGIMAGE = 5;
    static final int COL_TRACK_NAME = 6;
    static final int COL_TRACK_ID = 7;
    static final int COL_TRACK_PREVIEW = 8;
    static final int COL_TRACK_LINK = 9;


    private static final String LOG_TAG = TopTenActivityFragment.class.getSimpleName();

    // Interface to handle item clicks
    OnSongSelected mCallback;

    public interface OnSongSelected {
        public void playSelectedSong(String artistId, int position);
    }

    public TopTenActivityFragment() {
    }

    String artistId;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null || !savedInstanceState.containsKey("tracks")) {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra("artistId"))
            {
                // Intent provides artist id in single pane mode
                artistId = intent.getStringExtra("artistId");
                topTenSearch(artistId);
            }
            else {
                // Bundle provides artist id in two pane mode
                Bundle arguments = getArguments();
                if (arguments != null) {
                    artistId = arguments.getString("artistId");
                    topTenSearch(artistId);
                }
            }
        }
        else {
            // No activity required for saved instance on cursor loader
            Log.v(LOG_TAG, "Saved Instance");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create Cursor Adapter
        mTrackAdapter = new TrackCursorAdapter(getActivity(), null, 0);

        // Inflate fragment in bind List Adapter
        View fragmentView = inflater.inflate(R.layout.fragment_top_ten, container, false);
        mListView = (ListView) fragmentView.findViewById(R.id.track_list);
        mListView.setAdapter(mTrackAdapter);

        // Responds to click on list item
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Intent to open Player
                mCallback.playSelectedSong(artistId, position);
            }
        });

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
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
        super.onSaveInstanceState(outState);
    }

    /**
     * Searches Spotify API for Top Ten tracks for an artist.
     * Selects country based on device default.
     * @param artistId
     */
    public void topTenSearch(String artistId) {
        // Test calling Track Service
        Intent intent = new Intent(getActivity(), TrackService.class);
        intent.putExtra("artistId", artistId);
        getActivity().startService(intent);
    }

    // CURSOR LOADER

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String sortOrder = TrackContract.TrackEntry._ID + " ASC";

        Uri trackForArtistUri = TrackContract.TrackEntry.buildTrackArtist(artistId);

        return new CursorLoader(getActivity(),
                trackForArtistUri,
                TRACK_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTrackAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTrackAdapter.swapCursor(null);
    }
}

