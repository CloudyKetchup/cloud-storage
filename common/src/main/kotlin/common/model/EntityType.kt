package common.model

enum class EntityType(private val type: String) {

    FILE("File"),
    FOLDER("Folder");

    fun equalsType(type: String): Boolean {
        return this.type == type
    }

    override fun toString(): String {
        return this.type
    }
}