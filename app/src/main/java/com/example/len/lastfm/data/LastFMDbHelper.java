package com.example.len.lastfm.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.len.lastfm.data.LastFMContract.ArtistEntry;
import com.example.len.lastfm.data.LastFMContract.MemberEntry;
import com.example.len.lastfm.data.LastFMContract.SimilarEntry;
import com.example.len.lastfm.data.LastFMContract.TopTrackEntry;

public class LastFMDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "lastfm.db";

    public LastFMDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ARTIST_TABLE = "CREATE TABLE " + ArtistEntry.TABLE_NAME + " (" +
                ArtistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ArtistEntry.COLUMN_ARTIST_MBID + " TEXT NOT NULL UNIQUE, " +
                ArtistEntry.COLUMN_ARTIST_NAME + " TEXT NOT NULL, " +
                ArtistEntry.COLUMN_ARTIST_IMAGE + " TEXT, " +
                ArtistEntry.COLUMN_ARTIST_BIO + " TEXT, " +
                ArtistEntry.COLUMN_ARTIST_YEARFORMED + " INTEGER, " +
                ArtistEntry.COLUMN_ARTIST_PLACEFORMED + " TEXT, " +
                ArtistEntry.COLUMN_ARTIST_URL + " TEXT" +
                " );";

        final String SQL_CREATE_MEMBER_TABLE = "CREATE TABLE " + MemberEntry.TABLE_NAME + " (" +
                MemberEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MemberEntry.COLUMN_MEMBER_NAME + " TEXT NOT NULL, " +
                MemberEntry.COLUMN_MEMBER_YEARFROM + " INTEGER, " +
                MemberEntry.COLUMN_MEMBER_ARTIST_MBID + " TEXT NOT NULL" +
                " );";

        final String SQL_CREATE_TOP_TRACK_TABLE = "CREATE TABLE " + TopTrackEntry.TABLE_NAME + " (" +
                TopTrackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TopTrackEntry.COLUMN_TOP_TRACK_MBID + " TEXT NOT NULL UNIQUE, " +
                TopTrackEntry.COLUMN_TOP_TRACK_NAME + " TEXT NOT NULL, " +
                TopTrackEntry.COLUMN_TOP_TRACK_PLAYCOUNT + " INTEGER, " +
                TopTrackEntry.COLUMN_TOP_TRACK_LISTENERS + " INTEGER, " +
                TopTrackEntry.COLUMN_TOP_TRACK_IMAGE + " TEXT, " +
                TopTrackEntry.COLUMN_TOP_TRACK_ARTIST_MBID + " TEXT NOT NULL" +
                " );";

        final String SQL_CREATE_SIMILAR_TABLE = "CREATE TABLE " + SimilarEntry.TABLE_NAME + " (" +
                SimilarEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SimilarEntry.COLUMN_SIMILAR_MBID + " TEXT NOT NULL, " +
                SimilarEntry.COLUMN_SIMILAR_NAME + " TEXT NOT NULL UNIQUE, " +
                SimilarEntry.COLUMN_SIMILAR_IMAGE + " TEXT, " +
                SimilarEntry.COLUMN_SIMILAR_ARTIST_MBID + " TEXT NOT NULL" +
                " );";

        db.execSQL(SQL_CREATE_ARTIST_TABLE);
        db.execSQL(SQL_CREATE_MEMBER_TABLE);
        db.execSQL(SQL_CREATE_TOP_TRACK_TABLE);
        db.execSQL(SQL_CREATE_SIMILAR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ArtistEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SimilarEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MemberEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TopTrackEntry.TABLE_NAME);
        onCreate(db);
    }
}
