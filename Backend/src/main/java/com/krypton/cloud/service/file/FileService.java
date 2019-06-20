package com.krypton.cloud.service.file;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileService {

    Flux<HttpStatus> saveFiles(Flux<FilePart> files, String folder);

    Mono<HttpStatus> saveFile(Mono<FilePart> file, String folder);

    HttpStatus cutFile(String oldPath, String newPath);

    HttpStatus copyFile(String oldPath, String newPath);

    HttpStatus renameFile(String path, String newName);

    HttpStatus deleteFile(String path);

    Resource downloadFile(String path);
}
