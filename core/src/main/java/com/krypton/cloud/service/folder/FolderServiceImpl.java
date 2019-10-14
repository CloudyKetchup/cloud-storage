package com.krypton.cloud.service.folder;

import com.krypton.storagelayer.service.filesystem.FileSystemLayer;
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

import common.exception.ExceptionTools;
import util.log.LogFolder;
import util.log.LoggingService;

import static java.util.Objects.requireNonNull;

@Service
@AllArgsConstructor
public class FolderServiceImpl implements FolderService, ErrorHandler {

	private final FolderRecordServiceImpl folderRecordService;

	private final FileRecordServiceImpl fileRecordService;

	private final FolderRecordUtils folderRecordUtils;

	private final FolderRecordUpdaterImpl folderRecordUpdater;

	private final FileSystemLayer fileSystemLayer;
	
	@Override
	public HttpStatus create(String name, String path) {
		var folder = new File(path + "/" + name);
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
		var folder 	= new File(folderPath);
		var copy	= new File(copyPath + "/" + folder.getName());

		try {
			// create folder copy on filesystem
			if (fileSystemLayer.copy(folder, copy))
				// add folder copy to database
				return folderRecordUtils.copyFolder(copy);
			else throw new FolderIOException("Error when coping folder " + folder.getPath());
		} catch (IOException | FolderIOException e) {
			e.printStackTrace();
			return httpError(ExceptionTools.INSTANCE.stackTraceToString(e));
		}
	}

	@Override
	public HttpStatus move(String oldPath, String newFolder) {
		var folder 	= new File(oldPath);
		var newPath = newFolder + "/" + folder.getName();
		// move folder to new location
		if (fileSystemLayer.move(folder, newPath)) {
			return folderRecordUtils.moveFolder(oldPath, new File(newPath));
		}
		return httpError(new FolderIOException("Error when moving folder " + folder.getPath()).stackTraceToString());
	}

	@Override
	public HttpStatus rename(UUID id, String newName) {
	    var folderEntity = folderRecordService.getById(id);
	    var folder 		 = new File(folderEntity.getPath());
		var newPath 	 = Paths.get(folderEntity.getPath()).getParent().toAbsolutePath() + "/" + newName;
	    // rename folder locally on file system
	    if (fileSystemLayer.rename(folder, newName)) {
			// update folder name in database
			folderRecordUpdater.updateName(folderEntity.getPath(), newName);
			// update folder path in database
			return folderRecordUpdater.updatePath(folder, newPath);
		}
		return httpError(new FolderIOException("Error when renaming folder " + folder.getPath()).stackTraceToString());
	}

	@Override
	public HttpStatus delete(UUID id) {
		var path = folderRecordService.getById(id).getPath();

		deleteContent(id);

		folderRecordService.delete(path);

		if (folderRecordService.getByPath(path) == null && fileSystemLayer.delete(path))
			return HttpStatus.OK;
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus deleteContent(UUID id) {
		var folder = folderRecordService.getById(id);
		// check if folder is not empty
		folder.getFiles().parallelStream().forEach(f -> {
			var path = f.getPath();

			if (new File(path).delete()) fileRecordService.delete(path);
		});
		folder.getFolders().parallelStream().forEach(f -> {
			deleteContent(f.getId());

			delete(f.getId());
		});
		var folderFiles = new File(folder.getPath()).list();
        // check if folder is now empty
		return folderFiles != null && folderFiles.length == 0
				? HttpStatus.OK
				: httpError(new FolderIOException("Error while deleting folder " + folder.getPath() + " content").stackTraceToString());
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

		try {
		    if (requireNonNull(folder.listFiles()).length == 0) return "folder is empty";

			zip = Files.createTempFile(folder.getName(), ".zip");
		} catch (IOException e) {
			e.printStackTrace();
			return httpError(ExceptionTools.INSTANCE.stackTraceToString(e)).toString();
		} catch (NullPointerException e) {
			return "folder is empty";
		}
		// zip folder to file created in temp folder
		ZipUtil.pack(folder, new File(zip.toString()));

		return zip.toString();
	}
}
