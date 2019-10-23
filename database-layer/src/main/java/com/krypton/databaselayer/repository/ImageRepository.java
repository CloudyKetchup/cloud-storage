package com.krypton.databaselayer.repository;

import com.krypton.databaselayer.model.File;
import com.krypton.databaselayer.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {

    @Query(value = "select * from image where image.entity = :entity", nativeQuery = true)
    Optional<Image> getByEntity(@Param("entity") File entity);
}
