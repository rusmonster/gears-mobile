package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class NoRunCatchingRuleTest {
    private val assertThat = assertThatRule { NoRunCatchingRule() }

    @Test
    fun `flags runCatching standalone call`() {
        val code =
            """
            fun doWork() {
                runCatching { riskyOperation() }
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 5, "Use 'runCatchingCancellable' instead of 'runCatching' -- runCatching swallows CancellationException")
    }

    @Test
    fun `flags runCatching extension call`() {
        val code =
            """
            fun doWork() {
                result.runCatching { transform() }
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 12, "Use 'runCatchingCancellable' instead of 'runCatching' -- runCatching swallows CancellationException")
    }

    @Test
    fun `allows runCatchingCancellable`() {
        val code =
            """
            fun doWork() {
                runCatchingCancellable { riskyOperation() }
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows runCatching inside runCatchingCancellable implementation`() {
        val code =
            """
            fun runCatchingCancellable(block: () -> Unit) {
                runCatching { block() }
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
