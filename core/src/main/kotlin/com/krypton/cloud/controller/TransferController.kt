package com.krypton.cloud.controller

import com.krypton.databaselayer.service.file.FileRecordServiceImpl
import com.krypton.medialayer.service.MediaService
import lombok.AllArgsConstructor
import org.springframework.core.io.InputStreamResource

import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.util.*

@CrossOrigin
@RestController
@AllArgsConstructor
class TransferController(private val fileRecordServiceImpl: FileRecordServiceImpl) {

	/**
	 * Download file from input stream
	 *
	 * @param path
	 * */
	@GetMapping("/file/{path}/download")
	fun downloadFile(@PathVariable path : String) : ResponseEntity<Resource> {
		val file = File(path)
		val inputStream = file.inputStream()

		return ResponseEntity
			.ok()
			.contentType(MediaType.parseMediaType("application/octet-stream"))
			.headers(HttpHeaders().apply { set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${file.name}") })
			.body(InputStreamResource(inputStream))
	}

	@GetMapping("/file/{id}/thumbnail")
	suspend fun getThumbnail(@PathVariable id : String) : ResponseEntity<ByteArray> {
		val fileEntity = fileRecordServiceImpl.getById(UUID.fromString(id))
		val file : File? = when (fileEntity) { null -> null else -> File(fileEntity.path) }

		if (fileEntity != null) {
			var thumbnail = MediaService.getThumbnail(fileEntity.id)

			if (thumbnail == null || thumbnail.body == null) {

				if (file != null && file.exists()) {
					MediaService.createThumbnail(file, fileEntity.id.toString())

					thumbnail = MediaService.getThumbnail(fileEntity.id);
				}
			}
			if (thumbnail != null && thumbnail.body != null) return thumbnail
		}
		return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(file?.readBytes())
	}

	@GetMapping("/file/{id}/image")
	fun getImage(@PathVariable id : String) : HttpEntity<*> {
		val file = fileRecordServiceImpl.getById(UUID.fromString(id))

		return if (file != null && file.isMedia) {
			ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(File(file.path).readBytes())
		} else ResponseEntity.EMPTY
	}
}
