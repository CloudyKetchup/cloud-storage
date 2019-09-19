package com.krypton.cloud.service.folder.record.updater;

import com.krypton.cloud.model.Folder;
import org.springframework.http.HttpStatus;

import java.io.File;

/**
 * Service that updates folder record properties
 */
interface FolderRecordUpdater {

    /**
     * Updates folder name
     *
     * @param folder        folder for update
     * @param newName       new folder name
     * @return http status depending on success
     */
    @Deprecated
    HttpStatus updateName(String folder, String newName);

    /**
     * Updates folder path
     *
     * @param folder        folder for update
     * @param path          new folder path
     * @return http status depending on success
     */
    HttpStatus updatePath(File folder, String path);

    /**
     * set new location name for folder
     *
     * @param folder            folder who need location name update
     * @param newLocation       new folder location name
     */
    void updateLocation(Folder folder, String newLocation);

    void updateSize(Folder folder);
}
