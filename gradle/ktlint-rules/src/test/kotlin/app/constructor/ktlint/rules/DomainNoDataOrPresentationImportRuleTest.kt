package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class DomainNoDataOrPresentationImportRuleTest {
    private val assertThat = assertThatRule { DomainNoDataOrPresentationImportRule() }

    @Test
    fun `flags data import in domain package`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            import app.constructor.csdk.home.data.repository.CourseRepositoryImpl
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 1, "Domain code must not import from '.data.' packages -- domain has zero dependencies on data layer (§2.1)")
    }

    @Test
    fun `flags presentation import in domain package`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            import app.constructor.csdk.home.presentation.api.HomeScreen
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 1, "Domain code must not import from '.presentation.' packages -- domain has zero dependencies on presentation layer (§2.1)")
    }

    @Test
    fun `allows domain-to-domain imports`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            import app.constructor.csdk.home.domain.entity.Course
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows third-party imports in domain`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            import kotlinx.coroutines.flow.Flow
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores data imports in non-domain packages`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.impl

            import app.constructor.csdk.home.data.repository.CourseRepositoryImpl
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
