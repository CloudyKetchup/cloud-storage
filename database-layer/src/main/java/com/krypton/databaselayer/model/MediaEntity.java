package com.krypton.databaselayer.model;

import javax.persistence.*;

@MappedSuperclass
public abstract class MediaEntity extends Entity {

    @Column
    private String thumbnailPath;

    @Column
    private String path;

    MediaEntity() {}

    MediaEntity(String path, String thumbnailPath) {
        this.path = path;
        this.thumbnailPath = thumbnailPath;
    }

    public String getThumbnailPath() { return thumbnailPath; }

    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public String getPath() { return path; }

    public void setPath(String path) { this.path = path; }
}
