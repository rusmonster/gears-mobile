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
 * Enforces the `fetch` / `load` / `get` method naming convention on domain interfaces.
 *
 * **Spec reference:** FeatureSpecification.md §6 / §14.4 --
 *
 * | Prefix     | Meaning                                       | Allowed on                        |
 * |------------|-----------------------------------------------|-----------------------------------|
 * | `fetch*`   | Returns data from **local cache** only         | Cache, LocalStorage, Repository   |
 * | `load*`    | Fetches from **remote** only                   | ApiClient, Repository             |
 * | `get*`     | Cache first, falls back to remote if empty     | Repository only                   |
 * | `upsert*`  | Insert or update in local cache                | Cache, LocalStorage               |
 * | `delete*`  | Remove from local cache                        | Cache, LocalStorage               |
 * | `clear*`   | Clear all data in local cache                  | Cache, LocalStorage               |
 *
 * **Scope:** Only files whose package matches `*.domain.*` or ends with `.domain`.
 *
 * **What it checks (AST):**
 * For each [FUN] node that is a direct member of an `interface` body, the rule:
 * 1. Determines the interface "kind" from its name suffix (`Repository`, `Cache`,
 *    `LocalStorage`, `ApiClient`).
 * 2. Checks the function name prefix (`fetch`, `load`, `get`) against the allowed
 *    prefixes for that interface kind.
 *
 * Interfaces that don't match any known suffix are ignored -- the rule only applies
 * to the four standard domain interface types.
 *
 * **Auto-correct:** No -- renaming methods requires updating all call sites.
 */
class FetchLoadGetNamingRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:fetch-load-get-naming"),
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
        if (node.elementType != FUN) return

        // Only check methods directly inside an interface body
        val classBody = node.treeParent ?: return
        if (classBody.elementType != CLASS_BODY) return
        val classNode = classBody.treeParent ?: return
        if (classNode.elementType != CLASS || !classNode.isInterface()) return

        val interfaceName = classNode.findChildByType(IDENTIFIER)?.text ?: return
        val methodName = node.findChildByType(IDENTIFIER)?.text ?: return

        val kind = classifyInterface(interfaceName) ?: return
        val prefix = extractPrefix(methodName) ?: return

        if (prefix !in kind.allowedPrefixes) {
            emit(
                node.startOffset,
                "${kind.label} should not have '$prefix*' methods -- " +
                    "allowed prefixes: ${kind.allowedPrefixes.joinToString()} (§6 FeatureSpec)",
                false,
            )
        }
    }
}

/**
 * Represents a category of domain interface with its allowed method prefixes.
 *
 * @property label Human-readable name for error messages (e.g. "ApiClient").
 * @property allowedPrefixes The set of method name prefixes this interface type may use.
 */
private enum class InterfaceKind(val label: String, val allowedPrefixes: Set<String>) {
    REPOSITORY("Repository", setOf("fetch", "load", "get")),
    CACHE("Cache", setOf("fetch", "upsert", "delete", "clear")),
    LOCAL_STORAGE("LocalStorage", setOf("fetch", "upsert", "delete", "clear")),
    API_CLIENT("ApiClient", setOf("load")),
}

/**
 * Determines the [InterfaceKind] from the interface name suffix.
 *
 * Matching order matters -- `ApiClient` must be checked before a hypothetical `Client` suffix.
 * Returns `null` for interfaces that don't match any known pattern (those are ignored).
 */
private fun classifyInterface(name: String): InterfaceKind? = when {
    name.endsWith("Repository") -> InterfaceKind.REPOSITORY
    name.endsWith("Cache") -> InterfaceKind.CACHE
    name.endsWith("LocalStorage") -> InterfaceKind.LOCAL_STORAGE
    name.endsWith("ApiClient") -> InterfaceKind.API_CLIENT
    else -> null
}

/**
 * Extracts the method name prefix for validation.
 *
 * Recognizes read prefixes (`fetch`, `load`, `get`) and write prefixes
 * (`upsert`, `delete`, `clear`, `save`, `insert`, `update`, `remove`, `store`).
 * Write prefixes that are not in the allowed set for a given interface kind
 * will be flagged by the rule.
 *
 * Returns `null` if the method name doesn't start with any recognized prefix,
 * meaning the rule doesn't apply to that method.
 */
private fun extractPrefix(methodName: String): String? = when {
    methodName.startsWith("fetch") -> "fetch"
    methodName.startsWith("load") -> "load"
    methodName.startsWith("get") -> "get"
    methodName.startsWith("upsert") -> "upsert"
    methodName.startsWith("delete") -> "delete"
    methodName.startsWith("clear") -> "clear"
    methodName.startsWith("save") -> "save"
    methodName.startsWith("insert") -> "insert"
    methodName.startsWith("update") -> "update"
    methodName.startsWith("remove") -> "remove"
    methodName.startsWith("store") -> "store"
    else -> null
}
