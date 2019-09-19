package com.krypton.cloud.service.folder.record;

import com.krypton.cloud.model.*;
import com.krypton.cloud.repository.*;
import com.krypton.cloud.service.folder.record.updater.FolderRecordUpdaterImpl;
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

    private final FolderRepository folderRepository;

    private final FileRepository fileRepository;

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

    /**
     * remove all {@link Folder} child's records from database
     *
     * @param folder        parent folder
     */
    void removeAllFolderChilds(Folder folder) {
        folder.getFolders().parallelStream().forEach(childFolder -> {
            removeChildFolders(childFolder);

            removeChildFiles(childFolder);

            folderRepository.delete(childFolder);
        });
        removeChildFiles(folder);

        folderRecordUpdater.updateSize(folder);
    }

    /**
     * remove all {@link Folder}'s records inside {@link Folder},
     * will run recursive for all folders inside
     *
     * @param folder        parent folder
     */
    private void removeChildFolders(Folder folder) {
        folder.getFolders()
                .parallelStream()
                .forEach(this::removeAllFolderChilds);
    }

    /**
     * remove all {@link File}'s records inside {@link Folder}
     *
     * @param folder        parent folder
     */
    private void removeChildFiles(Folder folder) {
        folder.getFiles()
                .parallelStream()
                .forEach(fileRepository::delete);
    }
}