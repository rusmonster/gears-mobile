package app.constructor.csdk.logging

import android.util.Log

@Suppress("FunctionName")
internal actual fun ConsoleLogWriter() = object : L.LogWriter {
    override fun d(tag: String, t: Throwable?, message: () -> String) {
        Log.d(tag, message(), t)
    }

    override fun i(tag: String, t: Throwable?, message: () -> String) {
        Log.i(tag, message(), t)
    }

    override fun w(tag: String, t: Throwable?, message: () -> String) {
        Log.w(tag, message(), t)
    }

    override fun e(tag: String, t: Throwable?, message: () -> String) {
        Log.e(tag, message(), t)
    }

    override fun close() {}
}
