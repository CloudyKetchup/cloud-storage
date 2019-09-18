package com.krypton.cloud.repository;

import com.krypton.cloud.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    @Query(value = "select * from folder where folder.path = :path", nativeQuery = true)
    Folder getByPath(@Param("path") String path);
}