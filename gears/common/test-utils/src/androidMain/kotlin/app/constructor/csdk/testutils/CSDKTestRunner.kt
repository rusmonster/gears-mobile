package app.constructor.csdk.testutils

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import app.constructor.csdk.di.appmodule.ApplicationContextHolder

class CSDKTestRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle?) {
        ApplicationContextHolder.setContext(targetContext.applicationContext)
        super.onCreate(arguments)
    }
}
