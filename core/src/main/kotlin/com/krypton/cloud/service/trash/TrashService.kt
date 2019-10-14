package com.krypton.cloud.service.trash

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.krypton.databaselayer.model.*
import com.krypton.cloud.service.file.FileServiceImpl
import com.krypton.cloud.service.folder.FolderServiceImpl
import com.krypton.databaselayer.service.file.FileRecordServiceImpl
import com.krypton.databaselayer.service.folder.FolderRecordServiceImpl
import com.krypton.databaselayer.service.folder.FolderRecordUtils
import common.config.AppProperties
import common.model.EntityType
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

import com.krypton.databaselayer.service.trash.TrashRecordService
import common.exception.ExceptionTools
import common.exception.trash.backup.BackupDontExistException
import common.exception.trash.backup.BackupReadException
import common.exception.trash.backup.BackupWriteException
import common.model.LogType
import util.log.*
import java.io.IOException
import java.util.*

/**
 * Service handling logic related to trash folder
 * */
@Service
@AllArgsConstructor
class TrashService(
	private val trashRecordService	: TrashRecordService,
	private val fileService     	: FileServiceImpl,
	private val fileRecordService	: FileRecordServiceImpl,
	private val folderService   	: FolderServiceImpl,
	private val folderRecordService : FolderRecordServiceImpl,
	private val trashErrorHandler	: TrashErrorHandler
) {

	private val backupService = BackupService()

	/**
	 * Delete [TrashEntity] record from database, then delete its backup
	 *
	 * @param entity 	[TrashEntity] target
	 * @return boolean depending on result
	 * */
	private fun deleteRecordCompletely(entity: TrashEntity?) : Boolean {
		if (entity != null) {
			trashRecordService.delete(entity.id)

			if (!trashRecordService.exists(entity.id)) {
				try {
					backupService.deleteBackup(entity)
				} catch (e: Exception) {
					restoreToDefaultPath(entity)
					LoggingService.saveLog(ExceptionTools.stackTraceToString(e), LogType.ERROR)
				}
				return true
			}
		}
		return false
	}

	/**
	 * take a [TrashEntity] and restore [BaseEntity] from it to default location(Storage root)
	 *
	 * @param trashEntity	[TrashEntity] target
	 * */
	private fun restoreToDefaultPath(trashEntity : TrashEntity) {
		val entity : BaseEntity? = when(trashEntity.type) {
			EntityType.FILE -> fileRecordService.getById(trashEntity.entityId)
			EntityType.FOLDER -> folderRecordService.getById(trashEntity.entityId)
			else -> null
		}
		if (entity != null) {
			trashErrorHandler.restoreToDefaultPath(entity)
		}
	}

	/**
	 * delete all items in trash
	 *
	 * @return boolean depending on result success
	 * */
	fun emptyTrash() : Boolean {
		// list of items in trash
		val trashItems = AppProperties.trashFolder.listFiles()
		// delete every trash item
		listOf(*trashItems!!).parallelStream().forEach {
			val trashEntity = trashRecordService.getByPath(it.path)

			if (trashEntity != null) {
				// deleting result
				val success = when {
					it.isFile -> fileService.delete(trashEntity.entityId) == HttpStatus.OK
					it.isDirectory -> folderService.delete(trashEntity.entityId) == HttpStatus.OK
					else -> false
				}
				if (success) deleteRecordCompletely(trashEntity)
			}
		}
		return trashIsEmpty()
	}

	/**
	 * check if trash folder is empty
	 *
	 * @return true if is empty, otherwise false
	 * */
	fun trashIsEmpty() : Boolean = AppProperties.trashFolder.listFiles() == null

	/**
	 * move [BaseEntity] to trash folder, and add a record of its abstract
	 * trash representation = [TrashEntity]
	 *
	 * @param entity    entity extending from [BaseEntity]
	 * @return boolean depending on result success
	 * */
	fun moveToTrash(entity : BaseEntity) : Boolean {
		val updatedEntity : BaseEntity? = when (entity.type) {
			EntityType.FILE -> moveFileToTrash(entity as File)
			EntityType.FOLDER -> moveFolderToTrash(entity as Folder)
			else -> null
		}
		if (updatedEntity != null) {
			return try {
				val restorePoint = java.io.File(entity.path).parent

				backupService.writeBackup(TrashEntity(updatedEntity, restorePoint))
			} catch (e : BackupWriteException) {
				e.printStackTrace()
				false
			}
		}
		return false
	}

	/**
	 * move [File] to trash, will create [TrashEntity] for representing it in trash folder
	 *
	 * @param file      [File] entity
	 * @return [File] moved to trash, or null if not succeed
	 * */
	private fun moveFileToTrash(file : File) : File? {
		if (fileService.move(file.path, AppProperties.trashFolder.path) == HttpStatus.OK) {
			val updatedFile : File? = fileRecordService.getByPath("${AppProperties.trashFolder.path}/${file.name}")

			val restorePoint = java.io.File(file.path).parent

			if (updatedFile != null) {
				val trashEntity = trashRecordService.save(TrashEntity(updatedFile, restorePoint))

				return if (trashEntity != null) {
					updatedFile
				} else {
					try {
						// if trash entity was not saved, move file back from where it was deleted
						trashErrorHandler.moveFileBackIfError(updatedFile.id, restorePoint)
					} catch (e : NoSuchElementException) {
						e.printStackTrace()
						LoggingService.saveLog(ExceptionTools.stackTraceToString(e), LogType.ERROR)
					}
					null
				}
			}
		}
		return null
	}

	/**
	 * move [Folder] to trash, will create [TrashEntity] for representing it in trash folder
	 *
	 * @param folder    [Folder] entity
	 * @return [Folder] moved to trash, or null if not succeed
	 * */
	private fun moveFolderToTrash(folder : Folder) : Folder? {
		if (folderService.move(folder.path, AppProperties.trashFolder.path) == HttpStatus.OK) {
			val updatedFolder : Folder? = folderRecordService.getByPath("${AppProperties.trashFolder.path}/${folder.name}")

			val restorePoint = folderRecordService.getById(folder.parentId)

			if (updatedFolder != null && restorePoint != null) {
				val trashEntity = trashRecordService.save(TrashEntity(updatedFolder, restorePoint.path))

				return if (trashEntity != null) {
					updatedFolder
				} else {
					try {
						// if trash entity was not saved, move folder back from where it was deleted
						trashErrorHandler.moveFolderBackIfError(updatedFolder.id, restorePoint.path)
					} catch (e : NoSuchElementException) {
						e.printStackTrace()
						LoggingService.saveLog(ExceptionTools.stackTraceToString(e), LogType.ERROR)
					}
					null
				}
			}
		}
		return null
	}

	/**
	 * restore [BaseEntity] from trash
	 *
	 * @param id    [TrashEntity] id
	 * @return boolean depending on result
	 * */
	fun restoreFromTrash(id : UUID) : Boolean {
		// abstract representation of entity from trash
		val trashEntity = trashRecordService.getByEntityId(id)

		if (trashEntity != null) {
			val result : Boolean = when (trashEntity.type!!) {
				EntityType.FOLDER -> {
					val folder = folderRecordService.getById(id)
					// restore folder
					moveFolderBack(folder, trashEntity.restoreFolder)
				}
				EntityType.FILE -> {
					val file = fileRecordService.getById(id)
					// restore file
					moveFileBack(file, trashEntity.restoreFolder)
				}
			}
			// if restored with success, delete trash entity
			if (result) return deleteRecordCompletely(trashEntity)
		}
		return false
	}

	/**
	 * delete [BaseEntity] from trash
	 *
	 * @param id    [TrashEntity] id
	 * @return boolean depending on result
	 * */
	fun deleteFromTrash(id : UUID) : Boolean {
		val trashEntity : TrashEntity? = trashRecordService.getByEntityId(id)

		if (trashEntity != null) {
			val result : Boolean = when (trashEntity.type!!) {
				EntityType.FOLDER -> {
					val folder = folderRecordService.getById(id)
					// delete folder
					if (folder != null)
						folderService.delete(folder.id) == HttpStatus.OK
					else false
				}
				EntityType.FILE -> {
					val file = fileRecordService.getById(id)
					// delete file
					if (file != null)
						fileService.delete(file.id) == HttpStatus.OK
					else false
				}
			}
			if (result) return deleteRecordCompletely(trashEntity)
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
		return if (file != null)
			fileService.move(file.path, restoreFolder) == HttpStatus.OK
		else false
	}

	/**
	 * move folder to restore path from its trash entity representation
	 *
	 * @param folder        [Folder] entity
	 * @param restoreFolder path for restoring folder
	 * @return boolean depending on success
	 * */
	private fun moveFolderBack(folder : Folder?, restoreFolder: String) : Boolean {
		return if (folder != null)
			folderService.move(folder.path, restoreFolder) == HttpStatus.OK
		else false
	}

	/**
	 * add all files and folders from trash folder to database,
	 * similar to [FolderRecordUtils.addAllFoldersToDatabase], but uses data from backup files
	 * */
	fun restoreItems() {
		val files = java.io.File("${AppProperties.root}/Trash").listFiles()
		if (files != null) {
			val backupFiles = listOf(*files)

			backupFiles.parallelStream().forEach {
				val entity: BaseEntity? = when {
					it.isDirectory -> folderRecordService.getByPath(it.path) ?: null
					it.isFile -> fileRecordService.getByPath(it.path) ?: null
					else -> null
				}
				if (entity != null)
					try {
						val backup = backupService.readBackup(it)
						// save trash entity with restore point backup from trash entity saved earlier to backup file
						trashRecordService.save(TrashEntity(entity, backup.restoreFolder))
					} catch (e : BackupReadException) {
						// if file with backup was deleted or damaged, move file from trash to root of storage folder
						trashErrorHandler.restoreToDefaultPath(entity)
					}
			}
		}
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
	private class BackupService {

		private val backupFolder = java.io.File("${AppProperties.root}/Backup")

		init {
			if (!backupFolder.exists()) {
				backupFolder.mkdirs()
			}
		}

		/**
		 * save [TrashEntity] to file in YAML format
		 *
		 * @param trashEntity		entity for save
		 * @return boolean depending on result
		 * @throws 	[BackupWriteException] if failed to save backup
		 * 			[IOException] if failed to write backup file
		 * */
		@Throws(BackupWriteException::class, IOException::class)
		fun writeBackup(trashEntity : TrashEntity) : Boolean {
			val file = createNecessaryEnvironment(trashEntity)

			if (file != null) {
				ObjectMapper(YAMLFactory()).apply { writeValue(file, trashEntity) }

				return if (file.exists() && file.length() > 0)
					true
				else
					throw BackupWriteException("Failed to save { ${trashEntity.id} } backup file")
			}
			return false
		}

		/**
		 * read backup file for [TrashEntity], and create entity with data from file
		 *
		 * @param fsFile	file from trash folder that need to be restored with backup
		 * @return [TrashEntity] with data from file
		 * @throws 	[BackupReadException] if file with backup does not exist
		 * 			[IOException] if failed to read file and parse it as [TrashEntity]
		 * */
		@Throws(BackupReadException::class, IOException::class)
		fun readBackup(fsFile : java.io.File) : TrashEntity {
			// backup file
			val backup = java.io.File("${backupFolder.path}/${fsFile.path}.yml")

			if (backup.exists()) {
				val mapper = ObjectMapper(YAMLFactory()).apply { findAndRegisterModules() }

				return mapper.readValue(backup, TrashEntity::class.java)
			}
			throw BackupReadException("Failed to read TrashEntity backup, file does not exist")
		}

		/**
		 * delete backup file of [TrashEntity], used when entity is deleted from
		 * database and backup is not longer needed
		 *
		 * @param trashEntity		entity witch backup will be deleted
		 * @throws 	[BackupDontExistException] if backup file don't exist
		 * 			[IOException] if file was not deleted
		 * */
		@Throws(BackupDontExistException::class, IOException::class)
		fun deleteBackup(trashEntity : TrashEntity) {
			val file = java.io.File("${backupFolder.path}/${trashEntity.path}.yml")

			if (file.exists()) {
				if (!file.delete()) throw IOException()
			} else throw BackupDontExistException()
		}

		/**
		 * create folder and file for writing backup
		 *
		 * @param trashEntity 		[TrashEntity] that will be written to file
		 * @return created file for backup
		 * */
		private fun createNecessaryEnvironment(trashEntity : TrashEntity) : java.io.File? {
			// file for backup
			val file = java.io.File("${backupFolder.path}/${trashEntity.path}.yml")

			val folder = file.parentFile

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
					if (!file.createNewFile()) throw IOException()
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