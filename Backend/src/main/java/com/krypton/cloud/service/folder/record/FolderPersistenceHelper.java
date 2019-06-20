package com.krypton.cloud.service.folder.record;

import com.krypton.cloud.model.File;
import com.krypton.cloud.model.Folder;
import com.krypton.cloud.repository.FileRepository;
import com.krypton.cloud.repository.FolderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FolderPersistenceHelper {

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

            folderRepository.save(parent);
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

            folderRepository.save(parent);
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
     * if folder path was updated this method runs and update all
     * child folders and files records path's
     *
     * @param parent        folder who path was updated
     */
    public void updateChildsPaths(Folder parent) {
        updateFilesPaths(parent);

        updateFoldersPaths(parent);
    }

    /**
     * Update {@link File}' path inside {@link Folder}
     *
     * @param parent        folder who child files need path update
     */
    private void updateFilesPaths(Folder parent) {
        parent.getFiles().parallelStream().forEach(childFile -> {
            childFile.setPath(parent.getPath() + "\\" + childFile.getName());

            fileRepository.save(childFile);
        });
    }

    /**
     * Update {@link Folder}'s path inside parent {@link Folder}
     *
     * @param parent        folder who child folders need path update
     */
    private void updateFoldersPaths(Folder parent) {
        parent.getFolders().parallelStream().forEach(childFolder -> {
            childFolder.setPath(parent.getPath() + "\\" + childFolder.getName());

            folderRepository.save(childFolder);
            // run recursive for content inside child folder
            updateChildsPaths(childFolder);
        });
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