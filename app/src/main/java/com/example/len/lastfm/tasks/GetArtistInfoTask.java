package com.example.len.lastfm.tasks;


import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.len.lastfm.data.LastFMContract;
import com.example.len.lastfm.data.LastFMContract.ArtistEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetArtistInfoTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = GetArtistInfoTask.class.getSimpleName();
    private final Context mContext;

    public GetArtistInfoTask(Context context) {
        mContext = context;
    }

    private void getArtistInfoFromJson(String artistInfoJsonStr)throws JSONException {
        try {
            JSONObject artistJson = new JSONObject(artistInfoJsonStr);
            JSONObject artistObject = artistJson.getJSONObject("artist");

            String mbid = artistObject.getString("mbid");
            String url = artistObject.getString("url");
            String name = artistObject.getString("name");
            String image = artistObject.getJSONArray("image").getJSONObject(2).getString("#text");
            String bio = artistObject.getJSONObject("bio").getString("summary");
            int yearformed = 0;
            if(artistObject.getJSONObject("bio").has("yearformed")) {
                yearformed = artistObject.getJSONObject("bio").getInt("yearformed");
            }
            String placeformed = "";
            if(artistObject.getJSONObject("bio").has("placeformed")) {
                placeformed = artistObject.getJSONObject("bio").getString("placeformed");
            }

            ContentValues artistValues = new ContentValues();
            artistValues.put(ArtistEntry.COLUMN_ARTIST_MBID, mbid);
            artistValues.put(ArtistEntry.COLUMN_ARTIST_NAME, name);
            artistValues.put(ArtistEntry.COLUMN_ARTIST_IMAGE, image);
            artistValues.put(ArtistEntry.COLUMN_ARTIST_YEARFORMED, yearformed);
            artistValues.put(ArtistEntry.COLUMN_ARTIST_BIO, bio);
            artistValues.put(ArtistEntry.COLUMN_ARTIST_PLACEFORMED, placeformed);
            artistValues.put(ArtistEntry.COLUMN_ARTIST_URL, url);

            final String sArtistIdSelection =
                    LastFMContract.ArtistEntry.TABLE_NAME +
                            "." + LastFMContract.ArtistEntry.COLUMN_ARTIST_MBID + " = ? ";

            if ( mContext.getContentResolver().update( ArtistEntry.CONTENT_URI, artistValues, sArtistIdSelection,  new String[]{mbid} ) == 0 ) {
                mContext.getContentResolver().insert(ArtistEntry.CONTENT_URI, artistValues);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void getArtistInfo(String... params) {
        if (params.length == 0) {
            return;
        }
        String artistQuery = params[0];
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String artistJsonStr = null;

        String apiMethod = "artist.getinfo";
        String format = "json";
        String apiKey = "168436bdde0b53756590ce0fa0da4e9b";

        try {
            final String API_BASE_URL =
                    "http://ws.audioscrobbler.com/2.0/?";
            final String METHOD_PARAM = "method";
            final String ARTIST_PARAM = "mbid";
            final String API_KEY_PARAM = "api_key";
            final String FORMAT_PARAM = "format";

            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                    .appendQueryParameter(METHOD_PARAM, apiMethod)
                    .appendQueryParameter(ARTIST_PARAM, artistQuery)
                    .appendQueryParameter(API_KEY_PARAM, apiKey)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            artistJsonStr = buffer.toString();
            getArtistInfoFromJson(artistJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    @Override
    protected Void doInBackground(String... params) {
        getArtistInfo(params);
        return null;
    }
}
