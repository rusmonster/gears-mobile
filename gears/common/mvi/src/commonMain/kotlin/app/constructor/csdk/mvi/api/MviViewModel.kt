package app.constructor.csdk.mvi.api

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for a ViewModel following the MVI (Model-View-Intent) pattern.
 *
 * The MVI flow works as follows:
 * 1. The UI layer sends user interactions as [Action]s via [sendAction].
 * 2. The ViewModel processes actions sequentially (mutex-guarded) and updates [uiState].
 * 3. One-shot effects (e.g. navigation, toasts) are emitted as [Event]s.
 *
 * @param Action User-initiated intent (e.g. button click, text input). Processed via [sendAction].
 * @param UiState Immutable state that the UI observes and renders.
 * @param Event One-shot side effect consumed by the UI (not replayed on re-subscription).
 *
 * @see app.constructor.csdk.mvi.impl.BaseMviViewModelImpl for the base implementation.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName("KmpMviViewModel", exact = true)
interface MviViewModel<Action : Any, UiState : Any, Event : Any> {

    /** Observable UI state. Collectors always receive the latest value immediately. */
    val uiState: StateFlow<UiState>

    /** Stream of one-shot events. Events are NOT replayed to new collectors. */
    val events: SharedFlow<Event>

    /**
     * Enqueues an [action] for processing.
     *
     * Actions are processed sequentially in FIFO order. The returned [Job] completes
     * once the action has been fully handled.
     */
    fun sendAction(action: Action): Job

    /**
     * Clears all resources held by the ViewModel.
     */
    fun dispose()
}
