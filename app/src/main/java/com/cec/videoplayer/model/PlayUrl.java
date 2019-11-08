package com.cec.videoplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PlayUrl implements Parcelable {
    private String height;
    private String definition;
    private String width;
    private String bitrate;
    private String url;

    public void setHeight(String height) {
        this.height = height;
    }

    public String getHeight() {
        return height;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getWidth() {
        return width;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinition() {
        return definition;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public PlayUrl(String height, String width, String bitrate, String definition, String url) {
        this.height = height;
        this.width = width;
        this.bitrate = bitrate;
        this.definition = definition;
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int arg1) {
// TODO Auto-generated method stub
        parcel.writeString(height);
        parcel.writeString(width);
        parcel.writeString(definition);
        parcel.writeString(bitrate);
        parcel.writeString(url);
    }

    public static final Parcelable.Creator<PlayUrl> CREATOR = new Creator<PlayUrl>() {
        public PlayUrl createFromParcel(Parcel source) {
            PlayUrl pTemp = new PlayUrl("", "", "", "", "");

            pTemp.height = source.readString();
            pTemp.width = source.readString();
            pTemp.definition = source.readString();
            pTemp.bitrate = source.readString();
            pTemp.url = source.readString();

            return pTemp;
        }

        public PlayUrl[] newArray(int size) {
            return new PlayUrl[size];
        }
    };
}
