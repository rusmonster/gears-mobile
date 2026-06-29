package app.constructor.csdk.testutils

expect fun readZipEntryNames(zipPath: String): Set<String>
expect fun readZipEntryContent(zipPath: String, entryName: String): String?
