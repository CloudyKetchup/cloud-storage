package com.krypton.cloud.service.trash

import com.krypton.databaselayer.model.*
import com.krypton.cloud.service.file.FileServiceImpl
import com.krypton.cloud.service.folder.FolderServiceImpl
import com.krypton.databaselayer.service.file.FileRecordServiceImpl
import com.krypton.databaselayer.service.folder.FolderRecordServiceImpl
import common.config.AppProperties
import common.model.EntityType
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

import com.krypton.databaselayer.service.trash.TrashRecordService
import common.exception.ExceptionTools
import common.model.LogType
import util.log.*
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

	/**
	 * delete all items in trash
	 *
	 * @return boolean depending on result success
	 * */
	fun emptyTrash() : Boolean {
		val trashEntities = trashRecordService.getAll()

		trashEntities.parallelStream().forEach { deleteFromTrash(it.entityId) }

		return trashIsEmpty()
	}

	fun restoreAll() : Boolean {
		val trashEntities = trashRecordService.getAll()

		trashEntities.parallelStream().forEach { restoreFromTrash(it.entityId) }

		return trashIsEmpty()
	}

	/**
	 * check if trash folder is empty
	 *
	 * @return true if is empty, otherwise false
	 * */
	fun trashIsEmpty() : Boolean = trashRecordService.getAllItems().isEmpty()

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
		return updatedEntity != null
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
		val trashEntity = trashRecordService.getByEntityId(id)

		return if (trashEntity != null) {
			val result = when (trashEntity.type!!) {
				EntityType.FOLDER -> {
					val folder = folderRecordService.getById(id)

					moveFolderBack(folder, trashEntity.restoreFolder)
				}
				EntityType.FILE -> {
					val file = fileRecordService.getById(id)

					moveFileBack(file, trashEntity.restoreFolder)
				}
				else -> false
			}
			return if (result)
				trashRecordService.delete(trashEntity.id)
			else false
		} else false
	}

	/**
	 * delete [BaseEntity] from trash
	 *
	 * @param entityId	[BaseEntity] entity id
	 * @return boolean depending on result
	 * */
	fun deleteFromTrash(entityId : UUID) : Boolean {
		val trashEntity : TrashEntity? = trashRecordService.getByEntityId(entityId)

		if (trashEntity != null) {
			return when (trashEntity.type!!) {
				EntityType.FOLDER -> {
					val folder = folderRecordService.getById(entityId)
					// delete folder
					if (folder != null)
						folderService.delete(folder.id) == HttpStatus.OK
					else false
				}
				EntityType.FILE -> {
					val file = fileRecordService.getById(entityId)
					// delete file
					if (file != null)
						fileService.delete(file.id) == HttpStatus.OK
					else false
				}
			}
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
}
