package app.constructor.csdk.files

interface FileSystem {
    fun exists(path: String): Boolean

    fun delete(path: String): Boolean

    fun move(sourcePath: String, destPath: String): Boolean

    fun createFile(path: String): Boolean

    fun readText(path: String): String

    fun writeText(path: String, content: String)

    fun openWriter(path: String, append: Boolean = false): FileWriter

    fun getAppDirectory(): String

    fun createTempDir(): String
}

class FileSystemException(message: String, cause: Throwable? = null) : Exception(message, cause)

@Suppress("FunctionName")
expect fun FileSystem(): FileSystem
