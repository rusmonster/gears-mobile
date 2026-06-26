package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DATA_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces that boolean fields in `ViewItem` data classes use explicit visibility/state
 * naming: `is<Noun>Visible`, `is<Noun>Enabled`, or similar UI-intent suffixes.
 *
 * **Spec reference:** MR review rule -- "Boolean visibility fields should be explicit:
 * `isNewBadgeVisible` not `isNew`."
 *
 * **Scope:** Only `presentation.api` packages, only `data class` names ending in `ViewItem`.
 *
 * **What it checks (AST):**
 * For each [VALUE_PARAMETER] in the primary constructor of a matching data class, if the
 * parameter name starts with `is` and its type is `Boolean`, the rule checks that the name
 * ends with one of the allowed suffixes: `Visible`, `Enabled`, `Selected`, `Checked`,
 * `Expanded`, `Refreshing`, `Loading`.
 *
 * Names like `isProgressBadgeVisible` pass; names like `isNew` or `isCourseCompleted` fail.
 *
 * **AST note:** The rule visits each [VALUE_PARAMETER] and walks up the tree
 * (`VALUE_PARAMETER → VALUE_PARAMETER_LIST → PRIMARY_CONSTRUCTOR → CLASS`)
 * to verify the enclosing class is a matching ViewItem data class. This bottom-up
 * approach enables `@Suppress` on individual parameters.
 *
 * **Auto-correct:** No -- renaming fields requires updating all usages.
 */
class BooleanVisibilityNamingRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:boolean-visibility-naming"),
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
        if (node.elementType != VALUE_PARAMETER) return

        // Walk up: VALUE_PARAMETER → VALUE_PARAMETER_LIST → PRIMARY_CONSTRUCTOR → CLASS
        val classNode = node.treeParent
            ?.takeIf { it.elementType == VALUE_PARAMETER_LIST }
            ?.treeParent
            ?.takeIf { it.elementType == PRIMARY_CONSTRUCTOR }
            ?.treeParent
            ?.takeIf { it.elementType == CLASS }
            ?: return

        val className = classNode.findChildByType(IDENTIFIER)?.text ?: return
        if (!className.endsWith("ViewItem")) return
        if (!classNode.isDataClass()) return

        checkBooleanParam(node, className, emit)
    }

    /**
     * Checks a single constructor parameter. If it's a `Boolean` field starting with `is`,
     * verifies it ends with an allowed UI-intent suffix.
     */
    private fun checkBooleanParam(
        param: ASTNode,
        className: String,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        val paramName = param.findChildByType(IDENTIFIER)?.text ?: return

        // Only check fields starting with "is" (boolean naming convention)
        if (!paramName.startsWith("is")) return

        // Only check Boolean-typed parameters
        if (!param.text.contains("Boolean")) return

        if (!hasAllowedSuffix(paramName)) {
            emit(
                param.startOffset,
                "Boolean field '$paramName' in '$className' should use an explicit UI-intent name " +
                    "(e.g. is<Noun>Visible, is<Noun>Enabled) -- not raw state like 'isNew' or 'isCompleted'",
                false,
            )
        }
    }
}

/**
 * Allowed suffixes for boolean ViewItem fields. These clearly express UI intent
 * rather than leaking domain state into the view layer.
 */
private val ALLOWED_SUFFIXES = listOf(
    "Visible",
    "Enabled",
    "Selected",
    "Checked",
    "Expanded",
    "Refreshing",
    "Loading",
)

/**
 * Returns `true` if the boolean field name ends with one of the [ALLOWED_SUFFIXES].
 */
private fun hasAllowedSuffix(name: String): Boolean =
    ALLOWED_SUFFIXES.any { name.endsWith(it) }

/**
 * Returns `true` if this [CLASS] node has the `data` modifier keyword.
 */
private fun ASTNode.isDataClass(): Boolean {
    val modifierList = findChildByType(MODIFIER_LIST) ?: return false
    return modifierList.findChildByType(DATA_KEYWORD) != null
}

/**
 * Checks whether the package is in the `presentation.api` layer.
 */
private fun isPresentationApiPackage(packageDirectiveText: String): Boolean {
    val pkg = packageDirectiveText.removePrefix("package").trim()
    return pkg.contains(".presentation.api.") || pkg.endsWith(".presentation.api")
}
