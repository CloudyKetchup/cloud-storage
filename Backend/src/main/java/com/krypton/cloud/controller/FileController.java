package com.krypton.cloud.controller;

import com.krypton.cloud.service.file.FileServiceImpl;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.TestOnly;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@RestController
@AllArgsConstructor
@RequestMapping("/file")
public class FileController {

	private FileServiceImpl fileService;

	/**
	 * upload multiple files to specified folder
	 *
	 * @param files				files for save
	 * @param folder 			folder path where to save files
	 * @return http status
	 */
	@TestOnly
	@PostMapping("/upload/many")
	public Flux<HttpStatus> uploadFiles(
			@RequestPart("files") Flux<FilePart> files,
			@RequestPart("path") FormFieldPart folder
	) {
		return fileService.saveFiles(files, folder.value());
	}

	/**
	 * upload one file to specified folder path
	 *
	 * @param file 			file for save
	 * @param path          path to folder where to save file
	 * @return  http status
	 */
	@PostMapping("/upload/one")
	public Mono<HttpStatus> uploadFile(
			@RequestPart("file") Mono<FilePart> file,
			@RequestPart("path") FormFieldPart path
	) {
		return fileService.saveFile(file, path.value());
	}

	/**
	 * @param path 		file path
	 * @param name 		file name
	 * @return file
	 */
	@GetMapping("/{path}/{name}/download")
	public ResponseEntity<Resource> downloadFile(
			@PathVariable String path,
			@PathVariable String name
	) {
		return ResponseEntity
					.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
					.body(fileService.getFile(path));
	}

	/**
	 * @param request 	file path and new name
	 * @return http status
	 */
	@PostMapping("/rename")
	public HttpStatus renameFile(@RequestBody HashMap<String, String> request) {
		return fileService.renameFile(request.get("path"), request.get("newName"));
	}

	/**
	 * @param request 	file path
	 * @return http status
	 */
	@PostMapping("/delete")
	public HttpStatus deleteFile(@RequestBody HashMap<String, String> request) {
		return fileService.deleteFile(request.get("path"));
	}
}