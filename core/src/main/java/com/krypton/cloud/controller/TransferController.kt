package com.krypton.cloud.controller

import lombok.AllArgsConstructor
import org.springframework.core.io.InputStreamResource

import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*;
import java.io.File

@RestController
@AllArgsConstructor
class TransferController {

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

	 @GetMapping("/file/{path}/image")
	 fun getImage(@PathVariable path : String) : ResponseEntity<ByteArray> {
		val file = File(path)

		return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(file.readBytes())
	 }
 }
