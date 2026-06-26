package app.constructor.csdk.zip.impl

import app.constructor.csdk.zip.api.ZipPacker
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal class ZipPackerImpl : ZipPacker {
    override fun pack(inputFilePaths: List<String>, outputPath: String) {
        ZipOutputStream(FileOutputStream(outputPath)).use { zip ->
            for (path in inputFilePaths) {
                val file = File(path)
                if (!file.exists()) error("File not found: $path")
                FileInputStream(file).use { input ->
                    zip.putNextEntry(ZipEntry(file.name))
                    input.copyTo(zip)
                    zip.closeEntry()
                }
            }
        }
    }
}

actual fun ZipPacker(): ZipPacker = ZipPackerImpl()
