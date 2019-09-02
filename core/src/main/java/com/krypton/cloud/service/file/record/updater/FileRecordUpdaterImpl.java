package com.krypton.cloud.service.file.record.updater;

import java.io.File;

import com.krypton.cloud.exception.entity.database.FileDatabaseException;
import com.krypton.cloud.model.LogType;
import com.krypton.cloud.repository.FileRepository;
import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import com.krypton.cloud.service.handler.http.ErrorHandler;
import com.krypton.cloud.service.util.log.LogFolder;
import com.krypton.cloud.service.util.log.LoggingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FileRecordUpdaterImpl implements FileRecordUpdater, ErrorHandler {

    private final FileRecordServiceImpl fileRecordService;

    private final FileRepository fileRepository;

    private final LoggingService loggingService;

    @Override
    public HttpStatus updateName(String path, File newFile) {
        var file = fileRecordService.getByPath(path);

        file.setName(newFile.getName());

        fileRepository.save(file);

        if (nameUpdated(path, newFile.getName())) {
            updatePath(path, newFile.getPath());

            return HttpStatus.OK;
        } else {
            return httpError(new FileDatabaseException("Error updating name of file " + path).stackTraceToString());
        }
    }

    @Override
    public HttpStatus updatePath(String path, String newPath) {
        var file = fileRecordService.getByPath(path);

        file.setPath(newPath);

        fileRepository.save(file);

        if (!fileRecordService.exists(path)) {
            return HttpStatus.OK;
        } else {
            return httpError(new FileDatabaseException("Error updating path of file : " + path).stackTraceToString());
        }
    }

    @Override
    public HttpStatus httpError(String message) {
        loggingService.saveLog(new FileDatabaseException(message).stackTraceToString(),
                LogType.ERROR,
                LogFolder.DATABASE.getType() + LogFolder.FILE.getType());
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Check if file name was updated
     *
     * @param path      path of the file
     * @param newName   new file name
     * */
    private boolean nameUpdated(String path, String newName) {
        return fileRepository.getByPath(path).getName().equals(newName);
    }
}
