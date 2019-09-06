package com.krypton.cloud.repository;

import com.krypton.cloud.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    @Query(value = "select * from file where file.path = :path", nativeQuery = true)
    Optional<File> getWithOptional(@Param("path") String path);

    @Query(value = "select * from file where file.path = :path", nativeQuery = true)
    File getByPath(@Param("path") String path);
}