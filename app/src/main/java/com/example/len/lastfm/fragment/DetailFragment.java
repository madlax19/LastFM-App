package com.example.len.lastfm.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.len.lastfm.R;
import com.example.len.lastfm.adapter.SimilarAdapter;
import com.example.len.lastfm.adapter.TopTracksAdapter;
import com.example.len.lastfm.data.LastFMContract.ArtistEntry;
import com.example.len.lastfm.data.LastFMContract.SimilarEntry;
import com.example.len.lastfm.data.LastFMContract.TopTrackEntry;
import com.example.len.lastfm.tasks.GetArtistInfoTask;
import com.example.len.lastfm.tasks.GetSimilarTask;
import com.example.len.lastfm.tasks.GetTopTrackTask;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimilarAdapter mSimilarAdapter;
    private TopTracksAdapter mTopTrackAdapter;
    private ListView mSimilarListView;
    private ListView mTopTrackListView;
    private Parcelable mSimilarState;
    private Parcelable mTopTrackState;
    private ProgressBar mInfoSpiner;
    private ProgressBar mTopTrackSpiner;
    private ProgressBar mSimilarSpiner;
    private Uri mUri;
    private ShareActionProvider mShareActionProvider;
    private String mShareContent;

    private static final int ROOT_LOADER = 0;
    private static final int TOP_TRACKS_LOADER = 2;
    private static final int SIMILAR_LOADER = 3;

    private static final String SELECTED_TAB_KEY = "selected_tab";
    private static final String SELECTED_TOP_TRACK_KEY = "selected_top_track";
    private static final String SELECTED_SIMILAR_KEY = "selected_similar";

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

    private static final String[] SIMILAR_COLUMNS = {
            SimilarEntry.TABLE_NAME + "." + SimilarEntry._ID,
            SimilarEntry.COLUMN_SIMILAR_MBID,
            SimilarEntry.COLUMN_SIMILAR_NAME,
            SimilarEntry.COLUMN_SIMILAR_IMAGE,
            SimilarEntry.COLUMN_SIMILAR_ARTIST_MBID
    };

    public static final int COLUMN_SIMILAR_ID = 0;
    public static final int COLUMN_SIMILAR_MBID = 1;
    public static final int COLUMN_SIMILAR_NAME = 2;
    public static final int COLUMN_SIMILAR_IMAGE = 3;
    public static final int COLUMN_SIMILAR_ARTIST_MBID = 4;


    private static final String[] TOP_TRACKS_COLUMNS = {
            TopTrackEntry.TABLE_NAME + "." + TopTrackEntry._ID,
            TopTrackEntry.COLUMN_TOP_TRACK_MBID,
            TopTrackEntry.COLUMN_TOP_TRACK_NAME,
            TopTrackEntry.COLUMN_TOP_TRACK_PLAYCOUNT,
            TopTrackEntry.COLUMN_TOP_TRACK_LISTENERS,
            TopTrackEntry.COLUMN_TOP_TRACK_IMAGE,
            TopTrackEntry.COLUMN_TOP_TRACK_ARTIST_MBID
    };

    public static final int COLUMN_TOP_TRACK_ID = 0;
    public static final int COLUMN_TOP_TRACK_MBID = 1;
    public static final int COLUMN_TOP_TRACK_NAME = 2;
    public static final int COLUMN_TOP_TRACK_PLAYCOUNT = 3;
    public static final int COLUMN_TOP_TRACK_LISTENERS = 4;
    public static final int COLUMN_TOP_TRACK_IMAGE = 5;
    public static final int COLUMN_TOP_TRACK_ARTIST_MBID = 6;

    public interface Callback {
        public void onSimilarItemSelected(Uri uri);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable("URI");
        }
        //spiners setup
        mInfoSpiner = (ProgressBar)view.findViewById(R.id.artistInfoSpinner);
        mTopTrackSpiner = (ProgressBar)view.findViewById(R.id.topTrackSpinner);
        mSimilarSpiner = (ProgressBar)view.findViewById(R.id.similarSpinner);

        mInfoSpiner.setVisibility(View.GONE);
        mTopTrackSpiner.setVisibility(View.GONE);
        mSimilarSpiner.setVisibility(View.GONE);

        //Tab widget setup
        TabHost tabs = (TabHost) view.findViewById(android.R.id.tabhost);

        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("tag1");
        spec.setContent(R.id.tab1);
        spec.setIndicator(getResources().getString(R.string.tab1_title));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tag2");
        spec.setContent(R.id.tab2);
        spec.setIndicator(getResources().getString(R.string.tab2_title));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tag3");
        spec.setContent(R.id.tab3);
        spec.setIndicator(getResources().getString(R.string.tab3_title));
        tabs.addTab(spec);

        setHasOptionsMenu(true);

        for(int i=0; i < tabs.getTabWidget().getChildCount(); i++)
            tabs.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_bar_indicator);

        //similar list view setup
        mSimilarAdapter = new SimilarAdapter(getActivity(), null, 0);
        mSimilarListView = (ListView) view.findViewById(R.id.similarListView);
        mSimilarListView.setAdapter(mSimilarAdapter);

        mTopTrackAdapter = new TopTracksAdapter(getActivity(), null, 0);
        mTopTrackListView = (ListView) view.findViewById(R.id.topTracksListView);
        mTopTrackListView.setAdapter(mTopTrackAdapter);

        tabs.setCurrentTab(0);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_TAB_KEY)) {
                tabs.setCurrentTab(savedInstanceState.getInt(SELECTED_TAB_KEY));
            }
            if(savedInstanceState.containsKey(SELECTED_SIMILAR_KEY)) {
                mSimilarState = savedInstanceState.getParcelable(SELECTED_SIMILAR_KEY);
            }
            if (savedInstanceState.containsKey(SELECTED_TOP_TRACK_KEY)) {
                mTopTrackState = savedInstanceState.getParcelable(SELECTED_TOP_TRACK_KEY);
            }
        }

        mSimilarListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onSimilarItemSelected(ArtistEntry.buildArtistUriByMMBID(cursor.getString(COLUMN_SIMILAR_MBID)));
                }
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        TabHost tabs = (TabHost) getView().findViewById(android.R.id.tabhost);
        outState.putInt(SELECTED_TAB_KEY, tabs.getCurrentTab());
        outState.putParcelable(SELECTED_SIMILAR_KEY, mSimilarListView.onSaveInstanceState());
        outState.putParcelable(SELECTED_TOP_TRACK_KEY, mTopTrackListView.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(ROOT_LOADER, null, this);
        getLoaderManager().initLoader(SIMILAR_LOADER, null, this);
        getLoaderManager().initLoader(TOP_TRACKS_LOADER, null, this);

        startTasks();
        super.onActivityCreated(savedInstanceState);
    }

    private void startTasks()
    {
        Intent intent = getActivity().getIntent();
        Uri data = null;
        if(mUri != null) {
            data = mUri;
        } else {
            if (intent != null || intent.getData() != null) {
                data = intent.getData();
            }
        }
        if(data != null) {
            String artistMBID = ArtistEntry.getArtistIdFromUri(data);

            GetArtistInfoTask artistInfoTask = new GetArtistInfoTask(getActivity());
            artistInfoTask.execute(artistMBID);
            mInfoSpiner.setVisibility(View.VISIBLE);

            GetTopTrackTask topTrackTask = new GetTopTrackTask(getActivity());
            topTrackTask.execute(artistMBID);
            mTopTrackSpiner.setVisibility(View.VISIBLE);

            GetSimilarTask similarTask = new GetSimilarTask(getActivity());
            similarTask.execute(artistMBID);
            mSimilarSpiner.setVisibility(View.VISIBLE);

            getLoaderManager().restartLoader(ROOT_LOADER, null, this);
            getLoaderManager().restartLoader(SIMILAR_LOADER, null, this);
            getLoaderManager().restartLoader(TOP_TRACKS_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Intent intent = getActivity().getIntent();
        Uri data;
        if(mUri != null) {
            data = mUri;
        } else {
            if (intent != null || intent.getData() != null) {
                data = intent.getData();
            } else {
                return null;
            }
        }

        if(data == null) {
            return null;
        }

        switch (id) {
            case ROOT_LOADER: {
                return new CursorLoader(
                        getActivity(),
                        data,
                        ARTIST_COLUMNS,
                        null,
                        null,
                        null
                );
            }
            case SIMILAR_LOADER: {
                Uri getSimilarURI = SimilarEntry.buildSimilarUriByMMBID(SimilarEntry.getSimilarIdFromIntentUri(data));
                String sortOrder = SimilarEntry.COLUMN_SIMILAR_NAME + " ASC";
                return new CursorLoader(
                        getActivity(),
                        getSimilarURI,
                        SIMILAR_COLUMNS,
                        null,
                        null,
                        sortOrder
                );
            }
            case TOP_TRACKS_LOADER: {
                Uri getTopTracksURI = TopTrackEntry.buildTopTrackUriByMMBID(TopTrackEntry.getTopTrackIdFromIntentUri(data));
                String sortOrder = TopTrackEntry.COLUMN_TOP_TRACK_PLAYCOUNT + " ASC LIMIT 15";
                return new CursorLoader(
                        getActivity(),
                        getTopTracksURI,
                        TOP_TRACKS_COLUMNS,
                        null,
                        null,
                        sortOrder
                );
            }
            default:
                break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch(loader.getId()) {
            case ROOT_LOADER: {
                if (!data.moveToFirst()) {
                    return;
                }
                TextView titleTextView = (TextView) getView().findViewById(R.id.titleTextView);
                String title = "<h3>" + data.getString(COLUMN_ARTIST_NAME) + "</h3>";
                long yearformed = data.getLong(COLUMN_ARTIST_YEARFORMED);
                if(yearformed > 0) {
                    title += "<br><h5><b>" + yearformed + "</b>";
                }
                String placeformed = data.getString(COLUMN_ARTIST_PLACEFORMED);
                if(placeformed != null && !placeformed.isEmpty()) {
                    if(yearformed > 0) title += "<b>,</b>";
                    if(!placeformed.isEmpty()) {
                        title += "<b> " + placeformed + "</b>";
                    }
                }
                title += "</h5>";
                titleTextView.setText(Html.fromHtml(title));
                TextView bioTextView = (TextView) getView().findViewById(R.id.bioTextView);
                String bio = data.getString(COLUMN_ARTIST_BIO);
                if(bio != null) {
                    if(!bio.isEmpty()) {
                        bioTextView.setMovementMethod(LinkMovementMethod.getInstance());
                        bioTextView.setText(Html.fromHtml(bio));
                    }
                }

                ImageView imageView = (ImageView) getView().findViewById(R.id.imageView);
                Picasso.with(getActivity()).load(data.getString(COLUMN_ARTIST_IMAGE)).into(imageView);

                String artistUrl = data.getString(COLUMN_ARTIST_URL);
                if(artistUrl != null) {
                    mShareContent = artistUrl;
                }

                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareArtistIntent());
                }
                mInfoSpiner.setVisibility(View.GONE);
                break;
            }
            case SIMILAR_LOADER: {
                mSimilarAdapter.swapCursor(data);
                if(mSimilarState != null) {
                    mSimilarListView.onRestoreInstanceState(mSimilarState);
                }
                mSimilarSpiner.setVisibility(View.VISIBLE);
                break;
            }
            case TOP_TRACKS_LOADER: {
                mTopTrackAdapter.swapCursor(data);
                if(mTopTrackState != null) {
                    mTopTrackListView.onRestoreInstanceState(mTopTrackState);
                }
                mTopTrackSpiner.setVisibility(View.VISIBLE);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch(loader.getId()) {
            case SIMILAR_LOADER: {
                mSimilarAdapter.swapCursor(null);
                break;
            }
            case TOP_TRACKS_LOADER: {
                mTopTrackAdapter.swapCursor(null);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment_menu, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mShareContent != null) {
            mShareActionProvider.setShareIntent(createShareArtistIntent());
        }
    }

    private Intent createShareArtistIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareContent);
        return shareIntent;
    }
}
