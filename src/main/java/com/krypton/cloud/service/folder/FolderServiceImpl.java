package com.krypton.cloud.service.folder;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FolderServiceImpl implements FolderService {

	// root folder
	private final Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/cloud/")
			.toAbsolutePath().normalize();

	@Override
	public String[] getRootFilesList() {
		return new File(fileStorageLocation.toString()).list();
	}

	@Override
	public String[] getFolderContent(String folder) {
		return new File(fileStorageLocation + folder).list();
	}

	@Override
	public HttpStatus createFolder(String folderName, String folderPath) {
		return new File(fileStorageLocation + folderPath + folderName).mkdir()
                ? HttpStatus.OK
                : HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus renameFolder(String folder, String newName) {
	    var dir = new File(folder);
	    return dir.renameTo(new File(dir.getParent() + "/" + newName))
	    		? HttpStatus.OK
	    		: HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public HttpStatus deleteFolder(String folder) {
        var f = new File(fileStorageLocation + "/" + folder);
        
        // delete files first
        for (File file : Objects.requireNonNull(f.listFiles())) {
            // if file is directory call function recursive to all files from inside
            if (file.isDirectory()) {
            	deleteFolder(file.getAbsolutePath());
            }
			file.delete();
        }
        return f.delete() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public void createZip(String folder) {
		var fileList = Arrays.asList(new File(fileStorageLocation.toFile() + "/" + folder).listFiles());

		// write zip to with all files and directories
		writeZipFile(new File(fileStorageLocation.toString() + "/" + folder), fileList);
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

	/**
	 * write zip with all files to specified directory
	 *
	 * @param directoryToZip      directory for zip to be writed
	 * @param fileList            list of files for zip
	 */
	private void writeZipFile(File directoryToZip, List<File> fileList) {
		try {
			var zos = new ZipOutputStream(
					new FileOutputStream(File.createTempFile(directoryToZip.getName() + ".zip",".tmp"))
			);
			for (var file : fileList) {
				// only zip files, not directories
				if (!file.isDirectory()) addToZip(directoryToZip, file, zos);
			}
			zos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * add file to zip and write
	 *
	 * @param file 		file to be added to zip
	 * @param zos 		zip output stream
	 */
	private void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws IOException {
		var fis = new FileInputStream(file);

		var zipEntry = new ZipEntry(file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1));
		
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}
		zos.closeEntry();
		fis.close();
	}
}
