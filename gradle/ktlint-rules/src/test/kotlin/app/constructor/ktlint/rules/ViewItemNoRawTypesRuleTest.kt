package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ViewItemNoRawTypesRuleTest {
    private val assertThat = assertThatRule { ViewItemNoRawTypesRule() }

    @Test
    fun `flags Float field in ViewItem`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            data class CourseViewItem(
                val rating: Float,
            )
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 5, "ViewItem field 'rating' in 'CourseViewItem' uses raw type 'Float' -- prefer a pre-formatted String or dedicated UI type")
    }

    @Test
    fun `allows progress or fraction Float fields in ViewItem`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            data class CourseViewItem(
                val progress: Float,
                val progressFraction: Float,
                val downloadFraction: Double,
            )
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `flags Double field in ViewItem`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            data class ScoreViewItem(
                val score: Double,
            )
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 5, "ViewItem field 'score' in 'ScoreViewItem' uses raw type 'Double' -- prefer a pre-formatted String or dedicated UI type")
    }

    @Test
    fun `allows String field in ViewItem`() {
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
                val progress: Float,
            )
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores non-presentation-api packages`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            data class CourseViewItem(
                val progress: Float,
            )
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
