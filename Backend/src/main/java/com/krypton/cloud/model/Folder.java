package com.krypton.cloud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.krypton.cloud.model.common.EntityType;
import lombok.Data;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class Folder {

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
	private EntityType type = EntityType.FOLDER;

	@Column
	private String timeCreated;

	@Column
	private Float size = 0f;

	@Column
	private Integer parentId;

	@Column
	private Boolean root = false;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name= "parentId")
	@JsonIgnore
	private Set<Folder> folders = new HashSet<>();
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name= "parentId")
	@JsonIgnore
	private Set<File> files = new HashSet<>();

	public Folder() {}

	public Folder(java.io.File folder) {
		var time = LocalDateTime.now();

		this.name = folder.getName();
		this.path = folder.getPath();
		this.location = Paths.get(folder.getPath()).getParent().toFile().getName();
		this.timeCreated = time.getDayOfMonth() + "-" + time.getMonthValue() + "-" + time.getYear();

		if(this.path.equals("C:\\Users\\dodon\\cloud")) {
			root = true;
		}
	}
}