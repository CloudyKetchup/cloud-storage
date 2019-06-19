package com.krypton.cloud.config

import com.krypton.cloud.service.file.record.FileRecordServiceImpl
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl
import lombok.AllArgsConstructor
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service

import java.io.File
import java.util.Arrays

@Service
@AllArgsConstructor
class Startup(
        private val fileRecordService: FileRecordServiceImpl,
        private val folderRecordService: FolderRecordServiceImpl
) : CommandLineRunner {

    override fun run(vararg args: String) {

        val rootFolder = File("C:\\Users\\dodon\\cloud")

        if (!rootFolder.exists()) {
            rootFolder.mkdir()
        }

        val rootContent = Arrays.asList(*rootFolder.listFiles()!!)

        folderRecordService.addFolderRecord(rootFolder)

        folderRecordService.addAllFoldersToDatabase(rootContent)

        fileRecordService.addAllFilesToDatabase(rootContent)

        folderRecordService.addFolderChilds(rootContent)
    }
}
