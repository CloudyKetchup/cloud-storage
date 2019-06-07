package com.krypton.cloud.service.folder;

import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.net.MalformedURLException;
import java.util.*;

@Service
@AllArgsConstructor
public class FolderServiceImpl implements FolderService {

	private final FileRecordServiceImpl fileRecordService;

	private final FolderRecordServiceImpl folderRecordService;

	@Override
	public HashMap getRootData() {
		var root = folderRecordService.getByName("cloud");

		return new HashMap<String, Object>(){{
			put("folders", (Set) root.getFolders());
			put("files", (Set) root.getFiles());
			put("rootFolder", root);
		}};
	}

	@Override
	public HashMap getFolderData(Long id) {
		var folder = folderRecordService.getById(id);
		
		return new HashMap<String, Object>(){{
			put("folders", (Set) folder.getFolders());
			put("files", (Set) folder.getFiles());
			put("folderInfo", folder);
		}};
	}

	@Override
	public HttpStatus createFolder(String folderName, String folderPath) {
		var folder = new File(folderPath + "/" + folderName);
		
	    // make folder locally on file system
		return folder.mkdir()
                // then add record of folder to database
				? folderRecordService.addFolder(folder)
                // if fail return error http status
				: HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus cutFolder(String oldPath, String newPath) {
		var folder = new File(oldPath);
		var updatedPath = newPath + "\\" + folder.getName();
		
		return folder.renameTo(new File(updatedPath))
				? folderRecordService.updatePath(folder, updatedPath)
				: HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus renameFolder(String folderPath, String newName) {
	    var folder = new File(folderPath);
		var parentPath = Paths.get(folderPath).getParent().toFile().getAbsolutePath().toString();

	    // rename folder locally on file system
	    if (folder.renameTo(new File(parentPath + "\\" + newName))) {
			// update folder name in database
			folderRecordService.updateName(folderPath, newName);
			// uddate folder path in database because it contains name
			return folderRecordService.updatePath(folder, parentPath + "\\" + newName);
		}
		// if fail return error http status
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus deleteFolder(String folderPath) {
        var folder = new File(folderPath);

        // first delete inside content
        for (var file : Objects.requireNonNull(folder.listFiles())) {
            // if file is directory call function recursive to all files from inside
			if (file.isDirectory()) {
				deleteFolder(file.getAbsolutePath());
			}
			file.delete();

			removeRecords(folder.getName(), file.getName());			
        }
        folderRecordService.deleteFolderRecord(folder.getName());
        return folder.delete() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public Resource getFolder(String folder) {
		Resource resource = null;

		try {
			resource = new UrlResource("file", System.getProperty("java.io.tmpdir"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		assert resource != null;

		return resource;
	}

	/**
	 * Remove file record from folder record
	 *
	 * @param folder 	folder name
	 * @param file 		file name
	 */
	private void removeRecords(String folder, String file) {
		folderRecordService.removeFile(
				folderRecordService.getByName(folder),	// get folder record by name
				fileRecordService.getByName(file)		// get file record by name
		);
	}
}
