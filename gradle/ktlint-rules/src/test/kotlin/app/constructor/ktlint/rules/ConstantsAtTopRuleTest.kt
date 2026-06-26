package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ConstantsAtTopRuleTest {
    private val assertThat = assertThatRule { ConstantsAtTopRule() }

    @Test
    fun `flags const val after function`() {
        val code =
            """
            fun doWork() {}
            private const val MAX = 42
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 1, "File-level property 'MAX' should be declared before class/object/function declarations")
    }

    @Test
    fun `flags private val after class`() {
        val code =
            """
            class Foo
            private val TAG = "Foo"
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 1, "File-level property 'TAG' should be declared before class/object/function declarations")
    }

    @Test
    fun `allows const val before function`() {
        val code =
            """
            private const val MAX = 42
            fun doWork() {}
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows file with only properties`() {
        val code =
            """
            private const val A = 1
            private const val B = 2
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
