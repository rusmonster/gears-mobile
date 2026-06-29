package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class MissingEnumEntryKDocRuleTest {
    private val assertThat = assertThatRule { MissingEnumEntryKDocRule() }

    @Test
    fun `flags enum entry without KDoc in domain`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            enum class Status {
                ACTIVE,
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 5, "Enum entry 'ACTIVE' in domain must have a KDoc comment (§13 FeatureSpec)")
    }

    @Test
    fun `allows enum entry with KDoc`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            enum class Status {
                /** Currently active. */
                ACTIVE,
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores enum in non-domain package`() {
        val code =
            """
            package app.constructor.csdk.home.data

            enum class InternalStatus {
                OK,
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
