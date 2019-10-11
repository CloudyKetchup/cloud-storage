package com.krypton.cloud.service.folder;

import com.krypton.databaselayer.model.Folder;
import common.exception.entity.io.FolderIOException;
import com.krypton.cloud.service.handler.http.ErrorHandler;
import common.model.LogType;
import lombok.AllArgsConstructor;
import com.krypton.databaselayer.service.folder.updater.FolderRecordUpdaterImpl;
import com.krypton.databaselayer.service.folder.FolderRecordServiceImpl;
import com.krypton.databaselayer.service.folder.FolderRecordUtils;
import com.krypton.databaselayer.service.file.FileRecordServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.FileUtils;
import common.exception.ExceptionTools;
import util.log.LogFolder;
import util.log.LoggingService;

@Service
@AllArgsConstructor
public class FolderServiceImpl implements FolderService, ErrorHandler {

	private final FolderRecordServiceImpl folderRecordService;

	private final FileRecordServiceImpl fileRecordService;

	private final FolderRecordUtils folderRecordUtils;

	private final FolderRecordUpdaterImpl folderRecordUpdater;
	

	@Override
	public HttpStatus createFolder(String folderName, String folderPath) {
		var folder = new File(folderPath + "/" + folderName);
		// create folder locally on file system
		if (folder.mkdir()) {
			// add folder to database and return http status > ok
			return folderRecordService.save(new Folder(folder)) != null
					? HttpStatus.OK
					: httpError(new FolderIOException().stackTraceToString());
		}
		// if folder was not created, save log file and return http status
		return httpError(new FolderIOException().stackTraceToString());
	}

	@Override
	public HttpStatus copy(String folderPath, String copyPath) {
		var folder 		= new File(folderPath);
		var folderCopy 	= new File(copyPath + "/" + folder.getName());
		// copy folder to new path
		try {
			FileUtils.copyDirectory(folder, folderCopy);
			// check if file was copied successful
			if (folderCopy.exists())
				// add copied folder record to database and return http status
				return folderRecordUtils.copyFolder(folderCopy);
			else throw new FolderIOException("Error when coping folder " + folder.getPath());
		} catch (IOException | FolderIOException e) {
			e.printStackTrace();
			return httpError(ExceptionTools.INSTANCE.stackTraceToString(e));
		}
	}

	@Override
	public HttpStatus move(String oldPath, String newPath) {
		var folder 		= new File(oldPath);
		var folderCopy 	= new File(newPath + "/" + folder.getName());
		// move folder to new location
		if (folder.renameTo(folderCopy)) {
			return folderRecordUtils.moveFolder(oldPath, folderCopy);
		}
		return httpError(new FolderIOException("Error when moving folder " + folder.getPath()).stackTraceToString());
	}

	@Override
	public HttpStatus rename(String folderPath, String newName) {
	    var folder 		= new File(folderPath);
		var parentPath 	= Paths.get(folderPath).getParent().toFile().getPath();
	    // rename folder locally on file system
	    if (folder.renameTo(new File(parentPath + "/" + newName))) {
			// update folder name in database
			folderRecordUpdater.updateName(folderPath, newName);
			// update folder path in database because it contains name
			return folderRecordUpdater.updatePath(folder, parentPath + "/" + newName);
		}
		return httpError(new FolderIOException("Error when renaming folder " + folder.getPath()).stackTraceToString());
	}

	@Override
	public HttpStatus delete(String folderPath) {
        var folder = new File(folderPath);

        deleteFolderContent(folderPath);

        folderRecordService.delete(folder.getPath());

        return folder.delete()
				? HttpStatus.OK
				: httpError(new FolderIOException("Error while deleting folder" + folder.getPath()).stackTraceToString());
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
					delete(file.getPath());
				} else {
					file.delete();
					// delete file from database
					fileRecordService.delete(file.getPath());
				}
	        }
    	}
        // check if folder is now empty
		return new File(folderPath).list().length == 0
				? HttpStatus.OK
				: httpError(new FolderIOException("Error while deleting folder " + folderPath + " content").stackTraceToString());
	}

	@Override
	public HttpStatus httpError(String message) {
		LoggingService.INSTANCE.saveLog(message, LogType.ERROR, LogFolder.FOLDER.getType());

		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	/**
	 * get items count inside folder
	 *
	 * @param id 	folder id
	 * @return folders and files count
	 */
	public HashMap<String, Integer> getItemsCount(UUID id) {
		var folder = folderRecordService.getById(id);

		return new HashMap<>(){{
			put("foldersCount",  folder.getFolders().size());
			put("filesCount",  folder.getFiles().size());
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

		if (folder.listFiles() == null || folder.listFiles().length == 0) {
			return "folder is empty";
		}
		try {
			zip = Files.createTempFile(folder.getName(), ".zip");
		} catch (IOException e) {
			e.printStackTrace();
			return httpError(e.getLocalizedMessage()).toString();
		}
		// zip folder to file created in temp folder
		ZipUtil.pack(folder, new File(zip.toString()));

		return zip.toString();
	}
}
