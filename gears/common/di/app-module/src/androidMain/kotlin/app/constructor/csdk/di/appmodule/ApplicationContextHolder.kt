package app.constructor.csdk.di.appmodule

import android.content.Context
import kotlin.properties.Delegates

object ApplicationContextHolder {
    private var _context: Context by Delegates.notNull()

    val context: Context
        get() = _context

    fun setContext(context: Context) {
        _context = context.applicationContext
    }
}
