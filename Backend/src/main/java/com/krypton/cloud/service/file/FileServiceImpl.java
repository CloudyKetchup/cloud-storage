package com.krypton.cloud.service.file;

import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import com.krypton.cloud.service.file.record.updater.FileRecordUpdaterImpl;
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
public class FileServiceImpl implements FileService {

	private final FileRecordServiceImpl fileRecordService;

	private final FileRecordUpdaterImpl fileRecordUpdater;

	@Override
	public byte[] getFile(String path) {
		var file = new File(path);

		byte[] bytes = null;

		try {
			bytes = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes; 
	}

	@Override
	public Flux<HttpStatus> saveFiles(Flux<FilePart> files, String path) {
		return files.flatMap(file -> Flux.just(writeFilePart(file, path) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Override
	public Mono<HttpStatus> saveFile(Mono<FilePart> filePart, String path) {
		return filePart.flatMap(file -> Mono.just(writeFilePart(file, path) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Override
	public HttpStatus cutFile(String oldPath, String newPath) {
		var file = new File(oldPath);
		var fileCopy = new File(newPath + "\\" + file.getName());

		if (file.renameTo(fileCopy)) {
			// remove file with old path from database
			fileRecordService.delete(oldPath);
			// add file with new path to database
			fileRecordService.save(fileCopy);

			return HttpStatus.OK;
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus copyFile(String oldPath, String newPath) {
		var file = new File(oldPath);
		var fileCopy = new File(newPath + "\\" + file.getName());

		// copy file to new folder
		try {
			FileUtils.copyFile(file, fileCopy);
		} catch (IOException e) {
			e.printStackTrace();

			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		// check if file was copied successful
		if (fileCopy.exists()) {
			// add file copy to database
			return fileRecordService.save(fileCopy) != null ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus renameFile(String path, String newName) {
		var file = new File(path);
		var parentFolder = Paths.get(path).getParent().toFile().getPath();

		return file.renameTo(new File(parentFolder + "\\" + newName))
				? fileRecordUpdater.updateName(path, newName)
				: HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
    public HttpStatus deleteFile(String path) {
    	var file = new File(path);

		return file.delete()
    			? fileRecordService.delete(path)
    			: HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
	 * Save {@link FilePart} to disk then add record to database
	 *
	 * @param filePart	{@link FilePart} from request
	 * @param path 		path to folder where to save file
	 */
	private boolean writeFilePart(FilePart filePart, String path) {
		var file = new File(path + "\\" + filePart.filename());

		try {
			if (file.createNewFile()) {
				// transfer incoming file data to local file
                filePart.transferTo(file).subscribe();
            }
			return saveFileRecord(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return false;
	}

	/**
	 * add record of file from filesystem to database
	 *
	 * @param file 		file for database record
	 * @return boolean depending on success
	 */
	private boolean saveFileRecord(File file) {
		return fileRecordService.save(file) != null;
	}
}
