package com.krypton.databaselayer.service;

import reactor.util.annotation.Nullable;

import java.util.UUID;

/**
 * Standard record service
 */
public interface RecordService<T> {

	/**
	 * Get entity by it's id
	 *
	 * @param id 	entity id
	 * @return {@link T} or <code>null</code> if nothing was found
	 */
	@Nullable
    T getById(UUID id);

    /**
     * Makes a record of file or folder and save it to database
     *
     * @param entity 	file or folder from filesystem
     * @return saved {@link T}
     */
    T save(T entity);

    /**
     * Delete entity record from database, use path to find entity because it's unique
     *
     * @param id	entity id
     * @return boolean
     */
    boolean delete(UUID id);

 	/**
 	 * Check if entity exists in database
 	 *
 	 * @param id 	entity id
 	 * @return boolean
 	 */
    boolean exists(UUID id);
}
