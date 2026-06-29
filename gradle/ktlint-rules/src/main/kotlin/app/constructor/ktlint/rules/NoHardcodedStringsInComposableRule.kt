package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Flags hardcoded string literals passed to common Compose UI functions inside
 * `@Composable` functions.
 *
 * **Spec reference:** FeatureSpecification.md §2.4 / §12 -- All user-visible strings
 * must come from Compose resources (`composeResources/values/strings.xml`) via
 * `getString(Res.string.*)`. Composable functions should receive pre-formatted strings
 * through UiState, not hardcoded literals.
 *
 * **Scope:** All Kotlin files -- any file may contain `@Composable` functions.
 *
 * **What it checks (AST):**
 * 1. Finds [FUN] nodes annotated with `@Composable`.
 * 2. Within those functions, looks for [CALL_EXPRESSION] nodes whose callee name
 *    matches one of [FLAGGED_CALLEES] (`Text`, `placeholder`, `contentDescription`,
 *    `label`, `title`).
 * 3. If any [VALUE_ARGUMENT] of the call contains a [STRING_TEMPLATE] (string literal),
 *    the rule emits a warning.
 *
 * **Limitations:** This is a heuristic check -- it flags string literals in argument
 * positions of known UI functions. It does not perform type resolution, so it may
 * miss calls via aliases or flag non-UI overloads. Named arguments like
 * `contentDescription = "..."` are caught because the argument value still contains
 * a [STRING_TEMPLATE].
 *
 * **Auto-correct:** No -- replacing hardcoded strings requires creating string resources
 * and updating formatters.
 */
class NoHardcodedStringsInComposableRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:no-hardcoded-strings-composable"),
        about = About(
            maintainer = "Constructor",
        ),
    ) {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        if (node.elementType != FUN) return
        if (!node.hasComposableAnnotation()) return

        // Walk the entire subtree of this @Composable function
        checkSubtreeForHardcodedStrings(node, emit)
    }

    /**
     * Recursively walks the AST subtree looking for [CALL_EXPRESSION] nodes
     * that match [FLAGGED_CALLEES] and contain string literal arguments.
     */
    private fun checkSubtreeForHardcodedStrings(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        if (node.elementType == CALL_EXPRESSION) {
            val calleeName = node.findChildByType(REFERENCE_EXPRESSION)?.text
            if (calleeName != null && calleeName in FLAGGED_CALLEES) {
                val argList = node.findChildByType(VALUE_ARGUMENT_LIST)
                if (argList != null) {
                    checkArgumentsForStrings(argList, calleeName, emit)
                }
            }
        }

        var child = node.firstChildNode
        while (child != null) {
            checkSubtreeForHardcodedStrings(child, emit)
            child = child.treeNext
        }
    }

    /**
     * Checks each [VALUE_ARGUMENT] in the argument list for [STRING_TEMPLATE] nodes.
     */
    private fun checkArgumentsForStrings(
        argList: ASTNode,
        calleeName: String,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        var arg = argList.firstChildNode
        while (arg != null) {
            if (arg.elementType == VALUE_ARGUMENT && containsStringTemplate(arg)) {
                emit(
                    arg.startOffset,
                    "Hardcoded string in '$calleeName()' -- use string resources " +
                        "(getString(Res.string.*)) or pass via UiState instead",
                    false,
                )
            }
            arg = arg.treeNext
        }
    }
}

/**
 * Compose UI function names that should not receive hardcoded string literals.
 */
private val FLAGGED_CALLEES = setOf(
    "Text",
    "placeholder",
    "contentDescription",
    "label",
    "title",
)

/**
 * Returns `true` if this [FUN] node has a `@Composable` annotation.
 * Checks the modifier list for an annotation entry whose text contains "Composable".
 */
private fun ASTNode.hasComposableAnnotation(): Boolean {
    val text = this.text
    // Quick short-circuit: if "Composable" doesn't appear at all, skip
    if (!text.contains("Composable")) return false

    // Walk the function's direct children looking for MODIFIER_LIST → ANNOTATION_ENTRY
    var child = firstChildNode
    while (child != null) {
        if (child.elementType.toString() == "MODIFIER_LIST") {
            var annotation = child.firstChildNode
            while (annotation != null) {
                if (annotation.elementType.toString() == "ANNOTATION_ENTRY" &&
                    annotation.text.contains("Composable")
                ) {
                    return true
                }
                annotation = annotation.treeNext
            }
        }
        child = child.treeNext
    }
    return false
}

/**
 * Returns `true` if the given node or any of its descendants is a [STRING_TEMPLATE].
 */
private fun containsStringTemplate(node: ASTNode): Boolean {
    if (node.elementType == STRING_TEMPLATE) return true
    var child = node.firstChildNode
    while (child != null) {
        if (containsStringTemplate(child)) return true
        child = child.treeNext
    }
    return false
}
