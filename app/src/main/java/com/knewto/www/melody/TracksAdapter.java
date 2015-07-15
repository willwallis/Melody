package com.knewto.www.melody;

/**
 * Created by William Wallis on 15/07/2015.
 * ArrayAdapter to contain Top 10 Tracks returned from search
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TracksAdapter extends ArrayAdapter<TopTrack> {
    public TracksAdapter(Context context, ArrayList<TopTrack> topTrack) {
        super(context, 0, topTrack);
    }

    class ViewHolder {
        TextView trackName;
        TextView albumName;
        ImageView trackImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Create a viewholder to store my view ids
        ViewHolder holder;
        // Get the data item for this position
        TopTrack topTrack = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.track_list_item, parent, false);
            // store view Ids in the view holder
            holder = new ViewHolder();
            holder.trackName = (TextView) convertView.findViewById(R.id.item_track_name);
            holder.albumName = (TextView) convertView.findViewById(R.id.item_album_name);
            holder.trackImage = (ImageView) convertView.findViewById(R.id.item_track_pic);
            convertView.setTag(holder);
        }
        else {
            // For existing views retrieve view ids and store in holder
            holder = (ViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        holder.trackName.setText(topTrack.name);
        holder.albumName.setText(topTrack.album);

        // Load image with Picasso
        Picasso.with(getContext())
                .load(topTrack.image)
                .into(holder.trackImage);
        // Return the completed view to render on screen
        return convertView;
    }
}
