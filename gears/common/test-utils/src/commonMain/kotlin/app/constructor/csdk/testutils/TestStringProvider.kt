package app.constructor.csdk.testutils

import app.constructor.csdk.resources.DefaultStringProvider
import app.constructor.csdk.resources.StringProvider
import app.constructor.csdk.resources.resetDefaultStringProvider
import org.jetbrains.compose.resources.StringResource

/**
 * In iosSimulatorTest the getString() from CMP hangs, because of deadlock on the main thread which looks
 * unavoidable atm. So in tests we use the [TestStringProvider] with fake implementation which doesn't call CMP.
 */
object TestStringProvider : StringProvider {
    override suspend fun getString(stringId: StringResource) = "TestStringProvider: ${stringId.key}"

    override suspend fun getString(stringId: StringResource, vararg formatArgs: Any) =
        "TestStringProvider: ${stringId.key}(${formatArgs.joinToString()})"

    fun setup() {
        DefaultStringProvider = this
    }

    fun tearDown() = resetDefaultStringProvider()
}
