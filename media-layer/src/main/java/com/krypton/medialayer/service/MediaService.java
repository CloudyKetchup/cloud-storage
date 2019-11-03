package com.krypton.medialayer.service;

import com.krypton.medialayer.service.image.ImageProcessor;
import common.config.AppProperties;
import org.apache.commons.io.FileUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.util.annotation.Nullable;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

@Service
public abstract class MediaService {

    private static ImageProcessor imageProcessor = new ImageProcessor();

    public static File createThumbnail(File file, String name) throws IOException {
        var bufferedImage = imageProcessor.resizeImage(MediaSettings.THUMBNAIL_HEIGHT, MediaSettings.THUMBNAIL_WIDTH, file);

        if (bufferedImage.isEmpty()) throw new IOException();

        var thumbnail = new File(AppProperties.INSTANCE.getThumbnailsFolder().getPath() + "/" + name + ".jpg");

        ImageIO.write(bufferedImage.get(), "jpg", thumbnail);

        return thumbnail;
    }

    @Nullable
    public static ResponseEntity<byte[]> getThumbnail(String path) {
        byte[] thumbnail = null;

        try {
            thumbnail = FileUtils.readFileToByteArray(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(thumbnail);
    }
}
