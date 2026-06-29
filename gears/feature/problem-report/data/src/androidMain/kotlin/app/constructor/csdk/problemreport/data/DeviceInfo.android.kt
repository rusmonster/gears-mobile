package app.constructor.csdk.problemreport.data

import android.os.Build
import app.constructor.csdk.problemreport.domain.entity.DeviceMetadata

internal actual object DeviceInfo {
    actual fun collect(): DeviceMetadata = DeviceMetadata(
        os = "Android",
        osVersion = Build.VERSION.RELEASE,
        osArch = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
        deviceModel = Build.MODEL,
        deviceVendor = Build.MANUFACTURER,
        deviceType = "mobile",
    )
}
