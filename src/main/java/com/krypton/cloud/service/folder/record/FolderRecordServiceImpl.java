package com.krypton.cloud.service.folder.record;

import com.krypton.cloud.model.*;
import com.krypton.cloud.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class FolderRecordServiceImpl implements FolderRecordService {

    private final FolderRepository folderRepository;

    private final FileRepository fileRepository;

    @Override
    public Folder getById(Long id) {
        var folder = folderRepository.findById(id);

        return folder.get();
    }

    @Override
    public Folder getByName(String name) {
        return folderRepository.getByName(name);
    }

    @Override
    public Folder getByPath(String path) {
        return folderRepository.getByPath(path);
    }

    @Override
    public void deleteFolderRecord(String folderPath) {
        var folder = getByPath(folderPath);

        removeAllFolderChilds(folder);

        folderRepository.delete(folder);
    }

    @Override
    public HttpStatus updateName(String path, String newName) {
        var folder = getByPath(path);

        folder.setName(newName);

        folderRepository.save(folder);

        // folder name is updated so we udpate childs location name
        updateChildsLocation(folder);

        return HttpStatus.OK;
    }

    @Override
    public HttpStatus updatePath(java.io.File folder, String newPath, String oldParentPath) {
        var dbFolder = getByPath(folder.getPath());

        dbFolder.setPath(newPath);

        folderRepository.save(dbFolder);
        // update folder childs path
        updateChildsPaths(dbFolder);
        // new folder parent path
        var newParentPath = Paths.get(dbFolder.getPath()).getParent().toFile().getPath();

        return folderMoveProcedure(getByPath(newParentPath), getByPath(oldParentPath), dbFolder);
    }

    @Override
    public HttpStatus updatePath(java.io.File folder, String newPath) {
        var dbFolder = getByPath(folder.getPath());

        dbFolder.setPath(newPath);

        folderRepository.save(dbFolder);
        // update folder childs path
        updateChildsPaths(dbFolder);

        return HttpStatus.OK;
    }

    @Override
    public HttpStatus addFolder(java.io.File folder) {
        // check if folder with same path exist
        if(!folderExist(folder.getPath())) {
            folderRepository.save(new Folder(folder));
        }

        var parentPath = Paths.get(folder.getPath()).getParent().toAbsolutePath();
        // newly added to database folder as parent
        var parent = getByPath(parentPath.toString());

        // check if parent is present and file to be added as child isn't already inside
        if (parent != null && !folderHasChildFolder(parent, getByPath(folder.getPath()))) {
            // add new folder as child
            addFolderChild(parent, getByPath(folder.getPath()));

            // updateFolderSize(parent);
        }
        return HttpStatus.OK;
    }

    @Override
    public void addFileChild(Folder parent, File child) {
        if (
            parent  != null 
            && 
            child   != null 
            && 
            !folderHasChildFile(parent, child)
        ) {
            parent.getFiles().add(child);
        }
    }

    @Override
    public void addFolderChild(Folder parent, Folder child) {
        if (
            parent != null
            &&
            child  != null
            &&
            !folderHasChildFolder(parent, child)
        ) {
            parent.getFolders().add(child);

            folderRepository.save(parent);
        }
    }

    @Override
    public void removeFileChild(Folder folder, File child) {
        if (
            folder  != null
            &&
            child   != null
            &&
            folderHasChildFile(folder, child)
        ) {
            folder.getFiles().removeIf(file -> file.getId().equals(child.getId()));
        }
    }

    @Override
    public void removeFolderChild(Folder parent, Folder child) {
        if (
            parent  != null
            &&
            child   != null
            &&
            folderHasChildFolder(parent, child)
        ) {
            parent.getFolders().removeIf(folder -> folder.getId().equals(child.getId()));

            folderRepository.save(parent);
        }
    }

    @Override
    public boolean folderExist(String path) {
        return folderRepository.getByPath(path) != null;
    }

    /**
     * add record of folder that was copied to another directory
     *
     * @param copiedFolder  copied folder from filesystem
     * @return Http Status
     */
    public HttpStatus copyFolder(java.io.File copiedFolder) {
        addFolder(copiedFolder);

        addAllFoldersToDatabase(Arrays.asList(copiedFolder.listFiles()));

        return HttpStatus.OK;
    }

    /**
     * when move folder to another directory,update parent child relations
     *
     * @param newParent     new folder parent
     * @param oldParent     old folder parent 
     * @param child         child folder witch was moved from old parent to new
     * @return Http Status if parent/child relation was updated succesfull
     */
    private HttpStatus folderMoveProcedure(Folder newParent, Folder oldParent, Folder child) {
        // remove folder from old parent
        removeFolderChild(oldParent, child);

        addFolderChild(newParent, child);

        if (folderHasChildFolder(newParent, child) && !folderHasChildFolder(oldParent, child)) {
            return HttpStatus.OK;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * check if parent folder have specified folder inside
     *
     * @param parent    folder pretending to have child folder inside
     * @param child     folder pretending to be child
     * @return boolean depending if folder have specified folder as child
     */
    private boolean folderHasChildFolder(Folder parent, Folder child) {
        return parent.getFolders()
                .parallelStream()
                .anyMatch(folderInside -> folderInside.getId().equals(child.getId()));
    }

    /**
     * check if folder have specified file inside
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
     * update folder occupied disk memory size,
     * adds all folder inside elements occupied disk memory
     *
     * @param folder    folder for update
     */
    private void updateFolderSize(Folder folder) {
        float updatedSize = 0;

        // files sizes
        for (var f : folder.getFiles()) {
            updatedSize += f.getSize();
        }
        // folders sizes
        for (var f : folder.getFolders()) {
            updatedSize += f.getSize();
        }
        folder.setSize(updatedSize);

        folderRepository.save(folder);
    }

    /**
     * add all folders and their child folders to database
     *
     * @param content       folder content list(folders and files inside)
     */
    public void addAllFoldersToDatabase(List<java.io.File> content) {
        content.parallelStream().forEach(file -> {
            // if file is directory missing not present in database
            if (!folderExist(file.getPath()) && file.isDirectory()) {
                addFolder(file);

                var insideContent = Arrays.asList(file.listFiles());

                if (!insideContent.isEmpty()) addAllFoldersToDatabase(insideContent);
            }
        });
    }

    /**
     * add filesystem folder child elements as one to many records for folder entity in database
     *
     * @param childs        folder child's
     */
    public void addFolderChilds(List<java.io.File> childs) {
        childs.parallelStream().forEach(child -> {
            var parentPath = Paths.get(child.getPath()).getParent().toFile().getPath();

            var parent = getByPath(parentPath);

            if (parent != null) {
                if (child.isFile()) {
                    // get child file record
                    var dbFile = fileRepository.getByPath(child.getPath());

                    if (dbFile != null) addFileChild(parent, dbFile);
                } else if (child.isDirectory()) {
                    // get child folder record
                    var dbChild = getByPath(child.getPath());

                    if (!folderHasChildFolder(parent, dbChild))
                        addFolderChild(parent, dbChild);

                    // run recursive for folders inside child folder
                    addFolderChilds(Arrays.asList(child.listFiles()));
                }
            }
        });
    }

    /**
     * when folder is renamed,folders and files inside location need to be updated to new folder name
     *
     * @param parent        folder who child folders and files need update
     */
    private void updateChildsLocation(Folder parent) {
        var updatedParentName = parent.getName();

        parent.getFiles()
                .parallelStream()
                .forEach(childFile -> updateFileLocation(childFile, updatedParentName));

        parent.getFolders()
                .parallelStream()
                .forEach(childFolder -> updateFolderLocation(childFolder, updatedParentName));
    }

    /**
     * set new location name for folder
     *
     * @param folder            folder who need location name update
     * @param newLocation       new folder location name
     */
    private void updateFolderLocation(Folder folder, String newLocation) {
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

    /**
     * if folder path was updated this method runs and update all
     * child folders and files records path's
     *
     * @param parent        folder who path was updated 
     */
    private void updateChildsPaths(Folder parent) {
        // update child files path's
        parent.getFiles().parallelStream().forEach(childFile -> {
            childFile.setPath(parent.getPath() + "\\" + childFile.getName());

            fileRepository.save(childFile);
        });
        
        // update child folders path's
        parent.getFolders().parallelStream().forEach(childFolder -> {

            childFolder.setPath(parent.getPath() + "\\" + childFolder.getName());

            folderRepository.save(childFolder);
            // run recursive for content inside child folder
            updateChildsPaths(childFolder);
        });
    }

    /**
     * remove all folder childs records from database
     *
     * @param folder        parent folder
     */
    private void removeAllFolderChilds(Folder folder) {
        folder.getFolders().parallelStream().forEach(childFolder -> {
            removeChildFolders(childFolder);

            removeChildFiles(childFolder);

            folderRepository.delete(childFolder);
        });

        removeChildFiles(folder);
    }

    /**
     * remove all folders records inside folder,
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
     * remove all files records inside folder
     *
     * @param folder        parent folder
     */
    private void removeChildFiles(Folder folder) {
        folder.getFiles()
                .parallelStream()
                .forEach(fileRepository::delete);
    }
}
