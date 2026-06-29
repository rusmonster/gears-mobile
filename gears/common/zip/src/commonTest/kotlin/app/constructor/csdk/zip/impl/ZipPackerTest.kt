package app.constructor.csdk.zip.impl

import app.constructor.csdk.files.FileSystem
import app.constructor.csdk.testutils.readZipEntryContent
import app.constructor.csdk.testutils.readZipEntryNames
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ZipPackerTest {

    private lateinit var tempDir: String

    @BeforeTest
    fun setUp() {
        tempDir = FileSystem().createTempDir()
    }

    @AfterTest
    fun tearDown() {
        FileSystem().delete(tempDir)
    }

    @Test
    fun packCreatesZipContainingAllFiles() {
        val file1 = "$tempDir/alpha.txt".also { FileSystem().writeText(it, "hello") }
        val file2 = "$tempDir/beta.txt".also { FileSystem().writeText(it, "world") }
        val output = "$tempDir/out.zip"

        ZipPacker().pack(listOf(file1, file2), output)

        assertEquals(setOf("alpha.txt", "beta.txt"), readZipEntryNames(output))
    }

    @Test
    fun packPreservesFileContent() {
        val content = "the quick brown fox"
        val file = "$tempDir/data.txt".also { FileSystem().writeText(it, content) }
        val output = "$tempDir/out.zip"

        ZipPacker().pack(listOf(file), output)

        assertEquals(content, readZipEntryContent(output, "data.txt"))
    }

    @Test
    fun packWithEmptyInputCreatesEmptyZip() {
        val output = "$tempDir/empty.zip"

        ZipPacker().pack(emptyList(), output)

        assertTrue(output.isNotEmpty())
        assertTrue(readZipEntryNames(output).isEmpty())
    }

    @Test
    fun packUsesFileNameAsEntryName() {
        val file = "$tempDir/nested.txt".also { FileSystem().writeText(it, "x") }
        val output = "$tempDir/out.zip"

        ZipPacker().pack(listOf(file), output)

        assertEquals(setOf("nested.txt"), readZipEntryNames(output))
        assertNull(readZipEntryContent(output, file))
    }
}
