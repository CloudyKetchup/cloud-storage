package com.krypton.databaselayer.model;

import common.model.EntityType;
import lombok.ToString;

import javax.persistence.*;
import java.util.UUID;

@MappedSuperclass
@ToString
public abstract class BaseEntity extends Entity {

    @Column(columnDefinition = "BINARY(16)")
    private UUID parentId;

    @Column
    private String name;

    @Column(unique = true)
    private String path;

    @Column
    private String location;

    @Column
    private EntityType type;

    @Column
    private String timeCreated;

    @Column
    private String size;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }

    public void setPath(String path) { this.path = path; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public EntityType getType() { return type; }

    public void setType(EntityType type) { this.type = type; }

    public String getTimeCreated() { return timeCreated; }

    public void setTimeCreated(String timeCreated) { this.timeCreated = timeCreated; }

    public String getSize() { return size; }

    public void setSize(String size) { this.size = size; }

    public UUID getParentId() { return parentId; }

    public void setParentId(UUID parentId) { this.parentId = parentId; }
}
