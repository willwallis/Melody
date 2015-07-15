package com.knewto.www.melody;

/**
 * Created by William Wallis on 15/07/2015.
 * Class defining model for Track to show in search results
 */

import android.os.Parcel;
import android.os.Parcelable;

public class TopTrack implements Parcelable {
    public String name;
    public String image;
    public String album;

    public TopTrack(String name, String image, String album) {
        this.name = name;
        this.image = image;
        this.album = album;
    }

    private TopTrack(Parcel in){
        name = in.readString();
        image = in.readString();
        album = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(image);
        parcel.writeString(album);
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