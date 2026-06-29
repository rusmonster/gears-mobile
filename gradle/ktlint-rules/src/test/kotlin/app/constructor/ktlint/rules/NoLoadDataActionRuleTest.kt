package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class NoLoadDataActionRuleTest {
    private val assertThat = assertThatRule { NoLoadDataActionRule() }

    @Test
    fun `flags data object named LoadData implementing Action`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            sealed interface Action {
                data object LoadData : Action
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 5, "Action subtype 'LoadData' describes an implementation detail -- prefer user-intent names like 'Refresh', 'RetryLoad', 'OpenDetails'")
    }

    @Test
    fun `allows data object named Refresh`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            sealed interface Action {
                data object Refresh : Action
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores LoadData not implementing Action`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            sealed interface Command {
                data object LoadData : Command
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
