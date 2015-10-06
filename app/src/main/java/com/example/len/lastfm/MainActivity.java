package com.example.len.lastfm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.example.len.lastfm.fragment.DetailFragment;
import com.example.len.lastfm.fragment.LastFMFragment;

public class MainActivity extends ActionBarActivity implements LastFMFragment.Callback, DetailFragment.Callback {

    private final String LASTFMFRAGMENT_TAG = "LFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (findViewById(R.id.artist_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.artist_detail_container, new DetailFragment(), LASTFMFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        openDetail(contentUri);
    }

    @Override
    public void onSimilarItemSelected(Uri contentUri){
        openDetail(contentUri);
    }

    private void openDetail(Uri uri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable("URI", uri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_detail_container, fragment, LASTFMFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(uri);
            startActivity(intent);
        }
    }
}
