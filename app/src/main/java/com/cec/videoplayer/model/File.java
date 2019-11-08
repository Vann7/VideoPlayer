package com.cec.videoplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: cec
 * Date: 2019/7/3
 * Time: 11:40 AM
 * 视频文件信息
 */
public class File implements Parcelable {
    private String id;
    private String jsurl;
    private int filesize;
    private String title;
    private int height;
    private String swfurl;
    private List<PlayUrl> downurl;
    private int width;
    private double fps;
    private String path;
    private String bitrate;
    private String format;
    private String compressformat;
    private String playtime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public double getFps() {
        return fps;
    }

    public void setFps(double fps) {
        this.fps = fps;
    }

    public String getCompressformat() {
        return compressformat;
    }

    public void setCompressformat(String compressformat) {
        this.compressformat = compressformat;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPlaytime() {
        return playtime;
    }

    public void setPlaytime(String playtime) {
        this.playtime = playtime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public String getSwfurl() {
        return swfurl;
    }

    public void setSwfurl(String swfurl) {
        this.swfurl = swfurl;
    }

    public List<PlayUrl> getPlayurl() {
        return downurl;
    }

    public void setPlayurl(List<PlayUrl> playurl) {
        this.downurl = playurl;
    }

    public String getJsurl() {
        return jsurl;
    }

    public void setJsurl(String jsurl) {
        this.jsurl = jsurl;
    }

    public File() {
        super();
    }

    public File(Parcel source) {
        super();
        id = source.readString();
        width = source.readInt();
        height = source.readInt();
        filesize = source.readInt();
        fps = source.readDouble();
        jsurl = source.readString();
        title = source.readString();
        swfurl = source.readString();
        bitrate = source.readString();
        format = source.readString();
        playtime = source.readString();
        path = source.readString();
        compressformat = source.readString();
        if(downurl ==null){
            downurl = new ArrayList<PlayUrl>(){
            };
        }
        source.readTypedList(downurl, PlayUrl.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int arg1) {
// TODO Auto-generated method stub
        parcel.writeString(id);
        parcel.writeString(jsurl);
        parcel.writeInt(filesize);
        parcel.writeInt(height);
        parcel.writeInt(width);
        parcel.writeDouble(fps);
        parcel.writeString(title);
        parcel.writeString(swfurl);
        parcel.writeString(bitrate);
        parcel.writeString(format);
        parcel.writeString(playtime);
        parcel.writeString(path);
        parcel.writeString(compressformat);
        parcel.writeTypedList(downurl);

    }

    public static final Parcelable.Creator<File> CREATOR = new Creator<File>() {
        @Override
        public File[] newArray(int size) {
            return new File[size];
        }

        @Override
        public File createFromParcel(Parcel source) {
            return new File(source);
        }
    };
}

