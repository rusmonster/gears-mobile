package app.constructor.csdk.logging

import app.constructor.csdk.files.FileSystem

/** Single source of truth for the paths of all log files written by the SDK. */
object LogFiles {

    /**
     * Returns the absolute path of the active log file.
     *
     * This is the file currently being written to by [FileLogWriterImpl].
     */
    fun getLogPath(fileSystem: FileSystem): String = "${fileSystem.getAppDirectory()}/${LoggingConstants.LOG_FILE_NAME}"

    /**
     * Returns the absolute path of the backup log file.
     *
     * On each app launch the active log is rotated into this path so that
     * logs from the previous session are preserved alongside the current one.
     */
    fun getBackupLogPath(fileSystem: FileSystem): String =
        "${fileSystem.getAppDirectory()}/${LoggingConstants.LOG_FILE_BACKUP_NAME}"

    /**
     * Returns the paths of all log files that currently exist on disk.
     *
     * Files that have not yet been created are excluded from the result,
     * making this safe to call before any logging has occurred.
     */
    fun getPaths(fileSystem: FileSystem): List<String> = listOf(getLogPath(fileSystem), getBackupLogPath(fileSystem))
        .filter { fileSystem.exists(it) }
}
