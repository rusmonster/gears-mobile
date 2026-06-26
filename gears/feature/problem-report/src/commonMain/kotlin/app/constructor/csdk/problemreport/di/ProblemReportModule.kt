package app.constructor.csdk.problemreport.di

import app.constructor.csdk.files.FileSystem
import app.constructor.csdk.problemreport.data.CreateProblemReportUseCaseImpl
import app.constructor.csdk.problemreport.data.EncryptFileUseCaseImpl
import app.constructor.csdk.problemreport.presentation.api.ProblemReport
import app.constructor.csdk.problemreport.presentation.impl.ProblemReportViewModelImpl
import app.constructor.csdk.zip.impl.ZipPacker

object ProblemReportModule {

    fun newProblemReportViewModel(config: ProblemReport.Config): ProblemReport.ViewModel = ProblemReportViewModelImpl(
        createProblemReportUseCase = CreateProblemReportUseCaseImpl(
            zipPacker = ZipPacker(),
            fileSystem = FileSystem(),
            encryptFile = EncryptFileUseCaseImpl(config.publicKeyPem),
        ),
        config = config,
    )
}
