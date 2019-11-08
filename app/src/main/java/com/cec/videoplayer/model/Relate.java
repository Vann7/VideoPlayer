package com.cec.videoplayer.model;

/**
 * User: cec
 * Date: 2019/7/3
 * Time: 11:46 AM
 * 相关视频
 */
public class Relate {
    private String id;
    private int limit;
    private String title;
    private String image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Relate{" +
                "id='" + id + '\'' +
                "limit='" + limit + '\'' +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
