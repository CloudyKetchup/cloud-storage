package com.krypton.cloud.service.folder;

import com.krypton.cloud.exception.entity.io.FileIOException;
import com.krypton.cloud.exception.entity.io.FolderIOException;
import com.krypton.cloud.service.handler.http.ErrorHandler;
import com.krypton.cloud.service.util.exception.ExceptionTools;
import com.krypton.cloud.service.util.log.LogFolder;
import lombok.AllArgsConstructor;
import com.krypton.cloud.model.LogType;
import com.krypton.cloud.service.folder.record.updater.FolderRecordUpdaterImpl;
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl;
import com.krypton.cloud.service.folder.record.FolderRecordUtils;
import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import com.krypton.cloud.service.util.log.LoggingService;
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
public class FolderServiceImpl implements FolderService, ErrorHandler {

	private final FolderRecordServiceImpl folderRecordService;

	private final FileRecordServiceImpl fileRecordService;

	private final FolderRecordUtils folderRecordUtils;

	private final FolderRecordUpdaterImpl folderRecordUpdater;

	private final LoggingService loggingService;

	@Override
	public HttpStatus createFolder(String folderName, String folderPath) {
		var folder = new File(folderPath + "/" + folderName);
		// create folder locally on file system
		if (folder.mkdir()) {
			// add folder to database and return http status > ok
			return folderRecordService.save(folder);
		}
		// if folder was not created, save log file and return http status
		return httpError(new FolderIOException("Error while creating folder " + folder.getPath()).stackTraceToString());
	}

	@Override
	public HttpStatus copyFolder(String folderPath, String copyPath) {
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
			// save exception log
			return httpError(ExceptionTools.INSTANCE.stackTraceToString(e));
		}
	}

	@Override
	public HttpStatus cutFolder(String oldPath, String newPath) {
		var folder 		= new File(oldPath);
		var folderCopy 	= new File(newPath + "/" + folder.getName());
		// move folder to new location
		if (folder.renameTo(folderCopy)) {
			return folderRecordUtils.moveFolder(oldPath, folderCopy);
		}
		return httpError(new FolderIOException("Error when moving folder " + folder.getPath()).stackTraceToString());
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
		return httpError(new FolderIOException("Error when renaming folder " + folder.getPath()).stackTraceToString());
	}

	@Override
	public HttpStatus deleteFolder(String folderPath) {
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
					deleteFolder(file.getPath());
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
		loggingService.saveLog(message, LogType.ERROR, LogFolder.FOLDER.getType());

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

		if (folder.listFiles().length == 0) {
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

		return folder != null
				? zip.toString()
				: String.valueOf(httpError(new FileIOException("Error while zipping folder " + folder.getPath()).stackTraceToString()));
	}
}