package com.krypton.cloud.service.file;

import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class FileServiceImpl implements FileService {

	private final FileRecordServiceImpl fileRecordService;

	@Override
	public Flux<HttpStatus> saveFiles(Flux<FilePart> files, String path) {
		return files.flatMap(file -> Flux.just(writeFilePart(file, path) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Override
	public Mono<HttpStatus> saveFile(Mono<FilePart> filePart, String path) {
		return filePart.flatMap(file -> Mono.just(writeFilePart(file, path) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Override
	public Resource getFile(String path) {
		Resource resource = null;

		try {
			resource = new UrlResource(path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return resource;
	}

	@Override
	public HttpStatus renameFile(String path, String newName) {
		var file = new File(path);
		var parentFolder = Paths.get(path).getParent().toFile().getPath();

		return file.renameTo(new File(parentFolder + "\\" + newName))
				? fileRecordService.renameFile(path, newName)
				: HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
    public HttpStatus deleteFile(String path) {
    	var file = new File(path);

		return file.delete()
    			? fileRecordService.deleteFileRecord(path)
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
			// create file locally on disk
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
	 */
	private boolean saveFileRecord(File file) {
		return fileRecordService.addFileRecord(file) != null;
	}
}
