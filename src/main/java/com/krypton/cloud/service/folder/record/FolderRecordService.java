package com.krypton.cloud.service.folder.record;

import com.krypton.cloud.model.*;
import org.springframework.http.HttpStatus;

interface FolderRecordService {

    Folder getById(Long id);

    Folder getByName(String name);

    Folder getByPath(String path);

    void deleteFolderRecord(String folder);

    HttpStatus updateName(String folder, String newName);

    HttpStatus updatePath(java.io.File folder, String path);

    HttpStatus addFolder(java.io.File folder);

    void addFileChild(Folder folder, File file);

    void addFolderChild(Folder parent, Folder child);

    void removeFile(Folder folder, File file);

    void removeFolder(Folder parent, Folder child);

    boolean folderExist(String path);
}
