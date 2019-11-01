package com.cec.videoplayer.module;

import java.util.List;

public class VideoInfo {
    private String id;//视频id
    private String title;//名称
    private String hits;//点击次数
    private String swfurl;//flash地址
    private List<PlayUrl> downurl;//播放地址
    private String updateTime;//更新时间
    private String image;//缩略图路径
    private String href;//节目页面播放地址
    private int limit;//观看权限;0为可以观看，1为无权限
    private String fileattr;//0表示直播，1表示点播
    private String address;//直播URL

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

    public List<PlayUrl> getPlayurl() {
        return downurl;
    }

    public void setPlayurl(List<PlayUrl> playurl) {
        this.downurl = playurl;
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

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getFileattr() {
        return fileattr;
    }

    public void setFileattr(String fileattr) {
        this.fileattr = fileattr;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
