package com.krypton.cloud.model;

import common.model.EntityType;
import common.model.FileType;
import util.file.FileTools;
import org.apache.commons.io.FileUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file")
public class File {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "id",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(columnDefinition = "BINARY(16)")
	private UUID id;

	@Column
	private String name;

	@Column
	private String path;

	@Column
	private String location;

	@Column
	private EntityType type = EntityType.FILE;

	@Column
	private FileType extension;

	@Column
	private String timeCreated;

	@Column
	private String size;

	public File() {}

	public File(java.io.File file) {
		var time = LocalDateTime.now();

		name = file.getName();
		path = file.getPath();
		timeCreated = time.getDayOfMonth() + "-" + time.getMonthValue() + "-" + time.getYear();
		location = file.getParentFile().getName();
		size = FileTools.INSTANCE.getFileSize(FileUtils.sizeOf(file));

		setExtension(FileTools.INSTANCE.getFileExtension(file));
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public EntityType getType() {
		return type;
	}

	public void setType(EntityType type) {
		this.type = type;
	}

	public FileType getExtension() {
		return extension;
	}

	public void setExtension(FileType extension) {
		this.extension = extension;
	}

	public String getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(String timeCreated) {
		this.timeCreated = timeCreated;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
}
