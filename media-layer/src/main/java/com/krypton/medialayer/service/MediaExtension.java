package com.krypton.medialayer.service;

public enum MediaExtension {

    JPG("JPG"),
    JPEG("JPEG"),
    PNG("PNG"),
    GIF("GIF"),
    AVI("AVI"),
    MOV("MOV"),
    MP4("MP4"),
    UNKNOWN;

    private String type;

    MediaExtension(String type) { this.type = type; }

    MediaExtension() {}

    public String getType() { return type; }
}
