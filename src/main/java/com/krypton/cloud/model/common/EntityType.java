package com.krypton.cloud.model.common;

public enum EntityType {

	FILE("File"),
    FOLDER("Folder");

    private final String type;

    EntityType(String type) {
        this.type = type;
    }

    public boolean equalsType(String type) {
        return this.type.equals(type);
    }

    public String toString() {
        return this.type;
    }
}