package app.constructor.csdk.problemreport.presentation.impl

import app.constructor.csdk.logging.log
import app.constructor.csdk.mvi.impl.BaseMviViewModelImpl
import app.constructor.csdk.problemreport.domain.CreateProblemReportUseCase
import app.constructor.csdk.problemreport.domain.ProblemType
import app.constructor.csdk.problemreport.domain.entity.MetadataLabels
import app.constructor.csdk.problemreport.domain.entity.ProblemReportData
import app.constructor.csdk.problemreport.presentation.api.ProblemReport
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.Res
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_description
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_device
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_os
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_problem_type
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_report_id
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_steps_to_reproduce
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_report_auto_captured
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_report_char_counter_format
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_report_email_subject_format
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_report_submit_error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ProblemReportViewModelImpl(
    private val createProblemReportUseCase: CreateProblemReportUseCase,
    private val config: ProblemReport.Config,
) : BaseMviViewModelImpl<ProblemReport.Action, ProblemReport.UiState, ProblemReport.Event>(
    initialUiState = ProblemReport.UiState(),
),
    ProblemReport.ViewModel {

    init {
        viewModelScope.launch { updateInitialState() }
    }

    // @cpt-dod:cpt-cyberfabricmobile-dod-problem-report-form:p1
    private suspend fun updateInitialState() {
        // @cpt-begin:cpt-cyberfabricmobile-dod-problem-report-form:p1:inst-init
        val problemTypeOptions = ProblemType.entries.sortedBy { it.index }.map { it.toDisplayString() }
        val supportEmail = config.supportEmail

        val autoCapturedScreenshot = config.autoCapturedScreenshotPath?.let { path ->
            ProblemReport.Screenshot(
                filePath = path,
                displayName = getString(Res.string.problem_report_auto_captured),
                isAutoCaptured = true,
            )
        }

        val descriptionCounter = formatCharCounter(uiState.value.problemDescription.length)
        val reproStepsCounter = formatCharCounter(uiState.value.reproSteps.length)
        updateState { data ->
            data.copy(
                problemTypeOptions = problemTypeOptions,
                screenshots = if (autoCapturedScreenshot != null) listOf(autoCapturedScreenshot) else data.screenshots,
                supportEmail = supportEmail,
                descriptionCharCounter = descriptionCounter,
                reproStepsCharCounter = reproStepsCounter,
            )
        }
        // @cpt-end:cpt-cyberfabricmobile-dod-problem-report-form:p1:inst-init
    }

    override suspend fun executeAction(action: ProblemReport.Action) {
        when (action) {
            is ProblemReport.Action.SelectProblemType -> updateState {
                it.copy(problemTypeSelectedIndex = action.selectedIndex)
            }
            is ProblemReport.Action.ToggleLogs -> updateState { it.copy(includeLogs = action.include) }
            is ProblemReport.Action.UpdateDescription -> {
                // @cpt-begin:cpt-cyberfabricmobile-dod-problem-report-form:p1:inst-truncate
                val text = action.text.take(ProblemReport.MAX_TEXT_LENGTH)
                // @cpt-end:cpt-cyberfabricmobile-dod-problem-report-form:p1:inst-truncate
                val counter = formatCharCounter(text.length)
                updateState {
                    it.copy(problemDescription = text, descriptionCharCounter = counter)
                }
            }
            ProblemReport.Action.ClearDescription -> {
                val counter = formatCharCounter(0)
                updateState {
                    it.copy(problemDescription = "", descriptionCharCounter = counter)
                }
            }
            is ProblemReport.Action.UpdateSteps -> {
                val text = action.text.take(ProblemReport.MAX_TEXT_LENGTH)
                val counter = formatCharCounter(text.length)
                updateState {
                    it.copy(reproSteps = text, reproStepsCharCounter = counter)
                }
            }
            ProblemReport.Action.ClearSteps -> {
                val counter = formatCharCounter(0)
                updateState {
                    it.copy(reproSteps = "", reproStepsCharCounter = counter)
                }
            }
            is ProblemReport.Action.AddScreenshot -> onAddScreenshot(action.filePath)
            is ProblemReport.Action.RemoveScreenshot -> onRemoveScreenshot(action.filePath)
            ProblemReport.Action.Submit -> onSubmit()
            // @cpt-flow:cpt-cyberfabricmobile-flow-problem-report-dismiss-modal:p1
            ProblemReport.Action.DismissModal -> {
                // @cpt-begin:cpt-cyberfabricmobile-flow-problem-report-dismiss-modal:p1:inst-dismiss
                // @cpt-begin:cpt-cyberfabricmobile-state-problem-report-modal:p2:inst-t-dismiss
                updateState {
                    it.copy(modalState = ProblemReport.ModalState.None, isInputEnabled = true)
                }
                // @cpt-end:cpt-cyberfabricmobile-state-problem-report-modal:p2:inst-t-dismiss
                // @cpt-end:cpt-cyberfabricmobile-flow-problem-report-dismiss-modal:p1:inst-dismiss
            }
        }
    }

    private suspend fun resolveMetadataLabels() = MetadataLabels(
        reportId = getString(Res.string.metadata_report_id),
        problemType = getString(Res.string.metadata_problem_type),
        os = getString(Res.string.metadata_os),
        device = getString(Res.string.metadata_device),
        description = getString(Res.string.metadata_description),
        stepsToReproduce = getString(Res.string.metadata_steps_to_reproduce),
    )

    // @cpt-flow:cpt-cyberfabricmobile-flow-problem-report-submit:p1
    // @cpt-state:cpt-cyberfabricmobile-state-problem-report-modal:p2
    private suspend fun onSubmit() {
        // Guard on authoritative ViewModel state, not just the UI's enabled flag: a host can
        // dispatch Submit while the form is invalid or a submission is already in flight.
        val data = uiState.value
        if (!data.isSubmitEnabled || data.modalState == ProblemReport.ModalState.Submitting) {
            return
        }
        // Reject an out-of-range problem-type index instead of silently falling back to OTHER.
        val problemType = ProblemType.fromIndexOrNull(data.problemTypeSelectedIndex) ?: return

        // @cpt-begin:cpt-cyberfabricmobile-flow-problem-report-submit:p1:inst-submitting
        // @cpt-begin:cpt-cyberfabricmobile-state-problem-report-modal:p2:inst-t-submit
        updateState { it.copy(isInputEnabled = false, modalState = ProblemReport.ModalState.Submitting) }
        // @cpt-end:cpt-cyberfabricmobile-state-problem-report-modal:p2:inst-t-submit
        // @cpt-end:cpt-cyberfabricmobile-flow-problem-report-submit:p1:inst-submitting

        try {
            // @cpt-begin:cpt-cyberfabricmobile-flow-problem-report-submit:p1:inst-create
            val labels = resolveMetadataLabels()
            val result = createProblemReportUseCase.createReport(
                report = ProblemReportData(
                    problemType = problemType,
                    problemDescription = data.problemDescription,
                    reproSteps = data.reproSteps,
                    screenshotFilePaths = data.screenshots.map { it.filePath },
                    includeLogs = data.includeLogs,
                ),
                labels = labels,
            )
            // @cpt-end:cpt-cyberfabricmobile-flow-problem-report-submit:p1:inst-create
            val emailSubject = getString(
                Res.string.problem_report_email_subject_format,
                result.reportId,
            )
            // @cpt-begin:cpt-cyberfabricmobile-flow-problem-report-submit:p1:inst-ready
            // @cpt-begin:cpt-cyberfabricmobile-state-problem-report-modal:p2:inst-t-success
            updateState {
                it.copy(
                    modalState = ProblemReport.ModalState.None,
                    isInputEnabled = true,
                    emailSubject = emailSubject,
                )
            }
            emitEvent(ProblemReport.Event.ReadyToShare(result))
            // @cpt-end:cpt-cyberfabricmobile-state-problem-report-modal:p2:inst-t-success
            // @cpt-end:cpt-cyberfabricmobile-flow-problem-report-submit:p1:inst-ready
        } catch (e: CancellationException) {
            updateState { it.copy(isInputEnabled = true, modalState = ProblemReport.ModalState.None) }
            throw e
        } catch (e: Exception) {
            log.e(e) { "Failed to create problem report" }
            val errorMessage = e.message ?: getString(Res.string.problem_report_submit_error)
            // @cpt-begin:cpt-cyberfabricmobile-state-problem-report-modal:p2:inst-t-error
            updateState {
                it.copy(isInputEnabled = false, modalState = ProblemReport.ModalState.Error(errorMessage))
            }
            // @cpt-end:cpt-cyberfabricmobile-state-problem-report-modal:p2:inst-t-error
        }
    }

    private fun onAddScreenshot(filePath: String) {
        val displayName = filePath.substringAfterLast('/')
        updateState { data ->
            data.copy(
                screenshots = data.screenshots + ProblemReport.Screenshot(
                    filePath = filePath,
                    displayName = displayName,
                    isAutoCaptured = false,
                ),
            )
        }
    }

    private fun onRemoveScreenshot(filePath: String) {
        updateState { data ->
            data.copy(screenshots = data.screenshots.filter { it.filePath != filePath })
        }
    }

    private suspend fun formatCharCounter(current: Int): String =
        getString(Res.string.problem_report_char_counter_format, current, ProblemReport.MAX_TEXT_LENGTH)

    private inline fun updateState(transform: (ProblemReport.UiState) -> ProblemReport.UiState) {
        val newState = transform(uiState.value)
        // @cpt-begin:cpt-cyberfabricmobile-dod-problem-report-form:p1:inst-guard
        setState(
            newState.copy(
                isSubmitEnabled = newState.problemDescription.isNotBlank() &&
                    newState.problemTypeSelectedIndex >= 0 &&
                    newState.isInputEnabled,
            ),
        )
        // @cpt-end:cpt-cyberfabricmobile-dod-problem-report-form:p1:inst-guard
    }
}
