package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS_BODY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces that every method on an `interface` in `domain` and `presentation.api` packages
 * has a KDoc comment.
 *
 * **Spec reference:** FeatureSpecification.md §13 -- "Every method on those interfaces has
 * a KDoc comment (at minimum a one-line summary)."
 *
 * **Scope:** Same package filter as [MissingInterfaceKDocRule]:
 * - `*.domain.*` / `.domain`
 * - `*.presentation.api.*` / `.presentation.api`
 *
 * **What it checks (AST):**
 * When visiting a [FUN] node, the rule walks up the tree to determine if the function is
 * a direct member of an `interface` body (i.e., parent is [CLASS_BODY] whose parent is a
 * [CLASS] node with an `INTERFACE_KEYWORD`). If so, it checks for a `KDOC` child on the
 * [FUN] node. Functions inside `class`, `object`, or `companion object` bodies are ignored.
 *
 * **Auto-correct:** No -- KDoc content requires human authoring.
 */
class MissingMethodKDocRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:missing-method-kdoc"),
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
            isInScope = isPackageInScope(node.text)
            return
        }

        if (!isInScope) return

        if (node.elementType == FUN && node.isDirectInterfaceMember()) {
            if (!node.hasKDoc()) {
                val name = node.findChildByType(IDENTIFIER)?.text ?: "<unknown>"
                emit(
                    node.startOffset,
                    "Method '$name' on interface in domain/presentation:api must have a KDoc comment (§13 FeatureSpec)",
                    false,
                )
            }
        }
    }
}

/**
 * Returns `true` if this [FUN] node is a direct member of an `interface` body.
 *
 * Walks the AST parent chain: [FUN] → [CLASS_BODY] → [CLASS] (with interface keyword).
 * This ensures we only flag methods declared directly in interfaces, not in nested classes,
 * companion objects, or other non-interface bodies.
 */
private fun ASTNode.isDirectInterfaceMember(): Boolean {
    val classBody = treeParent ?: return false
    if (classBody.elementType != CLASS_BODY) return false
    val classNode = classBody.treeParent ?: return false
    return classNode.elementType == CLASS && classNode.isInterface()
}
