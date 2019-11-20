package com.krypton.cloud.config

import com.krypton.databaselayer.model.File
import com.krypton.databaselayer.service.IOEntityRecordService
import common.config.AppProperties
import lombok.AllArgsConstructor
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import util.log.LoggingService

@Configuration
@EnableScheduling
@AllArgsConstructor
class Maintenance(private val fileRecordService : IOEntityRecordService<File>) {

    /**
     * Every 10 days at 19:30 logs folder will be cleared
     */
    @Scheduled(cron = " 30 19 */10 * 6 * ")
    suspend fun clearLogsFolder() = LoggingService.clearLogsFolder()

	/**
	 * Delete thumbnails witch file was removed from database
	 * Runs every day at 12:00
	 * */
    @Scheduled(cron = " 0 12 1/1 * ? * ")
    suspend fun deleteOrphanThumbnails() {
		val thumbnailsFolder = AppProperties.thumbnailsFolder
        val files = fileRecordService.findAll()
        val thumbnails = listOf<java.io.File>(*thumbnailsFolder.listFiles())
        val whitelist = ArrayList<java.io.File>()

		files.parallelStream().forEach { file ->
            val t = thumbnails.filter { it.name === "${thumbnailsFolder.path}/${file.id}.jpg" }

			whitelist.add(t[0])
        }

		thumbnails.parallelStream().forEach { t ->
			if (whitelist.find { t.name === it.name } == null) t.delete()
		}
    }
}