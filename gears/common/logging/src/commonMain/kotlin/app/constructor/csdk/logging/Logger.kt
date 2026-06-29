package app.constructor.csdk.logging

import kotlin.concurrent.Volatile
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.update
import kotlin.reflect.KClass

object L {
    @Volatile
    var logLevel: LogLevel = LogLevel.DEBUG

    @Volatile
    private var isConsoleLoggingEnabled: Boolean = true

    @Volatile
    private var fileLogWriter: LogWriter? = null

    @Volatile
    private lateinit var writers: List<LogWriter>

    init {
        rebuildWriters()
    }

    /**
     * Must be called after the platform context is available (e.g. after CSDK.init on Android).
     * Initializes file-based logging so that logs are written to disk.
     */
    fun enableFileLogging() {
        if (fileLogWriter != null) return
        fileLogWriter = FileLogWriter()
        rebuildWriters()
    }

    fun setConsoleLoggingEnabled(enabled: Boolean) {
        isConsoleLoggingEnabled = enabled
        rebuildWriters()
    }

    private fun rebuildWriters() {
        writers = buildList {
            if (isConsoleLoggingEnabled) {
                add(ConsoleLogWriter())
            }
            fileLogWriter?.let { add(it) }
        }
    }

    fun d(tag: String, t: Throwable? = null, message: () -> String) {
        if (logLevel.weight >= LogLevel.DEBUG.weight) {
            writers.forEach { it.d(tag, t, message) }
        }
    }

    fun i(tag: String, t: Throwable? = null, message: () -> String) {
        if (logLevel.weight >= LogLevel.INFO.weight) {
            writers.forEach { it.i(tag, t, message) }
        }
    }

    fun w(tag: String, t: Throwable? = null, message: () -> String) {
        if (logLevel.weight >= LogLevel.WARN.weight) {
            writers.forEach { it.w(tag, t, message) }
        }
    }

    fun e(tag: String, t: Throwable? = null, message: () -> String) {
        if (logLevel.weight >= LogLevel.ERROR.weight) {
            writers.forEach { it.e(tag, t, message) }
        }
    }

    private val loggers = AtomicReference(mapOf<KClass<*>, Logger>())

    fun getLogger(cls: KClass<*>): Logger {
        val cached = loggers.load()
        cached[cls]?.let { return it }

        val logger = Logger(cls.simpleName ?: "UnknownClass")
        loggers.update { map -> map + (cls to logger) }
        return logger
    }

    enum class LogLevel(val weight: Int) {
        SILENT(0),
        ERROR(1),
        WARN(2),
        INFO(3),
        DEBUG(4),
    }

    class Logger(val tag: String) {
        fun d(t: Throwable? = null, message: () -> String) = d(tag, t, message)
        fun i(t: Throwable? = null, message: () -> String) = i(tag, t, message)
        fun w(t: Throwable? = null, message: () -> String) = w(tag, t, message)
        fun e(t: Throwable? = null, message: () -> String) = e(tag, t, message)
    }

    interface LogWriter {
        fun d(tag: String, t: Throwable? = null, message: () -> String)
        fun i(tag: String, t: Throwable? = null, message: () -> String)
        fun w(tag: String, t: Throwable? = null, message: () -> String)
        fun e(tag: String, t: Throwable? = null, message: () -> String)
        fun close()
    }
}

val Any.log: L.Logger get() = L.getLogger(this::class)

@Suppress("FunctionName")
internal expect fun ConsoleLogWriter(): L.LogWriter
