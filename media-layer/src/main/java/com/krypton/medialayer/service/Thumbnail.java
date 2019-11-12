package com.krypton.medialayer.service;

import com.krypton.medialayer.service.image.ImageProcessor;
import lombok.ToString;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

@ToString
public class Thumbnail {
    private BufferedImage image;
    private String path;
    private MediaExtension fileExtension;
    private int height = MediaSettings.THUMBNAIL_HEIGHT;
    private int width = MediaSettings.THUMBNAIL_WIDTH;

    private Thumbnail() {}

    private Thumbnail(String path) { this.path = path; }

    public BufferedImage getImage() { return image; }

    public String getPath() { return path; }

    public int getHeight() { return height; }

    public int getWidth() { return width; }

    public static class Builder {
        private Thumbnail thumbnail;
        private File file;

        Builder(File file, String path) {
            this.file = file;
            thumbnail = new Thumbnail(path);
        }

        public Builder destinationPath(String path) {
            thumbnail.path = path;
            return this;
        }

        public Builder type(MediaExtension fileExtension) {
            if (fileExtension.equals(MediaExtension.UNKNOWN))
                thumbnail.fileExtension = ImageProcessor.determineType(file);
            else
                thumbnail.fileExtension = fileExtension;
            return this;
        }

        public Builder size(int height, int width) {
            thumbnail.height = height;
            thumbnail.width = width;

            return this;
        }

        public Thumbnail build() {
            if (thumbnail.fileExtension == null) thumbnail.fileExtension = ImageProcessor.determineType(file);

            startProcessing(file);

            return thumbnail;
        }

        public Optional<Thumbnail> buildAndWrite() {
            build();

            try {
                thumbnail.createFile();
            } catch (IOException e) {
                e.printStackTrace();
                return Optional.empty();
            }
            return Optional.of(thumbnail);
        }

        private void startProcessing(File file) {
            switch (thumbnail.fileExtension) {
                case JPG:
                case JPEG:
                case PNG:
                    thumbnail.image = ImageProcessor.resize(thumbnail.height, thumbnail.width, file).orElse(null);
                    break;
                case GIF:
                    thumbnail.image = ImageProcessor.resizeGif(thumbnail.height, thumbnail.width, file).orElse(null);
                    break;
                default: break;
            }
        }
    }

    private void createFile() throws IOException {
        if (image != null) {
            var file = new File(path);

            if (!file.exists()) {
                if (!file.createNewFile()) throw new IOException();
            }
            ImageIO.write(image, fileExtension.getType(), file);
        }
    }
}
