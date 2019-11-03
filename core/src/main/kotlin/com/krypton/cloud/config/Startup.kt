package com.krypton.cloud.config

import com.krypton.databaselayer.model.Folder
import com.krypton.databaselayer.service.folder.FolderRecordServiceImpl
import common.config.AppProperties
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
class Startup (
    private val folderRecordService : FolderRecordServiceImpl
) : CommandLineRunner {

    // folder used for storage
    private val storage = AppProperties.storageFolder
    // folder used for trash
    private val trashFolder = AppProperties.trashFolder

    override fun run(vararg args: String) {
		createFolders()

        folderRecordService.apply {
            save(Folder(storage))
            save(Folder(trashFolder))
        }
    }

    private fun createFolders() = AppProperties.allFolders.forEach { if (!it.exists()) it.mkdirs() }
}
