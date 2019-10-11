package com.krypton.databaselayer.service.file.updater;

import java.io.File;

import org.springframework.http.HttpStatus;

public interface FileRecordUpdater {

    HttpStatus updateName(String path, File newFile);

    HttpStatus updatePath(String path, String newPath);
}
