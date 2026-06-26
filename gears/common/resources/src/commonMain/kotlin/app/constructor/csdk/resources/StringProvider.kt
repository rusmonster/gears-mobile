package app.constructor.csdk.resources

import org.jetbrains.compose.resources.StringResource

/**
 * In iosSimulatorTest the getString() from CMP hangs, because of deadlock on the main thread which looks
 * unavoidable atm. So in tests we override `StringProvider` with fake implementation which doesn't call CMP.
 */
interface StringProvider {
    suspend fun getString(stringId: StringResource): String
    suspend fun getString(stringId: StringResource, vararg formatArgs: Any): String
}

@Suppress("ktlint:standard:property-naming")
var DefaultStringProvider: StringProvider = DefaultStringProviderImpl()

fun resetDefaultStringProvider() {
    DefaultStringProvider = DefaultStringProviderImpl()
}

private class DefaultStringProviderImpl : StringProvider {
    override suspend fun getString(stringId: StringResource) = org.jetbrains.compose.resources.getString(stringId)

    override suspend fun getString(stringId: StringResource, vararg formatArgs: Any) =
        org.jetbrains.compose.resources.getString(stringId, *formatArgs)
}
