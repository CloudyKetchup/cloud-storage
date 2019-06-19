package com.krypton.cloud.service.folder.record;

import com.krypton.cloud.model.*;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;

interface FolderRecordService {

    Folder getById(Long id);

    Folder getByName(String name);

    Folder getByPath(String path);

    HttpStatus updateName(String folder, String newName);

    HttpStatus updatePath(java.io.File folder, String path, String oldParentPath);

    HttpStatus updatePath(java.io.File folder, String path);

    HttpStatus addFolderRecord(java.io.File folder);

    void deleteFolderRecord(String folder);

    Flux<Folder> getFolderFolders(Long id);

    Flux<File> getFolderFiles(Long id);

    void addFileChild(Folder folder, File file);

    void addFolderChild(Folder parent, Folder child);

    void removeFileChild(Folder folder, File file);

    void removeFolderChild(Folder parent, Folder child);

    boolean folderExist(String path);
}
