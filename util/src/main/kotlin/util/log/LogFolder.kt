package util.log

enum class LogFolder(var type: String) {

    ROOT("/"),
    FOLDER("/Folder"),
    FILE("/File"),
    DATABASE("/Database"),
	IO("/Io");

}