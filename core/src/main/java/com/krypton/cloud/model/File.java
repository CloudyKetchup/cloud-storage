package com.krypton.cloud.model;

import com.krypton.cloud.model.common.EntityType;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.EnumSet;

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
	private String location;

	@Column
	private EntityType type = EntityType.FILE;

	@Column
	private FileType fileType;

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


		setFileType(getFileType(file));
	}

	private FileType getFileType(java.io.File file) {
		return EnumSet.allOf(FileType.class)
				.stream()
				.filter(e -> e.getType().toLowerCase().equals(FilenameUtils.getExtension(file.getName())))
				.findAny()
				.orElse(FileType.OTHER);
	}
}
