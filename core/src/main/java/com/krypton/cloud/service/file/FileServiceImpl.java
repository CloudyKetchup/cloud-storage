package com.krypton.cloud.service.file;

import com.krypton.cloud.exception.entity.io.FileIOException;
import com.krypton.cloud.model.common.LogType;
import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import com.krypton.cloud.service.file.record.updater.FileRecordUpdaterImpl;
import com.krypton.cloud.service.handler.http.ErrorHandler;
import com.krypton.cloud.service.util.exception.ExceptionTools;
import com.krypton.cloud.service.util.log.*;
import com.krypton.cloud.service.handler.io.IOErrorHandler;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class FileServiceImpl implements FileService, IOErrorHandler, ErrorHandler {

	private final FileRecordServiceImpl fileRecordService;

	private final FileRecordUpdaterImpl fileRecordUpdater;

	private final LoggingService loggingService;

	@Override
	public Mono<HttpStatus> saveFile(FilePart file, String path) {
		var fullPath = path + "/" + file.filename();

		if (new File(fullPath).exists()) return Mono.just(HttpStatus.INTERNAL_SERVER_ERROR);
		// write file and save it to database
		return writeFilePart(file, fullPath).map(result -> {
			// if result is true check if file is in database
			if (result && fileRecordService.exists(fullPath)) {
				return HttpStatus.OK;
			}
			return HttpStatus.INTERNAL_SERVER_ERROR;
		});
	}

	@Override
	public HttpStatus move(String oldPath, String newPath) {
		var file = new File(oldPath);

		var fileCopy = new File(newPath + "/" + file.getName());

		if (file.renameTo(fileCopy)) {
			// remove file with old path from database
			fileRecordService.delete(oldPath);
			// add file with new path to database
			fileRecordService.save(fileCopy);

			return HttpStatus.OK;
		}
		return httpError(ExceptionTools.INSTANCE.stackTraceToString(new FileIOException("Error moving File => " + oldPath)));
	}

	@Override
	public HttpStatus copy(String oldPath, String newPath) {
		var file = new File(oldPath);

		var fileCopy = new File(newPath + "/" + file.getName());
		// copy file to new folder
		try {
			FileUtils.copyFile(file, fileCopy);
			// check if file was copied successful
			if (fileCopy.exists() && fileRecordService.save(fileCopy) != null)
				return HttpStatus.OK;
		} catch (IOException e) {
			e.printStackTrace();
			error(ExceptionTools.INSTANCE.stackTraceToString(e));
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus rename(String path, String newName) {
		var file = new File(path);

		var parentFolder = Paths.get(path).getParent().toFile().getPath();

		var newFile = new File(parentFolder + "/" + newName);

		return file.renameTo(newFile)
				? fileRecordUpdater.updateName(path, newFile)
				: httpError(new FileIOException("Error while renaming file " + file.getPath()).stackTraceToString());
	}

	@Override
	public HttpStatus delete(String path) {
		return new File(path).delete()
				? fileRecordService.delete(path)
				: httpError(new FileIOException("Error while deleting file " + path).stackTraceToString());
	}

	@Override
	public void error(String message) {
		loggingService.saveLog(message, LogType.ERROR, LogFolder.FILE.getType());
	}

	@Override
	public HttpStatus httpError(String message) {
		loggingService.saveLog(message, LogType.ERROR, LogFolder.FILE.getType());
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	/**
	 * Save {@link FilePart} to disk then add record to database
	 *
	 * @param filePart {@link FilePart} from request
	 * @param path     path to folder where to save file
	 */
	private Mono<Boolean> writeFilePart(FilePart filePart, String path) {
		var file = new File(path);

		try {
			// create file for data transfer
			if (file.createNewFile()) {
				// transfer incoming file data to local file
				return filePart.transferTo(file)
						.doOnSuccess(result -> fileRecordService.save(file))
						.doOnError(Throwable::printStackTrace)
						.thenReturn(true);
			}
			// in case that result is not returned rise error
			throw new FileIOException();
		} catch (FileIOException | IOException e) {
			e.printStackTrace();
			error(ExceptionTools.INSTANCE.stackTraceToString(e));
		}
		return Mono.just(false);
	}
}
