package com.krypton.databaselayer.service.file;

import com.krypton.databaselayer.service.folder.FolderRecordServiceImpl;
import com.krypton.databaselayer.service.folder.FolderRecordUtils;
import com.krypton.databaselayer.service.folder.updater.FolderRecordUpdaterImpl;
import com.krypton.databaselayer.service.IOEntityRecordService;
import com.krypton.databaselayer.model.File;
import com.krypton.databaselayer.repository.FileRepository;
import com.krypton.databaselayer.service.folder.FolderPersistenceHelper;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

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
        // file saved to database
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
    public boolean exists(String path) {
        return fileRepository.getByPath(path) != null;
    }

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
                save(new File(file));
            } else if (file.isDirectory()) {
                var insideContent = Arrays.asList(file.listFiles());
                // add all files inside
                addAllFilesToDatabase(insideContent);
            }
        });
    }
}
