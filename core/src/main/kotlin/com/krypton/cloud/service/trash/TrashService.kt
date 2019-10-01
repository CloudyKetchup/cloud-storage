@file:Suppress("RemoveRedundantQualifierName")

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
import common.exception.trash.backup.BackupDontExistException
import common.exception.trash.backup.BackupReadException
import common.exception.trash.backup.BackupWriteException
import common.model.LogType
import util.log.*
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * Service handling logic related to trash folder
 * */
@Service
@AllArgsConstructor
class TrashService(
	private val trashRepository : TrashRepository,
	private val fileService     : FileServiceImpl,
	private val fileRepository  : FileRepository,
	private val folderRepository: FolderRepository,
	private val folderService   : FolderServiceImpl,
	private val trashErrorHandler: TrashErrorHandler
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
				LoggingService.saveLog(e.stackTraceToString(), LogType.ERROR, LogFolder.ROOT.toString())
				return null
			}
		} else null
	}

	@Deprecated(message = "using path for finding entity, use delete(id : UUID) instead of this one")
	override fun delete(path : String) : Boolean {
		trashRepository.deleteByPath(path)

		return !exists(path)
	}

	override fun exists(path : String) : Boolean = trashRepository.findByEntityPath(path).isPresent

	/**
	 * delete [TrashEntity] by id
	 *
	 * @param id    uuid of [TrashEntity]
	 * @return boolean depending on success
	 * */
	private fun delete(id : UUID) : Boolean {
		val entity = trashRepository.findById(id).orElse(null)

		if (entity != null) {
			trashRepository.deleteById(id)

			if (!exists(id)) {
				try {
					BackupService.deleteBackup(entity)
				} catch (e : Exception) {
					e.printStackTrace()
					LoggingService.saveLog(ExceptionTools.stackTraceToString(e), LogType.ERROR, LogFolder.ROOT.toString())
				}
				return true
			}
		}
		return false
	}

	/**
	 * check if [TrashEntity] exists by id
	 *
	 * @param id    uuid of [TrashEntity]
	 * @return boolean depending on success
	 * */
	private fun exists(id : UUID) : Boolean = trashRepository.findById(id).isPresent

    fun getInfo() : HashMap<String, String> {
		val items = getAllItems()

		return HashMap<String, String>().apply {
			put("foldersCount", filterFolders(items).size.toString())
			put("filesCount", filterFiles(items).size.toString())
			put("totalItems", items.size.toString())
		}
	}

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
	fun moveToTrash(entity : BaseEntity) : Boolean {
		return when (entity.type) {
			EntityType.FILE -> moveFileToTrash(entity as File)
			EntityType.FOLDER -> moveFolderToTrash(entity as Folder)
			else -> false
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

	private fun filterFolders(folders : List<BaseEntity>) = folders.filter { it.type == EntityType.FOLDER }.map { it as Folder }

	private fun filterFiles(files : List<BaseEntity>) = files.filter { it.type == EntityType.FILE }.map { it as File }

	/**
	 * check if trash folder is empty
	 *
	 * @return true if is empty, otherwise false
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
	 * @return boolean depending on result
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

	/**
	 * delete [BaseEntity] from trash
	 *
	 * @param id    [TrashEntity] id
	 * @return boolean depending on result
	 * */
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
			val updatedFile : File? = fileRepository.getByPath("${AppProperties.trashFolder.path}/${file.name}")

			val restorePoint = java.io.File(file.path).parent

			if (updatedFile != null) {
				val trashEntity = save(TrashEntity(updatedFile, restorePoint))

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
		val restorePoint = folderRepository.findById(folder.parentId).orElse(null)

		if (folderService.move(folder.path, AppProperties.trashFolder.path) == HttpStatus.OK) {
			val updatedFolder : Folder? = folderRepository.getByPath("${AppProperties.trashFolder.path}/${folder.name}")

			if (updatedFolder != null) {
				val trashEntity = save(TrashEntity(updatedFolder, restorePoint.path))

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
				it.isDirectory -> folderRepository.getByPath(it.absolutePath) ?: null
				it.isFile -> fileRepository.getByPath(it.absolutePath) ?: null
				else -> null
			}
			if (entity != null)
				try {
					// save trash entity with restore point backup from trash entity saved earlier to database
					save(TrashEntity(entity, BackupService.readBackup(it).restoreFolder))
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
	private object BackupService {

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

