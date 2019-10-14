package com.krypton.cloud.controller

import com.krypton.databaselayer.model.BaseEntity
import com.krypton.databaselayer.model.File
import com.krypton.databaselayer.model.Folder
import com.krypton.databaselayer.service.IOEntityRecordService
import com.krypton.cloud.service.trash.TrashService
import com.krypton.databaselayer.service.trash.TrashRecordService
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.collections.HashMap

@CrossOrigin
@RestController
@RequestMapping("/trash")
@AllArgsConstructor
class TrashController(
	private val trashService: TrashService,
	private val trashRecordService: TrashRecordService,
	private val folderRecordService: IOEntityRecordService<Folder>,
	private val fileRecordService: IOEntityRecordService<File>
) {

	@GetMapping("/items")
	fun getTrashItems() : List<BaseEntity> = trashRecordService.getAllItems()

	@GetMapping("/info")
	fun getInfo() : HashMap<String, String> = trashRecordService.getInfo()

	@DeleteMapping("/empty-trash")
	fun emptyTrash() : HttpStatus {
		return if (trashService.emptyTrash())
			HttpStatus.OK
		else HttpStatus.INTERNAL_SERVER_ERROR
	}

	@DeleteMapping("/delete/{id}")
	fun deleteFromTrash(@PathVariable("id") id : String) : HttpStatus {
		return if (trashService.deleteFromTrash(UUID.fromString(id))) {
			HttpStatus.OK
		} else HttpStatus.INTERNAL_SERVER_ERROR
	}

	@PostMapping("/restore-from-trash")
	fun restoreFromTrash(@RequestBody request : HashMap<String, String>) : HttpStatus {
		return if (trashService.restoreFromTrash(UUID.fromString(request["id"]))) {
			HttpStatus.OK
		} else {
			HttpStatus.INTERNAL_SERVER_ERROR
		}
	}

	@PostMapping("/folder/move-to-trash")
	fun moveFolderToTrash(@RequestBody request : HashMap<String, String>) : HttpStatus {
		val folder = folderRecordService.getById(UUID.fromString(request["id"]))

		return if (trashService.moveToTrash(folder)) {
			HttpStatus.OK
		} else HttpStatus.INTERNAL_SERVER_ERROR
	}

	@PostMapping("/file/move-to-trash")
	fun moveFileToTrash(@RequestBody request : java.util.HashMap<String, String>) : HttpStatus {
		val file = fileRecordService.getById(UUID.fromString(request["id"]))

		if (file != null) {
			return if (trashService.moveToTrash(file))
				HttpStatus.OK
			else HttpStatus.INTERNAL_SERVER_ERROR
		}
		return HttpStatus.INTERNAL_SERVER_ERROR
	}
}