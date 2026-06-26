@file:Suppress("ktlint:constructor:test-method-naming")

package app.constructor.csdk.problemreport.data

import app.constructor.csdk.files.FileSystem
import app.constructor.csdk.logging.LogFiles
import app.constructor.csdk.problemreport.domain.ProblemType
import app.constructor.csdk.problemreport.domain.entity.MetadataLabels
import app.constructor.csdk.problemreport.domain.entity.ProblemReportData
import app.constructor.csdk.testutils.readZipEntryContent
import app.constructor.csdk.testutils.readZipEntryNames
import app.constructor.csdk.zip.impl.ZipPacker
import dev.mokkery.answering.calls
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CreateProblemReportUseCaseImplTest {

    private val fileSystem = FileSystem()
    private lateinit var tempDir: String
    private var capturedZipPath: String? = null

    private val encryptFile = mock<EncryptFileUseCase> {
        everySuspend { encrypt(any()) } calls { (zipPath: String) ->
            capturedZipPath = zipPath
            zipPath.replaceAfterLast('.', CpbFormat.FILE_EXTENSION)
        }
    }

    private val sut = CreateProblemReportUseCaseImpl(ZipPacker(), fileSystem, encryptFile)

    private val testLabels = MetadataLabels(
        reportId = "Report ID:",
        problemType = "Problem Type:",
        os = "OS:",
        device = "Device:",
        description = "Description:",
        stepsToReproduce = "Steps to Reproduce:",
    )

    @BeforeTest
    fun setUp() {
        tempDir = fileSystem.createTempDir()
        fileSystem.delete(LogFiles.getLogPath(fileSystem))
        fileSystem.delete(LogFiles.getBackupLogPath(fileSystem))
    }

    @AfterTest
    fun tearDown() {
        fileSystem.delete(tempDir)
        fileSystem.delete("${fileSystem.getAppDirectory()}/${CreateProblemReportUseCaseImpl.OUTPUT_ZIP_FILENAME}")
        fileSystem.delete(LogFiles.getLogPath(fileSystem))
        fileSystem.delete(LogFiles.getBackupLogPath(fileSystem))
    }

    @Test
    fun returnsResultWithCpbPath() = runTest {
        val result = sut.createReport(minimalReport(), testLabels)
        assertTrue(
            result.cpbPath.endsWith(".${CpbFormat.FILE_EXTENSION}"),
            "cpbPath must point to a .cpb file",
        )
    }

    @Test
    fun returnsNonEmptyReportId() = runTest {
        val result = sut.createReport(minimalReport(), testLabels)
        assertTrue(result.reportId.isNotBlank())
    }

    @Test
    fun eachReportHasUniqueReportId() = runTest {
        val id1 = sut.createReport(minimalReport(), testLabels).reportId
        val id2 = sut.createReport(minimalReport(), testLabels).reportId
        assertTrue(id1 != id2, "Each report must have a unique reportId")
    }

    @Test
    fun encryptFileIsCalledWithZipPath() = runTest {
        sut.createReport(minimalReport(), testLabels)
        val expectedZipPath = "${fileSystem.getAppDirectory()}/${CreateProblemReportUseCaseImpl.OUTPUT_ZIP_FILENAME}"
        assertEquals(expectedZipPath, capturedZipPath)
    }

    @Test
    fun metadataContainsReportId() = runTest {
        val result = sut.createReport(
            report = ProblemReportData(
                problemType = ProblemType.BUG,
                problemDescription = "",
                reproSteps = "",
                screenshotFilePaths = emptyList(),
                includeLogs = false,
            ),
            labels = testLabels,
        )
        val metadata = readZipEntryContent(capturedZipPath!!, "problem_report_metadata.txt")
        assertNotNull(metadata)
        assertTrue(metadata.contains(result.reportId))
    }

    @Test
    fun metadataContainsProblemTypeDescriptionAndSteps() = runTest {
        sut.createReport(
            report = ProblemReportData(
                problemType = ProblemType.BUG,
                problemDescription = "App crashes on launch",
                reproSteps = "Open the app, tap on courses",
                screenshotFilePaths = emptyList(),
                includeLogs = false,
            ),
            labels = testLabels,
        )

        val metadata = readZipEntryContent(capturedZipPath!!, "problem_report_metadata.txt")
        assertNotNull(metadata)
        assertTrue(metadata.contains("BUG"))
        assertTrue(metadata.contains("App crashes on launch"))
        assertTrue(metadata.contains("Open the app, tap on courses"))
    }

    @Test
    fun metadataOmitsStepsToReproduceWhenBlank() = runTest {
        sut.createReport(
            report = ProblemReportData(
                problemType = ProblemType.OTHER,
                problemDescription = "Something is off",
                reproSteps = "",
                screenshotFilePaths = emptyList(),
                includeLogs = false,
            ),
            labels = testLabels,
        )

        val metadata = readZipEntryContent(capturedZipPath!!, "problem_report_metadata.txt")
        assertNotNull(metadata)
        assertFalse(metadata.contains("Steps to Reproduce"))
    }

    @Test
    fun includesScreenshotFilesInZip() = runTest {
        val screenshotPath = "$tempDir/screenshot.png".also { fileSystem.writeText(it, "fake-png-data") }

        sut.createReport(
            report = ProblemReportData(
                problemType = ProblemType.UI,
                problemDescription = "",
                reproSteps = "",
                screenshotFilePaths = listOf(screenshotPath),
                includeLogs = false,
            ),
            labels = testLabels,
        )

        assertTrue("screenshot.png" in readZipEntryNames(capturedZipPath!!))
    }

    @Test
    fun excludesLogFilesWhenIncludeLogsFalse() = runTest {
        val logPath = LogFiles.getLogPath(fileSystem)
        fileSystem.createFile(logPath)
        fileSystem.writeText(logPath, "some log content")

        sut.createReport(
            report = ProblemReportData(
                problemType = ProblemType.BUG,
                problemDescription = "",
                reproSteps = "",
                screenshotFilePaths = emptyList(),
                includeLogs = false,
            ),
            labels = testLabels,
        )

        assertFalse(logPath.substringAfterLast('/') in readZipEntryNames(capturedZipPath!!))
    }

    @Test
    fun includesLogFilesInZip() = runTest {
        val logPath = LogFiles.getLogPath(fileSystem)
        fileSystem.createFile(logPath)
        fileSystem.writeText(logPath, "log line 1\nlog line 2")

        sut.createReport(
            report = ProblemReportData(
                problemType = ProblemType.PERFORMANCE,
                problemDescription = "",
                reproSteps = "",
                screenshotFilePaths = emptyList(),
                includeLogs = true,
            ),
            labels = testLabels,
        )

        assertTrue(logPath.substringAfterLast('/') in readZipEntryNames(capturedZipPath!!))
    }

    @Test
    fun includesAllFilesAndMetadataTogether() = runTest {
        val logPath = LogFiles.getLogPath(fileSystem)
        fileSystem.createFile(logPath)
        fileSystem.writeText(logPath, "log")
        val screenshotPath = "$tempDir/screen.png".also { fileSystem.writeText(it, "png") }

        sut.createReport(
            report = ProblemReportData(
                problemType = ProblemType.ACCOUNT,
                problemDescription = "Can't log in",
                reproSteps = "",
                screenshotFilePaths = listOf(screenshotPath),
                includeLogs = true,
            ),
            labels = testLabels,
        )

        val entries = readZipEntryNames(capturedZipPath!!)
        assertEquals(
            setOf(logPath.substringAfterLast('/'), "screen.png", "problem_report_metadata.txt"),
            entries,
        )
    }

    @Test
    fun deletesTemporaryMetadataFileAfterPacking() = runTest {
        val metadataPath = "${fileSystem.getAppDirectory()}/problem_report_metadata.txt"

        sut.createReport(
            report = ProblemReportData(ProblemType.CONTENT, "Missing video", "", emptyList(), includeLogs = false),
            labels = testLabels,
        )

        assertFalse(fileSystem.exists(metadataPath), "Metadata file should be cleaned up after packing")
    }

    private fun minimalReport() = ProblemReportData(
        problemType = ProblemType.BUG,
        problemDescription = "",
        reproSteps = "",
        screenshotFilePaths = emptyList(),
        includeLogs = false,
    )
}
