package com.krypton.cloud.service.file.record;

import com.krypton.cloud.model.File;
import org.springframework.http.HttpStatus;

public interface FileRecordService {

    File getById(Long id);

    File getByName(String name);

    File getByPath(String path);

    HttpStatus updatePath(String path, String newPath);

    File addFileRecord(java.io.File file);

    HttpStatus deleteFileRecord(String path);

    HttpStatus renameFile(String path, String newName);

    boolean fileExist(String path);
}
