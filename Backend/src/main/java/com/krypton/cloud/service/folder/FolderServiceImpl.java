package com.krypton.cloud.service.folder;

import com.krypton.cloud.model.Folder;
import com.krypton.cloud.service.folder.record.updater.FolderRecordUpdaterImpl;
import com.krypton.cloud.service.folder.record.FolderRecordServiceImpl;
import com.krypton.cloud.service.folder.record.FolderRecordUtils;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.net.MalformedURLException;
import java.util.*;

import org.apache.commons.io.FileUtils;

@Service
@AllArgsConstructor
public class FolderServiceImpl implements FolderService {

	private final FolderRecordServiceImpl folderRecordService;

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
		var folder = new File(folderPath);
		var folderCopy = new File(copyPath + "\\" + folder.getName());

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
		var folder = new File(oldPath);
		var folderCopy = new File(newPath + "\\" + folder.getName());
		
		if (folder.renameTo(folderCopy)) {
			return folderRecordUtils.moveFolder(oldPath, folderCopy);
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus renameFolder(String folderPath, String newName) {
	    var folder = new File(folderPath);
		var parentPath = Paths.get(folderPath).getParent().toFile().getPath();

	    // rename folder locally on file system
	    if (folder.renameTo(new File(parentPath + "\\" + newName))) {
			// update folder name in database
			folderRecordUpdater.updateName(folderPath, newName);
			// update folder path in database because it contains name
			return folderRecordUpdater.updatePath(folder, parentPath + "\\" + newName);
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
        }
        folderRecordService.delete(folder.getPath());
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
}
