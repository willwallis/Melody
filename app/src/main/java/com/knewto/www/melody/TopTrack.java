package com.knewto.www.melody;

/**
 * Created by William Wallis on 15/07/2015.
 * Class defining model for Track to show in search results
 */

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class TopTrack implements Parcelable {
    public String name;
    public String image;
    public String bigImage;
    public String album;
    public String trackUrl;

    public TopTrack(String name, String image, String bigImage, String album, String trackUrl) {
        this.name = name;
        this.image = image;
        this.bigImage = bigImage;
        this.album = album;
        this.trackUrl = trackUrl;
    }

    private TopTrack(Parcel in){
        name = in.readString();
        image = in.readString();
        bigImage = in.readString();
        album = in.readString();
        trackUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(image);
        parcel.writeString(bigImage);
        parcel.writeString(album);
        parcel.writeString(trackUrl);
    }

    public final Parcelable.Creator<TopTrack> CREATOR = new Parcelable.Creator<TopTrack>() {
        @Override
        public TopTrack createFromParcel(Parcel parcel) {
            return new TopTrack(parcel);
        }

        @Override
        public TopTrack[] newArray(int i) {
            return new TopTrack[i];
        }

    };
}