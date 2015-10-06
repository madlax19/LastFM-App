package com.example.len.lastfm.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.len.lastfm.R;
import com.example.len.lastfm.Utility;
import com.example.len.lastfm.adapter.LastFMAdapter;
import com.example.len.lastfm.data.LastFMContract.ArtistEntry;
import com.example.len.lastfm.tasks.SearchArtistTask;

public class LastFMFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private LastFMAdapter mLastFMAdapter;
    private ProgressBar mSpiner;
    private static final int LASTFM_LOADER = 0;

    private static final String[] ARTIST_COLUMNS = {
            ArtistEntry.TABLE_NAME + "." + ArtistEntry._ID,
            ArtistEntry.COLUMN_ARTIST_MBID,
            ArtistEntry.COLUMN_ARTIST_NAME,
            ArtistEntry.COLUMN_ARTIST_IMAGE,
            ArtistEntry.COLUMN_ARTIST_BIO,
            ArtistEntry.COLUMN_ARTIST_YEARFORMED,
            ArtistEntry.COLUMN_ARTIST_PLACEFORMED,
            ArtistEntry.COLUMN_ARTIST_URL
    };

    public static final int COLUMN_ARTIST_ID = 0;
    public static final int COLUMN_ARTIST_MBID = 1;
    public static final int COLUMN_ARTIST_NAME = 2;
    public static final int COLUMN_ARTIST_IMAGE = 3;
    public static final int COLUMN_ARTIST_BIO = 4;
    public static final int COLUMN_ARTIST_YEARFORMED = 5;
    public static final int COLUMN_ARTIST_PLACEFORMED = 6;
    public static final int COLUMN_ARTIST_URL = 7;

    public LastFMFragment() {

    }

    public interface Callback {
        public void onItemSelected(Uri uri);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mSpiner = (ProgressBar)view.findViewById(R.id.artistSpinner);
        mSpiner.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        // The CursorAdapter will take data from our cursor and populate the ListView.
        mLastFMAdapter = new LastFMAdapter(getActivity(), null, 0);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) view.findViewById(R.id.list_view);
        listView.setAdapter(mLastFMAdapter);

        // We'll call our MainActivity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(ArtistEntry.buildArtistUriByMMBID(cursor.getString(COLUMN_ARTIST_MBID)));
                }
            }
        });

        Button button = (Button) view.findViewById(R.id.searchButton);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonSearch(v);
            }
        });

        String str = Utility.getLastSearchStr(getActivity());
        ((EditText)view.findViewById(R.id.inputSearch)).setText(str);
    }

    public void buttonSearch(View view) {
        String searchString = getSearchString();
        Utility.setLastSearchStr(getActivity(), searchString);
        searchArtist(searchString);
        getLoaderManager().restartLoader(LASTFM_LOADER, null, this);
    }

    private String getSearchString() {
        return ((EditText)getView().findViewById(R.id.inputSearch)).getText().toString();
    }

    private void searchArtist(String searchString) {
        if (!searchString.isEmpty()) {
            SearchArtistTask searchTask = new SearchArtistTask(getActivity());
            searchTask.execute(searchString);
            mSpiner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LASTFM_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Sort order:  Ascending, by date.
        String sortOrder = ArtistEntry.COLUMN_ARTIST_NAME + " ASC";
        Uri queryUri;
        if(getSearchString().isEmpty()) {
            queryUri = ArtistEntry.buildArtistUriByName("1234567890987654321");
        } else {
            queryUri = ArtistEntry.buildArtistUriByName(getSearchString());
        }

        return new CursorLoader(getActivity(),
                queryUri,
                ARTIST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mLastFMAdapter.swapCursor(cursor);
        mSpiner.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mLastFMAdapter.swapCursor(null);
    }
}
