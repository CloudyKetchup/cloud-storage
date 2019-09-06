package com.krypton.cloud.service.file;

import com.krypton.cloud.exception.entity.io.FileIOException;
import com.krypton.cloud.model.LogType;
import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import com.krypton.cloud.service.file.record.updater.FileRecordUpdaterImpl;
import com.krypton.cloud.service.handler.http.ErrorHandler;
import com.krypton.cloud.service.util.commons.ExceptionTools;
import com.krypton.cloud.service.util.log.LogFolder;
import com.krypton.cloud.service.util.log.LoggingService;
import com.krypton.cloud.service.handler.io.IOErrorHandler;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class FileServiceImpl implements FileService, IOErrorHandler, ErrorHandler {

	private final FileRecordServiceImpl fileRecordService;

	private final FileRecordUpdaterImpl fileRecordUpdater;

	private final LoggingService loggingService;

	@Override
	public byte[] getFile(String path) {
		var file = new File(path);

		byte[] bytes = null;

		try {
			bytes = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			error(new FileIOException("Error while downloading file " + file.getPath()).stackTraceToString());
		}
		return bytes;
	}

	@Override
	public Flux<HttpStatus> saveFiles(Flux<FilePart> files, String path) {
		return files.flatMap(file -> Flux.just(writeFilePart(file, path) 
						? HttpStatus.OK 
						: httpError(new FileIOException("Error while saving file " + file.filename()).stackTraceToString())));
	}

	@Override
	public Mono<HttpStatus> saveFile(FilePart file, String path) {
		return Mono.just(writeFilePart(file, path)
						? HttpStatus.OK 
						: httpError(new FileIOException("Error while saving file " + file.filename()).stackTraceToString()));
	}

	@Override
	public HttpStatus cutFile(String oldPath, String newPath) {
		var file = new File(oldPath);

		var fileCopy = new File(newPath + "/" + file.getName());

		if (file.renameTo(fileCopy)) {
			// remove file with old path from database
			fileRecordService.delete(oldPath);
			// add file with new path to database
			fileRecordService.save(fileCopy);

			return HttpStatus.OK;
		}
		return httpError(new FileIOException("Error while moving file " + file.getPath()).stackTraceToString());
	}

	@Override
	public HttpStatus copyFile(String oldPath, String newPath) {
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
		}
		return httpError(new FileIOException("Error while coping file " + file.getPath()).stackTraceToString());
	}

	@Override
	public HttpStatus renameFile(String path, String newName) {
		var file = new File(path);

		var parentFolder = Paths.get(path).getParent().toFile().getPath();

		var newFile = new File(parentFolder + "/" + newName);

		return file.renameTo(newFile)
				? fileRecordUpdater.updateName(path, newFile)
				: httpError(new FileIOException("Error while renaming file " + file.getPath()).stackTraceToString());
	}

	@Override
	public HttpStatus deleteFile(String path) {
		return new File(path).delete()
				? fileRecordService.delete(path)
				: httpError(new FileIOException("Error while deleting file " + path).stackTraceToString());
	}

	@Override
	public void error(String message) {
		loggingService.saveLog(message != null ? message : "Blank log", LogType.ERROR, LogFolder.FILE.getType());
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
	private boolean writeFilePart(FilePart filePart, String path) {
		var file = new File(path + "/" + filePart.filename());

		try {
			if (file.createNewFile())
				// transfer incoming file data to local file
				filePart.transferTo(file).subscribe();
			else throw new FileIOException();
			// save file to database
			return fileRecordService.save(file) != null;
		} catch (Exception e) {
			e.printStackTrace();
			error(ExceptionTools.INSTANCE.stackTraceToString(e));
		}
		return false;
	}
}
