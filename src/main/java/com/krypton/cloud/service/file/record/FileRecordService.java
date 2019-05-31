package com.krypton.cloud.service.file.record;

import com.krypton.cloud.model.File;
import com.krypton.cloud.model.Folder;
import org.springframework.http.HttpStatus;

public interface FileRecordService {

    File getById(Long id);

    File getByName(String name);

    File addFile(java.io.File file);

    void deleteFileRecord(File file);

    HttpStatus updateName(String oldName, String newName);

    boolean fileExist(String name);
}
