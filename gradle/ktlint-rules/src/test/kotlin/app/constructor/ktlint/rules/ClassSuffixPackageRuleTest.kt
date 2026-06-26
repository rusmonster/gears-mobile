package app.constructor.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ClassSuffixPackageRuleTest {
    private val assertThat = assertThatRule { ClassSuffixPackageRule() }

    @Test
    fun `flags RepositoryImpl in wrong package`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            class CourseRepositoryImpl
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 1, "'CourseRepositoryImpl' has suffix 'RepositoryImpl' -- expected in .data. package")
    }

    @Test
    fun `allows RepositoryImpl in data package`() {
        val code =
            """
            package app.constructor.csdk.home.data.repository

            class CourseRepositoryImpl
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `flags Cache interface in data package`() {
        val code =
            """
            package app.constructor.csdk.home.data

            interface CourseCache
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 1, "'CourseCache' has suffix 'Cache' -- expected in .domain. package")
    }

    @Test
    fun `allows Cache interface in domain package`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            interface CourseCache
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `flags ViewModelImpl in domain package`() {
        val code =
            """
            package app.constructor.csdk.home.domain

            class HomeViewModelImpl
            """.trimIndent()
        assertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 1, "'HomeViewModelImpl' has suffix 'ViewModelImpl' -- expected in .presentation.impl. package")
    }

    @Test
    fun `allows ViewModelImpl in presentation impl package`() {
        val code =
            """
            package app.constructor.csdk.home.presentation.impl

            class HomeViewModelImpl
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores classes in common packages`() {
        val code =
            """
            package app.constructor.csdk.common.formatters

            class DateFormatter
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }

    @Test
    fun `ignores classes outside project namespace`() {
        val code =
            """
            package com.example.data

            class CourseRepositoryImpl
            """.trimIndent()
        assertThat(code).hasNoLintViolations()
    }
}
