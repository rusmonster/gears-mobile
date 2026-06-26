package app.constructor.csdk.files

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files

internal open class BaseJvmFileSystem : FileSystem {
    override fun exists(path: String): Boolean {
        return File(path).exists()
    }

    override fun delete(path: String): Boolean {
        return File(path).deleteRecursively()
    }

    override fun move(sourcePath: String, destPath: String): Boolean {
        return File(sourcePath).renameTo(File(destPath))
    }

    override fun createFile(path: String): Boolean {
        return try {
            File(path).createNewFile()
        } catch (_: Exception) {
            false
        }
    }

    override fun readText(path: String): String {
        return try {
            File(path).readText(Charsets.UTF_8)
        } catch (e: Exception) {
            throw FileSystemException("Failed to read file: $path", e)
        }
    }

    override fun writeText(path: String, content: String) {
        try {
            File(path).writeText(content, Charsets.UTF_8)
        } catch (e: Exception) {
            throw FileSystemException("Failed to write file: $path", e)
        }
    }

    override fun openWriter(path: String, append: Boolean): FileWriter {
        return try {
            val outputStream = FileOutputStream(File(path), append)
            val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)
            JvmFileWriter(writer)
        } catch (e: Exception) {
            throw FileSystemException("Failed to open writer: $path", e)
        }
    }

    open override fun getAppDirectory(): String {
        return System.getProperty("java.io.tmpdir") ?: "/tmp"
    }

    override fun createTempDir(): String = Files.createTempDirectory("tmp").toString()
}
