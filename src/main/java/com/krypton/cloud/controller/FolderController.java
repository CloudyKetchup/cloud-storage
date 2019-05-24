package com.krypton.cloud.controller;

import com.krypton.cloud.service.file.FileServiceImpl;
import com.krypton.cloud.service.folder.FolderServiceImpl;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/folder")
public class FolderController {

	private FolderServiceImpl folderService;

	public FolderController(FolderServiceImpl folderService, FileServiceImpl fileService) {
		this.folderService = folderService;
	}

	/**
	 * get root folders and files list
	 *
	 * @return folders and files list
	 */
    @GetMapping("/root/content")
    public String[] rootContent() {
    	return folderService.getRootFilesList();
    }

    /**
	 * get folders and files list from specified folder
	 *
	 * @param folder 		folder path
	 * @return folders and files list
	 */
    @GetMapping("/{folder}/content")
    public String[] folderContent(@PathVariable("folder") String folder) {
    	return folderService.getFolderContent(folder);
    }

    /**
	 * create new folder
	 *
	 * @param request 		folder name and folder path
	 * @return http status
	 */
    @PostMapping("/create")
    public HttpStatus createFolder(@RequestBody HashMap<String ,String> request) {
    	return folderService.createFolder(request.get("folderName"), request.get("folderPath"));
    }

    /**
     * delete folder
     *
     * @param folder 		folder path
	 * @return http status
	 */
    @PostMapping("/{folder}/delete")
    public HttpStatus deleteFolder(@PathVariable("folder") String folder) {
    	return folderService.deleteFolder(folder);
    }

    @GetMapping(value = "/{folder}/get", produces="application/zip")
	public ResponseEntity getFolder(@PathVariable("folder") String folder) {
		folderService.createZip(folder);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + folder + ".zip")
				.body(folderService.getFolder(folder));
	}
}
