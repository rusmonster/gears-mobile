package app.constructor.csdk.logging

@Suppress("FunctionName")
internal actual fun ConsoleLogWriter(): L.LogWriter = SimpleConsoleLogWriterImpl()
