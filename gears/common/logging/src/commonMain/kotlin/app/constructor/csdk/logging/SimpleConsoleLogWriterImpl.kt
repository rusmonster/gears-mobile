package app.constructor.csdk.logging

internal class SimpleConsoleLogWriterImpl : L.LogWriter {
    override fun d(tag: String, t: Throwable?, message: () -> String) = write(tag, t, message)

    override fun i(tag: String, t: Throwable?, message: () -> String) = write(tag, t, message)

    override fun w(tag: String, t: Throwable?, message: () -> String) = write(tag, t, message)

    override fun e(tag: String, t: Throwable?, message: () -> String) = write(tag, t, message)

    override fun close() {}

    private fun write(tag: String, t: Throwable?, message: () -> String) {
        println(LogMessageFormatter.format(tag, t, message))
    }
}
