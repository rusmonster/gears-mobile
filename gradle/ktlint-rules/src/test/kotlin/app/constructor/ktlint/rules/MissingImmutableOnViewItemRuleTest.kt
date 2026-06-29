package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class MissingImmutableOnViewItemRuleTest {
    private val assertThat = assertThatRule { MissingImmutableOnViewItemRule() }

    @Test
    fun `flags ViewItem data class without @Immutable`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            data class CourseViewItem(val title: String)
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 1, "ViewItem data class 'CourseViewItem' must be annotated with @Immutable (§8 FeatureSpec)")
    }

    @Test
    fun `allows ViewItem with @Immutable`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            @Immutable
            data class CourseViewItem(val title: String)
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores non-ViewItem classes`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            data class Course(val id: String)
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
