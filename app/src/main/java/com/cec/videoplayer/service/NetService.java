package com.cec.videoplayer.service;

public class NetService {
    private String siteId;
    private String ip;
    private String port;

    public NetService() {
        this.siteId = "f39c5711636e361301636e371bcb001a";
        this.ip = "115.28.215.145";
        this.port = "8080";
    }
    public String getSiteId(){
        return siteId;
    }
    public String getIp(){
        return ip;
    }
    public String getPort(){
        return port;
    }
    public void setSiteId(String siteId){
        this.siteId=siteId;
    }
    public void setIp(String ip){
        this.ip=ip;
    }
    public void setPort(String port){
        this.port=port;
    }
}
