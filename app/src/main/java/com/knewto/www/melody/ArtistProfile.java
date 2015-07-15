package com.knewto.www.melody;

/**
 * Created by William Wallis on 15/07/2015.
 * Class defining model for Artist Profile to show in search results
 */

import android.os.Parcel;
import android.os.Parcelable;

public class ArtistProfile implements Parcelable {
    public String name;
    public String image;
    public String id;

    public ArtistProfile (String name, String image, String id) {
        this.name = name;
        this.image = image;
        this.id = id;
    }

    private ArtistProfile(Parcel in){
        name = in.readString();
        image = in.readString();
        id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(image);
        parcel.writeString(id);
    }

    public final Parcelable.Creator<ArtistProfile> CREATOR = new Parcelable.Creator<ArtistProfile>() {
        @Override
        public ArtistProfile createFromParcel(Parcel parcel) {
            return new ArtistProfile(parcel);
        }

        @Override
        public ArtistProfile[] newArray(int i) {
            return new ArtistProfile[i];
        }

    };
}