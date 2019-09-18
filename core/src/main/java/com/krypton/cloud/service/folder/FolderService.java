package com.krypton.cloud.service.folder;

import com.krypton.cloud.model.Folder;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.UUID;

public interface FolderService {

    HttpStatus createFolder(String folderName, String folderPath);

    HttpStatus copyFolder(String folderPath, String copyPath);

    HttpStatus cutFolder(String oldPath, String newPath);

    HttpStatus renameFolder(String folderPath, String newName);

    HttpStatus deleteFolder(String folderPath);

    HttpStatus deleteFolderContent(String folderPath);
}
