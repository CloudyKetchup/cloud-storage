package com.krypton.cloud.service.file.record;

import com.krypton.cloud.model.File;
import com.krypton.cloud.service.record.RecordService;
import org.springframework.http.HttpStatus;

public interface FileRecordService extends RecordService {

    @Override
    File getById(Long id);

    @Override
    File getByPath(String path);

    @Override
    File save(java.io.File file);

    @Override
    HttpStatus delete(String path);

    @Override
    boolean exists(String path);
}
