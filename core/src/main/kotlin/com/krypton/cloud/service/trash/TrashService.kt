@file:Suppress("RemoveRedundantQualifierName")

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
				return try {
					backupService.deleteBackup(entity)
					true
				} catch (e: Exception) {
					e.printStackTrace()
					LoggingService.saveLog(
						ExceptionTools.stackTraceToString(e),
						LogType.ERROR,
						LogFolder.ROOT.type)
					false
				}
			}
		}
		return false
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
					it.isFile -> fileService.delete(it.path) == HttpStatus.OK
					it.isDirectory -> folderService.delete(it.path) == HttpStatus.OK
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
	fun trashIsEmpty() : Boolean {
		val trashItems = AppProperties.trashFolder.listFiles()

		trashItems?.forEach {
			if (it.exists()) return false
		} ?: return true
		return true
	}

	/**
	 * move [BaseEntity] to trash folder, and add a record of its abstract
	 * trash representation = [TrashEntity]
	 *
	 * @param entity    entity extending from [BaseEntity]
	 * @return boolean depending on result success
	 * */
	fun moveToTrash(entity : BaseEntity) : Boolean {
		val result = when (entity.type) {
			EntityType.FILE -> moveFileToTrash(entity as File)
			EntityType.FOLDER -> moveFolderToTrash(entity as Folder)
			else -> false
		}
		if (result) {
			return try {
				backupService.writeBackup(TrashEntity(entity, entity.path))
			} catch (e : BackupWriteException) {
				e.printStackTrace()
				false
			}
		}
		return false
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
						folderService.delete(folder.path) == HttpStatus.OK
					else false
				}
				EntityType.FILE -> {
					val file = fileRecordService.getById(id)
					// delete file
					if (file != null)
						fileService.delete(file.path) == HttpStatus.OK
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
	 * move [File] to trash, will create [TrashEntity] for representing it in trash folder
	 *
	 * @param file      [File] entity
	 * @return boolean depending on success
	 * */
	private fun moveFileToTrash(file : File) : Boolean {
		if (fileService.move(file.path, AppProperties.trashFolder.path) == HttpStatus.OK) {
			val updatedFile : File? = fileRecordService.getByPath("${AppProperties.trashFolder.path}/${file.name}")

			val restorePoint = java.io.File(file.path).parent

			if (updatedFile != null) {
				val trashEntity = trashRecordService.save(TrashEntity(updatedFile, restorePoint))

				return if (trashEntity != null) {
					true
				} else {
					try {
						// if trash entity was not saved, move file back from where it was deleted
						trashErrorHandler.moveFileBackIfError(updatedFile.id, restorePoint)
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
		val restorePoint = folderRecordService.getById(folder.parentId)

		if (folderService.move(folder.path, AppProperties.trashFolder.path) == HttpStatus.OK) {
			val updatedFolder : Folder? = folderRecordService.getByPath("${AppProperties.trashFolder.path}/${folder.name}")

			if (updatedFolder != null && restorePoint != null) {
				val trashEntity = trashRecordService.save(TrashEntity(updatedFolder, restorePoint.path))

				return if (trashEntity != null) {
					true
				} else {
					try {
						// if trash entity was not saved, move folder back from where it was deleted
						trashErrorHandler.moveFolderBackIfError(updatedFolder.id, restorePoint.path)
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
				it.isDirectory -> folderRecordService.getByPath(it.absolutePath) ?: null
				it.isFile -> fileRecordService.getByPath(it.absolutePath) ?: null
				else -> null
			}
			if (entity != null)
				try {
					// save trash entity with restore point backup from trash entity saved earlier to backup file
					trashRecordService.save(TrashEntity(entity, backupService.readBackup(it).restoreFolder))
				} catch (e : BackupReadException) {
					// if file with backup was deleted or damaged, move file from trash to root of storage folder
					trashErrorHandler.restoreToDefaultPath(entity)
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
					throw BackupWriteException("Failed to save TrashEntity => ${trashEntity.id} backup file")
			}
			return false
		}

		/**
		 * read backup file for [TrashEntity], and create entity with data from file
		 *
		 * @return [TrashEntity] with data from file
		 * @throws 	[BackupReadException] if file with backup does not exist
		 * 			[IOException] if failed to read file and parse it as [TrashEntity]
		 * */
		@Throws(BackupReadException::class, IOException::class)
		fun readBackup(fsFile : java.io.File) : TrashEntity {
			// backup file
			val file = java.io.File("${backupFolder.path}/${fsFile.path}/${fsFile.name}.yml")

			if (file.exists()) {
				val mapper = ObjectMapper(YAMLFactory()).apply { findAndRegisterModules() }

				return mapper.readValue(file, TrashEntity::class.java)
			}
			throw BackupReadException("Failed to read TrashEntity backup, file does not exist")
		}

		/**
		 * delete backup file of [TrashEntity], used when entity is deleted from
		 * database and backup is not longer needed
		 *
		 * @param trashEntity		entity witch backup will be deleted
		 * */
		@Throws(BackupDontExistException::class, IOException::class)
		fun deleteBackup(trashEntity : TrashEntity) {
			val file = java.io.File("${backupFolder.path}/${trashEntity.path}/${trashEntity.name}.yml")

			if (file.exists())
				if (!file.delete()) throw IOException()
			else throw BackupDontExistException()
		}

		/**
		 * create folder and file for writing backup
		 *
		 * @param trashEntity 		[TrashEntity] that will be written to file
		 * @return created file for backup
		 * */
		private fun createNecessaryEnvironment(trashEntity : TrashEntity) : java.io.File? {
			// folder where backup file will be located
			val folder = java.io.File("${backupFolder.path}/${trashEntity.path}")
			// file for backup
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
					if (!file.createNewFile()) throw  IOException()
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