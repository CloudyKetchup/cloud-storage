package com.krypton.cloud.repository;

import com.krypton.cloud.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    @Deprecated
    @Query(value = "select * from file where file.name = :name", nativeQuery = true)
    Optional<File> getByName(@Param("name") String name);

    @Query(value = "select * from file where file.path = :path", nativeQuery = true)
    File getByPath(@Param("path") String path);
}