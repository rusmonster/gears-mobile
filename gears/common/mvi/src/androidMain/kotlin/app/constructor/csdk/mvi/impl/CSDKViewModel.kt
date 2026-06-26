package app.constructor.csdk.mvi.impl

import androidx.lifecycle.ViewModel
import app.constructor.csdk.mvi.api.MviViewModel

open class CSDKViewModel : ViewModel() {

    private val self: MviViewModel<*, *, *>

    init {
        check(this is MviViewModel<*, *, *>) { "A class which extends CSDKViewModel must also implement MviViewModel" }
        self = this as MviViewModel<*, *, *>
    }

    override fun onCleared() {
        super.onCleared()
        self.dispose()
    }
}
