package com.krypton.cloud.service.folder.record;

import com.krypton.cloud.model.*;
import com.krypton.cloud.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    public HttpStatus updateName(String oldName, String newName) {
        var folder = folderRepository.getByName(oldName);

        folder.setName(newName);

        folderRepository.save(folder);

        return HttpStatus.OK;
    }

    @Override
    public HttpStatus addFolder(java.io.File folder) {
        // check if folder with same path exist
        if(!folderExist(folder.getPath())) {
            folderRepository.save(new Folder(folder));
        }
        // newly added to database folder as parent
        var parent = getByName(folder.getParentFile().getName());

        // check if parent is present and file to be added as child isn't already inside
        if (parent != null && !folderHasChildFolder(parent, getByName(folder.getName()))) {
            // add new folder as child
            addFolderChild(parent, getByName(folder.getName()));

            folderRepository.save(parent);
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
            updateFolderSize(folder);
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
            updateFolderSize(parent);
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
     * @param child     folder pretending to be inside
     * @return boolean
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
     * @param child     file pretending to be inside
     * @return boolean
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
        for(var f : folder.getFiles()) {
            updatedSize += f.getSize();
        }
        // folders sizes
        for(var f : folder.getFolders()) {
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
     * add folder child folders and files as one to many in database
     *
     * @param childs        folder child's
     */
    public void addFolderChilds(List<java.io.File> childs) {
        childs.parallelStream().forEach(child -> {
            var parent = getByName(child.getParentFile().getName());

            if (parent != null) {
                if (child.isFile()) {
                    fileRepository.getByName(child.getName())
                            .ifPresent(file -> addFileChild(parent, file));
                } else if (child.isDirectory()) {
                    addFolderChilds(Arrays.asList(child.listFiles()));
                }
            }
        });
    }
}
