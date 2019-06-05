package com.krypton.cloud.model;

import com.krypton.cloud.model.common.EntityType;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class File {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@Column
	private String path;

	@Column
	private EntityType type = EntityType.FILE;

	@Column
	private long timeUploaded;

	@Column
	private long lastTimeAccessed;

	@Column
	private float size;

	public File() {}

	public File(java.io.File file) {
		this.path = file.getPath();
		this.name = file.getName();
		this.size = (float) file.length() /1024 /1024;
	}
}