package app.constructor.csdk.problemreport.di

import androidx.lifecycle.SavedStateHandle
import app.constructor.csdk.mvi.impl.CSDKViewModel
import app.constructor.csdk.problemreport.presentation.api.ProblemReport
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CSDKProblemReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : CSDKViewModel(),
    ProblemReport.ViewModel by ProblemReportModule.newProblemReportViewModel(
        ProblemReport.Config(
            supportEmail = savedStateHandle.get<String>("supportEmail") ?: error("supportEmail is not provided"),
            publicKeyPem = savedStateHandle.get<ByteArray>("publicKeyPem") ?: error("publicKeyPem is not provided"),
            autoCapturedScreenshotPath = savedStateHandle.get("autoCapturedScreenshotPath"),
        ),
    )
