package com.krypton.databaselayer.repository;

import com.krypton.databaselayer.model.Folder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends EntityRepository<Folder, UUID> {

    @Override
    @Query(value = "select * from folder where folder.path = :path", nativeQuery = true)
    Optional<Folder> findByPath(@Param("path") String path);

    @Override
    @Query(value = "delete * from folder where folder.path = :path", nativeQuery = true)
    boolean deleteByPath(String path);

    @Query(value = "select * from folder where folder.path = :path", nativeQuery = true)
    Folder getByPath(@Param("path") String path);
}