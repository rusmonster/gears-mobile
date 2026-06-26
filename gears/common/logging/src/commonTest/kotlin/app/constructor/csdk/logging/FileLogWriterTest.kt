package app.constructor.csdk.logging

import app.constructor.csdk.files.FileSystem
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class FileLogWriterTest {
    private val realFs = FileSystem()
    private lateinit var mockFs: FileSystem
    private lateinit var tempDir: String
    private lateinit var logFilePath: String
    private lateinit var backupFilePath: String

    @BeforeTest
    fun setup() {
        tempDir = realFs.createTempDir()
        mockFs = mock<FileSystem> {
            every { getAppDirectory() } returns tempDir
            every { createTempDir() } calls { realFs.createTempDir() }
            every { exists(any()) } calls { (path: String) -> realFs.exists(path) }
            every { delete(any()) } calls { (path: String) -> realFs.delete(path) }
            every { move(any(), any()) } calls { (src: String, dst: String) -> realFs.move(src, dst) }
            every { createFile(any()) } calls { (path: String) -> realFs.createFile(path) }
            every { readText(any()) } calls { (path: String) -> realFs.readText(path) }
            every {
                writeText(
                    any(),
                    any(),
                )
            } calls { (path: String, content: String) -> realFs.writeText(path, content) }
            every {
                openWriter(
                    any(),
                    any(),
                )
            } calls { (path: String, append: Boolean) -> realFs.openWriter(path, append) }
        }
        logFilePath = "$tempDir/${LoggingConstants.LOG_FILE_NAME}"
        backupFilePath = "$tempDir/${LoggingConstants.LOG_FILE_BACKUP_NAME}"
    }

    @AfterTest
    fun teardown() {
        realFs.delete(tempDir)
    }

    private fun createWriter(scheduler: TestCoroutineScheduler): FileLogWriterImpl =
        FileLogWriterImpl(StandardTestDispatcher(scheduler), mockFs)

    @Test
    fun testFileRotationOnInitialization() = runTest {
        val existingContent = "Old log content"
        realFs.createFile(logFilePath)
        realFs.writeText(logFilePath, existingContent)

        assertTrue(realFs.exists(logFilePath), "Log file should exist before rotation")

        val writer = createWriter(testScheduler)
        testScheduler.advanceUntilIdle()

        assertTrue(realFs.exists(backupFilePath), "Backup file should exist after rotation")
        assertTrue(realFs.exists(logFilePath), "New log file should exist after rotation")
        assertEquals(
            existingContent,
            realFs.readText(backupFilePath),
            "Backup should contain previous session content",
        )

        writer.close()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun testLogWritingProducesPlaintextContent() = runTest {
        val writer = createWriter(testScheduler)
        testScheduler.advanceUntilIdle()

        writer.d("TestTag") { "Test message" }
        testScheduler.advanceUntilIdle()

        val content = realFs.readText(logFilePath)
        assertTrue(content.contains("[TestTag]"), "Log should contain tag")
        assertTrue(content.contains("Test message"), "Log should contain message")

        writer.close()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun testMultipleLogsWrittenInOrder() = runTest {
        val writer = createWriter(testScheduler)
        testScheduler.advanceUntilIdle()

        writer.d("Tag1") { "Message 1" }
        writer.i("Tag2") { "Message 2" }
        writer.w("Tag3") { "Message 3" }
        writer.e("Tag4") { "Message 4" }
        testScheduler.advanceUntilIdle()

        val content = realFs.readText(logFilePath)
        val idx1 = content.indexOf("Message 1")
        val idx2 = content.indexOf("Message 2")
        val idx3 = content.indexOf("Message 3")
        val idx4 = content.indexOf("Message 4")
        assertTrue(idx1 < idx2 && idx2 < idx3 && idx3 < idx4, "Messages should appear in order")

        writer.close()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun testExceptionLogging() = runTest {
        val writer = createWriter(testScheduler)
        testScheduler.advanceUntilIdle()

        val exception = RuntimeException("Test exception")
        writer.e("ErrorTag", exception) { "Error occurred" }
        testScheduler.advanceUntilIdle()

        val content = realFs.readText(logFilePath)
        assertTrue(content.contains("Error occurred"))
        assertTrue(content.contains("RuntimeException"))
        assertTrue(content.contains("Test exception"))

        writer.close()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun testNoLogWrittenWhenWriterClosed() = runTest {
        val writer = createWriter(testScheduler)
        testScheduler.advanceUntilIdle()

        writer.close()
        testScheduler.advanceUntilIdle()

        writer.d("Tag") { "Should not appear" }
        testScheduler.advanceUntilIdle()

        val content = realFs.readText(logFilePath)
        assertFalse(content.contains("Should not appear"), "Closed writer should not write logs")
    }
}
