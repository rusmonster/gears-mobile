package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces that `@Test` methods follow the underscore-separated naming convention:
 * `method_condition[_expectedResult]`.
 *
 * **Spec reference:** FeatureSpecification.md §14 -- Test naming convention:
 * `<method>_<condition>_<expectedResult>`, e.g. `filter_allCourses_excludesFinishedState`.
 * Two-part names like `format_setsId` are also accepted since the condition and
 * expected result are often naturally combined.
 *
 * **What it checks (AST):**
 * For each [FUN] node that has a `@Test` annotation in its modifier list, the rule
 * checks whether the function name contains at least one underscore character.
 * This enforces the structured naming convention over camelCase test names like
 * `testFilterCourses` or `shouldReturnData`.
 *
 * **Scope:** Feature packages only -- packages under `app.constructor.csdk.*` that
 * contain a layer segment (`.domain.`, `.data.`, `.presentation.`). Shared utility
 * modules (`common`, `files`, `mvi`, etc.) and legacy android-app tests are excluded.
 *
 * **Auto-correct:** No -- renaming tests requires understanding the test's intent.
 */
class TestMethodNamingRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:test-method-naming"),
        about = About(
            maintainer = "Constructor",
        ),
    ) {

    private var isInScope = false

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        if (node.elementType == PACKAGE_DIRECTIVE) {
            isInScope = isFeaturePackage(node.text.removePrefix("package").trim())
            return
        }

        if (!isInScope) return
        if (node.elementType != FUN) return
        if (!node.hasTestAnnotation()) return

        val methodName = node.findChildByType(IDENTIFIER)?.text ?: return

        if (!methodName.contains('_')) {
            emit(
                node.startOffset,
                "Test method '$methodName' should follow 'method_condition[_expectedResult]' " +
                    "naming convention (use underscores to separate parts)",
                false,
            )
        }
    }
}

/**
 * Returns `true` if the package belongs to a feature module.
 *
 * Feature packages always contain a layer segment (`.domain.`, `.data.`, `.presentation.`)
 * after the feature name, e.g. `app.constructor.csdk.courselist.domain.entity`.
 * Shared utility modules like `common`, `files`, `mvi` etc. do not have layer segments,
 * so this naturally excludes them without maintaining an explicit denylist.
 */
private fun isFeaturePackage(pkg: String): Boolean {
    if (!pkg.startsWith("app.constructor.csdk.")) return false
    val afterPrefix = pkg.removePrefix("app.constructor.csdk.")
    return afterPrefix.contains(".domain.") ||
        afterPrefix.contains(".data.") ||
        afterPrefix.contains(".presentation.") ||
        afterPrefix.endsWith(".domain") ||
        afterPrefix.endsWith(".data") ||
        afterPrefix.endsWith(".presentation")
}

/**
 * Returns `true` if this [FUN] node has a `@Test` annotation.
 * Excludes `@BeforeTest` and `@AfterTest` which also contain "Test".
 */
private fun ASTNode.hasTestAnnotation(): Boolean {
    val modifiers = findChildByType(MODIFIER_LIST) ?: return false
    var child = modifiers.firstChildNode
    while (child != null) {
        if (child.elementType.toString() == "ANNOTATION_ENTRY") {
            val annotationText = child.text.trim()
            if (annotationText == "@Test" || annotationText == "@kotlin.test.Test") {
                return true
            }
        }
        child = child.treeNext
    }
    return false
}
