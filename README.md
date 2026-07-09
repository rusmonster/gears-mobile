# Gears Mobile (Constructor Fabric)

A **Kotlin Multiplatform SDK** that ships shared, native feature behavior to **iOS and
Android** from one codebase. Business logic is written once in `commonMain` and built into
an Android **AAR** and an iOS **XCFramework** (via SKIE); each host keeps its native UI.

This build ships one feature — **Problem Report**: an in-app flow that captures a diagnostics
bundle (description, steps, screenshots, optional device logs), packages it, **encrypts it to
a support public key**, and hands the host a ready-to-share file.

- KMP project: [`gears/`](gears) · public entry point: `Gears`
- Why this stack: [`docs/WHY_GEARS.md`](docs/WHY_GEARS.md)
- Spec-driven docs (PRD → ADR → DESIGN → FEATURE, with code↔doc traceability): [`docs/sdlc/`](docs/sdlc)

## Build

```bash
cd gears
./gradlew assembleAndroidMain                          # Android library (AAR)
./gradlew :gears:assembleGearsReleaseXCFramework      # iOS framework (Gears.xcframework)
./gradlew jvmTest ktlintCheck detekt                    # tests + quality gates
```

## How it works

`Gears.newProblemReportViewModel(config)` returns an **MVI ViewModel** — dispatch `Action`s,
observe a `uiState` flow, and react to one-shot `Event`s. On `Submit`, the SDK builds an
encrypted `.cpb` and emits `Event.ReadyToShare(result)` with the file path; the **host** then
shares it (e.g. as an email attachment to `config.supportEmail`). The SDK never transmits
anything itself.

`ProblemReport.Config`:

| Field | Type | Notes |
|---|---|---|
| `supportEmail` | `String` | Destination address shown / used by the host when sharing. |
| `publicKeyPem` | `ByteArray` | PEM-encoded **RSA public key**; the report is encrypted to it. Keep the **private** key on the server/support side only. |
| `autoCapturedScreenshotPath` | `String?` | Optional screenshot pre-attached to the report. |

---

## Android

Initialize once (e.g. in `Application.onCreate`):

```kotlin
import app.constructor.gears.Gears
import app.constructor.gears.initialize

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Gears.initialize(this) // required before creating a ViewModel; enables logging + sweeps old report bundles
    }
}
```

Create the ViewModel and drive it from Compose:

```kotlin
import app.constructor.gears.Gears
import app.constructor.csdk.problemreport.presentation.api.ProblemReport

val config = ProblemReport.Config(
    supportEmail = "app-support@yourcompany.com",
    publicKeyPem = assets.open("report_public_key.pem").readBytes(),
)
val vm: ProblemReport.ViewModel = Gears.newProblemReportViewModel(config)

@Composable
fun ProblemReportScreen(vm: ProblemReport.ViewModel) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // One-shot events: when the encrypted report is ready, share it.
    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is ProblemReport.Event.ReadyToShare ->
                    shareReport(context, state.supportEmail, state.emailSubject, event.result.cpbPath)
            }
        }
    }

    // Render `state` and dispatch user intent, e.g.:
    // vm.sendAction(ProblemReport.Action.SelectProblemType(index))
    // vm.sendAction(ProblemReport.Action.UpdateDescription(text))
    // vm.sendAction(ProblemReport.Action.AddScreenshot(path))
    // vm.sendAction(ProblemReport.Action.ToggleLogs(include = true))
    // vm.sendAction(ProblemReport.Action.Submit)   // enabled when state.isSubmitEnabled
}

private fun shareReport(context: Context, to: String, subject: String, cpbPath: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(cpbPath))
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Send report"))
}
```

> **Hilt:** the SDK also ships `CSDKProblemReportViewModel` (a `@HiltViewModel`) that reads
> `supportEmail`, `publicKeyPem`, and `autoCapturedScreenshotPath` from the `SavedStateHandle`
> (navigation arguments) — inject it with `hiltViewModel()` instead of constructing the VM
> manually.

---

## iOS

Add `Gears.xcframework` to your app, then (SwiftUI example):

```swift
import Gears

// Initialize once before creating a ViewModel (e.g. in your `App` initializer or `AppDelegate`).
// Enables SDK file logging and sweeps report bundles older than 24h; no context is required on iOS.
Gears.shared.initialize()

extension Data {
    // Config.publicKeyPem is a Kotlin ByteArray — bridge it from Data:
    func toKotlinByteArray() -> KotlinByteArray {
        let result = KotlinByteArray(size: Int32(count))
        for (i, byte) in enumerated() {
            result.set(index: Int32(i), value: Int8(bitPattern: byte))
        }
        return result
    }
}

let config = ProblemReport.Config(
    supportEmail: "app-support@yourcompany.com",
    publicKeyPem: publicKeyPemData.toKotlinByteArray(),
    autoCapturedScreenshotPath: nil
)
let vm = Gears.shared.newProblemReportViewModel(config: config)

// SKIE exposes Kotlin flows as Swift AsyncSequences.
Task {
    for await state in vm.uiState {
        render(state) // state.problemTypeOptions, state.isSubmitEnabled, state.supportEmail, …
    }
}
Task {
    for await event in vm.events {
        switch onEnum(of: event) {
        case .readyToShare(let e):
            shareReport(cpbPath: e.result.cpbPath, to: /* state.supportEmail */, subject: /* state.emailSubject */)
        }
    }
}

// Dispatch user intent:
vm.sendAction(action: ProblemReport.ActionUpdateDescription(text: "App freezes on launch"))
vm.sendAction(action: ProblemReport.ActionSubmit())
```

Then present the `.cpb` at `result.cpbPath` via `MFMailComposeViewController` (attach the file,
prefill `to: config.supportEmail`) or a `UIActivityViewController`.

> **Storage:** the SDK writes each `.cpb` to the app files directory and sweeps any bundle older
> than 24h on the next `Gears.initialize(...)`, so shared reports don't accumulate even if the host
> never deletes them.

> Exact Swift symbol names (e.g. `ProblemReport.ActionSubmit`, `onEnum(of:)`) are generated by
> SKIE from the Kotlin `sealed` contract; see SKIE's docs for sealed/flow interop.

---

## Decrypting received reports (`.cpb`)

Reports arrive as encrypted `.cpb` attachments (encrypted to the public key you supplied in
`Config.publicKeyPem`). Decrypt one with the bundled Gradle task and your support **private
key**:

```bash
cd gears
./gradlew decryptReport -Pcpb=/path/to/report.cpb        # writes report.zip next to it
# or:  ./gradlew decryptReport -Pcpb=<report.cpb> -Pkey=<private_key.pem>
```

By default the task reads the private key from `~/.constructor/private_key.pem`.

**Full guide** — key setup, the `.cpb` format, and what's inside the decrypted ZIP:
**[docs/decrypt-report.md](docs/decrypt-report.md)**.
