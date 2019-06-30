package com.krypton.cloud.service.folder;

import com.krypton.cloud.model.Folder;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

public interface FolderService {

    Folder getFolderData(Long id);

    HttpStatus createFolder(String folderName, String folderPath);

    HttpStatus copyFolder(String folderPath, String copyPath);

    HttpStatus cutFolder(String oldPath, String newPath);

    HttpStatus renameFolder(String folderPath, String newName);

    HttpStatus deleteFolder(String folderPath);

    Resource getFolder(String folder);
}
