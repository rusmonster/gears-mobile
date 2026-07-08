package app.constructor.gears

import android.content.Context
import app.constructor.csdk.di.appmodule.ApplicationContextHolder
import app.constructor.csdk.logging.L

/**
 * Initialises Gears Mobile on Android.
 *
 * Must be called once before creating any ViewModel (typically from `Application.onCreate`).
 * It supplies the application [Context] required for file-system access when building a report,
 * and enables file-based logging so that SDK logs can be bundled into a report when the user
 * leaves "include logs" enabled.
 */
fun Gears.init(context: Context) {
    ApplicationContextHolder.setContext(context)
    // File logging depends on the app context for its storage directory, so enable it only
    // after the context has been set above.
    L.enableFileLogging()
}
