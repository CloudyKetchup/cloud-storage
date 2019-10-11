package com.krypton.databaselayer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@NoRepositoryBean
public interface EntityRepository<T, I> extends JpaRepository<T, I> {

    Optional<T> findByPath(@Param("path") String path);

    boolean deleteByPath(@Param("path") String path);
}
