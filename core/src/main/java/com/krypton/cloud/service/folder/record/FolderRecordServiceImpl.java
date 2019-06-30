package com.krypton.cloud.service.folder.record;

import com.krypton.cloud.model.*;
import com.krypton.cloud.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class FolderRecordServiceImpl implements FolderRecordService {

    private final FolderPersistenceHelper folderPersistenceHelper;

    private final FolderRepository folderRepository;

    @Override
    public Folder getById(Long id) {
        var folder = folderRepository.findById(id);

        return folder.get();
    }

    @Override
    public Folder getByPath(String path) {
        return folderRepository.getByPath(path);
    }

    @Override
    public HttpStatus save(java.io.File folder) {
        // check if folder with same path exist
        if(!exists(folder.getPath())) {
            folderRepository.save(new Folder(folder));
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

        return getByPath(folderPath) == null ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public boolean exists(String path) {
        return folderRepository.getByPath(path) != null;
    }

    /**
     * When {@link #save(java.io.File)} runs it save a {@link Folder},then it calls this method,
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

            // updateFolderSize(parent);
        }
    }

    /**
     * get folders inside a {@link Folder}
     *
     * @param id    folder id
     * @return a flux stream containing folders
     */
    public Flux<Folder> getFolderFolders(Long id) {
        return Flux.fromStream(getById(id)
                .getFolders()
                .stream()
                .sorted((folder1, folder2) -> (int) (folder1.getId() - folder2.getId())));
    }

    /**
     * get files inside a {@link Folder}
     *
     * @param id    folder id
     * @return a flux stream containing files
     */
    public Flux<File> getFolderFiles(Long id) {
        return Flux.fromStream(getById(id)
                .getFiles()
                .stream()
                .sorted((folder1, folder2) -> (int) (folder1.getId() - folder2.getId())));
    }
}