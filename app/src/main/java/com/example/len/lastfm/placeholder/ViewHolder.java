package com.example.len.lastfm.placeholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.len.lastfm.R;

public class ViewHolder {
    public final ImageView itemImage;
    public final TextView itemText;

    public ViewHolder(View view) {
        itemImage = (ImageView) view.findViewById(R.id.itemImage);
        itemText = (TextView) view.findViewById(R.id.itemText);
    }
}