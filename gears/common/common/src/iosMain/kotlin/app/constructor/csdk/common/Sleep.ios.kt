package app.constructor.csdk.common

import platform.posix.usleep

actual fun sleep(millis: Long) {
    usleep((millis * 1_000L).toUInt())
}
