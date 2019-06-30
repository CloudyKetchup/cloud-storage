package com.krypton.cloud.service.folder.record;

import com.krypton.cloud.model.Folder;
import com.krypton.cloud.service.record.RecordService;
import org.springframework.http.HttpStatus;

import java.io.File;

/**
 * Standard folder record service that manages folder records
 */
public interface FolderRecordService extends RecordService {

    @Override
    Folder getById(Long id);

    @Override
    Folder getByPath(String path);

    @Override
    HttpStatus save(File entity);

    @Override
    HttpStatus delete(String path);

    @Override
    boolean exists(String path);
}
