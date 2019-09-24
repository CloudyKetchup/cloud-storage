package com.krypton.cloud.service.folder.record;

import com.krypton.cloud.service.record.IOEntityRecordService;
import common.exception.entity.database.FolderDatabaseException;
import com.krypton.cloud.model.File;
import com.krypton.cloud.model.Folder;
import com.krypton.cloud.repository.FolderRepository;
import com.krypton.cloud.service.handler.http.ErrorHandler;
import common.model.LogType;
import util.log.LogFolder;
import util.log.LoggingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;

@Service
@AllArgsConstructor
public class FolderRecordServiceImpl implements IOEntityRecordService<Folder>, ErrorHandler {

    private final FolderPersistenceHelper folderPersistenceHelper;

    private final FolderRepository folderRepository;

    @Override
    public Folder getById(UUID id) {
        return folderRepository.findById(id).get();
    }

    @Override
    public Folder getByPath(String path) {
        return folderRepository.getByPath(path);
    }

    @Override
    public Folder save(Folder folder) {
        // check if folder with same path exist
        if (!exists(folder.getPath())) {
            folderRepository.save(folder);
        }
        // check if folder was saved
        if (!exists(folder.getPath())) {
            // save error log
            httpError("Error saving folder " + folder.getPath());
            return null;
        }
        var parentPath = Paths.get(folder.getPath()).getParent().toAbsolutePath();
        // newly added to database folder parent
        var parent = getByPath(parentPath.toString());

        addToParent(parent, getByPath(folder.getPath()));

        return folder;
    }

    @Override
    public boolean delete(String folderPath) {
        var folder = getByPath(folderPath);

        folderPersistenceHelper.removeAllFolderChilds(folder);

        folderRepository.delete(folder);

        return !exists(folderPath);
    }

    @Override
    public boolean exists(String path) {
        return folderRepository.getByPath(path) != null;
    }

    @Override
    public HttpStatus httpError(String message) {
        LoggingService.INSTANCE.saveLog(new FolderDatabaseException(message).stackTraceToString(),
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