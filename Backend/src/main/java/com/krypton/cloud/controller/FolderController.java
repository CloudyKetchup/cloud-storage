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
	 * get root folders list
	 *
	 * @return folders list
	 */
    @GetMapping("/root/content")
    public HashMap rootContent() {
    	return folderService.getRootData();
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
	 * create new folder to specified path
	 *
	 * @param request 		new folder data
	 * @return http status
	 */
    @PostMapping("/create")
    public HttpStatus createFolder(@RequestBody HashMap<String, String> request) {
    	return folderService.createFolder(request.get("name"), request.get("folderPath"));
    }

    /**
     * move folder from one location(folder) to another
     *
     * @param request       containing folder old and new path
     * @return http status
     */
    @PostMapping("/cut")
    public HttpStatus cutFolder(@RequestBody HashMap<String, String> request) {
    	return folderService.cutFolder(request.get("oldPath"), request.get("newPath"));
    }

    /**
     * copy folder to new path
     *
     * @param request       request containing  original folder path and path for folder copy
     * @return http status
     */
    @PostMapping("/copy")
    public HttpStatus copyFolder(@RequestBody HashMap<String, String> request) {
        return folderService.copyFolder(request.get("oldPath"), request.get("newPath"));
    }

    /**
	 * rename folder
	 *
	 * @param folder 		request containg folder path and new name
	 * @return http status
	 */
    @PostMapping("/rename")
	public HttpStatus renameFolder(@RequestBody HashMap<String, String> request) {
    	return folderService.renameFolder(request.get("folderPath"), request.get("newName"));
	}

    /**
     * delete folder
     *
     * @param request 		request containing folder path
	 * @return http status
	 */
    @PostMapping("/delete")
    public HttpStatus deleteFolder(@RequestBody HashMap<String, String> request) {
    	return request.get("folderPath") != null
			? folderService.deleteFolder(request.get("folderPath"))
			: HttpStatus.INTERNAL_SERVER_ERROR; 	
    }

    @GetMapping(value = "/{folder}/get", produces="application/zip")
	public ResponseEntity getFolder(@PathVariable("folder") String folder) {
		zipService.createZip(folder);
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + folder + ".zip")
				.body(folderService.getFolder(folder));
	}
}
