package com.krypton.cloud.controller

import common.config.AppProperties
import com.krypton.databaselayer.model.Folder
import com.krypton.cloud.service.folder.FolderServiceImpl
import com.krypton.databaselayer.service.folder.FolderRecordServiceImpl
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/folder")
class FolderController(
	private val folderService : FolderServiceImpl,
	private val folderRecordService : FolderRecordServiceImpl
) {

	/**
	 * get total and free disk space in GB format
	 *
	 * @return hash map containing disk space info
	 */
	@GetMapping("/root/memory")
	fun rootMemory() : HashMap<String, String> = HashMap<String, String>().apply {
		put("total", (File("/").totalSpace / 1024 / 1024 / 1024).toString())
		put("free", (File("/").freeSpace / 1024 / 1024 / 1024).toString())
	}

	/**
	 * get root folder id in [UUID] format
	 *
	 * @return  [UUID] in string format
	 * */
	 @GetMapping("/root/id")
	 fun rootId() : String = folderRecordService.getByPath(AppProperties.storageFolder.absolutePath).id.toString()

	@GetMapping("/root/data")
	fun getRootData() : HashMap<String, Any> {
		val rootFolder = folderRecordService.getById(UUID.fromString(rootId()))

		return HashMap<String, Any>().apply {
			put("id", rootFolder.id)
			put("memory", rootMemory())
			put("files", rootFolder.files)
			put("folders", rootFolder.folders)
		}
	}

	@GetMapping("/{id}/predecessors")
	fun getPredecessors(@PathVariable id : String) : List<Folder> {
		val predecessors = ArrayList<Folder>()

		val folder = folderRecordService.getById(UUID.fromString(id))

		folderRecordService.getPredecessors(folder, predecessors)

		return predecessors.reversed()
	}
	 /**
	  * get [Folder] entity data
	  *
	  * @param id    folder id
	  * @return [Folder]
	  */
	@GetMapping("/{id}/data")
	fun getFolderData(@PathVariable id : String) : Folder = folderRecordService.getById(UUID.fromString(id))

	/**
	 * get list of [Folder]'s inside a [Folder]
	 *
	 * @param id    parent folder id
	 * @return [Folder]'s list
	 */
	@GetMapping("/{id}/folders")
	fun getFolderFolders(@PathVariable id : String) : Flux<Folder> = folderRecordService.getFolderFolders(UUID.fromString(id))

	/**
	 * get list of [com.krypton.cloud.model.File]'s inside a [Folder]
	 *
	 * @param id    parent folder id
	 * @return [com.krypton.cloud.model.File]'s list
	 */
	@GetMapping("/{id}/files")
	fun getFolderFiles(@PathVariable id : String) : Flux<com.krypton.databaselayer.model.File> = folderRecordService.getFolderFiles(UUID.fromString(id))

	/**
	 * get information about inside content of folder
	 *
	 * @param id    folder id
	 * @return folder content information like inside files and folders number
	 */
	@GetMapping("/{id}/content_info")
	fun contentInfo(@PathVariable id : String) : HashMap<String, Int> = folderService.getItemsCount(UUID.fromString(id))

	/**
	 * create new folder to specified path
	 *
	 * @param request    new folder data
	 * @return http status
	 */
	@PostMapping("/create")
	fun createFolder(@RequestBody request : HashMap<String, String>) : HttpStatus = folderService.createFolder(request["name"], request["folderPath"])

	/**
	 * move folder from one location to another
	 *
	 * @param request    containing folder old and new path
	 * @return http status
	 */
	@PostMapping("/move")
	fun moveFolder(@RequestBody request : HashMap<String, String>) : HttpStatus = folderService.move(request["oldPath"]!!, request["newPath"]!!)

	/**
	 * copy folder to new path
	 *
	 * @param request    request containing  original folder path and path for folder copy
	 * @return http status
	 */
	@PostMapping("/copy")
	fun copyFolder(@RequestBody request : HashMap<String, String>) : HttpStatus = folderService.copy(request["oldPath"]!!, request["newPath"]!!)

	/**
	 * @param request    request containing folder path and new name
	 * @return http status
	 */
	@PostMapping("/rename")
	fun renameFolder(@RequestBody request: HashMap<String, String>) : HttpStatus = folderService.rename(request["path"]!!, request["newName"]!!)

	/**
	 * @param request    request containing folder path
	 * @return http status
	 */
	@PostMapping("/delete")
	fun deleteFolder(@RequestBody request : HashMap<String, String>) : HttpStatus = folderService.delete(request["path"]!!)

	/**
	 * @param request    request containing folder path
	 * @return http status
	 */
	@PostMapping("/delete-all")
	fun deleteFolderContent(@RequestBody request : HashMap<String, String>) : HttpStatus = folderService.deleteFolderContent(request["path"])

	/**
	 * zip folder into temporary folder and return path to it
	 *
	 * @param request    request with folder path
	 * @return path to zipped folder
	 */
	@PostMapping("/zip")
	fun zipFolder(@RequestBody request : HashMap<String, String>) : String = folderService.zipFolder(File(request["path"]!!))


}
