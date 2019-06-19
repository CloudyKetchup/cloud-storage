package com.krypton.cloud.controller;

import com.krypton.cloud.model.Folder;
import com.krypton.cloud.service.deliver.ZipService;
import com.krypton.cloud.service.folder.FolderServiceImpl;
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.HashMap;

@RestController
@AllArgsConstructor
@RequestMapping("/folder")
public class FolderController {

	private final FolderServiceImpl folderService;

	private final FolderRecordServiceImpl folderRecordService;

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
	 * get {@link Folder} entity data
	 *
	 * @param folder 		folder id
	 * @return {@link Folder}
	 */
    @GetMapping("/{folder}/data")
    public Folder getFolderData(@PathVariable("folder") Long folder) {
    	return folderService.getFolderData(folder);
    }

    /**
     * get list of {@link Folder}'s inside a {@link Folder}
     *
     * @param id        parent folder id
     * @return {@link Folder}'s list
     */
    @GetMapping("/{folder}/folders")
	public Flux<Folder> getFolderFolders(@PathVariable("folder") Long id) {
    	return folderRecordService.getFolderFolders(id);
	}

    /**
     * get list of {@link com.krypton.cloud.model.File}'s inside a {@link Folder}
     *
     * @param id        parent folder id
     * @return {@link com.krypton.cloud.model.File}'s list
     */
	@GetMapping("/{folder}/files")
    public Flux<com.krypton.cloud.model.File> getFolderFiles(@PathVariable("folder") Long id) {
        return folderRecordService.getFolderFiles(id);
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
     * move folder from one location to another
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
	 * @param request 		request containing folder path and new name
	 * @return http status
	 */
    @PostMapping("/rename")
	public HttpStatus renameFolder(@RequestBody HashMap<String, String> request) {
    	return folderService.renameFolder(request.get("path"), request.get("newName"));
	}

    /**
     * delete folder
     *
     * @param request 		request containing folder path
	 * @return http status
	 */
    @PostMapping("/delete")
    public HttpStatus deleteFolder(@RequestBody HashMap<String, String> request) {
    	return folderService.deleteFolder(request.get("path"));
    }

    @GetMapping(value = "/{folder}/get", produces="application/zip")
	public ResponseEntity downloadFolder(@PathVariable("folder") String folder) {
		zipService.createZip(folder);
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + folder + ".zip")
				.body(folderService.getFolder(folder));
	}
}
