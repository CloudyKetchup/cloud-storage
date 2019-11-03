package com.krypton.databaselayer.model;

import common.model.EntityType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.UUID;

@Entity
@ToString
@Table(name = "trash_entity")
@EqualsAndHashCode(callSuper = false)
public class TrashEntity extends com.krypton.databaselayer.model.Entity {

	@Column(columnDefinition = "BINARY(16)")
	private UUID entityId;

	@Column
	private String name;

	@Column
	private String path;

	@Column
	private EntityType type;

	@Column
	private String restoreFolder;

	public TrashEntity() {}

	public TrashEntity(BaseEntity entity, String restoreFolder) {
		this.entityId		= entity.getId();
		this.name 			= entity.getName();
		this.path 			= entity.getPath();
		this.type 			= entity.getType();
		this.restoreFolder 	= restoreFolder;
	}

	public UUID getEntityId() { return entityId; }

	public String getName() { return name; }

	public String getPath() { return path; }

	public EntityType getType() { return type; }

	public String getRestoreFolder() { return restoreFolder; }
}
