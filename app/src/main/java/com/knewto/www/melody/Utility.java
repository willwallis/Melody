package com.knewto.www.melody;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.knewto.www.melody.data.TrackContract;

/**
 * Created by willwallis on 9/15/15.
 */
public class Utility {

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

    public static Cursor topTracksCursor(Context context, String artistId) {
        String sortOrder = TrackContract.TrackEntry._ID + " ASC";

        Uri trackForArtistUri = TrackContract.TrackEntry.buildTrackArtist(artistId);

        Cursor trackCursor = context.getContentResolver().query(
                trackForArtistUri,  // The content URI of the words table
                TRACK_COLUMNS,                       // The columns to return for each row
                null,                   // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                sortOrder);                       // The sort order for the returned rows

        return trackCursor;
    }
}
