package app.constructor.gears

import android.content.Context
import app.constructor.csdk.di.appmodule.ApplicationContextHolder

/**
 * Initialises Gears Mobile on Android.
 *
 * Must be called once before creating any ViewModel (typically from `Application.onCreate`).
 * It supplies the application [Context] required for file-system access when building a report.
 */
fun Gears.init(context: Context) {
    ApplicationContextHolder.setContext(context)
}
