package com.krypton.cloud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.krypton.cloud.model.common.EntityType;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "folder")
public class Folder {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "id",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(columnDefinition = "BINARY(16)")
    public UUID id;

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

    @Column(columnDefinition = "BINARY(16)")
    private UUID parentId;

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

        if(this.path.equals(System.getProperty("user.home") + "/Desktop/Cloud")) {
            root = true;
        }
    }
}