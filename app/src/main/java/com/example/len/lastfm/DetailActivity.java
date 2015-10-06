package com.example.len.lastfm;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.example.len.lastfm.fragment.DetailFragment;

public class DetailActivity extends ActionBarActivity implements DetailFragment.Callback {
    private final String LASTFMFRAGMENT_TAG = "LFDTAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        DetailFragment fragment = new DetailFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artist_detail_container, fragment, LASTFMFRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onSimilarItemSelected(Uri uri) {
        Intent intent = new Intent(this, DetailActivity.class)
                .setData(uri);
        startActivity(intent);
    }
}
