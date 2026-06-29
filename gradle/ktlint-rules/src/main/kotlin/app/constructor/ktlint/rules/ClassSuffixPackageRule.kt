package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces that classes and interfaces with specific naming suffixes are located
 * in the correct architectural layer package.
 *
 * **Spec reference:** FeatureSpecification.md §6 -- Classes and Files naming table.
 *
 * **What it checks (AST):**
 * For each [CLASS] node (which covers both classes and interfaces in the Kotlin PSI),
 * the rule extracts the class/interface name and checks it against a table of
 * [SUFFIX_RULES]. Each entry maps a name suffix to a required package pattern and
 * whether the declaration must be an interface or a class.
 *
 * Matching is done longest-suffix-first to avoid false positives (e.g. `CacheImpl`
 * is matched before `Cache`). Interface-only entries skip non-interface declarations
 * and vice versa, so `CourseCache` (interface) and `CourseCacheImpl` (class) are
 * checked independently.
 *
 * **Scope:** Feature packages only (`app.constructor.csdk.<feature>.*`). Classes in
 * `common` packages are excluded to avoid false positives on shared utilities like
 * `DateFormatter` or `LogMessageFormatter`.
 *
 * **Auto-correct:** No -- moving a class to a different package requires updating
 * all imports and references.
 */
class ClassSuffixPackageRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:class-suffix-package"),
        about = About(
            maintainer = "Constructor",
        ),
    ) {

    private var currentPackage: String = ""
    private var isFeaturePackage: Boolean = false

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        if (node.elementType == PACKAGE_DIRECTIVE) {
            currentPackage = node.text.removePrefix("package").trim()
            isFeaturePackage = isInFeaturePackage(currentPackage)
            return
        }

        if (!isFeaturePackage) return
        if (node.elementType != CLASS) return

        val className = node.findChildByType(IDENTIFIER)?.text ?: return
        val isInterface = node.isInterface()

        for (rule in SUFFIX_RULES) {
            if (!className.endsWith(rule.suffix)) continue
            if (rule.mustBeInterface && !isInterface) continue
            if (!rule.mustBeInterface && isInterface) continue

            if (!rule.packageMatcher(currentPackage)) {
                emit(
                    node.startOffset,
                    "'$className' has suffix '${rule.suffix}' -- " +
                        "expected in ${rule.expectedPackageDescription} package",
                    false,
                )
            }
            return
        }
    }
}

/**
 * A single suffix-to-package mapping entry.
 *
 * @property suffix The class/interface name suffix to match.
 * @property mustBeInterface `true` if this entry only applies to interfaces,
 *   `false` if it only applies to classes.
 * @property expectedPackageDescription Human-readable description of the expected package
 *   for error messages (e.g. ".data." or ".domain.").
 * @property packageMatcher Predicate that returns `true` if the current package is valid
 *   for this suffix.
 */
private data class SuffixRule(
    val suffix: String,
    val mustBeInterface: Boolean,
    val expectedPackageDescription: String,
    val packageMatcher: (String) -> Boolean,
)

/**
 * Suffix rules ordered longest-first so that e.g. `CacheImpl` matches before `Cache`,
 * and `ApiClientImpl` matches before `ApiClient`.
 */
private val SUFFIX_RULES = listOf(
    // Impl classes -> .data.
    SuffixRule("CacheImpl", false, ".data.", ::isDataPackage),
    SuffixRule("ApiClientImpl", false, ".data.", ::isDataPackage),
    SuffixRule("RepositoryImpl", false, ".data.", ::isDataPackage),
    // Response classes -> .domain.remote.
    SuffixRule("Response", false, ".domain.remote.", ::isDomainRemotePackage),
    // Mapper classes -> .data.
    SuffixRule("DtoMapper", false, ".data.", ::isDataPackage),
    SuffixRule("ResponseMapper", false, ".data.", ::isDataPackage),
    // Presentation:impl classes
    SuffixRule("ViewModelImpl", false, ".presentation.impl.", ::isPresentationImplPackage),
    SuffixRule("UseCase", false, ".presentation.impl.", ::isPresentationImplPackage),
    SuffixRule("Formatter", false, ".presentation.impl.", ::isPresentationImplPackage),
    // Domain interfaces
    SuffixRule("Cache", true, ".domain.", ::isDomainPkg),
    SuffixRule("Repository", true, ".domain.", ::isDomainPkg),
    SuffixRule("ApiClient", true, ".domain.", ::isDomainPkg),
)

/**
 * Returns `true` if the package belongs to a feature module, not `common`.
 * Feature packages follow `app.constructor.csdk.<feature>.<layer>...` where
 * `<feature>` is not `common`.
 */
private fun isInFeaturePackage(pkg: String): Boolean {
    if (!pkg.startsWith("app.constructor.csdk.")) return false
    val afterPrefix = pkg.removePrefix("app.constructor.csdk.")
    val featureSegment = afterPrefix.substringBefore('.')
    return featureSegment != "common" && featureSegment.isNotEmpty()
}

private fun isDataPackage(pkg: String): Boolean =
    pkg.contains(".data.") || pkg.endsWith(".data")

private fun isDomainRemotePackage(pkg: String): Boolean =
    pkg.contains(".domain.remote.") || pkg.endsWith(".domain.remote")

private fun isPresentationImplPackage(pkg: String): Boolean =
    pkg.contains(".presentation.impl.") || pkg.endsWith(".presentation.impl")

private fun isDomainPkg(pkg: String): Boolean =
    pkg.contains(".domain.") || pkg.endsWith(".domain")
