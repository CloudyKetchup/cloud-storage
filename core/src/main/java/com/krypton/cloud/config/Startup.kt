package com.krypton.cloud.config

import com.krypton.cloud.service.file.record.FileRecordServiceImpl
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl
import com.krypton.cloud.service.folder.record.FolderRecordUtils
import lombok.AllArgsConstructor
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
@AllArgsConstructor
class Startup(
        private val fileRecordService   : FileRecordServiceImpl,
        private val folderRecordService : FolderRecordServiceImpl,
        private val folderRecordUtils   : FolderRecordUtils
) : CommandLineRunner {

    private val root = File(System.getProperty("user.home") + "/Desktop/Cloud")

    override fun run(vararg args: String) {

        if (!root.exists()) {
            root.mkdir()
        }

        val rootContent = Arrays.asList(*root.listFiles())

        folderRecordService.save(root)

        folderRecordUtils.addAllFoldersToDatabase(rootContent)

        fileRecordService.addAllFilesToDatabase(rootContent)
    }
}
