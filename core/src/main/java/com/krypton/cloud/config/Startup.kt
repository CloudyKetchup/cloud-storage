package com.krypton.cloud.config

import com.krypton.cloud.service.file.record.FileRecordServiceImpl
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl
import com.krypton.cloud.service.folder.record.FolderRecordUtils
import lombok.AllArgsConstructor
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service

/*
 * That actions will run on startup:
 *  - will load all files and folders located in cloud folder into database
 *  - initialize logs folder
 *  and so on...
 */
@Service
@AllArgsConstructor
class Startup(
        private val fileRecordService   : FileRecordServiceImpl,
        private val folderRecordService : FolderRecordServiceImpl,
        private val folderRecordUtils   : FolderRecordUtils,
        appProperties : AppProperties
) : CommandLineRunner {

    // folder used for storage
    private val storage = appProperties.storageFolder
    // folder used for storing log files
    private val logsFolder = appProperties.logsFolder
    // folder used for trash
    private val trashFolder = appProperties.trashFolder

    override fun run(vararg args: String) {
        createFolders()

        folderRecordService.apply {
            save(storage)
            save(trashFolder)
        }

        // list of files and folders from cloud root
        val rootContent = listOf(*storage.listFiles()!!)

        // if any files or folders exist, add them to database
        if (rootContent.isNotEmpty()) {
            folderRecordUtils.addAllFoldersToDatabase(rootContent)

            fileRecordService.addAllFilesToDatabase(rootContent)
        }
        // if trash folder have any files or folders, add them to database
        if (!trashFolder.listFiles().isNullOrEmpty()) {
            val trashContent = listOf(*trashFolder.listFiles()!!)

            folderRecordUtils.addAllFoldersToDatabase(trashContent)

            fileRecordService.addAllFilesToDatabase(trashContent)
        }
    }

    private fun createFolders() {
        // create storage folder
        if (!storage.exists()) storage.mkdirs()
        // create folder for logs if doesn't exist
        if (!logsFolder.exists()) logsFolder.mkdirs()
        // create trash folder
        if (!trashFolder.exists()) trashFolder.mkdirs()
    }
}
