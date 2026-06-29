package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class NoUiStateCastRuleTest {
    private val assertThat = assertThatRule { NoUiStateCastRule() }

    @Test
    fun `flags as cast to UiState subtype`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.impl

            fun process(state: Any) {
                val data = state as UiState.Data
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 16, "Avoid casting to UiState subtypes -- use exhaustive 'when' instead")
    }

    @Test
    fun `flags safe cast to UiState`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.impl

            fun process(state: Any) {
                val data = state as? UiState.Data
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 16, "Avoid casting to UiState subtypes -- use exhaustive 'when' instead")
    }

    @Test
    fun `allows cast to non-UiState type`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.impl

            fun process(value: Any) {
                val str = value as String
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores casts outside project packages`() {
        val code =
            """
            package com.example.app

            fun process(state: Any) {
                val data = state as UiState.Data
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `flags as cast in Android feature UI package`() {
        val code =
            """
            package app.constructor.feature.home.ui.notifications

            fun process(state: Any) {
                val data = state as NotificationHistory.UiState.Data
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 16, "Avoid casting to UiState subtypes -- use exhaustive 'when' instead")
    }

    @Test
    fun `flags safe cast in Android feature UI package`() {
        val code =
            """
            package app.constructor.feature.home.ui.notifications

            fun render(state: Any) {
                (state as? NotificationHistory.UiState.Data)?.let { _ -> }
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 6, "Avoid casting to UiState subtypes -- use exhaustive 'when' instead")
    }

    @Test
    fun `flags as cast in course-list package`() {
        val code =
            """
            package app.constructor.courses.ui

            fun process(state: Any) {
                val data = state as CourseList.UiState.Data
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 16, "Avoid casting to UiState subtypes -- use exhaustive 'when' instead")
    }

    @Test
    fun `flags as cast in host-app launcher package`() {
        val code =
            """
            package app.constructor.mobile

            fun process(state: Any) {
                val data = state as HomeUiState.Ready
            }
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 16, "Avoid casting to UiState subtypes -- use exhaustive 'when' instead")
    }

    @Test
    fun `ignores casts in junit test file even when package is in scope`() {
        val code =
            """
            package app.constructor.feature.groups

            import org.junit.Test

            class GroupsViewModelTest {
                @Test
                fun example() {
                    val state: Any = Any()
                    val ready = state as GroupsUiState.Ready
                }
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores casts in kotlin test file even when package is in scope`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.impl

            import kotlin.test.Test

            class HomeViewModelImplTest {
                @Test
                fun example() {
                    val state: Any = Any()
                    val ready = state as HomeUiState.Data
                }
            }
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
