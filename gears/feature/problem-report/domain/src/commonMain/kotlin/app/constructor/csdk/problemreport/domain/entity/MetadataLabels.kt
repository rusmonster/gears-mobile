package app.constructor.csdk.problemreport.domain.entity

data class MetadataLabels(
    val reportId: String,
    val problemType: String,
    val os: String,
    val device: String,
    val description: String,
    val stepsToReproduce: String,
)
