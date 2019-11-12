package com.krypton.databaselayer.service.image;

import com.krypton.databaselayer.model.File;
import com.krypton.databaselayer.model.Image;
import com.krypton.databaselayer.repository.ImageRepository;
import com.krypton.databaselayer.service.RecordService;
import com.krypton.medialayer.service.MediaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.util.annotation.Nullable;

import java.util.UUID;

@AllArgsConstructor
@Service
public class ImageRecordService implements RecordService<Image> {

    private final ImageRepository imageRepository;

    @Nullable
    @Override
    public Image getById(UUID id) {
        return imageRepository.findById(id).orElse(null);
    }

    @Override
    public Image save(Image entity) {
        return imageRepository.save(entity);
    }

    @Override
    public boolean delete(UUID id) {
        imageRepository.deleteById(id);

        return !exists(id);
    }

    public boolean exists(UUID id) {
        return imageRepository.findById(id).isPresent();
    }

    @Nullable
    private Image create(File file) {
        var thumbnail = MediaService.createThumbnail(new java.io.File(file.getPath()), file.getId().toString());

        return thumbnail
                .map(t -> new Image(file.getPath(), t.getPath(), t.getHeight(), t.getWidth()))
                .orElse(null);
    }

    @Nullable
    public Image createAndSave(File file) {
        return save(create(file));
    }
}
