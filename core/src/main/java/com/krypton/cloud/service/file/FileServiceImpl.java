package com.krypton.cloud.service.file;

import com.krypton.storagelayer.service.filesystem.FileSystemLayer;
import com.krypton.databaselayer.service.file.FileRecordServiceImpl;
import com.krypton.databaselayer.service.file.updater.FileRecordUpdaterImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@AllArgsConstructor
public class FileServiceImpl implements FileService {

	private final FileRecordServiceImpl recordService;

	private final FileRecordUpdaterImpl recordUpdater;

	private final FileSystemLayer filesystemLayer;

	@Override
	public Mono<HttpStatus> saveFile(FilePart filePart, String path) {
		if (new File(path).exists()) return Mono.just(HttpStatus.INTERNAL_SERVER_ERROR);

		var file = filesystemLayer.writeFilePart(filePart, path);

		if (file != null) {
			return file.flatMap(recordService::createAndSave)
						.map(result -> result ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return Mono.just(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public HttpStatus move(String oldPath, String location) {
		var file 	 = new File(oldPath);
		var newPath  = location + "/" + file.getName();
		// move file to new location
		if (filesystemLayer.move(file, newPath)) {
			// remove file with old path from database
			recordService.delete(oldPath);
			// add file with new path to database
			recordService.save(new com.krypton.databaselayer.model.File(new File(newPath)));

			return HttpStatus.OK;
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus copy(String oldPath, String newPath) {
		var file = new File(oldPath);
		var copy = new File(newPath + "/" + file.getName());

		try {
			filesystemLayer.copy(file, copy);
			// check if file was copied successful
			if (copy.exists() && recordService.save(new com.krypton.databaselayer.model.File(copy)) != null)
				return HttpStatus.OK;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus rename(UUID id, String newName) {
	    var path 		 = recordService.getById(id).getPath();
		var parentFolder = Paths.get(path).getParent().toFile().getPath();

		return filesystemLayer.rename(new File(path), newName)
				? recordUpdater.updateName(path, new File(parentFolder + "/" + newName))
				: HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus delete(UUID id) {
		var path = recordService.getById(id).getPath();

		if (filesystemLayer.delete(path)) {
			return recordService.delete(path)
					? HttpStatus.OK
					: HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
}
