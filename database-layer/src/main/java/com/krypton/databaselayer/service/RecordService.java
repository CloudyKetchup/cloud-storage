package com.krypton.databaselayer.service;

import java.util.UUID;

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
    T getById(UUID id);

    /**
     * Makes a record of file or folder
     *
     * @param entity 	file or folder from filesystem
     * @return saved entity
     */
    T save(T entity);

    /**
     * Delete entity record from database, use path find entity because it's unique
     *
     * @param path 		entity path
     * @return result of delete
     */
    boolean delete(String path);

 	/**
 	 * Check if entity exists in database
 	 *
 	 * @param path 		entity path
 	 * @return boolean depending on if entity exists in database
 	 */
    boolean exists(String path);
}
