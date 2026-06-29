package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class MissingSealedSubtypeKDocRuleTest {
    private val assertThat = assertThatRule { MissingSealedSubtypeKDocRule() }

    @Test
    fun `flags data object without KDoc in sealed UiState`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            sealed interface UiState {
                data object Loading : UiState
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 5, "Sealed subtype 'Loading' in 'UiState' must have a KDoc comment (§13)")
    }

    @Test
    fun `allows subtype with KDoc`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            sealed interface UiState {
                /** Initial loading state. */
                data object Loading : UiState
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores sealed interface not ending in UiState Action or Event`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            sealed interface Result {
                data object Success : Result
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `flags in sealed Action hierarchy`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            sealed interface Action {
                data object Refresh : Action
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 5, "Sealed subtype 'Refresh' in 'Action' must have a KDoc comment (§13)")
    }

    @Test
    fun `ignores non-presentation-api packages`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.impl

            sealed interface UiState {
                data object Loading : UiState
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
