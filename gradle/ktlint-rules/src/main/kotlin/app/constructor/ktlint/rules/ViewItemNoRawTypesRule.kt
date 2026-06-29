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
 * Flags raw/primitive numeric and date/time types in `ViewItem` data class fields.
 *
 * **Spec reference:** MR review rule -- "ViewItem fields should be pre-formatted for the UI.
 * Raw types like `Float`, `Double`, `Instant`, `LocalDate` belong in the domain layer;
 * the presentation layer should expose `String` or dedicated UI model types instead."
 *
 * **Scope:** Only `presentation.api` packages, only `data class` names ending in `ViewItem`.
 *
 * **What it checks (AST):**
 * For each [VALUE_PARAMETER] in the primary constructor of a matching data class, the rule
 * checks whether the parameter's type reference text matches any of the [DISALLOWED_TYPES].
 * The check uses word-boundary matching to avoid false positives on types like
 * `FloatingPointLayout` or `LocalDateRange`.
 *
 * **AST note:** The rule visits each [VALUE_PARAMETER] and walks up the tree
 * (`VALUE_PARAMETER → VALUE_PARAMETER_LIST → PRIMARY_CONSTRUCTOR → CLASS`)
 * to verify the enclosing class is a matching ViewItem data class. This bottom-up
 * approach enables `@Suppress` on individual parameters.
 *
 * **Auto-correct:** No -- replacing raw types requires introducing formatted alternatives
 * and updating mappers.
 */
class ViewItemNoRawTypesRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:viewitem-no-raw-types"),
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

        checkForRawType(node, className, emit)
    }

    /**
     * Checks a single constructor parameter for disallowed raw types.
     * The full parameter text (e.g. `val progress: Float`) is scanned against
     * each [DISALLOWED_TYPES] entry using word-boundary regex.
     */
    private fun checkForRawType(
        param: ASTNode,
        className: String,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        val paramText = param.text
        val paramName = param.findChildByType(IDENTIFIER)?.text ?: return

        // progressFraction: Float is OK for progress bars (§FeatureSpec MR convention)
        if (PROGRESS_FIELD_PATTERN.containsMatchIn(paramName)) return

        for (rawType in DISALLOWED_TYPES) {
            if (Regex("\\b${Regex.escape(rawType)}\\b").containsMatchIn(paramText)) {
                emit(
                    param.startOffset,
                    "ViewItem field '$paramName' in '$className' uses raw type '$rawType' -- " +
                        "prefer a pre-formatted String or dedicated UI type",
                    false,
                )
                return
            }
        }
    }
}

/**
 * Types that should not appear as fields in ViewItem data classes.
 * These are domain/data types that should be pre-formatted before reaching the UI.
 */
private val PROGRESS_FIELD_PATTERN = Regex("(?i)(progress|fraction)")

private val DISALLOWED_TYPES = listOf(
    "Float",
    "Double",
    "Instant",
    "LocalDate",
    "LocalDateTime",
    "LocalTime",
    "ZonedDateTime",
)

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
