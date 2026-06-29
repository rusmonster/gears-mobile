@file:Suppress("ktlint:constructor:constants-at-top")

package app.constructor.csdk.zip.impl

import app.constructor.csdk.zip.api.ZipPacker
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import no.synth.kmpzip.io.OutputStream
import no.synth.kmpzip.zip.ZipEntry
import no.synth.kmpzip.zip.ZipOutputStream
import platform.Foundation.NSString
import platform.Foundation.lastPathComponent
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fwrite

actual fun ZipPacker(): ZipPacker = ZipPackerImpl()

private const val BYTES_BUFFER_SIZE = 100 * 1024 // 100Kb

@OptIn(ExperimentalForeignApi::class)
internal class ZipPackerImpl : ZipPacker {
    override fun pack(inputFilePaths: List<String>, outputPath: String) {
        val buffer = ByteArray(BYTES_BUFFER_SIZE)

        ZipOutputStream(PosixFileOutputStream(outputPath)).use { zos ->
            for (inputFile in inputFilePaths) {
                @Suppress("CAST_NEVER_SUCCEEDS")
                val name = (inputFile as NSString).lastPathComponent
                zos.putNextEntry(ZipEntry(name))
                zos.writeFile(inputFile, buffer)
                zos.closeEntry()
            }
        }
    }

    private fun OutputStream.writeFile(path: String, buffer: ByteArray) {
        val file = fopen(path, "rb") ?: error("File not found: $path")
        try {
            buffer.usePinned { pinned ->
                while (true) {
                    val bytesRead = fread(pinned.addressOf(0), 1u, buffer.size.toULong(), file)
                    if (bytesRead == 0uL) break
                    this.write(buffer, 0, bytesRead.toInt())
                }
            }
        } finally {
            fclose(file)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PosixFileOutputStream(path: String) : OutputStream() {
    private val file = fopen(path, "wb") ?: error("Cannot open file for writing: $path")

    override fun write(b: Int) {
        val byte = byteArrayOf(b.toByte())
        byte.usePinned { fwrite(it.addressOf(0), 1u, 1u, file) }
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (len == 0) return
        b.usePinned { fwrite(it.addressOf(off), 1u, len.toULong(), file) }
    }

    override fun close() {
        fclose(file)
    }
}
