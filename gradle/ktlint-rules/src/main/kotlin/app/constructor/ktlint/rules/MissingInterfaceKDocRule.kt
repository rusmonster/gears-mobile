package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces that every `interface` declaration in `domain` and `presentation.api` packages
 * has a KDoc comment.
 *
 * **Spec reference:** FeatureSpecification.md §13 -- "Every interface in domain and
 * presentation:api has a KDoc comment describing its purpose and contract."
 *
 * **Scope:** Only files whose package matches one of:
 * - `*.domain.*` or ends with `.domain`
 * - `*.presentation.api.*` or ends with `.presentation.api`
 *
 * **What it checks (AST):**
 * For each `CLASS` node that contains an `INTERFACE_KEYWORD` among its children,
 * the rule checks whether a `KDOC` node exists as a direct child.
 * If not, a violation is emitted.
 *
 * **Auto-correct:** No -- KDoc content requires human authoring.
 */
class MissingInterfaceKDocRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:missing-interface-kdoc"),
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

        if (node.elementType == CLASS && node.isInterface()) {
            if (!node.hasKDoc()) {
                val name = node.findChildByType(IDENTIFIER)?.text ?: "<unknown>"
                emit(
                    node.startOffset,
                    "Interface '$name' in domain/presentation:api must have a KDoc comment (§13 FeatureSpec)",
                    false,
                )
            }
        }
    }
}
