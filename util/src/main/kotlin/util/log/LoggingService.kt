package util.log

import common.config.AppProperties
import common.exception.log.LogException
import common.exception.log.LogFileException
import common.exception.log.LogFolderException
import common.model.LogType
import java.time.LocalDateTime
import java.io.File
import java.io.IOException

object LoggingService {

    /**
     * Write log file to disk
     *
     * @param message   log text that will be written to file
     * @param type      [LogType] type
     * @param logFolder folder path where to save log, by default is logs folder root => .../Cloud/Logs/
     * */
    @JvmOverloads
    fun saveLog(message : String?, type : LogType, logFolder : String = LogFolder.ROOT.type) {
        if (message != null) {
            // create folder for log
            createLogFolder(path = logFolder, folderPath = { folderPath ->
                val logFile = File("$folderPath/${LocalDateTime.now()}-$type.txt")
                // create file and then write log to it
                if (createLogFile(logFile)) logFile.printWriter().use { it.println(message) }
            })
            // if message was null save error log
        } else saveLog(LogException("Log message was null or empty").stackTraceToString(), LogType.ERROR, LogFolder.ROOT.type)
    }

    /**
     * Create file where Log will be written
     * 
     * @param logFile   [File] where message will be written
     * @return boolean depending if file was created successful
     */
    private fun createLogFile(logFile : File) : Boolean {
        try {
            logFile.createNewFile()
            // check if file was created
            if (logFile.exists())
                return true    
            else
                throw LogFileException("Failed to create Log file :(")
        } catch (e : Exception) {
            e.printStackTrace()
        } 
        return false
    }

    /**
     * Create a folder where will be stored a Log file, every log file
     * will have it's own folder named by date and type
     *
     * @param folderPath    this function will return created folder path as lambda
     * @param path          path for log folder, by default is null
     */
    private fun createLogFolder(path : String? = null, folderPath: (path: String?) -> Unit) {
        val time = LocalDateTime.now()
        // folder for log named by time created
        val folder = File("${AppProperties.logsFolder.absolutePath}$path/$time")

        try {
            folder.mkdirs()
            // check if folder was created
            if (folder.exists())
                // return new folder path to higher order function(as lambda)
                folderPath.invoke(folder.path)
            else
                throw LogFolderException("Error while creating folder -> LoggingService.kt")
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    // Delete all logs
    fun clearLogsFolder() {
        try {
            File(AppProperties.logsFolder.absolutePath).listFiles()!!.forEach { it.deleteRecursively() }
        } catch (e : IOException) {
            saveLog(e.message!!, LogType.ERROR)
        }
    }
}