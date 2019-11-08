package com.cec.videoplayer.model;

import java.util.List;

/**
 * User: cec
 * Date: 2019/7/3
 * Time: 11:36 AM
 * 内容详情实体类
 */
public class ContentInfo {
    private String id;
    private int hits;
    private String title;
    private String siteId;
    private String updateTime;
    private int dayHits;
    private String image;
    private int video360;
    private int monthHits;
    private String href;
    private int weekHits;
    private String cateId;
    private List<com.cec.videoplayer.model.File> files;
    private List<com.cec.videoplayer.model.Relate> relates;


    @Override
    public String toString() {
        return "ContentInfo{" +
                "id='" + id + '\'' +
                ", hits=" + hits +
                ", title='" + title + '\'' +
                ", dayHits=" + dayHits +
                ", weekHits=" + weekHits +
                ", monthHits=" + monthHits +
                ", updateTime=" + updateTime +
                ", video360=" + video360 +
                ", href='" + href + '\'' +
                ", image='" + image + '\'' +
                ", cateId='" + cateId + '\'' +
                ", files=" + files +
                ", relates=" + relates +
                ", siteId=" + siteId +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getVideo360() {
        return video360;
    }

    public void setVideo360(int video360) {
        this.video360 = video360;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public int getDayHits() {
        return dayHits;
    }

    public void setDayHits(int dayHits) {
        this.dayHits = dayHits;
    }

    public int getWeekHits() {
        return weekHits;
    }

    public void setWeekHits(int weekHits) {
        this.weekHits = weekHits;
    }

    public int getMonthHits() {
        return monthHits;
    }

    public void setMonthHits(int monthHits) {
        this.monthHits = monthHits;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCateId() {
        return cateId;
    }

    public void setCateId(String cateId) {
        this.cateId = cateId;
    }

    public List<com.cec.videoplayer.model.File> getFiles() {
        return files;
    }

    public void setFiles(List<com.cec.videoplayer.model.File> files) {
        this.files = files;
    }

    public List<com.cec.videoplayer.model.Relate> getRelates() {
        return relates;
    }

    public void setRelates(List<com.cec.videoplayer.model.Relate> relates) {
        this.relates = relates;
    }

}