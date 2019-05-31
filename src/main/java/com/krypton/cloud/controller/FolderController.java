package com.krypton.cloud.controller;

import com.krypton.cloud.service.deliver.ZipService;
import com.krypton.cloud.service.folder.FolderServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/folder")
public class FolderController {

	private final FolderServiceImpl folderService;

	private final ZipService zipService;

	/**
	 * get root folders list
	 *
	 * @return folders list
	 */
    @GetMapping("/root/content")
    public HashMap rootContent() {
    	return folderService.getRootData();
    }

    /**
	 * get total and free disk space in GB format
	 *
	 * @return hash map containing disk space info
	 */
    @GetMapping("/root/memory")
	public HashMap rootMemory() {
    	return new HashMap<String, String>(){{
    		put("total", String.valueOf(new File("/").getTotalSpace()/1024/1024/1024));
    		put("free", String.valueOf(new File("/").getFreeSpace()/1024/1024/1024));
		}};
	}

    /**
	 * get folders and files list from specified folder
	 *
	 * @param folder 		folder id
	 * @return folders and files list
	 */
    @GetMapping("/{folder}/content")
    public HashMap folderContent(@PathVariable("folder") Long folder) {
    	return folderService.getFolderData(folder);
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
	 * rename folder
	 *
	 * @param folder 		folder name
	 * @param newName 		new name for folder
	 * @return http status
	 */
    @PostMapping("/rename/{folder}/newName={newName}")
	public HttpStatus renameFolder(
			@PathVariable("folder") String folder,
			@PathVariable("newName") String newName
	) {
    	return folderService.renameFolder(folder, newName);
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
		zipService.createZip(folder);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + folder + ".zip")
				.body(folderService.getFolder(folder));
	}
}
