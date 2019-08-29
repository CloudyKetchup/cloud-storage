package com.krypton.cloud.service.folder;
import lombok.AllArgsConstructor;
import com.krypton.cloud.model.Folder;
import com.krypton.cloud.service.folder.record.updater.FolderRecordUpdaterImpl;
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl;
import com.krypton.cloud.service.folder.record.FolderRecordUtils;
import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.FileUtils;

@Service
@AllArgsConstructor
public class FolderServiceImpl implements FolderService {

	private final FolderRecordServiceImpl folderRecordService;

	private final FileRecordServiceImpl fileRecordService;

	private final FolderRecordUtils folderRecordUtils;

	private final FolderRecordUpdaterImpl folderRecordUpdater;

	@Override
	public Folder getFolderData(Long id) {
		return folderRecordService.getById(id);
	}

	@Override
	public HttpStatus createFolder(String folderName, String folderPath) {
		var folder = new File(folderPath + "/" + folderName);
		
		// make folder locally on file system
		return folder.mkdir()
				// then add record of folder to database
				? folderRecordService.save(folder)
				// if fail return error http status
				: HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus copyFolder(String folderPath, String copyPath) {
		var folder 		= new File(folderPath);
		var folderCopy 	= new File(copyPath + "/" + folder.getName());

		// copy folder to new path
		try {
			FileUtils.copyDirectory(folder, folderCopy);
		} catch (IOException e) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		// check if file was copied successful
		if (folderCopy.exists()) {
			// add copied folder record
			return folderRecordUtils.copyFolder(folderCopy);
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus cutFolder(String oldPath, String newPath) {
		var folder 		= new File(oldPath);
		var folderCopy 	= new File(newPath + "/" + folder.getName());
		// move folder to new location
		if (folder.renameTo(folderCopy)) {
			return folderRecordUtils.moveFolder(oldPath, folderCopy);
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus renameFolder(String folderPath, String newName) {
	    var folder 		= new File(folderPath);
		var parentPath 	= Paths.get(folderPath).getParent().toFile().getPath();

	    // rename folder locally on file system
	    if (folder.renameTo(new File(parentPath + "/" + newName))) {
			// update folder name in database
			folderRecordUpdater.updateName(folderPath, newName);
			// update folder path in database because it contains name
			return folderRecordUpdater.updatePath(folder, parentPath + "/" + newName);
		}
		// if fail return error http status
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus deleteFolder(String folderPath) {
        var folder = new File(folderPath);

        deleteFolderContent(folderPath);

        folderRecordService.delete(folder.getPath());

        return folder.delete() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus deleteFolderContent(String folderPath) {
		var folder = new File(folderPath);
		// check if folder is not empty
		if (folder.listFiles() != null) {
			// delete inside content
	        for (var file : folder.listFiles()) {
	            // if file is directory delete content inside it
				if (file.isDirectory()) {
					deleteFolder(file.getPath());
				} else {
					file.delete();

					fileRecordService.delete(file.getPath());
				}
	        }
    	}
        // check if folder is now empty
        return new File(folderPath).list().length == 0 ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
	}

	/**
	 * get items count inside folder
	 *
	 * @param id 	folder id
	 * @return folders and files count
	 */
	public HashMap<String, Integer> getItemsCount(Long id) {
		var folder = folderRecordService.getById(id);

		return new HashMap<>(){{
			put("foldersCount", (Integer) folder.getFolders().size());
			put("filesCount", (Integer) folder.getFiles().size());
		}};
	}

	/**
	 * zip folder and return path to it
	 *
	 * @param folder 	folder to zip
	 * @return return path to zipped folder if success,"folder is empty" or internal server error
	 */
	public String zipFolder(File folder) {
		Path zip;

		if (folder.listFiles().length == 0) {
			return "folder is empty";
		}

		try {
			zip = Files.createTempFile(folder.getName(), ".zip");
		} catch (IOException e) {
			return String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// zip folder to file created in temp folder
		ZipUtil.pack(folder, new File(zip.toString()));

		return folder != null ? zip.toString() : String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}