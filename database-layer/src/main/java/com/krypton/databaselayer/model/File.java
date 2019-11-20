package com.krypton.databaselayer.model;

import common.model.EntityType;
import common.model.FileType;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import util.file.FileTools;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@ToString(callSuper = true)
public class File extends BaseEntity {

	@Column
	private FileType extension;

	@Column
	private Boolean isMedia = false;

	public File() {}

	public File(java.io.File file) {
		var time = LocalDateTime.now();

		setName(file.getName());
		setPath(file.getPath());
		setType(EntityType.FILE);
		setTimeCreated(time.getDayOfMonth() + "-" + time.getMonthValue() + "-" + time.getYear());
		setLocation(file.getParentFile().getName());
		setSize(FileTools.INSTANCE.getFileSize(FileUtils.sizeOf(file)));
		setExtension(FileTools.INSTANCE.getFileExtension(file));
		setIsMedia(FileTools.INSTANCE.isMediaFile(file));
	}

	public FileType getExtension() { return extension; }

	public void setExtension(FileType extension) { this.extension = extension; }

	public Boolean getIsMedia() { return isMedia; }

	public void setIsMedia(Boolean isMedia) { this.isMedia = isMedia; }
}
