package com.krypton.cloud.service.deliver;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {

	// root folder
	private final Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/cloud/")
			.toAbsolutePath().normalize();

	public void createZip(String folder) {
		var fileList = Arrays.asList(new File(fileStorageLocation.toFile() + "/" + folder).listFiles());

		// write zip to with all files and directories
		writeDirectoryToZip(new File(fileStorageLocation.toString() + "/" + folder), fileList);
	}

	/**
	 * write zip with all files to specified directory
	 *
	 * @param directory           directory for zipping
	 * @param fileList            list of files for zip
	 */
	private void writeDirectoryToZip(File directory, List<File> fileList) {
		try {
			var zos = new ZipOutputStream(
					new FileOutputStream(File.createTempFile(directory.getName() + ".zip",".tmp"))
			);
			for (var file : fileList) {
				// only zip files, not directories
				if (!file.isDirectory()) addToZip(directory, file, zos);
			}
			zos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * add file to zip
	 *
	 * @param file 		file to be added to zip
	 * @param zos 		zip output stream
	 */
	private void addToZip(File directory, File file, ZipOutputStream zos) throws IOException {
		var fis = new FileInputStream(file);

		var zipEntry = new ZipEntry(file.getCanonicalPath().substring(directory.getCanonicalPath().length() + 1));
		
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