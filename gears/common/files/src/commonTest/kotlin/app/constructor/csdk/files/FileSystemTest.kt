package app.constructor.csdk.files

import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FileSystemTest {
    private lateinit var fileSystem: FileSystem
    private lateinit var testFilePath: String
    private lateinit var testBackupPath: String

    @BeforeTest
    fun setup() {
        fileSystem = FileSystem()
        val appDir = fileSystem.getAppDirectory()
        val uniqueId = Random.nextInt()
        testFilePath = "$appDir/test-file-$uniqueId.txt"
        testBackupPath = "$appDir/test-file-backup-$uniqueId.txt"

        fileSystem.delete(testFilePath)
        fileSystem.delete(testBackupPath)
    }

    @AfterTest
    fun teardown() {
        fileSystem.delete(testFilePath)
        fileSystem.delete(testBackupPath)
    }

    @Test
    fun testFileExists() {
        assertFalse(fileSystem.exists(testFilePath), "File should not exist initially")

        fileSystem.createFile(testFilePath)

        assertTrue(fileSystem.exists(testFilePath), "File should exist after creation")
    }

    @Test
    fun testCreateFile() {
        assertTrue(fileSystem.createFile(testFilePath), "Should create new file")
        assertTrue(fileSystem.exists(testFilePath), "File should exist after creation")
    }

    @Test
    fun testDeleteFile() {
        fileSystem.createFile(testFilePath)
        assertTrue(fileSystem.exists(testFilePath), "File should exist")

        assertTrue(fileSystem.delete(testFilePath), "Should delete file")
        assertFalse(fileSystem.exists(testFilePath), "File should not exist after deletion")
    }

    @Test
    fun testMoveFile() {
        fileSystem.writeText(testFilePath, "Test content")
        assertTrue(fileSystem.exists(testFilePath), "Source file should exist")

        assertTrue(fileSystem.move(testFilePath, testBackupPath), "Should move file")
        assertFalse(fileSystem.exists(testFilePath), "Source file should not exist")
        assertTrue(fileSystem.exists(testBackupPath), "Destination file should exist")
        assertEquals("Test content", fileSystem.readText(testBackupPath))
    }

    @Test
    fun testWriteAndReadText() {
        val content = "Hello, World!"
        fileSystem.writeText(testFilePath, content)

        val readContent = fileSystem.readText(testFilePath)
        assertEquals(content, readContent)
    }

    @Test
    fun testWriterStreaming() {
        val writer = fileSystem.openWriter(testFilePath, append = false)
        writer.write("Line 1\n")
        writer.write("Line 2\n")
        writer.flush()
        writer.close()

        val content = fileSystem.readText(testFilePath)
        assertEquals("Line 1\nLine 2\n", content)
    }

    @Test
    fun testWriterAppend() {
        fileSystem.writeText(testFilePath, "Initial\n")

        val writer = fileSystem.openWriter(testFilePath, append = true)
        writer.write("Appended\n")
        writer.close()

        val content = fileSystem.readText(testFilePath)
        assertEquals("Initial\nAppended\n", content)
    }

    @Test
    fun testGetAppDirectory() {
        val appDir = fileSystem.getAppDirectory()
        assertNotNull(appDir)
        assertTrue(appDir.isNotEmpty(), "App directory should not be empty")
    }
}
