package com.krypton.cloud.service.folder.record.updater;

import com.krypton.cloud.model.*;
import com.krypton.cloud.repository.*;
import com.krypton.cloud.service.folder.record.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FolderRecordUpdaterImpl implements FolderRecordUpdater {

    private final FolderPersistenceHelper folderPersistenceHelper;

    private final FolderRecordServiceImpl folderRecordService;

    private final FolderRepository folderRepository;

    private final FileRepository fileRepository;

    @Override
    public HttpStatus updateName(String path, String newName) {
        var folder = folderRecordService.getByPath(path);

        folder.setName(newName);

        folderRepository.save(folder);
        // folder name is updated so we update child's location name
        updateChildsLocation(folder);

        return HttpStatus.OK;
    }

    @Override
    public HttpStatus updatePath(java.io.File folder, String newPath) {
        var dbFolder = folderRecordService.getByPath(folder.getPath());

        dbFolder.setPath(newPath);

        folderRepository.save(dbFolder);
        // update folder child's path
        folderPersistenceHelper.updateChildsPaths(dbFolder);

        return HttpStatus.OK;
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
     * set new location name for folder
     *
     * @param folder            folder who need location name update
     * @param newLocation       new folder location name
     */
    private void updateLocation(Folder folder, String newLocation) {
        folder.setLocation(newLocation);

        folderRepository.save(folder);
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
