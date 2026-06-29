package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class BooleanVisibilityNamingRuleTest {
    private val assertThat = assertThatRule { BooleanVisibilityNamingRule() }

    @Test
    fun `flags boolean without UI-intent suffix`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            data class CourseViewItem(
                val isNew: Boolean,
            )
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 5, "Boolean field 'isNew' in 'CourseViewItem' should use an explicit UI-intent name (e.g. is<Noun>Visible, is<Noun>Enabled) -- not raw state like 'isNew' or 'isCompleted'")
    }

    @Test
    fun `allows boolean with Visible suffix`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            data class CourseViewItem(
                val isBadgeVisible: Boolean,
            )
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows boolean with Enabled suffix`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            data class CourseViewItem(
                val isButtonEnabled: Boolean,
            )
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores non-boolean fields`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            data class CourseViewItem(
                val title: String,
            )
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores non-ViewItem classes`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            data class Course(
                val isNew: Boolean,
            )
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
