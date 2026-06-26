package app.constructor.csdk.files

import app.constructor.csdk.di.appmodule.ApplicationContextHolder

@Suppress("FunctionName")
actual fun FileSystem(): FileSystem = AndroidFileSystem()

internal class AndroidFileSystem : BaseJvmFileSystem() {
    override fun getAppDirectory(): String {
        return ApplicationContextHolder.context.filesDir.absolutePath
    }
}
