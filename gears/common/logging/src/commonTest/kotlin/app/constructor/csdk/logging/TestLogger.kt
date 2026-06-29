package app.constructor.csdk.logging

import kotlin.test.Test

class TestLogger {
    @Test
    fun test() {
        L.setConsoleLoggingEnabled(true)
        L.d("TestLogger") { "Hello Logger" }
        L.e("TestLogger", RuntimeException("FakeError")) { "Logger error" }
    }
}
