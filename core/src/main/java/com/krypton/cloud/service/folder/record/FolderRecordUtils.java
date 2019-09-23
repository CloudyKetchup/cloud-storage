package com.krypton.cloud.service.folder.record;

import common.exception.entity.database.FolderDatabaseException;
import com.krypton.cloud.model.Folder;
import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import com.krypton.cloud.service.handler.http.ErrorHandler;
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
public class FolderRecordUtils implements ErrorHandler {

    private final FolderRecordServiceImpl folderRecordService;

    private final FileRecordServiceImpl fileRecordService;

    @Override
    public HttpStatus httpError(String message) {
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
        folderRecordService.save(copiedFolder);

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

        folderRecordService.save(folder);
        // check if folder was moved successful
        if (folderRecordService.getByPath(oldPath) == null && folderRecordService.exists(folder.getPath())) {
            addAllFoldersToDatabase(Arrays.asList(folder.listFiles()));

            fileRecordService.addAllFilesToDatabase(Arrays.asList(folder.listFiles()));
            return HttpStatus.OK;
        }
        // save error log
        return httpError("Error occurred while moving folder " + folder.getPath() + " record");
    }

    /**
     * add all {@link Folder}'s and their child {@link Folder}'s to database
     *
     * @param content       folder content list(folders and files inside)
     */
    public void addAllFoldersToDatabase(List<File> content) {
        content.parallelStream().forEach(file -> {
            // if file is directory missing in database
            if (!folderRecordService.exists(file.getPath()) && file.isDirectory()) {
                folderRecordService.save(file);

                addAllFoldersToDatabase(Arrays.asList(file.listFiles()));
            }
        });
    }
}