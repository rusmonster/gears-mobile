package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.INTERFACE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Returns `true` if this `CLASS` node represents an `interface` (including `sealed interface`).
 *
 * The Kotlin PSI AST represents both `class` and `interface` as `CLASS` nodes.
 * The distinction is made by looking for an [INTERFACE_KEYWORD] token among the node's
 * direct children.
 */
internal fun ASTNode.isInterface(): Boolean =
    findChildByType(INTERFACE_KEYWORD) != null

/**
 * Returns `true` if this node has a [KDOC] direct child.
 */
internal fun ASTNode.hasKDoc(): Boolean =
    findChildByType(KDOC) != null

/**
 * Checks whether the package directive text places this file in the `domain` or
 * `presentation.api` layer of the SDK.
 *
 * Matching patterns:
 * - `package ...domain` or `package ...domain.entity` (contains `.domain.` or ends with `.domain`)
 * - `package ...presentation.api` or `package ...presentation.api.something`
 *
 * Implementation packages like `presentation.impl` and `data` are intentionally excluded --
 * §13 of the spec states that implementation classes do not require KDoc.
 */
internal fun isPackageInScope(packageDirectiveText: String): Boolean {
    val pkg = packageDirectiveText
        .removePrefix("package")
        .trim()

    return pkg.contains(".domain.") ||
        pkg.endsWith(".domain") ||
        pkg.contains(".presentation.api.") ||
        pkg.endsWith(".presentation.api")
}

/**
 * Checks whether the package directive places this file in a `domain` layer package only.
 *
 * Narrower than [isPackageInScope] -- excludes `presentation.api`.
 * Used by rules that only apply to domain-layer code (e.g. enum entry KDoc).
 */
internal fun isDomainPackage(packageDirectiveText: String): Boolean {
    val pkg = packageDirectiveText
        .removePrefix("package")
        .trim()

    return pkg.contains(".domain.") || pkg.endsWith(".domain")
}
