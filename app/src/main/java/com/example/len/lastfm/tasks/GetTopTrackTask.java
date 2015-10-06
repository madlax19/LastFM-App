package com.example.len.lastfm.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.len.lastfm.data.LastFMContract;

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

public class GetTopTrackTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = GetArtistInfoTask.class.getSimpleName();
    private final Context mContext;

    public GetTopTrackTask(Context context) {
        mContext = context;
    }

    private void getTopTracksFromJson(String topTracksJsonStr) throws JSONException {
        try {
            JSONObject topTracksJson = new JSONObject(topTracksJsonStr);
            if(topTracksJson.has("toptracks")) {
                JSONArray topTracksArray = topTracksJson.getJSONObject("toptracks").getJSONArray("track");
                Vector<ContentValues> cVVector = new Vector<ContentValues>();
                for (int i = 0; i < topTracksArray.length(); i++) {
                    JSONObject track = topTracksArray.getJSONObject(i);
                    String trackMbid = track.getString("mbid");
                    if (!trackMbid.isEmpty()) {
                        String trackName = track.getString("name");
                        String trackImage = "";
                        if (track.has("image")) {
                            JSONArray images = track.getJSONArray("image");
                            if (images.length() > 2) {
                                trackImage = track.getJSONArray("image").getJSONObject(2).getString("#text");
                            }
                        }
                        int playcount = track.getInt("playcount");
                        int listeners = track.getInt("listeners");
                        String artistMbid = track.getJSONObject("artist").getString("mbid");

                        ContentValues artistValues = new ContentValues();
                        artistValues.put(LastFMContract.TopTrackEntry.COLUMN_TOP_TRACK_MBID, trackMbid);
                        artistValues.put(LastFMContract.TopTrackEntry.COLUMN_TOP_TRACK_NAME, trackName);
                        artistValues.put(LastFMContract.TopTrackEntry.COLUMN_TOP_TRACK_IMAGE, trackImage);
                        artistValues.put(LastFMContract.TopTrackEntry.COLUMN_TOP_TRACK_PLAYCOUNT, playcount);
                        artistValues.put(LastFMContract.TopTrackEntry.COLUMN_TOP_TRACK_LISTENERS, listeners);
                        artistValues.put(LastFMContract.TopTrackEntry.COLUMN_TOP_TRACK_ARTIST_MBID, artistMbid);

                        cVVector.add(artistValues);
                    }
                }

                final String sTopTrackMBIDSelection =
                        LastFMContract.TopTrackEntry.TABLE_NAME +
                                "." + LastFMContract.TopTrackEntry.COLUMN_TOP_TRACK_MBID + " = ? ";
                Vector<ContentValues> valuesToInsert = new Vector<ContentValues>();
                for (ContentValues values : cVVector) {
                    String valuesMbid = values.get(LastFMContract.TopTrackEntry.COLUMN_TOP_TRACK_MBID).toString();
                    if (mContext.getContentResolver().update(LastFMContract.TopTrackEntry.CONTENT_URI, values, sTopTrackMBIDSelection, new String[]{valuesMbid}) == 0) {
                        valuesToInsert.add(values);
                    }
                }

                int inserted = 0;
                // add to database
                if (valuesToInsert.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[valuesToInsert.size()];
                    valuesToInsert.toArray(cvArray);
                    inserted = mContext.getContentResolver().bulkInsert(LastFMContract.TopTrackEntry.CONTENT_URI, cvArray);
                }

                Log.d(LOG_TAG, "GetTopTracksTask Complete. " + inserted + " Inserted");
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void getTopTracks(String... params) {
        if (params.length == 0) {
            return;
        }
        String artistQuery = params[0];
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String tracksJsonStr = null;

        String apiMethod = "artist.gettoptracks";
        String format = "json";
        String apiKey = "168436bdde0b53756590ce0fa0da4e9b";
        String limit = "15";

        try {
            final String API_BASE_URL =
                    "http://ws.audioscrobbler.com/2.0/?";
            final String METHOD_PARAM = "method";
            final String ARTIST_PARAM = "mbid";
            final String API_KEY_PARAM = "api_key";
            final String FORMAT_PARAM = "format";
            final String LIMIT_PARAM = "limit";

            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                    .appendQueryParameter(METHOD_PARAM, apiMethod)
                    .appendQueryParameter(ARTIST_PARAM, artistQuery)
                    .appendQueryParameter(API_KEY_PARAM, apiKey)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(LIMIT_PARAM, limit)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
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
            tracksJsonStr = buffer.toString();
            getTopTracksFromJson(tracksJsonStr);
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
        getTopTracks(params);
        return null;
    }
}
