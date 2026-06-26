package app.constructor.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CONST_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Enforces that file-level `const val` and `private val` declarations appear before
 * any class, object, or function declarations in the same file.
 *
 * **Spec reference:** MR review rule -- "Constants and file-level private vals go at the
 * top of the file, before class/function declarations."
 *
 * **Scope:** All Kotlin files -- this is a project-wide formatting rule.
 *
 * **What it checks (AST):**
 * When visiting the [FILE] node, the rule iterates its direct children in order.
 * It tracks whether any "declaration" node ([CLASS], [OBJECT_DECLARATION], [FUN]) has
 * been seen. If a file-level [PROPERTY] with `const` or `private` modifier appears
 * *after* a declaration, the rule emits a warning.
 *
 * Only top-level properties are checked -- properties inside classes or objects are
 * not affected.
 *
 * **Auto-correct:** No -- reordering declarations may change initialization order
 * and is best done manually.
 */
class ConstantsAtTopRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:constants-at-top"),
        about = About(
            maintainer = "Constructor",
        ),
    ) {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        if (node.elementType != FILE) return

        var seenDeclaration = false
        var child = node.firstChildNode

        while (child != null) {
            when (child.elementType) {
                CLASS, OBJECT_DECLARATION, FUN -> {
                    seenDeclaration = true
                }
                PROPERTY -> {
                    if (seenDeclaration && isConstOrPrivateVal(child)) {
                        val propName = child.findChildByType(IDENTIFIER)?.text ?: "?"
                        emit(
                            child.startOffset,
                            "File-level property '$propName' should be declared before " +
                                "class/object/function declarations",
                            false,
                        )
                    }
                }
            }
            child = child.treeNext
        }
    }
}

/**
 * Returns `true` if this [PROPERTY] node has either a `const` or `private` modifier,
 * indicating it is a file-level constant or private property that should appear at the top.
 */
private fun isConstOrPrivateVal(property: ASTNode): Boolean {
    val modifierList = property.findChildByType(MODIFIER_LIST) ?: return false
    return modifierList.findChildByType(CONST_KEYWORD) != null ||
        modifierList.findChildByType(PRIVATE_KEYWORD) != null
}
