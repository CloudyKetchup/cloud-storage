package com.krypton.cloud.service.folder.record;

import com.krypton.cloud.model.Folder;
import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
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
     * when move {@link Folder} to another {@link Folder},update parent child relations
     *
     * @param oldPath       old folder path
     * @param folder        folder from new location
     * @return Http Status if parent/child relation was updated successful
     */
    public HttpStatus moveFolder(String oldPath, File folder) {
        folderRecordService.delete(oldPath);

        folderRecordService.save(folder);
        // check if folder was moved successful
        return folderRecordService.getByPath(oldPath) == null && folderRecordService.exists(folder.getPath())
                ? HttpStatus.OK
                : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * add all {@link Folder}'s and their child {@link Folder}'s to database
     *
     * @param content       folder content list(folders and files inside)
     */
    public void addAllFoldersToDatabase(List<File> content) {
        content.parallelStream().forEach(file -> {
            // if file is directory missing not present in database
            if (!folderRecordService.exists(file.getPath()) && file.isDirectory()) {
                folderRecordService.save(file);

                addAllFoldersToDatabase(Arrays.asList(file.listFiles()));
            }
        });
    }
}
