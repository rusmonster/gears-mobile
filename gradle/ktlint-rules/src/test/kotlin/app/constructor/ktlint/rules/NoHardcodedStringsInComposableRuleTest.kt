package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class NoHardcodedStringsInComposableRuleTest {
    private val assertThat = assertThatRule { NoHardcodedStringsInComposableRule() }

    @Test
    fun `flags hardcoded string in Text call inside Composable`() {
        val code =
            """
            @Composable
            fun MyScreen() {
                Text("Hello World")
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 10, "Hardcoded string in 'Text()' -- use string resources (getString(Res.string.*)) or pass via UiState instead")
    }

    @Test
    fun `allows string resource reference`() {
        val code =
            """
            @Composable
            fun MyScreen() {
                Text(stringResource(R.string.hello))
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores non-Composable functions`() {
        val code =
            """
            fun myHelper() {
                Text("Hello World")
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
