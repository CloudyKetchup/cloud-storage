package com.krypton.databaselayer.model;

import common.model.EntityType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ToString
@Table(name = "trash_entity")
@EqualsAndHashCode(callSuper = false)
public class TrashEntity {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "id",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(columnDefinition = "BINARY(16)")
	private UUID id;

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

	public UUID getId() { return id; }

	public void setId(UUID id) { this.id = id; }

	public UUID getEntityId() { return entityId; }

	public String getName() { return name; }

	public String getPath() { return path; }

	public EntityType getType() { return type; }

	public String getRestoreFolder() { return restoreFolder; }
}
