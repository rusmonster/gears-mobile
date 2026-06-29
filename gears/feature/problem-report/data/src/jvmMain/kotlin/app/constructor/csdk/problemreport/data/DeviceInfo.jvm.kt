package app.constructor.csdk.problemreport.data

import app.constructor.csdk.problemreport.domain.entity.DeviceMetadata

internal actual object DeviceInfo {
    actual fun collect(): DeviceMetadata = DeviceMetadata(
        os = System.getProperty("os.name") ?: "JVM",
        osVersion = System.getProperty("os.version") ?: "unknown",
        osArch = System.getProperty("os.arch") ?: "unknown",
        deviceModel = System.getProperty("os.name") ?: "JVM",
        deviceVendor = System.getProperty("java.vendor") ?: "unknown",
        deviceType = "desktop",
    )
}
