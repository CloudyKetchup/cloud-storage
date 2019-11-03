package com.krypton.databaselayer.model;

import javax.persistence.*;

@MappedSuperclass
public abstract class MediaEntity extends Entity {

    @Column
    private String thumbnailPath;

    @Column
    private String path;

    @Column
    private Integer height;

    @Column
    private Integer width;

    MediaEntity() {}

    MediaEntity(String path, String thumbnailPath) {
        this.path = path;
        this.thumbnailPath = thumbnailPath;
    }

    MediaEntity(String path, String thumbnailPath, Integer height, Integer width) {
        this.path = path;
        this.thumbnailPath = thumbnailPath;
        this.height = height;
        this.width = width;
    }

    public String getThumbnailPath() { return thumbnailPath; }

    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public String getPath() { return path; }

    public void setPath(String path) { this.path = path; }

    public Integer getWidth() { return width; }

    public void setWidth(Integer width) { this.width = width; }

    public Integer getHeight() { return height; }

    public void setHeight(Integer height) { this.height = height; }
}
