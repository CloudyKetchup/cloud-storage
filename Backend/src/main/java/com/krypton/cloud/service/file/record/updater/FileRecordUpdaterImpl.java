package com.krypton.cloud.service.file.record.updater;

import com.krypton.cloud.repository.FileRepository;
import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FileRecordUpdaterImpl implements FileRecordUpdater {

    private final FileRecordServiceImpl fileRecordService;

    private final FileRepository fileRepository;

    @Override
    public HttpStatus updateName(String path, String newName) {
        var file = fileRecordService.getByPath(path);

        file.setName(newName);

        fileRepository.save(file);

        return fileRecordService.getByPath(path).getName().equals(newName) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public HttpStatus updatePath(String path, String newPath) {
        var file = fileRecordService.getByPath(path);

        file.setPath(newPath);

        fileRepository.save(file);

        return !fileRecordService.exists(path) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
