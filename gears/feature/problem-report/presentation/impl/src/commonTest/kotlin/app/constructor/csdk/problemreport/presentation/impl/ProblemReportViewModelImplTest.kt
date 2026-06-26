package app.constructor.csdk.problemreport.presentation.impl

import app.cash.turbine.test
import app.constructor.csdk.problemreport.domain.CreateProblemReportUseCase
import app.constructor.csdk.problemreport.domain.ProblemType
import app.constructor.csdk.problemreport.domain.entity.ProblemReportData
import app.constructor.csdk.problemreport.domain.entity.ProblemReportResult
import app.constructor.csdk.problemreport.presentation.api.ProblemReport
import app.constructor.csdk.testutils.TestStringProvider
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class ProblemReportViewModelImplTest {

    private lateinit var problemReporter: CreateProblemReportUseCase

    @BeforeTest
    fun setUp() {
        TestStringProvider.setup()
        problemReporter = mock<CreateProblemReportUseCase> {
            everySuspend { createReport(any(), any()) } returns FAKE_REPORT_RESULT
        }
    }

    @AfterTest
    fun tearDown() {
        TestStringProvider.tearDown()
    }

    private fun createViewModel(autoCapturedScreenshotPath: String? = null) = ProblemReportViewModelImpl(
        problemReporter,
        ProblemReport.Config(
            supportEmail = "support@example.com",
            publicKeyPem = ByteArray(0),
            autoCapturedScreenshotPath = autoCapturedScreenshotPath,
        ),
    )

    // --- initial state ---

    @Test
    fun initialState_afterInit_hasDefaults() = runTest {
        val vm = createViewModel()

        // wait for updateInitialState() completed
        val form = vm.uiState.first { it.problemTypeOptions.isNotEmpty() }

        assertEquals(-1, form.problemTypeSelectedIndex)
        assertEquals(ProblemType.entries.size, form.problemTypeOptions.size)
        assertTrue(form.includeLogs)
        assertFalse(form.isSubmitEnabled)
        assertEquals("", form.problemDescription)
        assertEquals("", form.reproSteps)
        assertTrue(form.screenshots.isEmpty())
        assertTrue(form.isInputEnabled)
        assertEquals(ProblemReport.ModalState.None, form.modalState)
    }

    @Test
    fun initialState_withAutoCapturedScreenshot_populatesScreenshots() = runTest {
        val vm = createViewModel(autoCapturedScreenshotPath = "/cache/auto.png")
        val form = vm.uiState
            .first { it.screenshots.isNotEmpty() }
        assertEquals(1, form.screenshots.size)
        assertEquals("/cache/auto.png", form.screenshots[0].filePath)
        assertTrue(form.screenshots[0].isAutoCaptured)
    }

    // --- form field actions ---

    @Test
    fun selectProblemType_updatesForm() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.SelectProblemType(ProblemType.PERFORMANCE.index)).join()
        assertEquals(ProblemType.PERFORMANCE.index, vm.uiState.value.problemTypeSelectedIndex)
    }

    @Test
    fun toggleLogs_toFalse_updatesForm() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.ToggleLogs(false)).join()
        assertFalse(vm.uiState.value.includeLogs)
    }

    @Test
    fun updateDescription_updatesForm() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.UpdateDescription("App freezes on tap")).join()
        assertEquals("App freezes on tap", vm.uiState.value.problemDescription)
    }

    @Test
    fun updateDescription_truncatesAtMaxLength() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.UpdateDescription("x".repeat(ProblemReport.MAX_TEXT_LENGTH + 50))).join()
        assertEquals(ProblemReport.MAX_TEXT_LENGTH, vm.uiState.value.problemDescription.length)
    }

    @Test
    fun updateSteps_updatesForm() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.UpdateSteps("1. Open app 2. Tap")).join()
        assertEquals("1. Open app 2. Tap", vm.uiState.value.reproSteps)
    }

    @Test
    fun updateSteps_truncatesAtMaxLength() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.UpdateSteps("y".repeat(ProblemReport.MAX_TEXT_LENGTH + 10))).join()
        assertEquals(ProblemReport.MAX_TEXT_LENGTH, vm.uiState.value.reproSteps.length)
    }

    @Test
    fun clearDescription_resetsToEmpty() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.UpdateDescription("Some text")).join()
        vm.sendAction(ProblemReport.Action.ClearDescription).join()
        assertEquals("", vm.uiState.value.problemDescription)
    }

    @Test
    fun clearSteps_resetsToEmpty() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.UpdateSteps("1. Open app")).join()
        vm.sendAction(ProblemReport.Action.ClearSteps).join()
        assertEquals("", vm.uiState.value.reproSteps)
    }

    // --- screenshot actions ---

    @Test
    fun addScreenshot_addsEntryWithFileNameAsDisplayName() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.AddScreenshot("/data/user/0/cache/shot.png")).join()
        val screenshots = vm.uiState.value.screenshots
        assertEquals(1, screenshots.size)
        assertEquals("/data/user/0/cache/shot.png", screenshots[0].filePath)
        assertEquals("shot.png", screenshots[0].displayName)
        assertFalse(screenshots[0].isAutoCaptured)
    }

    @Test
    fun addScreenshot_appendsToExistingList() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.AddScreenshot("/cache/a.png")).join()
        vm.sendAction(ProblemReport.Action.AddScreenshot("/cache/b.png")).join()
        assertEquals(2, vm.uiState.value.screenshots.size)
    }

    @Test
    fun removeScreenshot_removesMatchingEntry() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.AddScreenshot("/cache/a.png")).join()
        vm.sendAction(ProblemReport.Action.AddScreenshot("/cache/b.png")).join()
        vm.sendAction(ProblemReport.Action.RemoveScreenshot("/cache/a.png")).join()
        val paths = vm.uiState.value.screenshots.map { it.filePath }
        assertEquals(listOf("/cache/b.png"), paths)
    }

    // --- submit ---

    @Test
    fun submit_emitsReadyToShareEvent() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.SelectProblemType(ProblemType.BUG.index)).join()
        vm.sendAction(ProblemReport.Action.UpdateDescription("Bug found")).join()
        vm.events.test {
            vm.sendAction(ProblemReport.Action.Submit).join()
            val event = assertIs<ProblemReport.Event.ReadyToShare>(awaitItem())
            assertEquals(FAKE_REPORT_RESULT, event.result)
        }
    }

    @Test
    fun submit_passesFormDataToProblemReporter() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.SelectProblemType(ProblemType.ACCOUNT.index)).join()
        vm.sendAction(ProblemReport.Action.UpdateDescription("Can't log in")).join()
        vm.sendAction(ProblemReport.Action.UpdateSteps("Try opening the app")).join()
        vm.sendAction(ProblemReport.Action.ToggleLogs(true)).join()
        vm.sendAction(ProblemReport.Action.Submit).join()

        verifySuspend {
            problemReporter.createReport(
                report = ProblemReportData(
                    problemType = ProblemType.ACCOUNT,
                    problemDescription = "Can't log in",
                    reproSteps = "Try opening the app",
                    screenshotFilePaths = emptyList(),
                    includeLogs = true,
                ),
                labels = any(),
            )
        }
    }

    @Test
    fun submit_passesScreenshotPathsToProblemReporter() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.SelectProblemType(ProblemType.BUG.index)).join()
        vm.sendAction(ProblemReport.Action.UpdateDescription("Screenshot bug")).join()
        vm.sendAction(ProblemReport.Action.AddScreenshot("/cache/shot.png")).join()
        vm.sendAction(ProblemReport.Action.Submit).join()

        verifySuspend {
            problemReporter.createReport(
                report = ProblemReportData(
                    problemType = ProblemType.BUG,
                    problemDescription = "Screenshot bug",
                    reproSteps = "",
                    screenshotFilePaths = listOf("/cache/shot.png"),
                    includeLogs = true,
                ),
                labels = any(),
            )
        }
    }

    @Test
    fun submit_passesIncludeLogsFalse_whenLogsToggled() = runTest {
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.SelectProblemType(ProblemType.BUG.index)).join()
        vm.sendAction(ProblemReport.Action.UpdateDescription("Logs test")).join()
        vm.sendAction(ProblemReport.Action.ToggleLogs(true)).join()
        vm.sendAction(ProblemReport.Action.ToggleLogs(false)).join()
        vm.sendAction(ProblemReport.Action.Submit).join()

        verifySuspend {
            problemReporter.createReport(
                report = ProblemReportData(
                    problemType = ProblemType.BUG,
                    problemDescription = "Logs test",
                    reproSteps = "",
                    screenshotFilePaths = emptyList(),
                    includeLogs = false,
                ),
                labels = any(),
            )
        }
    }

    @Test
    fun submit_onError_setsErrorModalState() = runTest {
        everySuspend { problemReporter.createReport(any(), any()) } throws RuntimeException("Disk full")
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.SelectProblemType(ProblemType.BUG.index)).join()
        vm.sendAction(ProblemReport.Action.UpdateDescription("Error test")).join()
        vm.sendAction(ProblemReport.Action.Submit).join()
        val state = vm.uiState.value
        assertIs<ProblemReport.ModalState.Error>(state.modalState)
        assertFalse(state.isInputEnabled)
    }

    @Test
    fun dismissModal_restoresInputAndClearsModal() = runTest {
        everySuspend { problemReporter.createReport(any(), any()) } throws RuntimeException("Disk full")
        val vm = createViewModel()
        vm.sendAction(ProblemReport.Action.SelectProblemType(ProblemType.BUG.index)).join()
        vm.sendAction(ProblemReport.Action.UpdateDescription("Dismiss test")).join()
        vm.sendAction(ProblemReport.Action.Submit).join()
        vm.sendAction(ProblemReport.Action.DismissModal).join()
        val state = vm.uiState.value
        assertEquals(ProblemReport.ModalState.None, state.modalState)
        assertTrue(state.isInputEnabled)
    }

    private companion object {
        val FAKE_REPORT_RESULT = ProblemReportResult(
            reportId = "00000000-0000-0000-0000-000000000000",
            cpbPath = "/fake/problem_report.cpb",
        )
    }
}
