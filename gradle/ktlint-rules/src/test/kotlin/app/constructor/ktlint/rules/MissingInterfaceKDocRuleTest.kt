package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class MissingInterfaceKDocRuleTest {
    private val assertThat = assertThatRule { MissingInterfaceKDocRule() }

    @Test
    fun `flags interface without KDoc in domain package`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            interface CourseRepository {
                suspend fun getCourses(): List<String>
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 1, "Interface 'CourseRepository' in domain/presentation:api must have a KDoc comment (§13 FeatureSpec)")
    }

    @Test
    fun `flags interface without KDoc in presentation api package`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.api

            interface HomeScreen {
                fun render()
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 1, "Interface 'HomeScreen' in domain/presentation:api must have a KDoc comment (§13 FeatureSpec)")
    }

    @Test
    fun `allows interface with KDoc`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            /** Repository for courses. */
            interface CourseRepository {
                suspend fun getCourses(): List<String>
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores interface in data package`() {
        val code =
            """
            package app.constructor.csdk.home.data

            interface InternalHelper {
                fun help()
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores class without KDoc in domain`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            data class Course(val id: String)
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
