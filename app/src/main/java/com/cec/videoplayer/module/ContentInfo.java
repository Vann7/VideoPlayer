package com.cec.videoplayer.module;

import java.util.Date;
import java.util.List;

/**
 * User: cec
 * Date: 2019/7/3
 * Time: 11:36 AM
 * 内容详情实体类
 */
public class ContentInfo {
    private String id;
    private String title;
    private int hits;
    private int dayHits;
    private int weekHits;
    private int monthHits;
    private String updateTime;
    private String href;
    private String image;
    private String catestr;
    private List<File> files;
    private List<Relate> relates;


    @Override
    public String toString() {
        return "ContentInfo{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", dayHits=" + dayHits +
                ", weekHits=" + weekHits +
                ", updateTime=" + updateTime +
                ", href='" + href + '\'' +
                ", image='" + image + '\'' +
                ", catestr='" + catestr + '\'' +
                ", files=" + files +
                ", relates=" + relates +
                '}';
    }

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

    public String getCatestr() {
        return catestr;
    }

    public void setCatestr(String catestr) {
        this.catestr = catestr;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public List<Relate> getRelates() {
        return relates;
    }

    public void setRelates(List<Relate> relates) {
        this.relates = relates;
    }
}
