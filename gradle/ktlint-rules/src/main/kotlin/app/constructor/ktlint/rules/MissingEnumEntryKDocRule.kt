package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces that every entry in an `enum class` within `domain` packages has a KDoc comment.
 *
 * **Spec reference:** FeatureSpecification.md §13 -- "Domain entities (data class, enums)
 * must document non-obvious fields and enum values."
 *
 * **Scope:** Only files whose package matches:
 * - `*.domain.*` or ends with `.domain`
 *
 * Note: enums in `presentation.api` are intentionally excluded here because §13 only
 * requires KDoc on *sealed subtypes* in presentation:api (Action, UiState, Event), not
 * on enum entries. Sealed subtype KDoc is planned for a separate rule.
 *
 * **What it checks (AST):**
 * For each [ENUM_ENTRY] node, the rule checks whether it has a `KDOC` direct child.
 * The package-scope filter ensures only domain-layer enums are checked.
 *
 * **Auto-correct:** No -- KDoc content requires human authoring.
 */
class MissingEnumEntryKDocRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:missing-enum-entry-kdoc"),
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
            isInScope = isDomainPackage(node.text)
            return
        }

        if (!isInScope) return

        if (node.elementType == ENUM_ENTRY) {
            if (!node.hasKDoc()) {
                val name = node.findChildByType(IDENTIFIER)?.text ?: "<unknown>"
                emit(
                    node.startOffset,
                    "Enum entry '$name' in domain must have a KDoc comment (§13 FeatureSpec)",
                    false,
                )
            }
        }
    }
}
