package com.krypton.cloud.model;

import com.krypton.cloud.model.common.EntityType;
import lombok.Data;

import javax.persistence.*;

import java.time.LocalDateTime;

@Data
@Entity
public class File {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = false)
	private String name;

	@Column
	private String path;

	@Column
	private String location;

	@Column
	private EntityType type = EntityType.FILE;

	@Column
	private String timeCreated;

	@Column
	private float size;

	public File() {}

	public File(java.io.File file) {
		var time = LocalDateTime.now();

		this.name = file.getName();
		this.path = file.getPath();
		this.size = (float) file.length() /1024 /1024;
		this.timeCreated = time.getDayOfMonth() + "-" + time.getMonthValue() + "-" + time.getYear();
	}
}