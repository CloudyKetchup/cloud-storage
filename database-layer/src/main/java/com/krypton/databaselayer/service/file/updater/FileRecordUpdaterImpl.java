package com.krypton.databaselayer.service.file.updater;

import java.io.File;

import common.exception.entity.database.FileDatabaseException;
import com.krypton.databaselayer.repository.FileRepository;
import com.krypton.databaselayer.service.file.FileRecordServiceImpl;
import common.model.LogType;
import org.springframework.context.annotation.Bean;
import util.log.LogFolder;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import util.log.LoggingService;

@Service
@AllArgsConstructor
public class FileRecordUpdaterImpl implements FileRecordUpdater {

    private final FileRecordServiceImpl fileRecordService;

    private final FileRepository fileRepository;

    @Override
    public HttpStatus updateName(String path, File newFile) {
        var file = fileRecordService.getByPath(path);

        file.setName(newFile.getName());

        fileRepository.save(file);

        if (nameUpdated(path, newFile.getName())) {
            updatePath(path, newFile.getPath());

            return HttpStatus.OK;
        } else {
            return logError(new FileDatabaseException("Error updating name of file " + path).stackTraceToString());
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
            return logError(new FileDatabaseException("Error updating path of file : " + path).stackTraceToString());
        }
    }

    private HttpStatus logError(String message) {
        LoggingService.INSTANCE.saveLog(new FileDatabaseException(message).stackTraceToString(),
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
