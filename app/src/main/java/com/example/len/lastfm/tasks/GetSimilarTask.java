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

public class GetSimilarTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = GetArtistInfoTask.class.getSimpleName();
    private final Context mContext;

    public GetSimilarTask(Context context) {
        mContext = context;
    }

    private void getSimilarFromJson(String similarJsonStr, String parentMbid) throws JSONException {
        try {
            JSONObject similarJson = new JSONObject(similarJsonStr);
            JSONObject similarArtistObjetc = similarJson.getJSONObject("similarartists");
            if(!similarArtistObjetc.has("#text")) {
                JSONArray similarArray = similarArtistObjetc.getJSONArray("artist");
                Vector<ContentValues> cVVector = new Vector<ContentValues>();
                for (int i = 0; i < similarArray.length(); i++) {
                    JSONObject track = similarArray.getJSONObject(i);
                    String mbid = track.getString("mbid");
                    if (!mbid.isEmpty()) {
                        String name = track.getString("name");
                        String trackImage = "";
                        if (track.has("image")) {
                            JSONArray images = track.getJSONArray("image");
                            if (images.length() > 2) {
                                trackImage = track.getJSONArray("image").getJSONObject(2).getString("#text");
                            }
                        }
                        ContentValues similarValues = new ContentValues();
                        similarValues.put(LastFMContract.SimilarEntry.COLUMN_SIMILAR_MBID, mbid);
                        similarValues.put(LastFMContract.SimilarEntry.COLUMN_SIMILAR_NAME, name);
                        similarValues.put(LastFMContract.SimilarEntry.COLUMN_SIMILAR_IMAGE, trackImage);
                        similarValues.put(LastFMContract.SimilarEntry.COLUMN_SIMILAR_ARTIST_MBID, parentMbid);

                        cVVector.add(similarValues);
                    }
                }

                final String sSimilarMBIDSelection =
                        LastFMContract.SimilarEntry.TABLE_NAME +
                                "." + LastFMContract.SimilarEntry.COLUMN_SIMILAR_MBID + " = ? ";
                Vector<ContentValues> valuesToInsert = new Vector<ContentValues>();
                for (ContentValues values : cVVector) {
                    String valuesMbid = values.get(LastFMContract.SimilarEntry.COLUMN_SIMILAR_MBID).toString();
                    if (mContext.getContentResolver().update(LastFMContract.SimilarEntry.CONTENT_URI, values, sSimilarMBIDSelection, new String[]{valuesMbid}) == 0) {
                        valuesToInsert.add(values);
                    }
                }

                int inserted = 0;
                // add to database
                if (valuesToInsert.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[valuesToInsert.size()];
                    valuesToInsert.toArray(cvArray);
                    inserted = mContext.getContentResolver().bulkInsert(LastFMContract.SimilarEntry.CONTENT_URI, cvArray);
                }

                Log.d(LOG_TAG, "GetSimilarTask Complete. " + inserted + " Inserted");
            } else {
                Log.d(LOG_TAG, "Similar not found!");
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

        String apiMethod = "artist.getsimilar";
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
            getSimilarFromJson(tracksJsonStr, artistQuery);
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
