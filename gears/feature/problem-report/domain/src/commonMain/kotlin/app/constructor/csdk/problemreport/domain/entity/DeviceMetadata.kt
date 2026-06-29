package app.constructor.csdk.problemreport.domain.entity

/**
 * Hardware and OS information collected from the device running the app.
 *
 * @property os Operating system name (e.g. "Android", "iOS").
 * @property osVersion OS version string (e.g. "14.0").
 * @property osArch CPU architecture (e.g. "arm64-v8a", "arm64").
 * @property deviceModel Device model name (e.g. "Pixel 7", "iPhone").
 * @property deviceVendor Device manufacturer (e.g. "Google", "Apple").
 * @property deviceType Form factor — "mobile" or "tablet".
 */
data class DeviceMetadata(
    val os: String,
    val osVersion: String,
    val osArch: String,
    val deviceModel: String,
    val deviceVendor: String,
    val deviceType: String,
)
