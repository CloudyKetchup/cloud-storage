package com.krypton.cloud.service.folder;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

public interface FolderService {

	String[] getRootFilesList();

    String[] getFolderContent(String folder);

    HttpStatus createFolder(String folderName, String folderPath);

    HttpStatus renameFolder(String folder, String newName);

    HttpStatus deleteFolder(String folder);

    void createZip(String folder);

    Resource getFolder(String folder);
}
