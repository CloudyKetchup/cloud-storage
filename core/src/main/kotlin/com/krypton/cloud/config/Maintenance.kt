package com.krypton.cloud.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import util.log.LoggingService

@Configuration
@EnableScheduling
open class Maintenance {

    /**
     * Every 10 days at 19:30 logs folder will be cleared
     */
    @Scheduled(cron = " 30 19 */10 * 6 * ")
    fun clearLogsFolder() = LoggingService.clearLogsFolder()
}