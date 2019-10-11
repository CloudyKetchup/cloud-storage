package com.krypton.databaselayer.service.folder;

import com.krypton.databaselayer.model.*;
import com.krypton.databaselayer.service.folder.updater.FolderRecordUpdaterImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Helper service for folders entities, manages relationships of
 * folder entities, for example:
 * a folder was created inside another folder, so we need to do parent and child relation
 * and so on...
 * */
@Service
@AllArgsConstructor
public class FolderPersistenceHelper {

    private final FolderRecordUpdaterImpl folderRecordUpdater;

    /**
     * Add {@link File} as child to {@link Folder}
     *
     * @param parent        folder where to add file
     * @param child         file to be added
     */
    public void addFileChild(Folder parent, File child) {
        if (
            parent  != null
            &&
            child   != null
            &&
            !folderHasChildFile(parent, child)
        ) {
            parent.getFiles().add(child);

            folderRecordUpdater.updateSize(parent);
        }
    }

    /**
     * Add {@link Folder} as child to parent {@link Folder}
     *
     * @param parent        folder where to add folder
     * @param child         folder to be added
     */
    void addFolderChild(Folder parent, Folder child) {
        if (
            parent  != null
            &&
            child   != null
            &&
            !folderHasChildFolder(parent, child)
        ) {
            parent.getFolders().add(child);

            folderRecordUpdater.updateSize(parent);
        }
    }

    /**
     * check if parent folder have specified folder inside
     *
     * @param parent    folder pretending to have child folder inside
     * @param child     folder pretending to be child
     * @return boolean depending if folder have specified folder as child
     */
    boolean folderHasChildFolder(Folder parent, Folder child) {
        return parent.getFolders()
                .parallelStream()
                .anyMatch(folderInside -> folderInside.getId().equals(child.getId()));
    }

    /**
     * check if {@link Folder} have specified {@link File} inside
     *
     * @param parent    folder pretending to have child file inside
     * @param child     file pretending to be child
     * @return boolean depending if folder have specified file as child
     */
    private boolean folderHasChildFile(Folder parent, File child) {
        return parent.getFiles()
                .parallelStream()
                .anyMatch(fileInside -> fileInside.getId().equals(child.getId()));
    }
}