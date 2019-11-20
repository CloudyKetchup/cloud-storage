package com.krypton.medialayer.service.image;

import com.krypton.medialayer.service.MediaExtension;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Optional;

public class ImageProcessor {

    public static Optional<BufferedImage> resize(int height, int width, File image) {
        try {
            if (image.exists())
                return Optional.of(Thumbnails.of(image).size(width, height).asBufferedImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<BufferedImage> resize(int height, int width, BufferedImage image) {
        try {
            return Optional.of(Thumbnails.of(image).size(width, height).asBufferedImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<BufferedImage> resizeGif(int height, int width, File gif) {
        var gifFrame = getGifFrame(gif);

        return resize(height, width, gifFrame);
    }

    public static MediaExtension determineType(File image) {
        EnumSet<MediaExtension> extensions = EnumSet.allOf(MediaExtension.class);

        return extensions
                .stream()
                .filter(e -> e.getType().equalsIgnoreCase(FilenameUtils.getExtension(image.getName())))
                .findAny()
                .orElse(MediaExtension.UNKNOWN);
    }

    private static BufferedImage getGifFrame(File gif) {
        try {
            String[] imageatt = { "imageLeftPosition", "imageTopPosition", "imageWidth", "imageHeight" };

            var reader = ImageIO.getImageReadersByFormatName("gif").next();
            reader.setInput(ImageIO.createImageInputStream(gif), false);

            BufferedImage master = null;

            var image = reader.read(0);
            var metadata = reader.getImageMetadata(0);
            var children = metadata.getAsTree("javax_imageio_gif_image_1.0").getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                var nodeItem = children.item(i);

                if(nodeItem.getNodeName().equals("ImageDescriptor")){
                    var imageAttr = new HashMap<String, Integer>();

                    for (String s : imageatt) {
                        var attnode = nodeItem.getAttributes().getNamedItem(s);
                        imageAttr.put(s, Integer.valueOf(attnode.getNodeValue()));
                    }
                    master = new BufferedImage(imageAttr.get("imageWidth"), imageAttr.get("imageHeight"), BufferedImage.TYPE_INT_ARGB);
                    master.getGraphics().drawImage(image, imageAttr.get("imageLeftPosition"), imageAttr.get("imageTopPosition"), null);
                }
            }
            return master;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
