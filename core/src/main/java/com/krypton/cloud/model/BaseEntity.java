package com.krypton.cloud.model;

import common.model.EntityType;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
public class BaseEntity {

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

    public UUID getId() { return id; }

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
}
