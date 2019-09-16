package com.krypton.cloud.model;

import com.krypton.cloud.model.common.EntityType;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.persistence.*;
import java.text.DecimalFormat;
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
		size = getFileSize(FileUtils.sizeOf(file));

		setExtension(getFileExtension(file));
	}

	private String getFileSize(long size) {
		DecimalFormat df = new DecimalFormat("0.00");

		var sizeKb = 1024.0f;
		var sizeMb = sizeKb * sizeKb;
		var sizeGb = sizeMb * sizeKb;

		if (size < sizeMb)
			return df.format(size / sizeKb) + " Kb";
		else if (size < sizeGb)
			return df.format(size / sizeMb) + " Mb";
		else if (size < (sizeGb * sizeKb))
			return df.format(size / sizeGb) + " Gb";
		return "";
	}

	private FileType getFileExtension(java.io.File file) {
		return EnumSet.allOf(FileType.class)
				.stream()
				.filter(e -> e.getType().toLowerCase().equals(FilenameUtils.getExtension(file.getName())))
				.findAny()
				.orElse(FileType.OTHER);
	}
}
