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

    /** Returns the absolute paths of the entries directly inside [dirPath] (empty if none/not a dir). */
    fun listFiles(dirPath: String): List<String>

    /** Returns the last-modified time of [path] as epoch milliseconds, or `0` if unavailable. */
    fun lastModifiedMillis(path: String): Long
}

class FileSystemException(message: String, cause: Throwable? = null) : Exception(message, cause)

@Suppress("FunctionName")
expect fun FileSystem(): FileSystem
