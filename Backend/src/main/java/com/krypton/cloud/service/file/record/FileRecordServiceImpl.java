package com.krypton.cloud.service.file.record;

import com.krypton.cloud.model.File;
import com.krypton.cloud.model.Folder;
import com.krypton.cloud.repository.FileRepository;
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
        var file = fileRepository.getByName(name);

        assert file.isPresent();

        return file.get();
    }

    @Override
    public File addFile(java.io.File file) {
        return fileRepository.save(new File(file));
    }

    @Override
    public void deleteFileRecord(File file) {
        fileRepository.delete(file);
    }

    @Override
    public HttpStatus updateName(String oldName, String newName) {
        var file = getByName(oldName);

        file.setName(newName);

        fileRepository.save(file);

        return HttpStatus.OK;
    }

    @Override
    public boolean fileExist(String name) {
        return fileRepository.getByName(name).isPresent();
    }

    /**
     * add all cloud files records to database, runs on startup
     *
     * @param files         files list for database
     */
    public void addAllFilesToDatabase(List<java.io.File> files) {
        files.parallelStream().forEach(file -> {
            // if file is folder
            if (file.isDirectory()) {
                // call this function recursively for all files inside
                addAllFilesToDatabase(Arrays.asList(file.listFiles()));
            // if file does not exist in database
            } else if (!fileExist(file.getName())) {
                // add file to database
                var dbFile = addFile(file);

                // parent folder from database
                Folder parent;

                if (file.getParentFile().getPath().equals("C:\\Users\\dodon\\cloud")) {
                    parent = folderRecordService.getByPath("C:\\Users\\dodon\\cloud");
                } else {
                    parent = folderRecordService.getByPath(file.getParentFile().getPath());
                }

                folderRecordService.addFileChild(parent, dbFile);
            }
        });
    }
}
