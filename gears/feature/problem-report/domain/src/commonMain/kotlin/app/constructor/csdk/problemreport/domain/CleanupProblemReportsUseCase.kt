@file:Suppress("ktlint:constructor:missing-interface-kdoc")

package app.constructor.csdk.problemreport.domain

interface CleanupProblemReportsUseCase {

    /**
     * Deletes encrypted report bundles (`.cpb`) that are older than the retention window (24h),
     * reclaiming storage left behind by reports the host has already shared.
     */
    suspend fun cleanup()
}
