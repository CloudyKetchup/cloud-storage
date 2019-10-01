package com.krypton.cloud.controller

import com.krypton.cloud.model.BaseEntity
import com.krypton.cloud.model.File
import com.krypton.cloud.model.Folder
import com.krypton.cloud.service.record.IOEntityRecordService
import com.krypton.cloud.service.trash.TrashService
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.collections.HashMap

@RestController
@RequestMapping("/trash")
@AllArgsConstructor
class TrashController(
	private val trashService: TrashService,
	private val folderRecordService: IOEntityRecordService<Folder>,
	private val fileRecordService: IOEntityRecordService<File>
) {

	@GetMapping("/items")
	fun getTrashItems() : List<BaseEntity> = trashService.getAllItems()

	@GetMapping("/info")
	fun getInfo() : HashMap<String, String> = trashService.getInfo()

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

		return if (trashService.moveToTrash(file)) {
			HttpStatus.OK
		} else HttpStatus.INTERNAL_SERVER_ERROR
	}
}