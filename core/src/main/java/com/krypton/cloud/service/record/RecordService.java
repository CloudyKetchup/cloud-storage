package com.krypton.cloud.service.record;

import java.io.File;

/**
 * Standard record service
 */
public interface RecordService<T> {

	/**
	 * Get entity by it's id
	 *
	 * @param id 	entity id
	 * @return entity
	 */
    T getById(Long id);

    /**
     * Get entity by path,related to files and folders
     *
     * @param path 	entity path
     * @return entity
     */
    T getByPath(String path);

    /**
     * Makes a record of file or folder
     *
     * @param entity 	file or folder from filesystem
     * @return saved entity
     */
    T save(File entity);

    /**
     * Delete entity record from database, use path find entity because it's unique
     *
     * @param path 		entity path
     * @return result of delete
     */
    T delete(String path);

 	/**
 	 * Check if entity exists in database
 	 *
 	 * @param path 		entity path
 	 * @return boolean depending on if entity exists in database
 	 */
    boolean exists(String path);
}
