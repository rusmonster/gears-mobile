package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class MissingUiStatePropertyKDocRuleTest {
    private val assertThat = assertThatRule { MissingUiStatePropertyKDocRule() }

    @Test
    fun `flags property missing @property tag`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            /**
             * Data state.
             */
            data class Data(
                val items: List<String>,
            ) : UiState
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(7, 5, "Property 'items' in 'Data' is missing a @property KDoc tag (§13)")
    }

    @Test
    fun `allows property with @property tag`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            /**
             * Data state.
             * @property items the list of items.
             */
            data class Data(
                val items: List<String>,
            ) : UiState
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores data class not implementing UiState`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            data class ViewItem(
                val title: String,
            )
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
