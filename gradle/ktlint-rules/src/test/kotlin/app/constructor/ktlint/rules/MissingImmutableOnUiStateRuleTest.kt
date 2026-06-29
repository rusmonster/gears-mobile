package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class MissingImmutableOnUiStateRuleTest {
    private val assertThat = assertThatRule { MissingImmutableOnUiStateRule() }

    @Test
    fun `flags data class UiState subtype without @Immutable`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            sealed interface UiState {
                data class Data(val x: String) : UiState
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(3, 1, "sealed interface 'UiState' must be annotated with @Immutable (§8 FeatureSpec)"),
                LintViolation(4, 5, "UiState subtype 'Data' must be annotated with @Immutable (§8 FeatureSpec)"),
            )
    }

    @Test
    fun `allows data class with @Immutable`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            @Immutable
            sealed interface UiState {
                @Immutable
                data class Data(val x: String) : UiState
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores non-presentation-api packages`() {
        val code =
            """
            package app.constructor.csdk.home.data

            sealed interface UiState {
                data class Data(val x: String) : UiState
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
