package com.krypton.cloud.config

import com.krypton.cloud.service.file.record.FileRecordServiceImpl
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl
import com.krypton.cloud.service.folder.record.FolderRecordUtils
import lombok.AllArgsConstructor
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

/*
 * That actions will run on startup:
 *  - will load all files and folders located in cloud folder into database
 *  - initialize logs folder
 *  and so on...
 */
@Service
@AllArgsConstructor
class Startup(
        private val fileRecordService    : FileRecordServiceImpl,
        private val folderRecordService : FolderRecordServiceImpl,
        private val folderRecordUtils   : FolderRecordUtils
) : CommandLineRunner {

    // folder used for cloud storage
    private val root = File("${System.getProperty("user.home")}/Desktop/Cloud")
    // folder used for storing log files
    private val logsFolder = File("${root.path}/Logs")

    override fun run(vararg args: String) {
        
        // create root folder if doesn't exist
        if (!root.exists()) root.mkdir()

        if (!logsFolder.exists()) logsFolder.mkdir()

        // list of files and folders from cloud root
        val rootContent = Arrays.asList(*root.listFiles())

        folderRecordService.save(root)

        folderRecordUtils.addAllFoldersToDatabase(rootContent)

        fileRecordService.addAllFilesToDatabase(rootContent)
    }
}
