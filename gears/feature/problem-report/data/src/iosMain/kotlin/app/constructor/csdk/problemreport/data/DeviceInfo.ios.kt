package app.constructor.csdk.problemreport.data

import app.constructor.csdk.problemreport.domain.entity.DeviceMetadata
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad

internal actual object DeviceInfo {
    actual fun collect(): DeviceMetadata {
        val device = UIDevice.currentDevice
        return DeviceMetadata(
            os = "iOS",
            osVersion = NSProcessInfo.processInfo.operatingSystemVersionString,
            osArch = "arm64",
            deviceModel = device.model,
            deviceVendor = "Apple",
            deviceType = if (device.userInterfaceIdiom == UIUserInterfaceIdiomPad) "tablet" else "mobile",
        )
    }
}
