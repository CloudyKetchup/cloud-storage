package com.krypton.cloud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import common.model.EntityType;
import common.tools.CommonTools;
import util.file.FileTools;
import util.folder.FolderTools;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;

import java.time.LocalDateTime;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private UUID id;

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

        // check if runs inside docker container and this is root folder
        if (this.path.equals(CommonTools.INSTANCE.runsInsideContainer()
                ? "/Cloud/Storage"
                : System.getProperty("user.home") + "/Cloud/Storage")
        ) {
            root = true;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public Boolean getRoot() {
        return root;
    }

    public void setRoot(Boolean root) {
        this.root = root;
    }

    public Set<Folder> getFolders() {
        return folders;
    }

    public void setFolders(Set<Folder> folders) {
        this.folders = folders;
    }

    public Set<File> getFiles() {
        return files;
    }

    public void setFiles(Set<File> files) {
        this.files = files;
    }
}