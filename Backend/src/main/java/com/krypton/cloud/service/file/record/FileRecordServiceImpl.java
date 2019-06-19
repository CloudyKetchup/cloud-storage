package com.krypton.cloud.service.file.record;

import com.krypton.cloud.model.File;
import com.krypton.cloud.repository.FileRepository;
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class FileRecordServiceImpl implements FileRecordService {

    private final FileRepository fileRepository;

    private final FolderRecordServiceImpl folderRecordService;

    @Override
    public File getById(Long id) {
        return fileRepository.getOne(id);
    }

    @Override
    public File getByName(String name) {
        return fileRepository.getByName(name).get();
    }

    @Override
    public File getByPath(String path) {
        return fileRepository.getByPath(path);
    }

    @Override
    public File addFileRecord(java.io.File file) {
        // file saved to database
        var dbFile          = fileRepository.save(new File(file));
        // filesystem folder where file is located
        var parentFolder    = Paths.get(file.getPath()).getParent().toFile();
        // folder record where file need to be added as child
        var dbParentFolder  = folderRecordService.getByPath(parentFolder.getPath());

        folderRecordService.addFileChild(dbParentFolder, dbFile);

        return dbFile;
    }

    @Override
    public HttpStatus deleteFileRecord(String path) {
        var file = getByPath(path);

        fileRepository.delete(file);

        return !fileExist(file.getPath()) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public HttpStatus renameFile(String path, String newName) {
        var file = getByPath(path);

        file.setName(newName);

        fileRepository.save(file);

        return getByPath(path).getName().equals(newName) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public HttpStatus updatePath(String path, String newPath) {
        var file = getByPath(path);

        file.setPath(newPath);

        fileRepository.save(file);

        return getByPath(path).getPath().equals(newPath) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public boolean fileExist(String path) {
        return fileRepository.getByPath(path) != null;
    }

    /**
     * add all cloud files records to database, runs on startup
     *
     * @param files         files list for database
     */
    public void addAllFilesToDatabase(List<java.io.File> files) {
        files.parallelStream().forEach(file -> {
            if (file.isFile() && !fileExist(file.getPath())) {
                addFileRecord(file);
            }else if (file.isDirectory()) {
                var insideContent = Arrays.asList(file.listFiles());

                addAllFilesToDatabase(insideContent);
            }
        });
    }
}
