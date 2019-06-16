package com.krypton.cloud.controller;

import com.krypton.cloud.service.file.FileServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;

@RestController
@AllArgsConstructor
@RequestMapping("/files")
public class FileController {

	private FileServiceImpl fileService;

	/**
	 * upload multiple files to specified folder
	 *
	 * @param files				files for save
	 * @param folder 			folder path where to save files
	 * @return http status
	 */
	@PostMapping("/upload/many")
	public Mono<HttpStatus> uploadFiles(
			@RequestPart("files") Flux<FilePart> files,
			@RequestPart("folder") String folder
	) {
		return fileService.saveFiles(files, folder);
	}

	/**
	 * upload one file to specified
	 *
	 * @param file 			file for save
	 * @param path          path where to save file
	 * @return  http status
	 */
	@PostMapping("/upload/one")
	public Mono<HttpStatus> uploadFile(
			@RequestPart("file") Mono<FilePart> file,
			@RequestParam("path") String path
    ) {
		System.out.println(path);

		return Mono.just(HttpStatus.OK);
//		return fileService.saveFile(file, folder);
	}

	/**
	 * download file
	 *
	 * @param file 		file name
	 * @param folder 	file path directory
	 * @return file
	 */
	@GetMapping("/{folder}/{file}/get")
	public ResponseEntity<Resource> getFile(
			@PathVariable("file") String file,
			@PathVariable("folder") String folder
	) {
		return ResponseEntity
					.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file + "\"")
					.body(fileService.getFile(file, folder));
	}

	/**
	 * rename file
	 *
	 * @param request 	file name, path and new name
	 * @return http status
	 */
	@PostMapping("/file/rename")
	public HttpStatus renameFile(@RequestBody HashMap<String, String> request) {
		
		return fileService.renameFile(request.get("file"), request.get("folder"), request.get("newName"));
	}

	/**
	 * delete file from folder
	 *
	 * @param file 		file for delete
	 * @param folder 	file path directory
	 * @return http status
	 */
	@PostMapping("{folder}/{file}/remove")
	public HttpStatus deleteFile(
		@PathVariable("file") String file,
		@PathVariable("folder") String folder
	) {
		return fileService.deleteFile(file, folder);
	}
}