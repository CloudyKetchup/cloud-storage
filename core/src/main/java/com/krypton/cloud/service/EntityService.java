package com.krypton.cloud.service;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public interface EntityService {

    HttpStatus move(String oldPath, String newFolder);

    HttpStatus copy(String folderPath, String copyPath);

    HttpStatus rename(UUID id, String newName);

    HttpStatus delete(UUID id);
}
