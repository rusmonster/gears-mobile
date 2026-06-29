package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DATA_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Flags `Action` subtypes named `LoadData` -- MVI actions should describe **user intent**,
 * not implementation details like data loading.
 *
 * **Spec reference:** MR review rule -- "Action subtypes should describe user intent.
 * `LoadData` is an implementation detail; prefer names like `Refresh`, `RetryLoad`,
 * `OpenDetails`, etc."
 *
 * **Scope:** Only `presentation.api` packages, where MVI contracts live.
 *
 * **What it checks (AST):**
 * For each [CLASS] or [OBJECT_DECLARATION] node that:
 * 1. Has the `data` modifier keyword (is a `data class` or `data object`).
 * 2. Implements an interface whose name ends with `Action` (via [SUPER_TYPE_LIST]).
 * 3. Is named exactly `LoadData`.
 *
 * The rule emits a warning suggesting a more intent-descriptive name.
 *
 * **Auto-correct:** No -- renaming action subtypes requires updating all dispatchers.
 */
class NoLoadDataActionRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:no-load-data-action"),
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
        // Action subtypes can be data class or data object
        if (node.elementType != CLASS && node.elementType != OBJECT_DECLARATION) return

        val name = node.findChildByType(IDENTIFIER)?.text ?: return
        if (name != "LoadData") return

        // Must be a data class/object
        if (!node.isDataClassOrObject()) return

        // Must implement an interface ending in "Action"
        if (!node.implementsActionInterface()) return

        emit(
            node.startOffset,
            "Action subtype '$name' describes an implementation detail -- " +
                "prefer user-intent names like 'Refresh', 'RetryLoad', 'OpenDetails'",
            false,
        )
    }
}

/**
 * Returns `true` if this node has the `data` modifier keyword,
 * making it either a `data class` or `data object`.
 */
private fun ASTNode.isDataClassOrObject(): Boolean {
    val modifierList = findChildByType(MODIFIER_LIST) ?: return false
    return modifierList.findChildByType(DATA_KEYWORD) != null
}

/**
 * Returns `true` if this node's [SUPER_TYPE_LIST] contains a reference
 * to an interface whose name ends with `Action`.
 *
 * Matches patterns like `: Action`, `: SomeFeature.Action`, etc.
 */
private fun ASTNode.implementsActionInterface(): Boolean {
    val superTypeList = findChildByType(SUPER_TYPE_LIST) ?: return false
    return Regex("\\bAction\\b").containsMatchIn(superTypeList.text)
}

/**
 * Checks whether the package is in the `presentation.api` layer.
 */
private fun isPresentationApiPackage(packageDirectiveText: String): Boolean {
    val pkg = packageDirectiveText.removePrefix("package").trim()
    return pkg.contains(".presentation.api.") || pkg.endsWith(".presentation.api")
}
