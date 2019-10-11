package com.krypton.databaselayer.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.krypton.databaselayer.model")
@EnableJpaRepositories(basePackages = "com.krypton.databaselayer.repository")
public class DatabaseLayerConfiguration {}
