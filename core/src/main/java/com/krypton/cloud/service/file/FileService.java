package com.krypton.cloud.service.file;

import com.krypton.cloud.service.IOEntityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface FileService extends IOEntityService {

    Mono<HttpStatus> saveFile(FilePart file, String folder);

}
