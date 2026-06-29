package app.constructor.ktlint.rules

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId

/**
 * Entry point for the custom Constructor SDK ktlint rule set.
 *
 * This provider is discovered at runtime via the standard [java.util.ServiceLoader] mechanism.
 * The META-INF/services file references this class so that ktlint picks up all rules
 * defined in [getRuleProviders].
 *
 * Rule set ID: `constructor`
 * Individual rule IDs follow the pattern `constructor:<rule-name>`.
 */
internal val RULE_SET_ID = "constructor"

class ConstructorRuleSetProvider : RuleSetProviderV3(RuleSetId(RULE_SET_ID)) {

    override fun getRuleProviders(): Set<RuleProvider> = setOf(
        //KDoc enforcement
        RuleProvider { MissingInterfaceKDocRule() },
        RuleProvider { MissingMethodKDocRule() },
        RuleProvider { MissingEnumEntryKDocRule() },
        //@Immutable enforcement + runCatching
        RuleProvider { MissingImmutableOnUiStateRule() },
        RuleProvider { MissingImmutableOnViewItemRule() },
        RuleProvider { NoRunCatchingRule() },
        //Naming conventions
        RuleProvider { FetchLoadGetNamingRule() },
        RuleProvider { BooleanVisibilityNamingRule() },
        RuleProvider { NoLoadDataActionRule() },
        //Formatting / structure
        RuleProvider { ViewItemNoRawTypesRule() },
        RuleProvider { ConstantsAtTopRule() },
        RuleProvider { NoHardcodedStringsInComposableRule() },
        //Class/layer naming conventions
        RuleProvider { ClassSuffixPackageRule() },
        //UI pattern enforcement + KDoc gaps
        RuleProvider { NoUiStateCastRule() },
        RuleProvider { MissingSealedSubtypeKDocRule() },
        RuleProvider { MissingUiStatePropertyKDocRule() },
        //Test + naming polish + layer isolation
        RuleProvider { TestMethodNamingRule() },
        RuleProvider { DomainNoDataOrPresentationImportRule() },
    )
}
