package com.krypton.cloud.service.file.record;

import com.krypton.cloud.exception.entity.database.FileDatabaseException;
import com.krypton.cloud.model.File;
import com.krypton.cloud.model.LogType;
import com.krypton.cloud.repository.FileRepository;
import com.krypton.cloud.service.folder.record.FolderPersistenceHelper;
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl;
import com.krypton.cloud.service.util.log.LogFolder;
import com.krypton.cloud.service.util.log.LoggingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class FileRecordServiceImpl implements FileRecordService {

    private final FileRepository fileRepository;

    private final FolderRecordServiceImpl folderRecordService;

    private final FolderPersistenceHelper folderPersistenceHelper;

    private final LoggingService loggingService;

    @Override
    public File getById(Long id) {
        return fileRepository.getOne(id);
    }

    @Override
    public File getByPath(String path) {
        return fileRepository.getByPath(path);
    }

    @Override
    public File save(java.io.File file) {
        // file saved to database
        var dbFile          = fileRepository.save(new File(file));
        // filesystem folder where file is located
        var parentFolder    = Paths.get(file.getPath()).getParent().toFile();
        // folder record where file need to be added as child
        var dbParentFolder  = folderRecordService.getByPath(parentFolder.getPath());

        folderPersistenceHelper.addFileChild(dbParentFolder, dbFile);

        return dbFile;
    }

    @Override
    public HttpStatus delete(String path) {
        var file = getByPath(path);

        fileRepository.delete(file);

        if (!exists(file.getPath())) {
            return HttpStatus.OK;
        } else {
            // save error log
            loggingService.saveLog(new FileDatabaseException("Error deleting file " + path + " entity").stackTraceToString(),
                    LogType.ERROR,
                    LogFolder.DATABASE.getType() + LogFolder.DATABASE.getType());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Override
    public boolean exists(String path) {
        return fileRepository.getByPath(path) != null;
    }

    /**
     * Add all files inside folder to database
     *
     * WARNING!!! if you want to add all folder content
     * and you'll use {@link com.krypton.cloud.service.folder.record.FolderRecordUtils#addAllFoldersToDatabase(List)}
     *  >>> call this method after calling method mentioned previously, or it will not 
     *  work because you need to add all folders inside folder to database first, then all files, 
     *  otherwise, content inside folder inside main folder will not be added, sounds confusing, i know :)
     *
     * @param files         files list for database
     */
    public void addAllFilesToDatabase(List<java.io.File> files) {
        files.parallelStream().forEach(file -> {
            // if file is file and don't already exist in database
            if (file.isFile() && !exists(file.getPath())) {
                save(file);
                // if is a folder
            } else if (file.isDirectory()) {
                var insideContent = Arrays.asList(file.listFiles());
                // add all files inside
                addAllFilesToDatabase(insideContent);
            }
        });
    }
}
