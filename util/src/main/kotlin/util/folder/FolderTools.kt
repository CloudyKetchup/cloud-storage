package util.folder

import java.io.File

object FolderTools {

    fun getFolderLength(folder : File) : Long {
        val length = LongArray(1)

        for (child in folder.listFiles()!!) {
            if (child.isFile)
                length[0] += child.length()
            else if (child.isDirectory)
                length[0] += getFolderLength(child)
        }
        return length[0]
    }
}