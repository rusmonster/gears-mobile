package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.KtNodeTypes

/**
 * Flags `as` and `as?` casts to `UiState` subtypes.
 *
 * **Spec reference:** FeatureSpecification.md §11.1 -- UiState should be consumed
 * via exhaustive `when` expressions, not via manual type casts. Casting bypasses
 * the compiler's exhaustiveness check and can lead to runtime crashes when new
 * states are added, and on the UI side it tends to mask layout bugs that only
 * surface when a given state is reached.
 *
 * **What it checks (AST):**
 * Looks for `BINARY_WITH_TYPE` nodes (which represent `as` / `as?` expressions)
 * whose text contains "UiState". This catches patterns like:
 * - `uiState as CourseList.UiState.Error`
 * - `state as? CourseList.UiState.Data`
 *
 * **Scope:** Kotlin files in:
 * - `app.constructor.csdk.*` (KMP presentation/impl ViewModels)
 * - `app.constructor.feature.*` (Android feature/UI modules)
 * - `app.constructor.courses.*` (course-list mini-app)
 * - `app.constructor.mobile.*` (Android host-app / launcher)
 *
 * Test sources are skipped: a file is treated as a test if its imports include
 * `org.junit.`, `kotlin.test.`, or `io.kotest.`. Asserting concrete state types
 * via casts is a common, legitimate idiom in unit tests.
 *
 * **Auto-correct:** No -- replacing casts with `when` requires restructuring
 * the control flow.
 */
class NoUiStateCastRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:no-uistate-cast"),
        about = About(
            maintainer = "Constructor",
        ),
    ) {

    private var isInScope = false
    private var isTestFile = false

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        if (node.elementType == PACKAGE_DIRECTIVE) {
            val pkg = node.text.removePrefix("package").trim()
            isInScope = pkg.isInScopePackage()
            isTestFile = false
            return
        }

        if (node.elementType == IMPORT_LIST) {
            val imports = node.text
            isTestFile = imports.contains("import org.junit.") ||
                imports.contains("import kotlin.test.") ||
                imports.contains("import io.kotest.")
            return
        }

        if (!isInScope || isTestFile) return

        // BINARY_WITH_TYPE represents `expr as Type` and `expr as? Type`
        if (node.elementType == KtNodeTypes.BINARY_WITH_TYPE) {
            if (node.text.contains("UiState")) {
                emit(
                    node.startOffset,
                    "Avoid casting to UiState subtypes -- use exhaustive 'when' instead",
                    false,
                )
            }
        }
    }

    private fun String.isInScopePackage(): Boolean {
        val prefixes = listOf(
            "app.constructor.csdk",
            "app.constructor.feature",
            "app.constructor.courses",
            "app.constructor.mobile",
        )
        return prefixes.any { this == it || this.startsWith("$it.") }
    }
}
