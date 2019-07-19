package com.cec.videoplayer.module;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * 栏目信息
 */
public class CategoryInfo implements Parcelable {

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

    @Override
    public int describeContents() {
// TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int arg1) {
// TODO Auto-generated method stub
        parcel.writeString(id);
        parcel.writeString(uniqueStr);
        parcel.writeString(parentId);
        parcel.writeString(siteId);
        parcel.writeString(name);
    }

    public static final Parcelable.Creator<CategoryInfo> CREATOR = new Creator<CategoryInfo>() {
        public CategoryInfo createFromParcel(Parcel source) {
            CategoryInfo pTemp = new CategoryInfo("", "", "", "", "");

            pTemp.id = source.readString();
            pTemp.uniqueStr = source.readString();
            pTemp.parentId = source.readString();
            pTemp.siteId = source.readString();
            pTemp.name = source.readString();

            return pTemp;
        }

        public CategoryInfo[] newArray(int size) {
            return new CategoryInfo[size];
        }
    };
}
