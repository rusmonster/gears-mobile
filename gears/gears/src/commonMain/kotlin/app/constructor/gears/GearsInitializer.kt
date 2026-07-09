@file:OptIn(ExperimentalAtomicApi::class)

package app.constructor.gears

import app.constructor.csdk.common.runCatchingCancellable
import app.constructor.csdk.common.sleep
import app.constructor.csdk.logging.L
import app.constructor.csdk.logging.log
import app.constructor.csdk.problemreport.di.ProblemReportModule
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

/**
 * Owns Gears' one-time initialization: enabling SDK file logging and gating ViewModel creation
 * until initialization has completed.
 */
internal object GearsInitializer {

    private enum class InitPhase { NOT_STARTED, IN_PROGRESS, DONE }

    private val initPhase = AtomicReference(InitPhase.NOT_STARTED)

    private fun doInit() {
        log.d { "Initializing Gears" }
        L.enableFileLogging()
        cleanupOldReportsAsync()
    }

    // Fire-and-forget housekeeping: reclaim storage from report bundles the host already shared.
    private fun cleanupOldReportsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatchingCancellable {
                ProblemReportModule.newCleanupProblemReportsUseCase().cleanup()
            }.onFailure { log.e(it) { "Failed to clean up old problem reports" } }
        }
    }

    /**
     * Shared, platform-agnostic initialization — runs **exactly once**.
     *
     * Invoked by the platform `Gears.initialize(...)` entry points once the platform-specific
     * setup (e.g. the Android application context) is in place. Enables SDK file logging so logs
     * can be bundled into a report.
     *
     * The first caller wins a CAS and performs initialization; any concurrent callers block until
     * it completes, and later calls return immediately.
     */
    fun init() {
        while (true) {
            when (initPhase.load()) {
                InitPhase.DONE -> break
                // Another thread is initializing — back off briefly, then re-check until DONE.
                InitPhase.IN_PROGRESS -> {
                    sleep(millis = 1)
                    continue
                }
                InitPhase.NOT_STARTED ->
                    if (initPhase.compareAndSet(InitPhase.NOT_STARTED, InitPhase.IN_PROGRESS)) {
                        try {
                            doInit()
                        } finally {
                            initPhase.store(InitPhase.DONE)
                        }
                        break
                    }
            }
        }
    }

    fun ensureInit() {
        check(initPhase.load() == InitPhase.DONE) {
            "Gears is not initialized. Call Gears.initialize(...) once before creating a ViewModel."
        }
    }
}
