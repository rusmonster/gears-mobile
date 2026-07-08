package app.constructor.gears

import app.constructor.csdk.problemreport.di.ProblemReportModule
import app.constructor.csdk.problemreport.presentation.api.ProblemReport

/**
 * Public entry point for Constructor Fabric Gears Mobile.
 */
@Suppress("SpellCheckingInspection")
object Gears {

    /**
     * Creates a new problem-report [ProblemReport.ViewModel].
     *
     * @param config Host-supplied settings — support email, encryption public key, and an
     *   optional auto-captured screenshot. See [ProblemReport.Config].
     * @return A ready-to-use ViewModel driving the problem-report UI; create one per screen.
     * @throws IllegalStateException if `Gears.initialize(...)` was not called first.
     */
    fun newProblemReportViewModel(config: ProblemReport.Config): ProblemReport.ViewModel {
        GearsInitializer.ensureInit()
        return ProblemReportModule.newProblemReportViewModel(config)
    }
}
