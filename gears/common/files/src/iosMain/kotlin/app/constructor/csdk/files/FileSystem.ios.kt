package app.constructor.csdk.files

import kotlin.random.Random
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.seekToEndOfFile
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile

@Suppress("FunctionName")
actual fun FileSystem(): FileSystem = IosFileSystem()

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
internal class IosFileSystem : FileSystem {
    private val fileManager = NSFileManager.defaultManager

    override fun exists(path: String): Boolean {
        return fileManager.fileExistsAtPath(path)
    }

    override fun delete(path: String): Boolean {
        return try {
            fileManager.removeItemAtPath(path, null)
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun move(sourcePath: String, destPath: String): Boolean {
        return try {
            fileManager.moveItemAtPath(sourcePath, destPath, null)
            true
        } catch (_: Exception) {
            false
        }
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun createFile(path: String): Boolean {
        return try {
            ("" as NSString).writeToFile(
                path,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null,
            )
        } catch (_: Exception) {
            false
        }
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun readText(path: String): String {
        val data = fileManager.contentsAtPath(path)
            ?: throw FileSystemException("File not found: $path")
        return (NSString.create(data, NSUTF8StringEncoding) ?: "") as String
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun writeText(path: String, content: String) {
        val success = (content as NSString).writeToFile(
            path,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null,
        )
        if (!success) {
            throw FileSystemException("Failed to write file: $path")
        }
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun openWriter(path: String, append: Boolean): FileWriter {
        if (!fileManager.fileExistsAtPath(path)) {
            createFile(path)
        }

        val handle = NSFileHandle.fileHandleForWritingAtPath(path)
            ?: throw FileSystemException("Failed to open file for writing: $path")

        if (append) {
            handle.seekToEndOfFile()
        }

        return IosFileWriter(handle)
    }

    override fun createTempDir(): String {
        val dir = NSTemporaryDirectory() + "tmp-${Random.nextLong()}"
        fileManager.createDirectoryAtPath(
            path = dir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        return dir
    }

    override fun getAppDirectory(): String {
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val path = (urls.first() as NSURL).path ?: ""
        if (path.isNotEmpty() && !fileManager.fileExistsAtPath(path)) {
            fileManager.createDirectoryAtPath(path, withIntermediateDirectories = true, attributes = null, error = null)
        }
        return path
    }

    override fun listFiles(dirPath: String): List<String> {
        val names = fileManager.contentsOfDirectoryAtPath(dirPath, null) ?: return emptyList()
        return names.filterIsInstance<String>().map { "$dirPath/$it" }
    }

    override fun lastModifiedMillis(path: String): Long {
        val attributes = fileManager.attributesOfItemAtPath(path, null) ?: return 0L
        val modified = attributes[NSFileModificationDate] as? NSDate ?: return 0L
        return (modified.timeIntervalSince1970 * 1000).toLong()
    }
}
