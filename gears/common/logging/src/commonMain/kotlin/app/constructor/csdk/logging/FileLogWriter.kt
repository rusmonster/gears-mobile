package app.constructor.csdk.logging

import app.constructor.csdk.files.FileSystem
import app.constructor.csdk.files.FileWriter
import kotlin.concurrent.Volatile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

@Suppress("FunctionName")
internal fun FileLogWriter(): L.LogWriter {
    return try {
        FileLogWriterImpl()
    } catch (_: Exception) {
        NoOpFileLogWriter
    }
}

internal class FileLogWriterImpl(
    dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1),
    private val fileSystem: FileSystem = FileSystem(),
) : L.LogWriter {
    private val logScope = CoroutineScope(dispatcher)

    @Volatile
    private var fileWriter: FileWriter? = null

    init {
        logScope.launch {
            try {
                val logFilePath = LogFiles.getLogPath(fileSystem)
                val backupFilePath = LogFiles.getBackupLogPath(fileSystem)

                if (fileSystem.exists(logFilePath)) {
                    if (fileSystem.exists(backupFilePath)) {
                        fileSystem.delete(backupFilePath)
                    }
                    fileSystem.move(logFilePath, backupFilePath)
                }
                fileSystem.createFile(logFilePath)
                fileWriter = fileSystem.openWriter(logFilePath, append = true)
            } catch (_: Exception) {
                // Failed to initialize - fileWriter remains null
            }
        }
    }

    override fun d(tag: String, t: Throwable?, message: () -> String) {
        write(tag, t, message)
    }

    override fun i(tag: String, t: Throwable?, message: () -> String) {
        write(tag, t, message)
    }

    override fun w(tag: String, t: Throwable?, message: () -> String) {
        write(tag, t, message)
    }

    override fun e(tag: String, t: Throwable?, message: () -> String) {
        write(tag, t, message)
    }

    private fun write(tag: String, t: Throwable?, message: () -> String) {
        val formattedMessage = LogMessageFormatter.format(tag, t, message)
        logScope.launch {
            val writer = fileWriter ?: return@launch
            writer.write(formattedMessage)
            writer.write("\n")
            writer.flush()
        }
    }

    override fun close() {
        logScope.launch {
            @Suppress("ktlint:constructor:no-run-catching")
            runCatching { fileWriter?.close() }
            fileWriter = null
        }
    }
}

private object NoOpFileLogWriter : L.LogWriter {
    override fun d(tag: String, t: Throwable?, message: () -> String) {}
    override fun i(tag: String, t: Throwable?, message: () -> String) {}
    override fun w(tag: String, t: Throwable?, message: () -> String) {}
    override fun e(tag: String, t: Throwable?, message: () -> String) {}
    override fun close() {}
}
