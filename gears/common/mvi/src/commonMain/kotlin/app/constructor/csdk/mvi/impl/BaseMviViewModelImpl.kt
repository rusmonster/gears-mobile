package app.constructor.csdk.mvi.impl

import app.constructor.csdk.common.runCatchingCancellable
import app.constructor.csdk.logging.L
import app.constructor.csdk.mvi.api.MviViewModel
import app.constructor.csdk.resources.DefaultStringProvider
import app.constructor.csdk.resources.StringProvider
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.incrementAndFetch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val loggerId = AtomicLong(0)

@Suppress("ktlint:constructor:class-suffix-package")
abstract class BaseMviViewModelImpl<Action : Any, UiState : Any, Event : Any>(
    initialUiState: UiState,
    val stringProvider: StringProvider = DefaultStringProvider,
) : MviViewModel<Action, UiState, Event>,
    StringProvider by stringProvider {

    private val logger = L.Logger("${this::class.simpleName}-${loggerId.incrementAndFetch()}")

    private val _uiState = MutableStateFlow(initialUiState)
    override val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    override val events = _events.asSharedFlow()

    private val dispatcher = Dispatchers.Default.limitedParallelism(1)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.e(throwable) { "!!! Unhandled exception in viewModelScope !!!" }
    }

    protected val viewModelScope = CoroutineScope(
        SupervisorJob() + dispatcher + exceptionHandler,
    )

    private val mutex = Mutex()

    protected fun setState(uiState: UiState) {
        if (_uiState.value == uiState) {
            logger.d { "Skipping equal state update: ${uiState::class.simpleName}" }
            return
        }

        logger.d { "Emitting uiState: ${uiState::class.simpleName}" }
        _uiState.value = uiState
    }

    protected suspend fun emitEvent(event: Event) {
        logger.d { "Emitting event: ${event::class.simpleName}" }
        _events.emit(event)
    }

    override fun sendAction(action: Action) = viewModelScope.launch {
        logger.d { "sendAction [$action] in state [${uiState.value::class.simpleName}]" }

        mutex.withLock {
            runCatchingCancellable {
                logger.d { "executeAction [$action] in state [${uiState.value::class.simpleName}]" }
                executeAction(action)
            }.onFailure { t ->
                logger.e(t) { "!!! Error in executeAction(). THIS SHOULD NEVER HAPPEN !!!" }
            }
        }
    }

    // Should not throw any exceptions
    protected abstract suspend fun executeAction(action: Action)

    override fun dispose() {
        logger.d { "dispose" }
        viewModelScope.cancel(CancellationException("[${logger.tag}] clear() method has been called"))
    }
}
