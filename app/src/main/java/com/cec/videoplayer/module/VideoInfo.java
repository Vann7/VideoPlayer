package com.cec.videoplayer.module;

public class VideoInfo {
    private String id;//视频id
    private String title;//名称
    private String hits;//点击次数
    private String swfurl;//flash地址
    private String playurl;//播放地址
    private String updateTime;//更新时间
    private String image;//缩略图路径
    private String href;//节目页面播放地址

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHits() {
        return hits;
    }

    public void setHits(String hits) {
        this.hits = hits;
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

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

}
