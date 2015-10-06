package com.example.len.lastfm.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.example.len.lastfm.R;
import com.example.len.lastfm.fragment.LastFMFragment;
import com.example.len.lastfm.placeholder.ViewHolder;
import com.squareup.picasso.Picasso;

public class LastFMAdapter  extends CursorAdapter {
    private Context mContext;
    public LastFMAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.lastfm_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.itemText.setText(cursor.getString(LastFMFragment.COLUMN_ARTIST_NAME));
        String uriStr = cursor.getString(LastFMFragment.COLUMN_ARTIST_IMAGE);
        if(!uriStr.isEmpty()) {
            Picasso.with(mContext).load(uriStr).resize(60, 60).into(viewHolder.itemImage);
        }
    }
}
