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

    // Exact path passed to encrypt() (the plaintext ZIP inside the per-report temp dir).
    private var encryptInputPath: String? = null

    // The use case deletes the plaintext ZIP right after encryption, so the mock preserves a copy
    // (keyed by the unique per-report dir name) for tests that need to inspect the archive contents.
    private var capturedZipPath: String? = null

    private val encryptFile = mock<EncryptFileUseCase> {
        everySuspend { encrypt(any()) } calls { (zipPath: String) ->
            encryptInputPath = zipPath
            val reportDirName = zipPath.substringBeforeLast('/').substringAfterLast('/')
            val preserved = "$tempDir/preserved-$reportDirName.zip"
            fileSystem.move(zipPath, preserved)
            capturedZipPath = preserved
            preserved.replaceAfterLast('.', CpbFormat.FILE_EXTENSION)
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
    fun encryptFileIsCalledWithZipInIsolatedTempDir() = runTest {
        sut.createReport(minimalReport(), testLabels)
        assertTrue(
            encryptInputPath!!.endsWith("/${CreateProblemReportUseCaseImpl.OUTPUT_ZIP_FILENAME}"),
            "encrypt() must receive the report ZIP",
        )
        assertTrue(
            encryptInputPath!!.substringBeforeLast('/') != fileSystem.getAppDirectory(),
            "The ZIP must live in a per-report temp dir, not directly in the app directory",
        )
    }

    @Test
    fun deletesPlaintextArtifactsAfterEncryption() = runTest {
        sut.createReport(minimalReport(), testLabels)
        val reportDir = encryptInputPath!!.substringBeforeLast('/')
        assertFalse(
            fileSystem.exists(reportDir),
            "Per-report temp dir (plaintext ZIP + metadata) must be deleted after encryption",
        )
    }

    @Test
    fun eachReportUsesIsolatedArtifactPaths() = runTest {
        val cpb1 = sut.createReport(minimalReport(), testLabels).cpbPath
        val cpb2 = sut.createReport(minimalReport(), testLabels).cpbPath
        assertTrue(cpb1 != cpb2, "Each report must produce a unique .cpb path")
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
    fun cpbPathIsScopedToReportId() = runTest {
        val result = sut.createReport(minimalReport(), testLabels)
        assertTrue(
            result.cpbPath.endsWith("problem_report_${result.reportId}.${CpbFormat.FILE_EXTENSION}"),
            "cpbPath must be scoped to the report id",
        )
    }

    private fun minimalReport() = ProblemReportData(
        problemType = ProblemType.BUG,
        problemDescription = "",
        reproSteps = "",
        screenshotFilePaths = emptyList(),
        includeLogs = false,
    )
}
