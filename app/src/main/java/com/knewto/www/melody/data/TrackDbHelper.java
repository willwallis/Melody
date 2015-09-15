package com.knewto.www.melody.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.knewto.www.melody.data.TrackContract.TrackEntry;

/**
 * Created by willwallis on 9/14/15.
 */
public class TrackDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "track.db";
    public TrackDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TRACK_TABLE = "CREATE TABLE " + TrackEntry.TABLE_NAME + " (" +

                TrackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TrackEntry.COLUMN_ARTIST_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_ARTIST_ID + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_ALBUM_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_ALBUM_IMAGE + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_ALBUM_BIGIMAGE + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TRACK_ID + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TRACK_PREVIEW + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TRACK_LINK + " TEXT NOT NULL " +
                " );";

            sqLiteDatabase.execSQL(SQL_CREATE_TRACK_TABLE);
           }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    }
