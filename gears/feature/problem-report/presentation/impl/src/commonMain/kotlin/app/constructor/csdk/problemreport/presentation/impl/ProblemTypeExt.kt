package app.constructor.csdk.problemreport.presentation.impl

import app.constructor.csdk.problemreport.domain.ProblemType
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.Res
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_account
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_bug
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_content
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_other
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_performance
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_ui
import org.jetbrains.compose.resources.getString

/** Returns the localised display name for this problem type. */
internal suspend fun ProblemType.toDisplayString(): String = when (this) {
    ProblemType.BUG -> getString(Res.string.problem_type_bug)
    ProblemType.UI -> getString(Res.string.problem_type_ui)
    ProblemType.PERFORMANCE -> getString(Res.string.problem_type_performance)
    ProblemType.ACCOUNT -> getString(Res.string.problem_type_account)
    ProblemType.CONTENT -> getString(Res.string.problem_type_content)
    ProblemType.OTHER -> getString(Res.string.problem_type_other)
}
