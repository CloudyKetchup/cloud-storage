package com.krypton.databaselayer.service.trash

import com.krypton.databaselayer.model.BaseEntity
import com.krypton.databaselayer.model.File
import com.krypton.databaselayer.model.Folder
import com.krypton.databaselayer.model.TrashEntity
import com.krypton.databaselayer.repository.FileRepository
import com.krypton.databaselayer.repository.FolderRepository
import com.krypton.databaselayer.repository.TrashRepository
import com.krypton.databaselayer.service.IOEntityRecordService
import common.model.EntityType
import org.springframework.stereotype.Service
import java.util.*

@Service
class TrashRecordService(
	private val trashRepository	: TrashRepository,
	private val folderRepository: FolderRepository,
	private val fileRepository	: FileRepository
) : IOEntityRecordService<TrashEntity> {

	override fun getById(id : UUID) : TrashEntity? = trashRepository.findById(id).orElse(null)

	override fun getByPath(path : String) : TrashEntity? = trashRepository.findByPath(path).orElse(null)

	override fun save(entity : TrashEntity) : TrashEntity? = trashRepository.save(entity) ?: null

	override fun delete(path : String) : Boolean {
		val entity = getByPath(path)

		if (entity != null) trashRepository.delete(entity)

		return !exists(path)
	}

	override fun exists(path : String) : Boolean = trashRepository.findByPath(path).isPresent

	/**
	 * delete [TrashEntity] by id
	 *
	 * @param id    uuid of [TrashEntity]
	 * @return boolean depending on success
	 * */
	override fun delete(id : UUID) : Boolean {
		trashRepository.deleteById(id)

		return !exists(id)
	}

	/**
	 * check if [TrashEntity] exists by id
	 *
	 * @param id    uuid of [TrashEntity]
	 * @return boolean depending on success
	 * */
	override fun exists(id : UUID) : Boolean = trashRepository.findById(id).isPresent

	fun getByEntityId(id : UUID) : TrashEntity? = trashRepository.findByEntityId(id).orElse(null)

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

	private fun filterFolders(folders : List<BaseEntity>) = folders.filter { it.type == EntityType.FOLDER }.map { it as Folder }

	private fun filterFiles(files : List<BaseEntity>) = files.filter { it.type == EntityType.FILE }.map { it as File }
}