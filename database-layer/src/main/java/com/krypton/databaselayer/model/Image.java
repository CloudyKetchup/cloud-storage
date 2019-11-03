package com.krypton.databaselayer.model;

import javax.persistence.Entity;

@Entity
public class Image extends MediaEntity {

    public Image() { super(); }

    public Image(String path, String thumbnailPath, Integer height, Integer width) {
        super(path, thumbnailPath, height, width);
    }
}
