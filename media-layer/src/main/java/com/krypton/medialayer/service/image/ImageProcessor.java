package com.krypton.medialayer.service.image;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
public class ImageProcessor {

    public Optional<BufferedImage> resizeImage(int height, int width, File image) {
        BufferedImage file = null;

        try {
            file = Thumbnails.of(image).forceSize(width, height).asBufferedImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(file);
    }
}
