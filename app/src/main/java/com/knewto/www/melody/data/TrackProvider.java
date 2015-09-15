package com.knewto.www.melody.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by willwallis on 9/14/15.
 */
public class TrackProvider extends ContentProvider {

    private TrackDbHelper mOpenHelper;
    private final String LOG_TAG = TrackProvider.class.getSimpleName();
    private UriMatcher sUriMatcher;
    static final int TRACK = 100;
    static final int TRACK_WITH_ARTIST = 200;

    public boolean onCreate() {
        mOpenHelper = new TrackDbHelper(getContext());
        sUriMatcher = buildUriMatcher(); // matches uri
        return true;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TrackContract.CONTENT_AUTHORITY;

        // Add additional matchers for each uri
        matcher.addURI(authority, TrackContract.PATH_TRACK, TRACK);
        matcher.addURI(authority, TrackContract.PATH_TRACK + "/*", TRACK_WITH_ARTIST);
        return matcher;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case TRACK_WITH_ARTIST: {

                String uriSelection = TrackContract.TrackEntry.TABLE_NAME+
                        "." + TrackContract.TrackEntry.COLUMN_ARTIST_ID + " = ? ";
                String artistId = TrackContract.TrackEntry.geArtistIdFromUri(uri);
                String[] uriSelectionArgs = new String[]{artistId};

                retCursor = db.query(
                        TrackContract.TrackEntry.TABLE_NAME,
                        projection,
                        uriSelection,
                        uriSelectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case TRACK: {
                retCursor = db.query(
                        TrackContract.TrackEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case TRACK: {
                // remove existing records
                int rowsDeleted;
                rowsDeleted = db.delete(
                        TrackContract.TrackEntry.TABLE_NAME, "1", null);
                Log.v(LOG_TAG, "Records Deleted: " + rowsDeleted);
                // add new records
                int returnCount = 0;
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TrackContract.TrackEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                Log.v(LOG_TAG, "Records Inserted: " + returnCount);
                return returnCount;
            }
            default: {
                return super.bulkInsert(uri, values);
            }
        }
    }
}