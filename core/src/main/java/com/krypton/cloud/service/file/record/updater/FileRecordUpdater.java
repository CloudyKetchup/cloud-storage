package com.krypton.cloud.service.file.record.updater;

import java.io.File;

import org.springframework.http.HttpStatus;

public interface FileRecordUpdater {

    HttpStatus updateName(String path, File newFile);

    HttpStatus updatePath(String path, String newPath);
}
