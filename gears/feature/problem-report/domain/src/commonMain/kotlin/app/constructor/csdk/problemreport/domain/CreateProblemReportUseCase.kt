@file:Suppress("ktlint:constructor:missing-interface-kdoc")

package app.constructor.csdk.problemreport.domain

import app.constructor.csdk.problemreport.domain.entity.MetadataLabels
import app.constructor.csdk.problemreport.domain.entity.ProblemReportData
import app.constructor.csdk.problemreport.domain.entity.ProblemReportResult

interface CreateProblemReportUseCase {

    /**
     * Creates an encrypted zip archive containing a metadata file derived from [report], device logs
     * (if [report.includeLogs] is true), and the screenshots referenced by [report].
     *
     * @param report Structured data describing the problem.
     * @param labels Pre-resolved format strings for metadata fields.
     * @return Result containing the encrypted zip path, encryption key, and IV.
     */
    suspend fun createReport(report: ProblemReportData, labels: MetadataLabels): ProblemReportResult
}
