package com.example.len.lastfm.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;


public class LastFMContract {

    public static final String CONTENT_AUTHORITY = "com.example.len.lastfm";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ARTIST = "artist";
    public static final String PATH_MEMBER = "member";
    public static final String PATH_TOP_TRACK = "top_track";
    public static final String PATH_SIMILAR = "similar";

    public static final class ArtistEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;

        // Table name
        public static final String TABLE_NAME = "artist";

        public static final String COLUMN_ARTIST_MBID = "mbid";
        public static final String COLUMN_ARTIST_NAME = "name";
        public static final String COLUMN_ARTIST_IMAGE = "image";
        public static final String COLUMN_ARTIST_BIO = "bio";
        public static final String COLUMN_ARTIST_YEARFORMED = "yearformed";
        public static final String COLUMN_ARTIST_PLACEFORMED = "placeformed";
        public static final String COLUMN_ARTIST_URL = "url";

        public static Uri buildArtistUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }

        public static Uri buildArtistUriByMMBID(String mbid) {
            return CONTENT_URI.buildUpon().appendPath("mbid").appendPath(mbid).build();
        }

        public static Uri buildArtistUriByName(String name) {
            return CONTENT_URI.buildUpon().appendPath("name").appendPath(name).build();
        }

        public static String getArtistIdFromUri (Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getArtistNameFromUri (Uri uri){
            return uri.getPathSegments().get(2);
        }
    }

    public static final class MemberEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEMBER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEMBER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEMBER;

        // Table name
        public static final String TABLE_NAME = "member";

        public static final String COLUMN_MEMBER_NAME = "name";
        public static final String COLUMN_MEMBER_YEARFROM = "yearfrom";
        public static final String COLUMN_MEMBER_ARTIST_MBID = "ambid";

        public static Uri buildMemberUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }

    public static final class TopTrackEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOP_TRACK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOP_TRACK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOP_TRACK;

        // Table name
        public static final String TABLE_NAME = "top_tracks";

        public static final String COLUMN_TOP_TRACK_MBID = "mbid";
        public static final String COLUMN_TOP_TRACK_NAME = "name";
        public static final String COLUMN_TOP_TRACK_PLAYCOUNT = "playcount";
        public static final String COLUMN_TOP_TRACK_LISTENERS = "listeners";
        public static final String COLUMN_TOP_TRACK_IMAGE = "image";
        public static final String COLUMN_TOP_TRACK_ARTIST_MBID = "ambid";

        public static Uri buildTopTrackUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
        public static Uri buildTopTrackUriByMMBID(String mbid) {
            return CONTENT_URI.buildUpon().appendPath(mbid).build();
        }

        public static String getTopTrackIdFromIntentUri (Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getTopTrackIdFromUri (Uri uri){
            return uri.getPathSegments().get(1);
        }
    }

    public static final class SimilarEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SIMILAR).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SIMILAR;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SIMILAR;

        // Table name
        public static final String TABLE_NAME = "similar";

        public static final String COLUMN_SIMILAR_MBID = "mbid";
        public static final String COLUMN_SIMILAR_NAME = "name";
        public static final String COLUMN_SIMILAR_IMAGE = "image";
        public static final String COLUMN_SIMILAR_ARTIST_MBID = "ambid";

        public static Uri buildSimilarUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
        public static Uri buildSimilarUriByMMBID(String mbid) {
            return CONTENT_URI.buildUpon().appendPath(mbid).build();
        }

        public static String getSimilarIdFromIntentUri (Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getSimilarIdFromUri (Uri uri){
            return uri.getPathSegments().get(1);
        }
    }

}
