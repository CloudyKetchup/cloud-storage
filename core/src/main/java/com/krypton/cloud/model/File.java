package com.krypton.cloud.model;

import com.krypton.cloud.model.common.EntityType;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.UUID;

@Data
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
