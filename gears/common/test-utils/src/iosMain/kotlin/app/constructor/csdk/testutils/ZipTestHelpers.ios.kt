package app.constructor.csdk.testutils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import no.synth.kmpzip.zip.ZipInputStream
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.getBytes

actual fun readZipEntryNames(zipPath: String): Set<String> = readZipEntries(zipPath).keys

actual fun readZipEntryContent(zipPath: String, entryName: String): String? =
    readZipEntries(zipPath)[entryName]?.decodeToString()

@OptIn(ExperimentalForeignApi::class)
private fun readZipEntries(zipPath: String): Map<String, ByteArray> {
    val nsData = NSFileManager.defaultManager.contentsAtPath(zipPath) ?: return emptyMap()
    val result = mutableMapOf<String, ByteArray>()
    ZipInputStream(nsData.toByteArray()).use { zis ->
        while (true) {
            val entry = zis.nextEntry ?: break
            if (!entry.isDirectory) {
                result[entry.name] = zis.readBytes()
            }
        }
    }
    return result
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(length.toInt())
    if (bytes.isNotEmpty()) bytes.usePinned { getBytes(it.addressOf(0), length) }
    return bytes
}
