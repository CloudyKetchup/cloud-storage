package com.krypton.cloud.service.folder.record.updater;

import com.krypton.cloud.model.*;
import com.krypton.cloud.repository.*;
import common.config.AppProperties;
import util.file.FileTools;
import util.folder.FolderTools;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FolderRecordUpdaterImpl implements FolderRecordUpdater {

    private final FolderRepository folderRepository;

    private final FileRepository fileRepository;

    @Override
    public HttpStatus updateName(String path, String newName) {
        var folder = folderRepository.getByPath(path);

        folder.setName(newName);

        folderRepository.save(folder);
        // folder name is updated so we update child's location name
        updateChildsLocation(folder);

        return HttpStatus.OK;
    }

    @Override
    public HttpStatus updatePath(java.io.File folder, String newPath) {
        var dbFolder = folderRepository.getByPath(folder.getPath());

        dbFolder.setPath(newPath);

        folderRepository.save(dbFolder);
        // update folder child's path
        updateChildsPaths(dbFolder);

        return HttpStatus.OK;
    }

    @Override
    public void updateLocation(Folder folder, String newLocation) {
        folder.setLocation(newLocation);

        folderRepository.save(folder);
    }

    @Override
    public void updateSize(Folder folder) {
        var fsFolder = new java.io.File(folder.getPath());

        var sizeLength = FolderTools.INSTANCE.getFolderLength(fsFolder);

        folder.setSize(FileTools.INSTANCE.getFileSize(sizeLength));

        folderRepository.save(folder);

        var parentFolder = folderRepository.getByPath(fsFolder.getParentFile().getPath());

        if (parentFolder != null)
            if (!parentFolder.getPath().equals(AppProperties.INSTANCE.getRoot())) updateSize(parentFolder);
    }

    /**
     * when folder is renamed,folders and files inside location need to be updated to new folder name
     *
     * @param parent        folder who child folders and files need update
     */
    private void updateChildsLocation(Folder parent) {
        var updatedParentName = parent.getName();

        updateFilesLocation(parent, updatedParentName);

        updateFoldersLocation(parent, updatedParentName);
    }

    /**
     * when {@link #updateChildsLocation(Folder)} run's it updates location
     * of folders and files inside,this method updates files
     *
     * @param parent            parent folder
     * @param updatedParentName parent new name for location name
     */
    private void updateFilesLocation(Folder parent, String updatedParentName) {
        parent.getFiles()
                .parallelStream()
                .forEach(childFile -> updateFileLocation(childFile, updatedParentName));
    }

    /**
     * when {@link #updateChildsLocation(Folder)} run's it updates location
     * of folders and files inside,this method updates folders
     *
     * @param parent            parent folder
     * @param updatedParentName parent new name for location name
     */
    private void updateFoldersLocation(Folder parent, String updatedParentName) {
        parent.getFolders()
                .parallelStream()
                .forEach(childFolder -> updateLocation(childFolder, updatedParentName));
    }

    /**
     * if folder path was updated this method runs and update all
     * child folders and files records path's
     *
     * @param parent        folder who path was updated
     */
    private void updateChildsPaths(Folder parent) {
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
            childFile.setPath(parent.getPath() + "/" + childFile.getName());

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
            childFolder.setPath(parent.getPath() + "/" + childFolder.getName());

            folderRepository.save(childFolder);
            // run recursive for content inside child folder
            updateChildsPaths(childFolder);
        });
    }

    /**
     * set new location name for file
     *
     * @param file              file who need location name update
     * @param newLocation       new file location name
     */
    private void updateFileLocation(File file, String newLocation) {
        file.setLocation(newLocation);

        fileRepository.save(file);
    }
}
