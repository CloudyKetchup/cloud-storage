package com.krypton.databaselayer.service.file;

import com.krypton.databaselayer.model.Image;
import com.krypton.databaselayer.service.folder.FolderRecordServiceImpl;
import com.krypton.databaselayer.service.folder.FolderRecordUtils;
import com.krypton.databaselayer.service.folder.updater.FolderRecordUpdaterImpl;
import com.krypton.databaselayer.service.IOEntityRecordService;
import com.krypton.databaselayer.model.File;
import com.krypton.databaselayer.repository.FileRepository;
import com.krypton.databaselayer.service.folder.FolderPersistenceHelper;
import com.krypton.databaselayer.service.image.ImageRecordService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class FileRecordServiceImpl implements IOEntityRecordService<File> {

    private final FolderRecordServiceImpl folderRecordService;

    private final FileRepository fileRepository;

    private final FolderPersistenceHelper folderPersistenceHelper;

    private final FolderRecordUpdaterImpl folderRecordUpdater;

    private final ImageRecordService imageService;

    @Override
    @Nullable
    public File getById(UUID id) {
        return fileRepository.findById(id).orElse(null);
    }

    @Override
    public File getByPath(String path) {
        return fileRepository.getByPath(path);
    }

    @Override
    public File save(File file) {
        fileRepository.save(file);
        // filesystem folder where file is located
        var parentFolder    = Paths.get(file.getPath()).getParent().toFile();
        // folder record where file need to be added as child
        var dbParentFolder  = folderRecordService.getByPath(parentFolder.getPath());

        folderPersistenceHelper.addFileChild(dbParentFolder, file);

        return file;
    }

    @Override
    public boolean delete(String path) {
        var file = getByPath(path);

        fileRepository.delete(file);

        var parent = new java.io.File(file.getPath()).getParentFile();

        folderRecordUpdater.updateSize(folderRecordService.getByPath(parent.getPath()));

        return !exists(path);
    }

    @Override
    public boolean delete(UUID id) {
        var file = getById(id);

        if (file != null) {
            fileRepository.delete(file);

            var parent = new java.io.File(file.getPath()).getParentFile();

            folderRecordUpdater.updateSize(folderRecordService.getByPath(parent.getPath()));

            return !exists(id);
        }
        return false;
    }

    @Override
    public boolean exists(String path) {
        return fileRepository.getByPath(path) != null;
    }

    @Override
    public boolean exists(UUID id) {
        return fileRepository.findById(id).isPresent();
    }

    /**
     * take {@link java.io.File} as parameter and save it to database,
     * this is just a shortcut to {@link #save(File)} but with {@link java.io.File}
     *
     * @param file      {@link java.io.File} target
     * @return saved {@link File} entity
     * */
    public File save(java.io.File file) { return save(new File(file)); }

    /**
     * Add all files inside folder to database
     *
     * WARNING!!! if you want to add all folder content
     * and you'll use {@link FolderRecordUtils#addAllFoldersToDatabase(List)}
     *  >>> call this method after calling method mentioned previously, or it will not
     *  work because you need to add all folders inside folder to database first, then all files,
     *  otherwise, content inside folder inside main folder will not be added, sounds confusing, i know :)
     *
     * @param files         files list for database
     */
    public void addAllFilesToDatabase(List<java.io.File> files) {
        files.parallelStream().forEach(file -> {
            if (file.isFile()
                    &&
                    !exists(file.getPath())             // if file already does not exist in database
                    &&
                    !file.getName().startsWith(".")     // if file is not ignored, like(.DS_STORE, .vimrc, ...)
            ) {
                createAndSave(file).subscribe();
            } else if (file.listFiles() != null) {
                var insideContent = Arrays.asList(file.listFiles());
                // add all files inside
                addAllFilesToDatabase(insideContent);
            }
        });
    }

    /**
     * Take a {@link java.io.File} and save it to database,
     * will create a {@link com.krypton.databaselayer.model.File} entity and save it to database.
     * If file have jpg extension will create a resized thumbnail as {@link Image}
     * for it and will assign it as one to one relationship to this file
     *
     * @param file      {@link java.io.File} target
     * @return {@link Mono<Boolean>} result
     * */
    public Mono<Boolean> createAndSave(java.io.File file) {
        return Mono.just(save(file))
                .map(entity -> {
                    if (entity == null) {
                        return false;
                        // if file is a jpg image, create a resized thumbnail for it
                    } else if (withThumbnail(entity)) {
                        assignImage(entity, imageService.createAndSave(entity));

                        var check = getById(entity.getId());

                        if (check != null) return check.getImage() != null;
                    }
                    return false;
                })
                .doOnError(Throwable::printStackTrace)
                .onErrorReturn(false);
    }

    private boolean withThumbnail(File file) {
        switch (file.getExtension()) {
            case IMAGE_JPG:
            case IMAGE_JPEG:
            case IMAGE_PNG:
            case IMAGE_RAW:
            case IMAGE_GIF:
            case MP4:
            case AVI:
            case MOV:
            case MKV:
                return true;
            default: return false;
        }
    }

    /**
     * Link a image to a file entity
     *
     * @param file      {@link File} entity
     * @param image     {@link Image} to be assigned
     * @return {@link File}
     * */
    private File assignImage(File file, Image image) {
        file.setImage(image);

        return save(file);
    }
}
