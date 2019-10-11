package com.krypton.databaselayer.repository;

import com.krypton.databaselayer.model.File;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends EntityRepository<File, UUID> {

    @Override
    @Query(value = "select * from file where file.path = :path", nativeQuery = true)
    Optional<File> findByPath(@Param("path") String path);

    @Override
    @Query(value = "delete * from file where file.path = :path", nativeQuery = true)
    boolean deleteByPath(String path);

    @Query(value = "select * from file where file.path = :path", nativeQuery = true)
    File getByPath(@Param("path") String path);
}