package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class TestMethodNamingRuleTest {
    private val assertThat = assertThatRule { TestMethodNamingRule() }

    @Test
    fun `flags test method without underscore in feature package`() {
        val code =
            """
            package app.constructor.csdk.home.domain.entity

            import kotlin.test.Test

            class HomeTest {
                @Test
                fun testSomething() {}
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(6, 5, "Test method 'testSomething' should follow 'method_condition[_expectedResult]' naming convention (use underscores to separate parts)")
    }

    @Test
    fun `allows test method with underscore`() {
        val code =
            """
            package app.constructor.csdk.home.domain.entity

            import kotlin.test.Test

            class HomeTest {
                @Test
                fun fetchCourses_emptyList_returnsDefault() {}
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores test methods in common utility packages`() {
        val code =
            """
            package app.constructor.csdk.files

            import kotlin.test.Test

            class FileSystemTest {
                @Test
                fun testFileExists() {}
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores test methods in common package`() {
        val code =
            """
            package app.constructor.csdk.common

            import kotlin.test.Test

            class UtilTest {
                @Test
                fun testHelper() {}
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores non-test methods`() {
        val code =
            """
            package app.constructor.csdk.home.domain.entity

            class HomeTest {
                fun helperMethod() {}
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
