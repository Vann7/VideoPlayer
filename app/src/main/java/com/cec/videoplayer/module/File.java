package com.cec.videoplayer.module;

/**
 * User: cec
 * Date: 2019/7/3
 * Time: 11:40 AM
 * 视频文件信息
 */
public class File {
    private String id;
    private String bitrate;
    private String format;
    private double fps;
    private String compressformat;
    private int height;
    private String path;
    private String playtime;
    private String title;
    private int width;
    private int filesize;
    private String swfurl;
    private String playurl;
    private String jsurl;

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

    public String getPlayurl() {
        return playurl;
    }

    public void setPlayurl(String playurl) {
        this.playurl = playurl;
    }

    public String getJsurl() {
        return jsurl;
    }

    public void setJsurl(String jsurl) {
        this.jsurl = jsurl;
    }
}

