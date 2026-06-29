package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Flags usages of `runCatching` and suggests `runCatchingCancellable` instead.
 *
 * **Why:** `runCatching` silently swallows `CancellationException`, which breaks structured
 * concurrency in coroutines. The project provides `runCatchingCancellable` in `:common:common`
 * as a safe alternative that re-throws `CancellationException`.
 *
 * **Scope:** All Kotlin files -- this is a project-wide rule, not layer-specific.
 *
 * **What it checks (AST):**
 * Looks for [CALL_EXPRESSION] nodes whose first child is a [REFERENCE_EXPRESSION] with
 * text exactly `"runCatching"`. This catches both standalone calls (`runCatching { ... }`)
 * and extension calls (`foo.runCatching { ... }`).
 *
 * Calls to `runCatchingCancellable` are NOT flagged -- the [REFERENCE_EXPRESSION] text
 * won't match since we check for exact equality, not a substring.
 *
 * **Legitimate exceptions:** The implementation of `runCatchingCancellable` itself uses
 * `runCatching` internally (to safely invoke `onCancel`). The rule automatically
 * skips `runCatching` calls inside a function named `runCatchingCancellable`.
 *
 * **Auto-correct:** No -- switching to `runCatchingCancellable` may require adding the
 * import and potentially adjusting parameters (`onCancel`, `finally`).
 */
class NoRunCatchingRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:no-run-catching"),
        about = About(
            maintainer = "Constructor",
        ),
    ) {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        if (node.elementType != REFERENCE_EXPRESSION) return
        if (node.text != "runCatching") return

        // Ensure this is actually a call, not just a reference in an import or type position.
        // The parent should be a CALL_EXPRESSION or a DOT_QUALIFIED_EXPRESSION containing one.
        val parent = node.treeParent ?: return
        if (parent.elementType != CALL_EXPRESSION && parent.elementType != DOT_QUALIFIED_EXPRESSION) return

        // Skip if inside the runCatchingCancellable implementation itself
        if (isInsideRunCatchingCancellable(node)) return

        emit(
            node.startOffset,
            "Use 'runCatchingCancellable' instead of 'runCatching' -- runCatching swallows CancellationException",
            false,
        )
    }
}

/**
 * Walks up the AST to find an enclosing [FUN] node named `runCatchingCancellable`.
 * Returns `true` if this `runCatching` call is inside the implementation of the safe alternative.
 */
private fun isInsideRunCatchingCancellable(node: ASTNode): Boolean {
    var current = node.treeParent
    while (current != null) {
        if (current.elementType == FUN) {
            val name = current.findChildByType(IDENTIFIER)?.text
            return name == "runCatchingCancellable"
        }
        current = current.treeParent
    }
    return false
}
