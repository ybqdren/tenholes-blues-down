package com.zw.kq.model;

/**
 * Created by Zhao Wen on 2021/2/14
 * 页面提取model
 */

public class PageUrlModel {
    private String id;
    private String name;
    /**
     * 曲谱地址
     */
    private String imageUrl;

    /**
     * 伴奏地址
     */
    private String audioUrl;

    /**
     * 当前曲谱页存储
     */
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "PageUrlModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", audioUrl='" + audioUrl + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
