package app.constructor.csdk.testutils

import java.io.File
import java.util.zip.ZipInputStream

actual fun readZipEntryNames(zipPath: String): Set<String> {
    val names = mutableSetOf<String>()
    ZipInputStream(File(zipPath).inputStream()).use { zis ->
        var entry = zis.nextEntry
        while (entry != null) {
            names += entry.name
            entry = zis.nextEntry
        }
    }
    return names
}

actual fun readZipEntryContent(zipPath: String, entryName: String): String? {
    ZipInputStream(File(zipPath).inputStream()).use { zis ->
        var entry = zis.nextEntry
        while (entry != null) {
            if (entry.name == entryName) return zis.readBytes().toString(Charsets.UTF_8)
            entry = zis.nextEntry
        }
    }
    return null
}
