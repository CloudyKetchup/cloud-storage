package com.krypton.medialayer.service;

import common.config.AppProperties;
import org.apache.commons.io.FileUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.util.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

public abstract class MediaService {

    public static Optional<Thumbnail> createThumbnail(File file, String name) {
        var path = (AppProperties.INSTANCE.getThumbnailsFolder().getPath() + "/" + name + ".jpg");

        return new Thumbnail.Builder(file, path).buildAndWrite();
    }

    @Nullable
    public static ResponseEntity<byte[]> getThumbnail(String path) {
        byte[] thumbnail = null;

        try {
            thumbnail = FileUtils.readFileToByteArray(new File(path));
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) e.printStackTrace();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(thumbnail);
    }
}
