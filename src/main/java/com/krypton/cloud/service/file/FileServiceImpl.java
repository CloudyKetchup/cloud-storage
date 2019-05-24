package com.krypton.cloud.service.file;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileServiceImpl implements FileService {

	// root folder
	private final Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/cloud/")
			.toAbsolutePath().normalize();

	@Override
	public Mono<HttpStatus> saveFiles(Flux<FilePart> files, String folder) {
		return files.flatMap(it -> it.transferTo(new File(folder + it.filename())))
				.then(Mono.just(HttpStatus.OK));
	}

	@Override
	public Mono<HttpStatus> saveFile(Mono<FilePart> file, String foler) {
		return file.map(it -> it.transferTo(new File(foler + it.filename())))
				.then(Mono.just(HttpStatus.OK));
	}

	@Override
	public Resource getFile(String file, String folder) {
		Resource resource = null;

		var filePath = Paths.get(folder).resolve(file).normalize().toUri();

		try {
			resource = new UrlResource(filePath);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		assert resource != null;

		return resource;
	}

	@Override
	public HttpStatus renameFile(String file, String folder, String newName) {
		var baseFolder = fileStorageLocation + "/" + folder + "/";
		
		return new File(baseFolder + file).renameTo(new File(baseFolder + newName))
				? HttpStatus.OK
				: HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
    public HttpStatus deleteFile(String file, String folder) {
    	return new File(folder + "/" + file).delete()
    			? HttpStatus.OK
    			: HttpStatus.INTERNAL_SERVER_ERROR;
    }
}