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
    public void deleteFolderRecord(String folder) {
        folderRepository.delete(getByName(folder));
    }

    @Override
    public HttpStatus updateName(String path, String newName) {
        var folder = getByPath(path);

        folder.setName(newName);

        folderRepository.save(folder);

        return HttpStatus.OK;
    }

    @Override
    public HttpStatus updatePath(java.io.File folder, String newPath) {
        var dbFolder = getByPath(folder.getPath());

        dbFolder.setPath(newPath);

        folderRepository.save(dbFolder);

        updateChildPaths(dbFolder);

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
        if (parent != null && !folderHasChildFolder(parent, getByName(folder.getName()))) {
            // add new folder as child
            addFolderChild(parent, getByName(folder.getName()));

            updateFolderSize(parent);
        }
        return HttpStatus.OK;
    }

    @Override
    public void addFileChild(Folder folder, File file) {
        if (
            folder  != null 
            && 
            file    != null 
            && 
            !folderHasChildFile(folder, file)
        ) {
            folder.getFiles().add(file);
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
        }
    }

    @Override
    public void removeFile(Folder folder, File file) {
        folder.getFiles().remove(file);

        folderRepository.save(folder);
    }

    @Override
    public void removeFolder(Folder parent, Folder child) {
        parent.getFolders().remove(child);

        folderRepository.save(parent);
    }

    @Override
    public boolean folderExist(String path) {
        return folderRepository.getByPath(path) != null;
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
                .anyMatch(insideFolder -> insideFolder.getId().equals(child.getId()));
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
                .anyMatch(insideFile -> insideFile.getId().equals(child.getId()));
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
            var parentPath = Paths.get(child.getPath()).getParent().toAbsolutePath();

            var parent = getByPath(parentPath.toString());

            if (parent != null) {
                if (child.isFile()) {
                    
                    fileRepository.getByName(child.getName())
                            .ifPresent(file -> addFileChild(parent, file));
                } else if (child.isDirectory()) {
                    var dbChild = getByPath(child.getPath());

                    if (!folderHasChildFolder(parent, dbChild))
                        addFolderChild(parent, dbChild);

                    addFolderChilds(Arrays.asList(child.listFiles()));
                }
            }
        });
    }

    /**
     * if folder path was updated this method runs and update all
     * child folders and files records path's
     *
     * @param parent        folder who path was updated 
     */
    private void updateChildPaths(Folder parent) {
        // update child files path's
        parent.getFiles().parallelStream().forEach(childFile -> {
            childFile.setPath(parent.getPath() + "\\" + childFile.getName());

            fileRepository.save(childFile);
        });
        
        // update child folders path's
        parent.getFolders().parallelStream().forEach(childFolder -> {

            childFolder.setPath(parent.getPath() + "\\" + childFolder.getName());

            folderRepository.save(childFolder);

            if (!childFolder.getFolders().isEmpty()) updateChildPaths(childFolder);
        });
    }
}
