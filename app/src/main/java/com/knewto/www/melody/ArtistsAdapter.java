package com.knewto.www.melody;

/**
 * Created by William Wallis on 15/07/2015.
 * ArrayAdapter to contain Artist Profiles returned from search
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

public class ArtistsAdapter extends ArrayAdapter<ArtistProfile> {
    public ArtistsAdapter(Context context, ArrayList<ArtistProfile> artistProfile) {
        super(context, 0, artistProfile);
    }

    class ViewHolder {
        TextView artistName;
        ImageView artistImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Create a viewholder to store my view ids
        ViewHolder holder;
        // Get the data item for this position
        ArtistProfile artistProfile = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.artist_list_item, parent, false);
            // store view Ids in the view holder
            holder = new ViewHolder();
            holder.artistName = (TextView) convertView.findViewById(R.id.item_artist_name);
            holder.artistImage = (ImageView) convertView.findViewById(R.id.item_artist_pic);
            convertView.setTag(holder);
        }
        else {
            // For existing views retrieve view ids and store in holder
            holder = (ViewHolder) convertView.getTag();
        }
        // Lookup view for data population
        TextView artistName = holder.artistName;
        // Populate the data into the template view using the data object
        artistName.setText(artistProfile.name);

        // Load image with Picasso
        ImageView artistImage = holder.artistImage;

        Picasso.with(getContext())
                .load(artistProfile.image)
                .into(artistImage);
        // Return the completed view to render on screen
        return convertView;
    }
}