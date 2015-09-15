package com.knewto.www.melody.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by willwallis on 9/14/15.
 */
public class TrackContract {
    public static final String CONTENT_AUTHORITY = "com.knewto.www.melody";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TRACK = "track";

    public static final class TrackEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACK).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;
        public static final String TABLE_NAME = "track";

        public static final String COLUMN_ARTIST_NAME = "artist_name";
        public static final String COLUMN_ARTIST_ID = "artist_id";
        public static final String COLUMN_ALBUM_NAME = "album_name";
        public static final String COLUMN_ALBUM_IMAGE = "album_image";
        public static final String COLUMN_ALBUM_BIGIMAGE = "album_bigimage";
        public static final String COLUMN_TRACK_NAME = "track_name";
        public static final String COLUMN_TRACK_ID = "track_id";
        public static final String COLUMN_TRACK_PREVIEW = "track_preview";
        public static final String COLUMN_TRACK_LINK = "track_link";

        public static Uri buildTrackUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrackArtist(String artistId) {
            return CONTENT_URI.buildUpon().appendPath(artistId).build();
        }

        public static String geArtistIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }
}