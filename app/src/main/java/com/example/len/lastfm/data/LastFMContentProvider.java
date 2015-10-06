package com.example.len.lastfm.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class LastFMContentProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private LastFMDbHelper mOpenHelper;

    static final int ARTIST = 100;
    static final int ARTIST_BY_MBID = 101;
    static final int ARTIST_BY_NAME = 102;

    static final int MEMBER = 200;

    static final int TOP_TRACK = 300;
    static final int TOP_TRACK_FOR_ARTIST = 301;

    static final int SIMILAR = 400;
    static final int SIMILAR_FOR_ARTIST = 401;

    private static final String sArtistLikeNameSelection =
            LastFMContract.ArtistEntry.TABLE_NAME +
                    "." + LastFMContract.ArtistEntry.COLUMN_ARTIST_NAME + " LIKE ? ";

    private static final String sArtistIdSelection =
            LastFMContract.ArtistEntry.TABLE_NAME +
                    "." + LastFMContract.ArtistEntry.COLUMN_ARTIST_MBID + " = ? ";

    private static final String sSimilarMBIDSelection =
            LastFMContract.SimilarEntry.TABLE_NAME +
                    "." + LastFMContract.SimilarEntry.COLUMN_SIMILAR_ARTIST_MBID + " = ? ";

    private static final String sTopTracksMBIDSelection =
            LastFMContract.TopTrackEntry.TABLE_NAME +
                    "." + LastFMContract.TopTrackEntry.COLUMN_TOP_TRACK_ARTIST_MBID + " = ? ";

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = LastFMContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, LastFMContract.PATH_ARTIST, ARTIST);
        matcher.addURI(authority, LastFMContract.PATH_ARTIST + "/mbid/*", ARTIST_BY_MBID);
        matcher.addURI(authority, LastFMContract.PATH_ARTIST + "/name/*", ARTIST_BY_NAME);

        matcher.addURI(authority, LastFMContract.PATH_MEMBER, MEMBER);

        matcher.addURI(authority, LastFMContract.PATH_TOP_TRACK, TOP_TRACK);
        matcher.addURI(authority, LastFMContract.PATH_TOP_TRACK + "/*", TOP_TRACK_FOR_ARTIST);

        matcher.addURI(authority, LastFMContract.PATH_SIMILAR, SIMILAR);
        matcher.addURI(authority, LastFMContract.PATH_SIMILAR + "/*", SIMILAR_FOR_ARTIST);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new LastFMDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ARTIST:
                return LastFMContract.ArtistEntry.CONTENT_TYPE;
            case MEMBER:
                return LastFMContract.MemberEntry.CONTENT_TYPE;
            case TOP_TRACK:
                return LastFMContract.TopTrackEntry.CONTENT_TYPE;
            case SIMILAR:
                return LastFMContract.SimilarEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case ARTIST:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LastFMContract.ArtistEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ARTIST_BY_MBID: {
                retCursor = getArtistById(uri, projection, sortOrder);
                break;
            }

            case ARTIST_BY_NAME: {
                retCursor = getArtistsByName(uri, projection, sortOrder);
                break;
            }

            case MEMBER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LastFMContract.MemberEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case TOP_TRACK: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LastFMContract.TopTrackEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case TOP_TRACK_FOR_ARTIST: {
                String mbid = LastFMContract.TopTrackEntry.getTopTrackIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LastFMContract.TopTrackEntry.TABLE_NAME,
                        projection,
                        sTopTracksMBIDSelection,
                        new String[]{mbid},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case SIMILAR: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LastFMContract.SimilarEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case SIMILAR_FOR_ARTIST: {
                String mbid = LastFMContract.SimilarEntry.getSimilarIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LastFMContract.SimilarEntry.TABLE_NAME,
                        projection,
                        sSimilarMBIDSelection,
                        new String[]{mbid},
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ARTIST: {
                long _id = db.insert(LastFMContract.ArtistEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = LastFMContract.ArtistEntry.buildArtistUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MEMBER: {
                long _id = db.insert(LastFMContract.MemberEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = LastFMContract.MemberEntry.buildMemberUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TOP_TRACK: {
                long _id = db.insert(LastFMContract.TopTrackEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = LastFMContract.TopTrackEntry.buildTopTrackUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SIMILAR: {
                long _id = db.insert(LastFMContract.SimilarEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = LastFMContract.SimilarEntry.buildSimilarUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case ARTIST:
                rowsDeleted = db.delete(
                        LastFMContract.ArtistEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MEMBER:
                rowsDeleted = db.delete(
                        LastFMContract.MemberEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TOP_TRACK:
                rowsDeleted = db.delete(
                        LastFMContract.TopTrackEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SIMILAR:
                rowsDeleted = db.delete(
                        LastFMContract.SimilarEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ARTIST:
                rowsUpdated = db.update(LastFMContract.ArtistEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case ARTIST_BY_MBID: {
                rowsUpdated = db.update(LastFMContract.ArtistEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            }
            case MEMBER:
                rowsUpdated = db.update(LastFMContract.MemberEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TOP_TRACK:
                rowsUpdated = db.update(LastFMContract.TopTrackEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case SIMILAR:
                rowsUpdated = db.update(LastFMContract.SimilarEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;
        switch (match) {
            case ARTIST:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        String artistMBID = value.get(LastFMContract.ArtistEntry.COLUMN_ARTIST_MBID).toString();
                        if ( db.update(LastFMContract.ArtistEntry.TABLE_NAME, value, sArtistIdSelection, new String[]{artistMBID}) == 0 ) {
                            long _id = db.insert(LastFMContract.ArtistEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case MEMBER:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(LastFMContract.MemberEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case TOP_TRACK:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(LastFMContract.TopTrackEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case SIMILAR:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(LastFMContract.SimilarEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Cursor getArtistById(Uri uri, String[] projection, String sortOrder) {
        String artistMBID = LastFMContract.ArtistEntry.getArtistIdFromUri(uri);
        return mOpenHelper.getReadableDatabase().query(
                LastFMContract.ArtistEntry.TABLE_NAME,
                projection,
                sArtistIdSelection,
                new String[]{artistMBID},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getArtistsByName(Uri uri, String[] projection, String sortOrder) {
        String artistName = LastFMContract.ArtistEntry.getArtistNameFromUri(uri);
        return mOpenHelper.getReadableDatabase().query(
                LastFMContract.ArtistEntry.TABLE_NAME,
                projection,
                sArtistLikeNameSelection,
                new String[]{"%"+artistName+"%"},
                null,
                null,
                sortOrder
        );
    }
}
