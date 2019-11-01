package com.cec.videoplayer.module;

import java.util.List;

public class PagingVideoInfo {
    private int pageCount;
    private int pageNo;
    private int totalRows;
    private int pageSize;
    private List<VideoInfo> info;

    public void setPageCount(int pageCount){
        this.pageCount=pageCount;
    }

    public int getPageCount(){
        return pageCount;
    }

    public void setPageNo(int pageNo){
        this.pageNo=pageNo;
    }

    public int getPageNo(){
        return pageNo;
    }

    public void setTotalRows(int totalRows){
        this.totalRows=totalRows;
    }

    public int getTotalRows(){
        return totalRows;
    }

    public void setPageSize(int pageSize){
        this.pageSize=pageSize;
    }

    public int getPageSize(){
        return pageSize;
    }

    public void setInfo(List<VideoInfo> info) {
        this.info = info;
    }

    public List<VideoInfo> getInfo(){
        return info;
    }
}
