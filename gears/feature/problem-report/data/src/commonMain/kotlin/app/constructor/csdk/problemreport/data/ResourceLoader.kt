package app.constructor.csdk.problemreport.data

import gearsmobile.feature.problem_report.data.generated.resources.Res

internal object ResourceLoader {
    suspend fun loadBytes(name: String): ByteArray = Res.readBytes("files/$name")
}
