package app.constructor.csdk.problemreport.presentation.api

import app.constructor.csdk.annotations.Immutable
import app.constructor.csdk.mvi.api.MviViewModel
import app.constructor.csdk.problemreport.domain.entity.ProblemReportResult

/** Top-level namespace for the problem-report feature's MVI contract. */
interface ProblemReport {
    /**
     * Host-supplied configuration for the problem-report feature.
     *
     * @property supportEmail Destination address the finalised report is sent to.
     * @property publicKeyPem PEM-encoded RSA public key used to encrypt the report archive.
     * @property autoCapturedScreenshotPath Optional path to a screenshot captured automatically
     *   when the feature was opened; pre-attached to the report when present.
     */
    class Config(
        val supportEmail: String,
        val publicKeyPem: ByteArray,
        val autoCapturedScreenshotPath: String? = null,
    )

    /** User intents that can be dispatched to the ViewModel. */
    sealed interface Action {
        /** Sets the category using its index into [UiState.problemTypeOptions]. */
        data class SelectProblemType(val selectedIndex: Int) : Action

        /** Toggles whether device error logs are included in the report. */
        data class ToggleLogs(val include: Boolean) : Action

        /** Updates the free-text description of the problem. */
        data class UpdateDescription(val text: String) : Action

        /** Clears the description field. */
        data object ClearDescription : Action

        /** Updates the optional steps-to-reproduce field. */
        data class UpdateSteps(val text: String) : Action

        /** Clears the steps-to-reproduce field. */
        data object ClearSteps : Action

        /** Adds a screenshot at [filePath] to the report. */
        data class AddScreenshot(val filePath: String) : Action

        /** Removes the screenshot identified by [filePath] from the report. */
        data class RemoveScreenshot(val filePath: String) : Action

        /** Packages and finalises the report. */
        data object Submit : Action

        /** Dismisses the current modal overlay (error or submitting). */
        data object DismissModal : Action
    }

    /**
     * Screen state for the problem-report form.
     *
     * @property problemTypeOptions Localised display labels for each problem category, in order.
     * @property problemTypeSelectedIndex Index of the currently selected category in [problemTypeOptions].
     * @property includeLogs Whether device error logs will be bundled.
     * @property problemDescription Free-text description entered by the user.
     * @property reproSteps Optional steps needed to reproduce the issue.
     * @property screenshots Screenshots attached to the report.
     * @property isSubmitEnabled Whether the submit action is allowed (description is non-blank and not currently submitting).
     * @property isInputEnabled False while the report archive is being created.
     * @property modalState Current modal overlay state (none, submitting, or error).
     * @property descriptionCharCounter Pre-formatted character counter for the description field.
     * @property reproStepsCharCounter Pre-formatted character counter for the repro-steps field.
     * @property supportEmail Destination email address loaded from resources.
     * @property emailSubject Pre-formatted email subject populated after successful submission.
     */
    @Immutable
    data class UiState(
        val problemTypeOptions: List<String> = emptyList(),
        val problemTypeSelectedIndex: Int = -1,
        val includeLogs: Boolean = true,
        val problemDescription: String = "",
        val reproSteps: String = "",
        val screenshots: List<Screenshot> = emptyList(),
        val isSubmitEnabled: Boolean = false,
        val isInputEnabled: Boolean = true,
        val modalState: ModalState = ModalState.None,
        val descriptionCharCounter: String = "",
        val reproStepsCharCounter: String = "",
        val supportEmail: String = "",
        val emailSubject: String = "",
    )

    /** One-shot events emitted by the ViewModel for the UI to handle. */
    sealed interface Event {
        /**
         * The report archive has been created and is ready to be shared.
         *
         * @property result Contains the archive path and metadata for the share intent.
         */
        data class ReadyToShare(val result: ProblemReportResult) : Event
    }

    /**
     * A single screenshot attached to the report.
     *
     * @property filePath Absolute path to the image file on disk.
     * @property displayName Label shown in the UI (filename or "Auto captured").
     * @property isAutoCaptured True when the screenshot was taken automatically on navigation.
     */
    @Immutable
    data class Screenshot(
        val filePath: String,
        val displayName: String,
        val isAutoCaptured: Boolean,
    )

    /** Modal overlay states shown during and after report submission. */
    @Immutable
    sealed interface ModalState {
        data object None : ModalState
        data object Submitting : ModalState
        data class Error(val message: String) : ModalState
    }

    /** Convenience alias for the MVI ViewModel parameterised with this contract's types. */
    typealias ViewModel = MviViewModel<Action, UiState, Event>

    companion object {
        /** Maximum number of characters allowed in description and steps-to-reproduce fields. */
        const val MAX_TEXT_LENGTH = 1000
    }
}
