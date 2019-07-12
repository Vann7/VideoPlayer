package com.cec.videoplayer.module;

/**
 * 栏目信息
 */
public class CategoryInfo {

    private String id;
    private String uniqueStr;
    private String parentId;
    private String siteId;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUniqueStr() {
        return uniqueStr;
    }

    public void setUniqueStr(String uniqueStr) {
        this.uniqueStr = uniqueStr;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryInfo(String id, String uniqueStr, String parentId, String siteId, String name) {
        this.id = id;
        this.uniqueStr = uniqueStr;
        this.parentId = parentId;
        this.siteId = siteId;
        this.name = name;
    }
}
