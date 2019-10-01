package com.krypton.cloud.service.trash

import com.krypton.cloud.model.BaseEntity
import com.krypton.cloud.model.File
import com.krypton.cloud.model.Folder
import com.krypton.cloud.model.TrashEntity
import com.krypton.cloud.repository.FileRepository
import com.krypton.cloud.repository.FolderRepository
import com.krypton.cloud.service.file.FileService
import com.krypton.cloud.service.folder.FolderService
import common.config.AppProperties
import common.model.EntityType
import lombok.AllArgsConstructor
import org.springframework.stereotype.Service
import java.util.*

@AllArgsConstructor
@Service
class TrashErrorHandler(
	private val fileRepository: FileRepository,
	private val folderRepository: FolderRepository,
	private val fileService: FileService,
	private val folderService: FolderService
) {

	/**
	 * if something went wrong with [TrashEntity] record, [BaseEntity] from inside it
	 * will be restored to root of storage folder. Error may be caused with backup file
	 * of [TrashEntity] was deleted or corrupted and [BaseEntity] restore point can't
	 * be restored to [TrashEntity], so we just move entity from trash back to storage folder
	 *
	 * @param entity    [BaseEntity] that needs to be restored to storage folder
	 * */
	internal fun restoreToDefaultPath(entity : BaseEntity) =
		when (entity.type!!) {
			EntityType.FILE -> moveFileBackIfError(entity.id, AppProperties.storageFolder.path)
			EntityType.FOLDER -> moveFolderBackIfError(entity.id, AppProperties.storageFolder.path)
		}

	/**
	 * used by [restoreToDefaultPath], will move file back to root of storage folder
	 *
	 * @param fileId            id of [File] entity
	 * @param restorePoint      path where to move file
	 * @throws [NoSuchElementException] if error occurred on restoring file
	 * */
	@Throws(NoSuchElementException::class)
	internal fun moveFileBackIfError(fileId : UUID, restorePoint : String) {
		val file = fileRepository.findById(fileId).orElseThrow()

		fileService.move(file.path, restorePoint)
	}

	/**
	 * used by [restoreToDefaultPath], will move folder back to root of storage folder
	 *
	 * @param folderId          of [Folder] entity
	 * @param restorePoint      path where to move folder
	 * @throws [NoSuchElementException] if error occurred on restoring folder
	 * */
	@Throws(NoSuchElementException::class)
	internal fun moveFolderBackIfError(folderId : UUID, restorePoint : String) {
		val folder = folderRepository.findById(folderId).orElseThrow()

		folderService.move(folder.path, restorePoint)
	}
}