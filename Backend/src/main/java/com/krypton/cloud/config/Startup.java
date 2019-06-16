package com.krypton.cloud.config;

import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;

@Service
@AllArgsConstructor
public class Startup implements CommandLineRunner {

    private final FileRecordServiceImpl fileRecordService;

    private final FolderRecordServiceImpl folderRecordService;

    @Override
    public void run(String... args) {

        var rootFolder = new File("C:\\Users\\dodon\\cloud");

        if (!rootFolder.exists()) {
            rootFolder.mkdir();
        }

        var rootContent = Arrays.asList(rootFolder.listFiles());

        folderRecordService.addFolder(rootFolder);

        folderRecordService.addAllFoldersToDatabase(rootContent);

        fileRecordService.addAllFilesToDatabase(rootContent);

        folderRecordService.addFolderChilds(rootContent);
    }
}
