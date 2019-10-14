package com.krypton.storagelayer.service.filesystem;

import java.io.File;
import java.io.IOException;

/**
 * service for file and folder on filesystem
 */
public interface FileSystemService {

    /**
     * move entity from one folder to another
     * 
     * @param newPath   path where entity needs to be moved
     * @return http status depending on success
     */
    boolean move(File file, String newPath);

    /**
     * copy entity from one folder to another
     * 
     * @return http status depending on success
     */
    boolean copy(File file, File destFile) throws IOException;

    /**
     * rename entity
     * 
     * @param file      file or folder to remove
     * @param newName   new entity name
     * @return http status depending on success
     */
    boolean rename(File file, String newName);

    /**
     * delete entity, find by path
     * 
     * @param   path    entity path
     * @return http status depending on success
     */
    boolean delete(String path);
}