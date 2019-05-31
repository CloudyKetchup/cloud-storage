package com.krypton.cloud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.krypton.cloud.model.common.EntityType;
import lombok.Data;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class Folder {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@Column
	private String name;

	@Column
	private String path;

	@Column
	private EntityType type = EntityType.FOLDER;

	@Column
	private long timeUploaded;

	@Column
	private long lastTimeAccessed;

	@Column
	private float size = 0;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonIgnore
	private Set<Folder> folders = new HashSet<>();
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonIgnore
	private Set<File> files = new HashSet<>();

	public Folder() {}

	public Folder(java.io.File folder) {
		this.path = folder.getPath();
		this.name = folder.getName();
	}
}