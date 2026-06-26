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
        val appDir = fileSystem.getAppDirectory()
        val outputPath = "$appDir/$OUTPUT_ZIP_FILENAME"
        val metadataPath = "$appDir/$METADATA_FILENAME"

        // @cpt-begin:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-meta
        val reportId = Uuid.random().toString()
        val deviceMetadata = DeviceInfo.collect()
        // @cpt-end:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-meta

        // @cpt-begin:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-logs
        val logPaths = if (report.includeLogs) LogFiles.getPaths(fileSystem) else emptyList()
        // @cpt-end:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-logs

        // @cpt-begin:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-zip
        fileSystem.createFile(metadataPath)
        fileSystem.writeText(metadataPath, formatMetadata(reportId, report, deviceMetadata, labels))
        val allPaths = logPaths + report.screenshotFilePaths + metadataPath
        zipPacker.pack(allPaths, outputPath)
        fileSystem.delete(metadataPath)
        // @cpt-end:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-zip

        // @cpt-begin:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-encrypt
        val cpbPath = encryptFile.encrypt(outputPath)
        // @cpt-end:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-encrypt

        // @cpt-begin:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-result
        return ProblemReportResult(
            reportId = reportId,
            cpbPath = cpbPath,
        )
        // @cpt-end:cpt-cyberfabricmobile-algo-problem-report-create:p2:inst-result
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
    }
}
