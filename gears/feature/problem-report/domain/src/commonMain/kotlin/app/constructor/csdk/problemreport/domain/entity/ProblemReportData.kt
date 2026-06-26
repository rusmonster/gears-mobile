package app.constructor.csdk.problemreport.domain.entity

import app.constructor.csdk.problemreport.domain.ProblemType

/**
 * Structured data collected from the user for a problem report.
 *
 * @property problemType Category that best describes the problem.
 * @property problemDescription Free-text description provided by the user.
 * @property reproSteps Optional steps needed to reproduce the issue.
 * @property screenshotFilePaths Absolute paths of screenshot files to attach.
 * @property includeLogs Whether to include device log files in the archive.
 */
data class ProblemReportData(
    val problemType: ProblemType,
    val problemDescription: String,
    val reproSteps: String,
    val screenshotFilePaths: List<String>,
    val includeLogs: Boolean,
)
