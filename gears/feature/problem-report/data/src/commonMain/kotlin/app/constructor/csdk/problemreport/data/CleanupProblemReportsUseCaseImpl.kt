package app.constructor.csdk.problemreport.data

import app.constructor.csdk.common.currentTimeMillis
import app.constructor.csdk.files.FileSystem
import app.constructor.csdk.logging.log
import app.constructor.csdk.problemreport.domain.CleanupProblemReportsUseCase

class CleanupProblemReportsUseCaseImpl(
    private val fileSystem: FileSystem,
    private val clock: () -> Long = ::currentTimeMillis,
) : CleanupProblemReportsUseCase {

    override suspend fun cleanup() {
        val now = clock()
        val staleReports = fileSystem.listFiles(fileSystem.getAppDirectory())
            .filter { it.endsWith(".${CpbFormat.FILE_EXTENSION}") }
            .filter { now - fileSystem.lastModifiedMillis(it) > MAX_AGE_MILLIS }

        if (staleReports.isEmpty()) return

        log.d { "Cleaning up ${staleReports.size} stale problem report(s)" }
        staleReports.forEach { fileSystem.delete(it) }
    }

    private companion object {
        const val MAX_AGE_MILLIS = 24L * 60 * 60 * 1000 // 24h
    }
}
