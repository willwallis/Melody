package com.knewto.www.melody;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.knewto.www.melody.data.TrackContract.TrackEntry;
import com.squareup.picasso.Picasso;

/**
 * Created by willwallis on 9/14/15.
 */
public class TrackCursorAdapter extends CursorAdapter {

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final TextView trackName;
        public final TextView albumName;
        public final ImageView trackImage;

        public ViewHolder(View view) {
            trackName = (TextView) view.findViewById(R.id.item_track_name);
            albumName = (TextView) view.findViewById(R.id.item_album_name);
            trackImage = (ImageView) view.findViewById(R.id.item_track_pic);

        }
    }

    public TrackCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.track_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Get values from cursor
        String trackName = cursor.getString(cursor.getColumnIndexOrThrow(TrackEntry.COLUMN_TRACK_NAME));
        String albumName = cursor.getString(cursor.getColumnIndexOrThrow(TrackEntry.COLUMN_ALBUM_NAME));
        String trackImage = cursor.getString(cursor.getColumnIndexOrThrow(TrackEntry.COLUMN_ALBUM_IMAGE));

        // Set view values
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.trackName.setText(trackName);
        viewHolder.albumName.setText(albumName);

        // Load image with Picasso
        Picasso.with(context)
                .load(trackImage)
                .into(viewHolder.trackImage);

    }
}
