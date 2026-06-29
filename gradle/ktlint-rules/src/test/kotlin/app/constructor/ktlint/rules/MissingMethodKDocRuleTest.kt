package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class MissingMethodKDocRuleTest {
    private val assertThat = assertThatRule { MissingMethodKDocRule() }

    @Test
    fun `flags method without KDoc on domain interface`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            /** Repository. */
            interface CourseRepository {
                suspend fun getCourses(): List<String>
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(5, 5, "Method 'getCourses' on interface in domain/presentation:api must have a KDoc comment (§13 FeatureSpec)")
    }

    @Test
    fun `allows method with KDoc`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            /** Repository. */
            interface CourseRepository {
                /** Fetches all courses. */
                suspend fun getCourses(): List<String>
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores methods in non-interface classes`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            class CourseHelper {
                fun doSomething() {}
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
