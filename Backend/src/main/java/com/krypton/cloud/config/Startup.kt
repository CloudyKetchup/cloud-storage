package com.krypton.cloud.config

import com.krypton.cloud.service.file.record.FileRecordServiceImpl
import com.krypton.cloud.service.folder.record.FolderPersistenceHelper
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl
import com.krypton.cloud.service.folder.record.FolderRecordUtils
import lombok.AllArgsConstructor
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service

import java.io.File
import java.util.Arrays

@Service
@AllArgsConstructor
class Startup(
        private val fileRecordService: FileRecordServiceImpl,
        private val folderRecordService: FolderRecordServiceImpl,
        private val folderRecordUtils: FolderRecordUtils
) : CommandLineRunner {

    override fun run(vararg args: String) {

        val rootFolder = File("C:\\Users\\dodon\\cloud")

        if (!rootFolder.exists()) {
            rootFolder.mkdir()
        }

        val rootContent = Arrays.asList(*rootFolder.listFiles()!!)

        folderRecordService.save(rootFolder)

        folderRecordUtils.addAllFoldersToDatabase(rootContent)

        fileRecordService.addAllFilesToDatabase(rootContent)
    }
}
