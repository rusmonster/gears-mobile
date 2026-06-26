package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS_BODY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DATA_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SEALED_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces KDoc on `data object` and `data class` subtypes inside sealed
 * `UiState`, `Action`, and `Event` hierarchies.
 *
 * **Spec reference:** FeatureSpecification.md §13, rule 4 -- Every sealed subtype
 * must have a brief comment describing when it is emitted or shown.
 *
 * **What it checks (AST):**
 * For each [CLASS] or [OBJECT_DECLARATION] with the `data` modifier, the rule walks
 * up the tree to check if the parent is a sealed interface named `*UiState`, `*Action`,
 * or `*Event`. If so and the subtype lacks KDoc, a violation is emitted. This bottom-up
 * approach enables `@Suppress` on individual subtypes.
 *
 * **Scope:** `presentation.api` packages only.
 *
 * **Auto-correct:** No -- KDoc must be written by a human.
 */
class MissingSealedSubtypeKDocRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:missing-sealed-subtype-kdoc"),
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
            isInScope = isPresentationApiPkg(node.text)
            return
        }

        if (!isInScope) return
        if (node.elementType != CLASS && node.elementType != OBJECT_DECLARATION) return
        if (!node.hasDataModifier()) return

        // Walk up: subtype CLASS/OBJECT_DECLARATION → CLASS_BODY → sealed interface CLASS
        val sealedNode = node.treeParent
            ?.takeIf { it.elementType == CLASS_BODY }
            ?.treeParent
            ?.takeIf { it.elementType == CLASS && it.isSealedInterface() }
            ?: return

        val sealedName = sealedNode.findChildByType(IDENTIFIER)?.text ?: return
        if (!SEALED_HIERARCHY_NAMES.any { sealedName.endsWith(it) }) return

        if (!node.hasKDoc()) {
            val subtypeName = node.findChildByType(IDENTIFIER)?.text ?: "?"
            emit(
                node.startOffset,
                "Sealed subtype '$subtypeName' in '$sealedName' must have a KDoc comment (§13)",
                false,
            )
        }
    }
}

private val SEALED_HIERARCHY_NAMES = setOf("UiState", "Action", "Event")

/**
 * Returns `true` if this [CLASS] node is a `sealed interface`.
 */
private fun ASTNode.isSealedInterface(): Boolean {
    if (!isInterface()) return false
    val modifiers = findChildByType(MODIFIER_LIST) ?: return false
    return modifiers.findChildByType(SEALED_KEYWORD) != null
}

/**
 * Returns `true` if this node has the `data` modifier keyword.
 */
private fun ASTNode.hasDataModifier(): Boolean {
    val modifiers = findChildByType(MODIFIER_LIST) ?: return false
    return modifiers.findChildByType(DATA_KEYWORD) != null
}

private fun isPresentationApiPkg(packageDirectiveText: String): Boolean {
    val pkg = packageDirectiveText.removePrefix("package").trim()
    return pkg.contains(".presentation.api.") || pkg.endsWith(".presentation.api")
}
