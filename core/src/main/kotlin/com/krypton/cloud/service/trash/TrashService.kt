package com.krypton.cloud.service.trash

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.krypton.cloud.model.*
import com.krypton.cloud.repository.*
import com.krypton.cloud.service.file.FileServiceImpl
import com.krypton.cloud.service.folder.FolderServiceImpl
import com.krypton.cloud.service.record.RecordService
import common.config.AppProperties
import common.model.EntityType
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

import com.krypton.cloud.service.folder.record.FolderRecordUtils
import common.exception.ExceptionTools
import common.exception.entity.backup.BackupReadException
import common.exception.entity.backup.BackupWriteException
import common.model.LogType
import util.log.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * Service handling logic related to trash folder
 * */
@Service
@AllArgsConstructor
class TrashService<T : BaseEntity>(
        private val trashRepository : TrashRepository,
        private val fileService     : FileServiceImpl,
        private val fileRepository  : FileRepository,
        private val folderRepository: FolderRepository,
        private val folderService   : FolderServiceImpl
) : RecordService<TrashEntity> {

    override fun getById(id : UUID) : TrashEntity? = trashRepository.findById(id).orElse(null)

    override fun save(entity : TrashEntity) : TrashEntity? {
        val savedEntity = trashRepository.save(entity) ?: null

        return if (savedEntity != null) {
            return try {
                BackupService.writeBackup(entity)

                savedEntity
            } catch (e : BackupWriteException) {
                delete(savedEntity.id)
                return null
            }
        } else null
    }

    override fun delete(path : String) : Boolean { return false }

    override fun exists(path : String) : Boolean { return false }

    /**
     * delete [TrashEntity] by id
     *
     * @param id    uuid of [TrashEntity]
     * @return boolean depending on success
     * */
    private fun delete(id : UUID) : Boolean {
        trashRepository.deleteById(id)

        return !exists(id)
    }

    /**
     * check if [TrashEntity] exists by id
     *
     * @param id    uuid of [TrashEntity]
     * @return boolean depending on success
     * */
    private fun exists(id : UUID) : Boolean = trashRepository.findById(id).isPresent

    /**
     * return all items entities located in trash folder
     *
     * @return list of items in trash folder
     * */
    fun getAllItems() : List<BaseEntity> {
        val items = ArrayList<BaseEntity>()

        trashRepository.all.forEach {
            val entity : BaseEntity? = when (it.type) {
                EntityType.FOLDER -> folderRepository.findById(it.entityId).orElse(null)
                EntityType.FILE -> fileRepository.findById(it.entityId).orElse(null)
                else -> null
            }
            if (entity != null) items.add(entity)
        }
        return items
    }

    /**
     * move [BaseEntity] to trash folder, and add a record of its abstract trash representation = [TrashEntity]
     *
     * @param entity    entity extending from [BaseEntity]
     * @return boolean depending on result success
     * */
    fun moveToTrash(entity : T) : Boolean =
            // path from where entity was moved, used for restore option
            when (entity.type) {
                EntityType.FILE -> moveFileToTrash(entity as File)
                EntityType.FOLDER -> moveFolderToTrash(entity as Folder)
                else -> false
            }

	/**
	 * delete all items in trash
	 * @return boolean depending on result success
	 * */
	fun emptyTrash() : Boolean {
		// list of items in trash
		val trashItems = AppProperties.trashFolder.listFiles()
		// delete every trash item
		listOf(*trashItems!!).parallelStream().forEach {
            val trashEntity = trashRepository.findByEntityPath(it.path).orElse(null)

            if (trashEntity != null) {
                // deleting result
                val success = when {
                    it.isFile -> fileService.delete(it.path) == HttpStatus.OK
                    it.isDirectory -> folderService.delete(it.path) == HttpStatus.OK
                    else -> false
                }
                if (success) delete(trashEntity.id)
            }
        }
        return trashIsEmpty()
	}

    /**
     * check if trash folder is empty
     * */
    private fun trashIsEmpty() : Boolean {
        val trashItems = AppProperties.trashFolder.listFiles()

        trashItems?.forEach {
            if (it.exists()) return false
        } ?: return true
        return true
    }

    /**
     * restore [BaseEntity] from trash
     *
     * @param id    [TrashEntity] id
     * */
    fun restoreFromTrash(id : UUID) : Boolean {
        // abstract representation of entity from trash
        val trashEntity = trashRepository.findByEntityId(id).orElse(null)

        if (trashEntity != null) {
            val result : Boolean = when (trashEntity.type!!) {
                EntityType.FOLDER -> {
                    val folder = folderRepository.findById(id).orElse(null)
                    // restore folder
                    moveFolderBack(folder, trashEntity.restoreFolder)
                }
                EntityType.FILE -> {
                    val file = fileRepository.findById(id).orElse(null)
                    // restore file
                    moveFileBack(file, trashEntity.restoreFolder)
                }
            }
            // if restored with success, delete trash entity
            if (result) return delete(trashEntity.id)
        }
        return false
    }

    fun deleteFromTrash(id : UUID) : Boolean {
        val trashEntity : TrashEntity? = trashRepository.findByEntityId(id).orElse(null)

        if (trashEntity != null) {
            val result : Boolean = when (trashEntity.type!!) {
                EntityType.FOLDER -> {
                    val folder = folderRepository.findById(id).orElse(null)
                    // delete folder
                    if (folder != null)
                        folderService.delete(folder.path) == HttpStatus.OK
                    else false
                }
                EntityType.FILE -> {
                    val file = fileRepository.findById(id).orElse(null)
                    // delete file
                    if (file != null)
                        fileService.delete(file.path) == HttpStatus.OK
                    else false
                }
            }
            if (result) return delete(trashEntity.id)
        }
        return false
    }

    /**
     * move file to restore path from its trash entity representation
     *
     * @param file          [File] entity
     * @param restoreFolder path for restoring file
     * @return boolean depending on success
     * */
    private fun moveFileBack(file : File?, restoreFolder: String) : Boolean {
        if (file != null) {
            return fileService.move(file.path, restoreFolder) == HttpStatus.OK
        }
        return false
    }

    /**
     * move folder to restore path from its trash entity representation
     *
     * @param folder        [Folder] entity
     * @param restoreFolder path for restoring folder
     * @return boolean depending on success
     * */
    private fun moveFolderBack(folder : Folder?, restoreFolder: String) : Boolean {
        if (folder != null) {
            return folderService.move(folder.path, restoreFolder) == HttpStatus.OK
        }
        return false
    }

    /**
     * move [File] to trash, will create [TrashEntity] for representing it in trash folder
     *
     * @param file      [File] entity
     * @return boolean depending on success
     * */
    private fun moveFileToTrash(file : File) : Boolean {
        if (fileService.move(file.path, AppProperties.trashFolder.path) == HttpStatus.OK) {
            val updatedFile : File? = fileRepository.getByPath("${AppProperties.trashFolder.path}/${file.name}")

            val restoreFolder = java.io.File(file.path).parent

            if (updatedFile != null) {
                val trashEntity = save(TrashEntity(updatedFile, restoreFolder))

                return if (trashEntity != null) {
                    true
                } else {
                    try {
                        moveFileBackIfError(updatedFile.id, restoreFolder)
                    } catch (e : NoSuchElementException) {
                        e.printStackTrace()
                        LoggingService.saveLog(ExceptionTools.stackTraceToString(e), LogType.ERROR)
                    }
                    false
                }
            }
        }
        return false
    }

    /**
     * move [Folder] to trash, will create [TrashEntity] for representing it in trash folder
     *
     * @param folder    [Folder] entity
     * @return boolean depending on success
     * */
    private fun moveFolderToTrash(folder : Folder) : Boolean {
        if (folderService.move(folder.path, AppProperties.trashFolder.path) == HttpStatus.OK) {
            val updatedFolder : Folder? = folderRepository.getByPath("${AppProperties.trashFolder.path}/${folder.name}")

            val restoreFolder = java.io.File(folder.path).parent

            if (updatedFolder != null) {
                val trashEntity = save(TrashEntity(updatedFolder, restoreFolder))

                return if (trashEntity != null) {
                    true
                } else {
                    try {
                        moveFolderBackIfError(updatedFolder.id, restoreFolder)
                    } catch (e : NoSuchElementException) {
                        e.printStackTrace()
                        LoggingService.saveLog(ExceptionTools.stackTraceToString(e), LogType.ERROR)
                    }
                    false
                }
            }
        }
        return false
    }

    /**
     * add all files and folders from trash folder to database,
     * similar to [FolderRecordUtils.addAllFoldersToDatabase]
     *
     * @param files     list of elements inside trash folder
     * */
    fun addAllTrashEntities(files : List<java.io.File>) {
        files.parallelStream().forEach {
            val entity: BaseEntity? = when {
                it.isDirectory -> folderRepository.getByPath(it.absolutePath) ?: null
                it.isFile -> fileRepository.getByPath(it.absolutePath) ?: null
                else -> null
            }
            if (entity != null)
                try {
                    // save trash entity with restore point backup from trash entity saved earlier to database
                    save(TrashEntity(entity, BackupService.readBackup(it).restoreFolder))
                } catch(e : BackupReadException) {
                    // if file with backup was deleted or damaged, move file from trash to root of storage folder
                    restoreToDefaultPath(entity)
                }
        }
    }

    private fun restoreToDefaultPath(entity : BaseEntity) =
        when (entity.type!!) {
            EntityType.FILE -> moveFileBackIfError(entity.id, AppProperties.storageFolder.path)
            EntityType.FOLDER -> moveFileBackIfError(entity.id, AppProperties.storageFolder.path)
        }

    @Throws(NoSuchElementException::class)
    private fun moveFileBackIfError(fileId : UUID, restorePoint : String) {
        val file = fileRepository.findById(fileId).orElseThrow()

        fileService.move(file.path, restorePoint)
    }

    @Throws(NoSuchElementException::class)
    private fun moveFolderBackIfError(folderId : UUID, restorePoint : String) {
        val folder = folderRepository.findById(folderId).orElseThrow()

        fileService.move(folder.path, restorePoint)
    }

    /**
     * Service for storing [TrashEntity]'s restore path locally on a file.
     *
     * On every startup files and folders from trash folder are added to database,
     * restore path to [TrashEntity] is passed from file or folder, causing to set
     * restore path as path from trash folder, that's why this service was created
     * every creation or update of [TrashEntity] will save restore path to a file,
     * witch will be used on startup to set restore path for all files and folders
     * from trash folder
     * */
    private object BackupService {

        private val backupFolder = java.io.File("${AppProperties.root}/Backup")

        init {
            if (!backupFolder.exists()) {
                backupFolder.mkdirs()
            }
        }

        @Throws(Exception::class)
        fun writeBackup(trashEntity : TrashEntity) : Boolean {
            val file = createNecessaryEnvironment(trashEntity)

            if (file != null) {
                ObjectMapper(YAMLFactory()).apply { writeValue(file, trashEntity) }

                return if (file.exists() && file.length() > 0)
                    true
                else
                    throw BackupWriteException("Failed to save TrashEntity => ${trashEntity.id} backup file")
            }
            return false
        }

        @Throws(BackupReadException::class)
        fun readBackup(fsFile : java.io.File) : TrashEntity {
            val file = java.io.File("${backupFolder.path}/${fsFile.path}/${fsFile.name}.yml")

            if (file.exists()) {
                val mapper = ObjectMapper(YAMLFactory()).apply { findAndRegisterModules() }

                return mapper.readValue(file, TrashEntity::class.java)
            }
            throw BackupReadException("Failed to read TrashEntity backup, file does not exist")
        }

        private fun createNecessaryEnvironment(trashEntity : TrashEntity) : java.io.File? {
            val folder = java.io.File("${backupFolder.path}/${trashEntity.path}")

            val file = java.io.File("${folder.path}/${trashEntity.name}.yml")

            if (!folder.exists()) {
                try {
                    if (!folder.mkdirs()) throw IOException()
                } catch (e : IOException) {
                    e.printStackTrace()
                    LoggingService.saveLog(ExceptionTools.stackTraceToString(e), LogType.ERROR)
                    return null
                }
            }
            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e : IOException) {
                    e.printStackTrace()
                    LoggingService.saveLog(ExceptionTools.stackTraceToString(e), LogType.ERROR)
                    return null
                }
            }
            return file
        }
    }
}
