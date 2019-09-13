package com.krypton.cloud.model;

public enum FileType {
	
	IMAGE_JPG("JPG"),
	IMAGE_PNG("PNG"),
	IMAGE_RAW("RAW"),
	IMAGE_GIF("GIF"),
	ARCHIVE_ZIP("ZIP"),
	ARCHIVE_TAR("TAR"),
	ARCHIVE_7Z("7z"),
	ARCHIVE_RAR("RAR"),
	MKV("MKV"),
	MP3("MP3"),
	MP4("MP4"),
	TXT("TXT"),
	VIM("VIM"),
	JAR("JAR"),
	EXE("EXE"),
	BAT("BAT"),
	DMG("DMG"),
	APK("APK"),
	IMG_EXEC("IMAGE"),
	SHELL("SH"),
	JAVA("JAVA"),
	KOTLIN("KT"),
	HTML("HTML"),
	CSS("CSS"),
	SCSS("SCSS"),
	JAVASCRIPT("JS"),
	TYPESCRIPT("TS"),
	JSX("JSX"),
	TSX("TSX"),
	GRADLE("GRADLE"),
	JSON("JSON"),
	XML("XML"),
	YML("YML"),
	OTHER("OTHER");

	private final String type;

	public String getType() {
		return type;
	}

	FileType(String type) {
		this.type = type;
	}

	public boolean equalsType(String type) {
		return this.type.equals(type);
	}

	public String toString() {
		return this.type;
	}
}
