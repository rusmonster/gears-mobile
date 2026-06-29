package app.constructor.csdk.common

import app.constructor.csdk.logging.L
import kotlinx.coroutines.CancellationException

inline fun <R> runCatchingCancellable(onCancel: () -> Unit = {}, finally: () -> Unit = {}, block: () -> R): Result<R> {
    return try {
        Result.success(block()).also { finally() }
    } catch (e: CancellationException) {
        runCatching {
            onCancel()
        }.onFailure { t ->
            L.w("runCatchingCancellable", t) { "Error in onCancel" }
        }
        finally()
        throw e
    } catch (e: Throwable) {
        Result.failure<R>(e).also { finally() }
    }
}
