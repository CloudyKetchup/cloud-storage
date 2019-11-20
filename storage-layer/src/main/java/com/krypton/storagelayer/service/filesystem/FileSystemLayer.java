package com.krypton.storagelayer.service.filesystem;

import org.apache.commons.io.FileUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.io.File;
import java.io.IOException;

@Service
public class FileSystemLayer implements FileSystemService {

    @Override
    public boolean move(File file, String newPath) { return file.renameTo(new File(newPath)); }

    @Override
    public boolean copy(File file, File destFile) throws IOException {
        if (file.isDirectory())
            FileUtils.copyDirectory(file, destFile);
        else
            FileUtils.copyFile(file, destFile);
        return destFile.exists();
    }

    @Override
    public boolean rename(File file, String newName) {
        var parentFolder = file.getParentFile().getAbsolutePath();

        return file.renameTo(new File(parentFolder + "/" + newName));
    }

    @Override
    public boolean delete(String path) { return new File(path).delete(); }

    @Nullable
    public Mono<File> writeFilePart(FilePart filePart, String path) {
        var file = new File(path);

        return Mono.just(file).map(f -> {
            try {
                return f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }).flatMap(result -> filePart.transferTo(file).onErrorReturn(null).thenReturn(file));
    }


}
