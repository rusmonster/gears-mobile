package app.constructor.gears

import android.content.Context
import app.constructor.csdk.di.appmodule.ApplicationContextHolder

/**
 * Initialises Gears Mobile on Android.
 *
 * Must be called once before creating any ViewModel (typically from `Application.onCreate`).
 * It supplies the application [Context] required for file-system access when building a report,
 * and enables file-based logging so that SDK logs can be bundled into a report when the user
 * leaves "include logs" enabled.
 */
fun Gears.initialize(context: Context) {
    // The app context must be set before shared init runs, because file logging resolves its
    // storage directory through it.
    ApplicationContextHolder.setContext(context)
    GearsInitializer.init()
}
