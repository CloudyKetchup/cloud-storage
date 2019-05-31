package com.krypton.cloud.service.folder;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

public interface FolderService {

	HashMap getRootData();

    HashMap getFolderData(Long id);

    HttpStatus createFolder(String folderName, String folderPath);

    HttpStatus renameFolder(String folder, String newName);

    HttpStatus deleteFolder(String folderPath);

    Resource getFolder(String folder);
}
