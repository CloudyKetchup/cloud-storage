package com.krypton.cloud.repository;

import com.krypton.cloud.model.TrashEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface TrashRepository extends JpaRepository<TrashEntity, UUID> {

    @Query(value = "select * from trash_entity where trash_entity.entity_id = :id", nativeQuery = true)
    Optional<TrashEntity> findByEntityId(@Param("id") UUID id);

	@Query(value = "select * from trash_entity where trash_entity.path = :path", nativeQuery = true)
	Optional<TrashEntity> findByEntityPath(@Param("path") String path);

	@Query(value = "select * from trash_entity", nativeQuery = true)
    List<TrashEntity> getAll();
}
