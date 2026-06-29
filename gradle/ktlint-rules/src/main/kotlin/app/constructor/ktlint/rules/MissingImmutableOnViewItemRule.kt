package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DATA_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces that every `data class` whose name ends with `ViewItem` in `presentation.api`
 * packages is annotated with `@Immutable`.
 *
 * **Spec reference:** FeatureSpecification.md §8 --
 * "All view-item data classes passed in `UiState.Data` (e.g. `CourseViewItem`)
 * are annotated `@Immutable`."
 *
 * **Scope:** Only files whose package matches:
 * - `*.presentation.api.*` or ends with `.presentation.api`
 *
 * **What it checks (AST):**
 * For each [CLASS] node that has a `data` modifier keyword and an [IDENTIFIER] ending
 * with `ViewItem`, the rule checks whether the node's [MODIFIER_LIST] contains an
 * [ANNOTATION_ENTRY] with the short name `Immutable`.
 *
 * **Naming convention:** The suffix `ViewItem` is the standard naming pattern from §6
 * of the spec (e.g. `CourseViewItem`, `HeaderViewItem`, `AssignmentCardViewItem`).
 *
 * **Auto-correct:** No -- adding annotations changes public API surface.
 */
class MissingImmutableOnViewItemRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:missing-immutable-viewitem"),
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
            isInScope = isPresentationApiPackage(node.text)
            return
        }

        if (!isInScope) return
        if (node.elementType != CLASS) return

        val name = node.findChildByType(IDENTIFIER)?.text ?: return

        if (name.endsWith("ViewItem") && node.isDataClass()) {
            if (!node.hasImmutableAnnotation()) {
                emit(
                    node.startOffset,
                    "ViewItem data class '$name' must be annotated with @Immutable (§8 FeatureSpec)",
                    false,
                )
            }
        }
    }
}

/**
 * Returns `true` if this [CLASS] node has the `data` modifier keyword.
 *
 * In the Kotlin PSI AST, `data class Foo(...)` has a [DATA_KEYWORD] child inside
 * its [MODIFIER_LIST].
 */
private fun ASTNode.isDataClass(): Boolean {
    val modifierList = findChildByType(MODIFIER_LIST) ?: return false
    return modifierList.findChildByType(DATA_KEYWORD) != null
}

/**
 * Returns `true` if this node has an `@Immutable` annotation in its [MODIFIER_LIST].
 */
private fun ASTNode.hasImmutableAnnotation(): Boolean {
    val modifierList = findChildByType(MODIFIER_LIST) ?: return false
    var child = modifierList.firstChildNode
    while (child != null) {
        if (child.elementType == ANNOTATION_ENTRY && child.text.contains("Immutable")) {
            return true
        }
        child = child.treeNext
    }
    return false
}

/**
 * Checks whether the package is in the `presentation.api` layer only.
 */
private fun isPresentationApiPackage(packageDirectiveText: String): Boolean {
    val pkg = packageDirectiveText.removePrefix("package").trim()
    return pkg.contains(".presentation.api.") || pkg.endsWith(".presentation.api")
}
