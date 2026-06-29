package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SEALED_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces that `sealed interface UiState` and all its concrete subtypes are annotated
 * with `@Immutable` in `presentation.api` packages.
 *
 * **Spec reference:** FeatureSpecification.md §8 --
 * 1. Every `sealed interface UiState` is annotated `@Immutable`.
 * 2. Every concrete subtype (`data object`, `data class`) is also annotated individually.
 *
 * **Scope:** Only files whose package matches:
 * - `*.presentation.api.*` or ends with `.presentation.api`
 *
 * **What it checks (AST):**
 * 1. For each `CLASS` node that is a `sealed interface` named `UiState`, checks for an
 *    `@Immutable` annotation in its [MODIFIER_LIST].
 * 2. For each `CLASS` node (data class / data object) whose [SUPER_TYPE_LIST] references
 *    `UiState`, checks for the same annotation.
 *
 * The rule looks for the short name `Immutable` in [ANNOTATION_ENTRY] nodes -- this matches
 * `app.constructor.csdk.annotations.Immutable` regardless of import alias.
 *
 * **Auto-correct:** No -- adding annotations changes public API surface and should be
 * a conscious decision.
 */
class MissingImmutableOnUiStateRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:missing-immutable-uistate"),
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
        // UiState subtypes can be CLASS (data class) or OBJECT_DECLARATION (data object)
        if (node.elementType != CLASS && node.elementType != OBJECT_DECLARATION) return

        val name = node.findChildByType(IDENTIFIER)?.text ?: return

        // Check 1: sealed interface UiState itself
        if (name == "UiState" && node.isSealed() && node.isInterface()) {
            if (!node.hasAnnotation("Immutable")) {
                emit(
                    node.startOffset,
                    "sealed interface 'UiState' must be annotated with @Immutable (§8 FeatureSpec)",
                    false,
                )
            }
        }

        // Check 2: any class/object that implements UiState
        if (node.implementsInterface("UiState")) {
            if (!node.hasAnnotation("Immutable")) {
                emit(
                    node.startOffset,
                    "UiState subtype '$name' must be annotated with @Immutable (§8 FeatureSpec)",
                    false,
                )
            }
        }
    }
}

/**
 * Returns `true` if this [CLASS] node has a `sealed` modifier keyword.
 *
 * In the Kotlin PSI AST, modifier keywords like `sealed` live inside the [MODIFIER_LIST]
 * child of a [CLASS] node, not as direct children of [CLASS] itself.
 */
private fun ASTNode.isSealed(): Boolean {
    val modifierList = findChildByType(MODIFIER_LIST) ?: return false
    return modifierList.findChildByType(SEALED_KEYWORD) != null
}

/**
 * Returns `true` if this [CLASS] node's [SUPER_TYPE_LIST] contains a reference to [name].
 *
 * Uses a simple text-based check on the super type list node's text content.
 * This matches `UiState` in `data class Foo(...) : UiState` and `data object Bar : UiState`.
 *
 * The regex ensures we match whole words only (e.g. `\bUiState\b`) to avoid false positives
 * on names like `MyUiStateHelper`.
 */
private fun ASTNode.implementsInterface(name: String): Boolean {
    val superTypeList = findChildByType(SUPER_TYPE_LIST) ?: return false
    return Regex("\\b${Regex.escape(name)}\\b").containsMatchIn(superTypeList.text)
}

/**
 * Returns `true` if this node has an annotation with the given short [name]
 * (e.g. `"Immutable"`) in its [MODIFIER_LIST].
 *
 * Scans [ANNOTATION_ENTRY] children of the [MODIFIER_LIST] and checks if any
 * annotation's text contains [name]. Works with both `@Immutable` and
 * `@app.constructor.csdk.annotations.Immutable`.
 */
private fun ASTNode.hasAnnotation(name: String): Boolean {
    val modifierList = findChildByType(MODIFIER_LIST) ?: return false
    var child = modifierList.firstChildNode
    while (child != null) {
        if (child.elementType == ANNOTATION_ENTRY && child.text.contains(name)) {
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
