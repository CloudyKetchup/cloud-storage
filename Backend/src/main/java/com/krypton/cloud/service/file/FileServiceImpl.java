package com.krypton.cloud.service.file;

import com.krypton.cloud.service.file.record.FileRecordServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class FileServiceImpl implements FileService {

	private final FileRecordServiceImpl fileRecordService;

	@Override
	public Mono<HttpStatus> saveFiles(Flux<FilePart> files, String folder) {
		return files.subscribeOn(Schedulers.parallel())
				.map(it -> {
				    var file = new File(folder + "\\" + it.filename());

				    it.transferTo(file);
                    return fileRecordService.addFile(file);
                })
                .then(Mono.just(HttpStatus.OK));
	}

	@Override
	public Mono<HttpStatus> saveFile(Mono<FilePart> filePart, String folder) {
        // add file record to database
        return filePart.subscribeOn(Schedulers.parallel())
                .map(it -> {
                    var file = new File(folder + "\\"  + it.filename());

                    it.transferTo(file);
                    return fileRecordService.addFile(file);
                })
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
		var baseFolder = "C:\\Users\\dodon\\cloud" + "\\" + folder + "\\";
		
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