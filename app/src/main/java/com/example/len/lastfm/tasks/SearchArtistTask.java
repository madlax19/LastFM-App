package com.example.len.lastfm.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import static com.example.len.lastfm.data.LastFMContract.ArtistEntry;


public class SearchArtistTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = SearchArtistTask.class.getSimpleName();
    private final Context mContext;

    public SearchArtistTask(Context context) {
        mContext = context;
    }

    private boolean DEBUG = true;

    private void getArtistsDataFromJson(String artistsJsonStr)
            throws JSONException {

        try {
            JSONObject artistJson = new JSONObject(artistsJsonStr);
            JSONArray artistArray = artistJson.getJSONObject("results").getJSONObject("artistmatches").getJSONArray("artist");
            Vector<ContentValues> cVVector = new Vector<ContentValues>(artistArray.length());
            for(int i = 0; i < artistArray.length(); i++) {
                // Get the JSON object representing the day
                JSONObject artist = artistArray.getJSONObject(i);
                String mbid = artist.getString("mbid");
                if(!mbid.isEmpty()) {
                    String name = artist.getString("name");
                    String image = artist.getJSONArray("image").getJSONObject(2).getString("#text");
                    String url = artist.getString("url");

                    ContentValues artistValues = new ContentValues();
                    artistValues.put(ArtistEntry.COLUMN_ARTIST_MBID, mbid);
                    artistValues.put(ArtistEntry.COLUMN_ARTIST_NAME, name);
                    artistValues.put(ArtistEntry.COLUMN_ARTIST_IMAGE, image);
                    artistValues.put(ArtistEntry.COLUMN_ARTIST_URL, url);

                    cVVector.add(artistValues);
                }
            }

            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                ContentResolver contentResolver = mContext.getContentResolver();
                inserted = contentResolver.bulkInsert(ArtistEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "SearchArtistTask Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }
        String artistQuery = params[0];
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String artistsJsonStr = null;

        String apiMethod = "artist.search";
        String format = "json";
        String apiKey = "168436bdde0b53756590ce0fa0da4e9b";

        try {
            final String API_BASE_URL =
                    "http://ws.audioscrobbler.com/2.0/?";
            final String METHOD_PARAM = "method";
            final String ARTIST_PARAM = "artist";
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
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            artistsJsonStr = buffer.toString();
            getArtistsDataFromJson(artistsJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
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
        return null;
    }
}
