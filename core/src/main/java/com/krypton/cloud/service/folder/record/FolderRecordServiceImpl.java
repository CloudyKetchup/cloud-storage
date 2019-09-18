package com.krypton.cloud.service.folder.record;

import com.krypton.cloud.exception.entity.database.FolderDatabaseException;
import com.krypton.cloud.model.*;
import com.krypton.cloud.repository.*;
import com.krypton.cloud.service.handler.http.ErrorHandler;
import com.krypton.cloud.service.util.log.LogFolder;
import com.krypton.cloud.service.util.log.LoggingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;

@Service
@AllArgsConstructor
public class FolderRecordServiceImpl implements FolderRecordService, ErrorHandler {

    private final FolderPersistenceHelper folderPersistenceHelper;

    private final FolderRepository folderRepository;

    private final LoggingService loggingService;

    @Override
    public Folder getById(UUID id) {
        return folderRepository.findById(id).get();
    }

    @Override
    public Folder getByPath(String path) {
        return folderRepository.getByPath(path);
    }

    @Override
    public HttpStatus save(java.io.File folder) {
        // check if folder with same path exist
        if (!exists(folder.getPath())) {
            folderRepository.save(new Folder(folder));
        }
        // check if folder was saved
        if (!exists(folder.getPath())) {
            // save error log
            return httpError("Error saving folder " + folder.getPath());
        }
        var parentPath = Paths.get(folder.getPath()).getParent().toAbsolutePath();
        // newly added to database folder parent
        var parent = getByPath(parentPath.toString());

        addToParent(parent, getByPath(folder.getPath()));

        return HttpStatus.OK;
    }

    @Override
    public HttpStatus delete(String folderPath) {
        var folder = getByPath(folderPath);

        folderPersistenceHelper.removeAllFolderChilds(folder);

        folderRepository.delete(folder);

        if (getByPath(folderPath) == null) {
            return HttpStatus.OK;
        } else {
            // save error log
            return httpError("Error deleting folder " + folderPath);
        }
    }

    @Override
    public boolean exists(String path) {
        return folderRepository.getByPath(path) != null;
    }

    @Override
    public HttpStatus httpError(String message) {
        loggingService.saveLog(new FolderDatabaseException(message).stackTraceToString(),
                LogType.ERROR,
                LogFolder.DATABASE.getType() + LogFolder.FOLDER.getType());
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * When {@link #save} runs it save a {@link Folder},then it calls this method,
     * witch will add to parent folder newly added folder as child
     *
     * @param parent    folder where to add child folder
     * @param folder    folder to be added as child
     */
    private void addToParent(Folder parent, Folder folder) {
        // check if parent is present and file to be added as child isn't already inside
        if (parent != null && !folderPersistenceHelper.folderHasChildFolder(parent, folder)) {
            // add new folder as child
            folderPersistenceHelper.addFolderChild(parent, folder);
        }
    }

    /**
     * get folders inside a {@link Folder}
     *
     * @param id    folder id
     * @return a flux stream containing folders
     */
    public Flux<Folder> getFolderFolders(UUID id) {
        return Flux.fromStream(getById(id)
                .getFolders()
                .stream()
                .sorted(Comparator.comparing(Folder::getId)));
    }

    /**
     * get files inside a {@link Folder}
     *
     * @param id    folder id
     * @return a flux stream containing files
     */
    public Flux<File> getFolderFiles(UUID id) {
        return Flux.fromStream(getById(id)
                .getFiles()
                .stream()
                .sorted(Comparator.comparing(File::getId)));
    }
}