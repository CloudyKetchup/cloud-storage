package com.krypton.cloud.config

import com.krypton.cloud.service.file.record.FileRecordServiceImpl
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl
import com.krypton.cloud.service.folder.record.FolderRecordUtils
import lombok.AllArgsConstructor
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service
import java.io.File

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
        private val appProperties : AppProperties
) : CommandLineRunner {

    private val root = appProperties.root

    // folder used for storing log files
    private val logsFolder = File("${root.path}/Logs")

    override fun run(vararg args: String) {
        // create root folder if doesn't exist
        if (!root.exists()) root.mkdirs()
        // create folder for logs if doesn't exist
        if (!logsFolder.exists()) logsFolder.mkdirs()
        // list of files and folders from cloud root
        val rootContent = listOf(*root.listFiles()!!)

        folderRecordService.save(root)
        // if any files or folders exist, add them to database
        if (rootContent.isNotEmpty()) {
            folderRecordUtils.addAllFoldersToDatabase(rootContent)

            fileRecordService.addAllFilesToDatabase(rootContent)
        }
    }
}
