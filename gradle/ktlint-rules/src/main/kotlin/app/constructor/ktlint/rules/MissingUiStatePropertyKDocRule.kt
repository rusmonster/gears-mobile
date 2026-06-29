package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DATA_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces that every property in `UiState.Data` data classes has documentation
 * via a `@property` tag in the class-level KDoc.
 *
 * **Spec reference:** FeatureSpecification.md §13, rule 3 -- Every property in a
 * `UiState.Data` data class should have a `@property` tag explaining what it
 * represents and when it is set.
 *
 * **What it checks (AST):**
 * For each [VALUE_PARAMETER], the rule walks up the tree to find the enclosing
 * `data class` in a `presentation.api` package that implements `UiState`. It then
 * reads the class-level KDoc and checks for a `@property <name>` tag matching the
 * parameter. This bottom-up approach enables `@Suppress` on individual parameters.
 *
 * **Scope:** `presentation.api` packages only.
 *
 * **Auto-correct:** No -- documentation must be written by a human.
 */
class MissingUiStatePropertyKDocRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:missing-uistate-property-kdoc"),
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
        if (node.elementType != VALUE_PARAMETER) return

        // Walk up: VALUE_PARAMETER → VALUE_PARAMETER_LIST → PRIMARY_CONSTRUCTOR → CLASS
        val classNode = node.treeParent
            ?.takeIf { it.elementType == VALUE_PARAMETER_LIST }
            ?.treeParent
            ?.takeIf { it.elementType == PRIMARY_CONSTRUCTOR }
            ?.treeParent
            ?.takeIf { it.elementType == CLASS }
            ?: return

        if (!classNode.isDataClassNode()) return
        if (!classNode.implementsUiState()) return

        val className = classNode.findChildByType(IDENTIFIER)?.text ?: return
        val kdocText = classNode.findChildByType(KDOC)?.text
        val paramName = node.findChildByType(IDENTIFIER)?.text ?: return

        if (!hasPropertyTag(kdocText, paramName)) {
            emit(
                node.startOffset,
                "Property '$paramName' in '$className' is missing a @property KDoc tag (§13)",
                false,
            )
        }
    }
}

/**
 * Returns `true` if the KDoc text contains a `@property <name>` tag.
 */
private fun hasPropertyTag(kdocText: String?, paramName: String): Boolean {
    if (kdocText == null) return false
    return Regex("@property\\s+${Regex.escape(paramName)}\\b").containsMatchIn(kdocText)
}

/**
 * Returns `true` if this [CLASS] node has `UiState` in its super type list.
 */
private fun ASTNode.implementsUiState(): Boolean {
    val superTypeList = findChildByType(SUPER_TYPE_LIST) ?: return false
    return superTypeList.text.contains("UiState")
}

/**
 * Returns `true` if this [CLASS] node has the `data` modifier.
 */
private fun ASTNode.isDataClassNode(): Boolean {
    val modifiers = findChildByType(MODIFIER_LIST) ?: return false
    return modifiers.findChildByType(DATA_KEYWORD) != null
}

private fun isPresentationApiPkg(packageDirectiveText: String): Boolean {
    val pkg = packageDirectiveText.removePrefix("package").trim()
    return pkg.contains(".presentation.api.") || pkg.endsWith(".presentation.api")
}
