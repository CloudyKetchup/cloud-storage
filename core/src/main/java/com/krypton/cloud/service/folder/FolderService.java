package com.krypton.cloud.service.folder;

import com.krypton.cloud.service.EntityService;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public interface FolderService extends EntityService {

    HttpStatus create(String name, String path);

    HttpStatus deleteContent(UUID id);
}
