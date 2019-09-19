package com.krypton.cloud.model;

import com.krypton.cloud.model.common.EntityType;
import com.krypton.cloud.service.util.file.FileTools;
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
		size = FileTools.INSTANCE.getFileSize(FileUtils.sizeOf(file));

		setExtension(FileTools.INSTANCE.getFileExtension(file));
	}
}
