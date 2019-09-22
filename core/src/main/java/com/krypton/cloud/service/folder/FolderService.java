package com.krypton.cloud.service.folder;

import com.krypton.cloud.service.IOEntityService;
import org.springframework.http.HttpStatus;

public interface FolderService extends IOEntityService {

    HttpStatus createFolder(String name, String path);

    HttpStatus deleteFolderContent(String path);
}
