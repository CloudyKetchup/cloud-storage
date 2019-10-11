package com.krypton.databaselayer.service.folder;

import common.exception.entity.database.FolderDatabaseException;
import com.krypton.databaselayer.model.Folder;
import com.krypton.databaselayer.service.file.FileRecordServiceImpl;
import common.model.LogType;
import util.log.LogFolder;
import util.log.LoggingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class FolderRecordUtils {

    private final FolderRecordServiceImpl folderRecordService;

    private final FileRecordServiceImpl fileRecordService;

    private HttpStatus logError(String message) {
        LoggingService.INSTANCE.saveLog(new FolderDatabaseException(message).stackTraceToString(),
                LogType.ERROR,
                LogFolder.DATABASE.getType() + LogFolder.FOLDER.getType());
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * add {@link Folder} that was copied to another {@link Folder}
     *
     * @param copiedFolder  copied folder from filesystem
     * @return Http Status
     */
    public HttpStatus copyFolder(java.io.File copiedFolder) {
        folderRecordService.save(new Folder(copiedFolder));

        var folderContent = Arrays.asList(copiedFolder.listFiles());

        fileRecordService.addAllFilesToDatabase(folderContent);

        addAllFoldersToDatabase(folderContent);

        return HttpStatus.OK;
    }

    /**
     * when move {@link Folder} to another {@link Folder}, update parent < - > child relations
     *
     * @param oldPath       old folder path
     * @param folder        folder from new location
     * @return Http Status if parent/child relation was updated successful
     */
    public HttpStatus moveFolder(String oldPath, File folder) {
        folderRecordService.delete(oldPath);

        folderRecordService.save(new Folder(folder));
        // check if folder was moved successful
        if (folderRecordService.getByPath(oldPath) == null && folderRecordService.exists(folder.getPath())) {
            addAllFoldersToDatabase(Arrays.asList(folder.listFiles()));

            fileRecordService.addAllFilesToDatabase(Arrays.asList(folder.listFiles()));
            return HttpStatus.OK;
        }
        // save error log
        return logError("Error occurred while moving folder " + folder.getPath() + " record");
    }

    /**
     * add all {@link Folder}'s and their child {@link Folder}'s to database
     *
     * @param content       folder content list(folders and files inside)
     */
    public void addAllFoldersToDatabase(List<File> content) {
        content.parallelStream().forEach(folder -> {
            // if file is directory missing in database
            if (!folderRecordService.exists(folder.getPath()) && folder.isDirectory()) {
                folderRecordService.save(new Folder(folder));

                addAllFoldersToDatabase(Arrays.asList(folder.listFiles()));
            }
        });
    }
}