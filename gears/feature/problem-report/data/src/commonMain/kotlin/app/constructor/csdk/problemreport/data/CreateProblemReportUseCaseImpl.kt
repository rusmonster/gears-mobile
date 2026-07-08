package app.constructor.csdk.problemreport.data

import app.constructor.csdk.files.FileSystem
import app.constructor.csdk.logging.LogFiles
import app.constructor.csdk.problemreport.domain.CreateProblemReportUseCase
import app.constructor.csdk.problemreport.domain.entity.DeviceMetadata
import app.constructor.csdk.problemreport.domain.entity.MetadataLabels
import app.constructor.csdk.problemreport.domain.entity.ProblemReportData
import app.constructor.csdk.problemreport.domain.entity.ProblemReportResult
import app.constructor.csdk.zip.api.ZipPacker
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateProblemReportUseCaseImpl(
    private val zipPacker: ZipPacker,
    private val fileSystem: FileSystem,
    private val encryptFile: EncryptFileUseCase,
) : CreateProblemReportUseCase {

    // @cpt-algo:cpt-cyberfabricmobile-algo-problem-report-create:p2
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createReport(report: ProblemReportData, labels: MetadataLabels): ProblemReportResult {
        // @cpt-begin:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-meta
        val reportId = Uuid.random().toString()
        val deviceMetadata = DeviceInfo.collect()
        // @cpt-end:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-meta

        // Isolate every report in its own temp directory so that two concurrent ViewModels
        // (or rapid repeated submissions) can't overwrite or corrupt each other's plaintext
        // artifacts, and a later report can't replace a path already returned to an earlier one.
        val reportDir = fileSystem.createTempDir()
        val metadataPath = "$reportDir/$METADATA_FILENAME"
        val zipPath = "$reportDir/$OUTPUT_ZIP_FILENAME"

        try {
            // @cpt-begin:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-logs
            val logPaths = if (report.includeLogs) LogFiles.getPaths(fileSystem) else emptyList()
            // @cpt-end:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-logs

            // @cpt-begin:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-zip
            fileSystem.createFile(metadataPath)
            fileSystem.writeText(metadataPath, formatMetadata(reportId, report, deviceMetadata, labels))
            val allPaths = logPaths + report.screenshotFilePaths + metadataPath
            zipPacker.pack(allPaths, zipPath)
            // @cpt-end:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-zip

            // @cpt-begin:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-encrypt
            val encryptedPath = encryptFile.encrypt(zipPath)
            // Move the encrypted artifact out of the temp directory to a stable, report-scoped
            // path so it survives cleanup of the plaintext directory below.
            val cpbPath = "${fileSystem.getAppDirectory()}/${cpbFileName(reportId)}"
            fileSystem.move(encryptedPath, cpbPath)
            // @cpt-end:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-encrypt

            // @cpt-begin:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-result
            return ProblemReportResult(
                reportId = reportId,
                cpbPath = cpbPath,
            )
            // @cpt-end:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-result
        } finally {
            // Always remove the plaintext artifacts (metadata + ZIP, which may contain
            // screenshots and logs) once encryption has succeeded or failed. Leaving them on
            // disk would weaken the confidentiality guarantee the encrypted `.cpb` provides.
            fileSystem.delete(reportDir)
        }
    }

    private fun formatMetadata(
        reportId: String,
        report: ProblemReportData,
        device: DeviceMetadata,
        labels: MetadataLabels,
    ) = buildString {
        appendLine("${labels.reportId} $reportId")
        appendLine("${labels.problemType} ${report.problemType.name}")
        appendLine("${labels.os} ${device.os} ${device.osVersion} (${device.osArch})")
        appendLine("${labels.device} ${device.deviceVendor} ${device.deviceModel} (${device.deviceType})")
        appendLine("${labels.description} ${report.problemDescription}")
        if (report.reproSteps.isNotBlank()) {
            appendLine("${labels.stepsToReproduce} ${report.reproSteps}")
        }
    }

    internal companion object {
        const val METADATA_FILENAME = "problem_report_metadata.txt"
        const val OUTPUT_ZIP_FILENAME = "problem_report.zip"

        /** Report-scoped name for the final encrypted artifact, unique per [reportId]. */
        fun cpbFileName(reportId: String) = "problem_report_$reportId.${CpbFormat.FILE_EXTENSION}"
    }
}
