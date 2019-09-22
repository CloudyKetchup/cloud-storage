package com.krypton.cloud.service

import org.springframework.http.HttpStatus
import java.util.*

/**
 * service for file and folder on filesystem
 */
interface IOEntityService {

    /**
     * move entity from one folder to another
     * 
     * @param oldPath   path where entity is located
     * @param newPath   path where entity needs to be moved
     * @return http status depending on success
     */
    fun move(oldPath: String, newPath: String) : HttpStatus

    /**
     * copy entity from one folder to another
     * 
     * @param oldPath   path where entity is located
     * @param newPath   path for entity copy
     * @return http status depending on success
     */
    fun copy(oldPath: String, newPath: String) : HttpStatus

    /**
     * rename entity
     * 
     * @param path      entity path
     * @param newName   new entity name
     * @return http status depending on success
     */
    fun rename(path: String, newName: String) : HttpStatus

    /**
     * delete entity, find by path
     * 
     * @param   path    entity path
     * @return http status depending on success
     */
    fun delete(path: String) : HttpStatus
}