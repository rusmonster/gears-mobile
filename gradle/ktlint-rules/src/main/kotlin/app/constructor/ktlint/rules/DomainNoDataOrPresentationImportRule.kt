package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces that domain-layer code does not import from `data` or `presentation` packages.
 *
 * **Spec reference:** FeatureSpecification.md §2.1 / §5 -- The domain module has
 * zero internal dependencies. Domain code must not reference data-layer or
 * presentation-layer types to maintain clean architecture boundaries.
 *
 * **What it checks (AST):**
 * 1. Determines if the file is in a `.domain.` package via [PACKAGE_DIRECTIVE].
 * 2. For each [IMPORT_DIRECTIVE], checks if the imported package contains `.data.`
 *    or `.presentation.` segments.
 * 3. Only flags imports from the same project namespace (`app.constructor.csdk.`)
 *    to avoid false positives on third-party libraries.
 *
 * **Scope:** Domain packages only (`*.domain.*` or ending in `.domain`).
 *
 * **Auto-correct:** No -- fixing layer violations requires architectural refactoring.
 */
class DomainNoDataOrPresentationImportRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:domain-no-data-or-presentation-import"),
        about = About(
            maintainer = "Constructor",
        ),
    ) {

    private var isInDomain = false

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        if (node.elementType == PACKAGE_DIRECTIVE) {
            isInDomain = isDomainPackage(node.text)
            return
        }

        if (!isInDomain) return
        if (node.elementType != IMPORT_DIRECTIVE) return

        val importText = node.text.removePrefix("import").trim()

        // Only check project-internal imports
        if (!importText.startsWith("app.constructor.csdk.")) return

        when {
            importText.contains(".data.") -> {
                emit(
                    node.startOffset,
                    "Domain code must not import from '.data.' packages -- " +
                        "domain has zero dependencies on data layer (§2.1)",
                    false,
                )
            }
            importText.contains(".presentation.") -> {
                emit(
                    node.startOffset,
                    "Domain code must not import from '.presentation.' packages -- " +
                        "domain has zero dependencies on presentation layer (§2.1)",
                    false,
                )
            }
        }
    }
}
