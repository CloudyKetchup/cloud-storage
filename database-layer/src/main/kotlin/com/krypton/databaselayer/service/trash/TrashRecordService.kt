package com.krypton.databaselayer.service.trash

import com.krypton.databaselayer.model.BaseEntity
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

	override fun findAll() : List<TrashEntity> = trashRepository.findAll();

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

	fun getAll() : List<TrashEntity> = trashRepository.all

	/**
	 * return all items entities located in trash folder
	 *
	 * @return [List] of [BaseEntity]
	 * */
	fun getAllItems() : List<BaseEntity> {
		val items = ArrayList<BaseEntity>()

		getAll().forEach {
			val entity : BaseEntity? = when (it.type) {
				EntityType.FOLDER -> folderRepository.findById(it.entityId).orElse(null)
				EntityType.FILE -> fileRepository.findById(it.entityId).orElse(null)
				else -> null
			}
			if (entity != null) items.add(entity)
		}
		return items
	}
}
