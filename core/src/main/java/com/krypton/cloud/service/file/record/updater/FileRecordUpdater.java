package com.krypton.cloud.service.file.record.updater;

import org.springframework.http.HttpStatus;

public interface FileRecordUpdater {

    HttpStatus updateName(String path, String newName);

    HttpStatus updatePath(String path, String newPath);
}
