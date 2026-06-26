package app.constructor.csdk.problemreport.data

import app.constructor.csdk.problemreport.domain.entity.DeviceMetadata

internal expect object DeviceInfo {
    fun collect(): DeviceMetadata
}
