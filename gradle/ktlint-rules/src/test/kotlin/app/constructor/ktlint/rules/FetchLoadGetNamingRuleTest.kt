package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class FetchLoadGetNamingRuleTest {
    private val assertThat = assertThatRule { FetchLoadGetNamingRule() }

    @Test
    fun `flags load on Cache interface`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            interface CourseCache {
                suspend fun loadCourses(): List<String>
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 5, "Cache should not have 'load*' methods -- allowed prefixes: fetch, upsert, delete, clear (§6 FeatureSpec)")
    }

    @Test
    fun `allows fetch on Cache`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            interface CourseCache {
                suspend fun fetchCourses(): List<String>
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `flags save on Cache`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            interface CourseCache {
                suspend fun saveCourse(id: String)
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 5, "Cache should not have 'save*' methods -- allowed prefixes: fetch, upsert, delete, clear (§6 FeatureSpec)")
    }

    @Test
    fun `allows upsert on Cache`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            interface CourseCache {
                suspend fun upsertCourse(id: String)
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `flags fetch on ApiClient`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            interface CourseApiClient {
                suspend fun fetchCourses(): List<String>
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 5, "ApiClient should not have 'fetch*' methods -- allowed prefixes: load (§6 FeatureSpec)")
    }

    @Test
    fun `allows load on ApiClient`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            interface CourseApiClient {
                suspend fun loadCourses(): List<String>
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows all three prefixes on Repository`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            interface CourseRepository {
                suspend fun fetchCourses(): List<String>
                suspend fun loadCourses(): List<String>
                suspend fun getCourses(): List<String>
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores non-domain packages`() {
        val code =
            """
            package app.constructor.csdk.home.data

            interface CourseCache {
                suspend fun loadCourses(): List<String>
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
