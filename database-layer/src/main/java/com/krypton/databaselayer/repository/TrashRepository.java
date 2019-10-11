package com.krypton.databaselayer.repository;

import com.krypton.databaselayer.model.TrashEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrashRepository extends EntityRepository<TrashEntity, UUID> {

	@Override
	@Query(value = "select * from trash_entity where trash_entity.path = :path", nativeQuery = true)
	Optional<TrashEntity> findByPath(@Param("path") String path);

	@Override
	@Query(value = "delete * from trash_entity where trash_entity.path = :path", nativeQuery = true)
	boolean deleteByPath(String path);

	@Query(value = "select * from trash_entity where trash_entity.entity_id = :id", nativeQuery = true)
	Optional<TrashEntity> findByEntityId(@Param("id") UUID id);

	@Query(value = "select * from trash_entity", nativeQuery = true)
	List<TrashEntity> getAll();
}
