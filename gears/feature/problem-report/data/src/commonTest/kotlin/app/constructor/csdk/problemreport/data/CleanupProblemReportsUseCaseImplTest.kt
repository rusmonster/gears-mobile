@file:Suppress("ktlint:constructor:test-method-naming")

package app.constructor.csdk.problemreport.data

import app.constructor.csdk.files.FileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CleanupProblemReportsUseCaseImplTest {

    private val fileSystem = FileSystem()
    private val createdPaths = mutableListOf<String>()

    @AfterTest
    fun tearDown() {
        createdPaths.forEach { fileSystem.delete(it) }
    }

    @Test
    fun deletesCpbFilesOlderThan24h() = runTest {
        val cpb = writeFile("cleanup_old", CpbFormat.FILE_EXTENSION)
        val ageJustOver24h = fileSystem.lastModifiedMillis(cpb) + DAY_MILLIS + 1_000
        val sut = CleanupProblemReportsUseCaseImpl(fileSystem, clock = { ageJustOver24h })

        sut.cleanup()

        assertFalse(fileSystem.exists(cpb), "A .cpb older than 24h must be deleted")
    }

    @Test
    fun keepsCpbFilesYoungerThan24h() = runTest {
        val cpb = writeFile("cleanup_recent", CpbFormat.FILE_EXTENSION)
        val stillWithin24h = fileSystem.lastModifiedMillis(cpb) + DAY_MILLIS - 1_000
        val sut = CleanupProblemReportsUseCaseImpl(fileSystem, clock = { stillWithin24h })

        sut.cleanup()

        assertTrue(fileSystem.exists(cpb), "A .cpb younger than 24h must be kept")
    }

    @Test
    fun ignoresNonCpbFiles() = runTest {
        val other = writeFile("cleanup_other", "txt")
        val farFuture = fileSystem.lastModifiedMillis(other) + 10 * DAY_MILLIS
        val sut = CleanupProblemReportsUseCaseImpl(fileSystem, clock = { farFuture })

        sut.cleanup()

        assertTrue(fileSystem.exists(other), "Non-.cpb files must never be deleted")
    }

    private fun writeFile(name: String, extension: String): String {
        val path = "${fileSystem.getAppDirectory()}/${name}_$SUFFIX.$extension"
        fileSystem.writeText(path, "content")
        createdPaths += path
        return path
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000

        // Distinguishes this test's artifacts from anything else in the shared app directory.
        const val SUFFIX = "gears_cleanup_test"
    }
}
