package com.dou361.ijkplayer.module;

import java.util.Date;

/**
 * User: cec
 * Date: 2019/7/4
 * Time: 10:12 AM
 */
public class Comment {
    private String id;
    private String contentId;
    private String content;
    private Date createTime;
    private String userName;
    private String userId;
    private String siteId;
    private String currentTime;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date date) {
        this.createTime = date;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }


    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", userName='" + userName + '\'' +
                ", content='" + content + '\'' +
                ", createTime='"+createTime.toString()+'\''+
                ", currentTime='"+currentTime+'\''+
                '}';
    }


}
