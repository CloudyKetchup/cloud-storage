package com.krypton.cloud.service.file;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileService {

    Mono<HttpStatus> saveFiles(Flux<FilePart> files, String folder);

    Mono<HttpStatus> saveFile(Mono<FilePart> file, String folder);

    Resource getFile(String file, String folder);

    HttpStatus renameFile(String file, String folder, String newName);

    HttpStatus deleteFile(String file, String folder);
}
