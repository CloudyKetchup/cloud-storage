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
import java.util.UUID;

public abstract class MediaService {

    public static final String THUMBNAILS_PATH = AppProperties.INSTANCE.getThumbnailsFolder().getPath();

    public static Optional<Thumbnail> createThumbnail(File file, String id) {
        var path = MediaService.THUMBNAILS_PATH + "/" + id + ".jpg";

        return new Thumbnail.Builder(file, path).buildAndWrite();
    }

    @Nullable
    public static ResponseEntity<byte[]> getThumbnail(UUID id) {
        byte[] thumbnail = null;
        var path = MediaService.THUMBNAILS_PATH + "/" + id.toString() + ".jpg";

        try {
            thumbnail = FileUtils.readFileToByteArray(new File(path));
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) e.printStackTrace();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(thumbnail);
    }
}
