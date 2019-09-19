package com.krypton.cloud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.krypton.cloud.model.common.EntityType;
import com.krypton.cloud.service.util.file.FileTools;
import com.krypton.cloud.service.util.folder.FolderTools;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.beans.factory.annotation.Value;

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
    private String size;

    @Column(columnDefinition = "BINARY(16)")
    private UUID parentId;

    @Column
    private Boolean root = false;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "parentId")
    @JsonIgnore
    private Set<Folder> folders = new HashSet<>();
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "parentId")
    @JsonIgnore
    private Set<File> files = new HashSet<>();

    public Folder() {}

    public Folder(java.io.File folder) {
        var time = LocalDateTime.now();

        name        = folder.getName();
        path        = folder.getPath();
        location    = Paths.get(folder.getPath()).getParent().toFile().getName();
        timeCreated = time.getDayOfMonth() + "-" + time.getMonthValue() + "-" + time.getYear();
        size        = FileTools.INSTANCE.getFileSize(FolderTools.INSTANCE.getFolderLength(folder));

        if (this.path.equals(System.getProperty("user.home") + "/Desktop/Cloud")) {
            root = true;
        }
    }
}