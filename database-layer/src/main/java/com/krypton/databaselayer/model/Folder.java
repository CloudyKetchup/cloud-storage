package com.krypton.databaselayer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import common.model.EntityType;
import common.tools.CommonTools;
import lombok.ToString;
import util.file.FileTools;
import util.folder.FolderTools;

import javax.persistence.*;
import javax.persistence.Entity;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@ToString(callSuper = true)
public class Folder extends BaseEntity {

    @Column
    private Boolean root = false;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parentId")
    @JsonIgnore
    private Set<Folder> folders = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parentId")
    @JsonIgnore
    private Set<File> files = new HashSet<>();

    public Folder() {}

    public Folder(java.io.File folder) {
        var time = LocalDateTime.now();

        setName(folder.getName());
        setPath(folder.getPath());
        setType(EntityType.FOLDER);
        setLocation(Paths.get(folder.getPath()).getParent().toFile().getName());
        setTimeCreated(time.getDayOfMonth() + "-" + time.getMonthValue() + "-" + time.getYear());
        setSize(FileTools.INSTANCE.getFileSize(FolderTools.INSTANCE.getFolderLength(folder)));

        // check if runs inside docker container and this is root folder
        if (this.getPath().equals(CommonTools.INSTANCE.runsInsideContainer()
                ? "/Cloud/Storage"
                : System.getProperty("user.home") + "/Cloud/Storage")
        ) {
            root = true;
        }
    }

    public Boolean getRoot() { return root; }

    public void setRoot(Boolean root) { this.root = root; }

    public Set<Folder> getFolders() { return folders; }

    public void setFolders(Set<Folder> folders) { this.folders = folders; }

    public Set<File> getFiles() { return files; }

    public void setFiles(Set<File> files) { this.files = files; }
}