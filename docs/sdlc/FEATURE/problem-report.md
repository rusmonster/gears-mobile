# Feature: Problem Report


<!-- toc -->

- [1. Feature Context](#1-feature-context)
  - [1.1 Overview](#11-overview)
  - [1.2 Purpose](#12-purpose)
  - [1.3 Actors](#13-actors)
  - [1.4 References](#14-references)
- [2. Actor Flows (CDSL)](#2-actor-flows-cdsl)
  - [Submit a problem report](#submit-a-problem-report)
  - [Dismiss the modal](#dismiss-the-modal)
- [3. Processes / Business Logic (CDSL)](#3-processes--business-logic-cdsl)
  - [Create problem-report archive](#create-problem-report-archive)
  - [Clean up old report bundles](#clean-up-old-report-bundles)
- [4. States (CDSL)](#4-states-cdsl)
  - [Submission modal state machine](#submission-modal-state-machine)
- [5. Definitions of Done](#5-definitions-of-done)
  - [Form capture and validation](#form-capture-and-validation)
  - [Encrypted packaging](#encrypted-packaging)
- [6. Acceptance Criteria](#6-acceptance-criteria)

<!-- /toc -->

- [x] `p1` - **ID**: `cpt-cyberfabricmobile-featstatus-problem-report`

> Reconstructed from code via `cf-sdlc-reverse-engineer`. The flow / algo / state / dod IDs
> are traced to `@cpt-*` markers in the implementation (**FULL** traceability; `cfs validate`
> coverage 6/6). Product-intent IDs (requirements, actors) are defined in the PRD and have been
> stakeholder-reviewed (2026-06-23).
## 1. Feature Context

### 1.1 Overview

Lets a user report a problem from inside a host app: pick a category, describe the issue,
optionally add steps and screenshots, choose whether to include device logs, then submit.
The feature packages the report into an **encrypted archive (`.cpb`)** and emits a
ready-to-share event so the host can hand it off (e.g. via an email intent).

### 1.2 Purpose

Provides a turnkey, cross-platform diagnostics-capture flow so every host app collects
problem reports the same way on iOS and Android, with the payload encrypted to a support
public key before it ever leaves the SDK.

**Requirements**: `cpt-cyberfabricmobile-fr-report-a-problem`, `cpt-cyberfabricmobile-nfr-report-confidentiality`

**Principles**: `cpt-cyberfabricmobile-principle-mvi`, `cpt-cyberfabricmobile-principle-shared-logic-native-ui`

> Requirement IDs are defined in the PRD and have been stakeholder-reviewed (2026-06-23).

### 1.3 Actors

| Actor | Role in Feature |
|-------|-----------------|
| `cpt-cyberfabricmobile-actor-end-user` | Fills and submits the report form |
| `cpt-cyberfabricmobile-actor-host-app` | Calls `Gears.newProblemReportViewModel(Config)`, renders UI, shares the resulting `.cpb` |

### 1.4 References

- **Design**: [DESIGN.md](../DESIGN/DESIGN.md)
- **PRD**: [PRD.md](../PRD/PRD.md)
- **Dependencies**: `:common:zip`, `:common:files`, `:common:logging`, `:common:mvi`, `cryptography-kotlin`

## 2. Actor Flows (CDSL)

**Use cases**: `cpt-cyberfabricmobile-usecase-report-a-problem`

### Submit a problem report

- [x] `p1` - **ID**: `cpt-cyberfabricmobile-flow-problem-report-submit`

**Actor**: `cpt-cyberfabricmobile-actor-end-user`

**Success Scenarios**:
- User fills a non-blank description, taps Submit, and receives a shareable encrypted report.

**Error Scenarios**:
- Report assembly/encryption fails → an error modal is shown; input is re-enabled on dismiss.
- Submit dispatched while the form is invalid or a submission is already in flight → the action
  is ignored (no state change), guarding on authoritative ViewModel state rather than the UI's
  enabled flag. An out-of-range problem-type index is rejected rather than defaulting to `OTHER`.

**Steps** (run only after the guard above passes):
1. [x] - `p1` - Set `modalState = Submitting`, `isInputEnabled = false` - `inst-submitting`
2. [x] - `p1` - Build `ProblemReportData` from `UiState` and run `cpt-cyberfabricmobile-algo-problem-report-create` - `inst-create`
3. [x] - `p1` - **RETURN** `Event.ReadyToShare(ProblemReportResult)`; host opens a share/email intent to `Config.supportEmail` - `inst-ready`

### Dismiss the modal

- [x] `p1` - **ID**: `cpt-cyberfabricmobile-flow-problem-report-dismiss-modal`

**Actor**: `cpt-cyberfabricmobile-actor-end-user`

**Steps**:
1. [x] - `p1` - On `Action.DismissModal`, set `modalState = None`, `isInputEnabled = true` - `inst-dismiss`

Evidence: `gears/feature/problem-report/presentation/impl/.../ProblemReportViewModelImpl.kt`,
`gears/feature/problem-report/presentation/api/.../ProblemReport.kt`.

## 3. Processes / Business Logic (CDSL)

### Create problem-report archive

- [x] `p2` - **ID**: `cpt-cyberfabricmobile-algo-problem-report-create`

**Input**: `ProblemReportData` (type, description, repro steps, screenshot paths, includeLogs), `MetadataLabels`

**Output**: `ProblemReportResult` (reportId, cpbPath)

**Steps**:
1. [x] - `p1` - Generate `reportId` and collect device metadata (`DeviceInfo` expect/actual) - `inst-meta`
2. [x] - `p1` - **IF** `includeLogs` **THEN** gather log files from `:common:logging` - `inst-logs`
3. [x] - `p1` - In a **per-report temp directory**, write metadata then pack logs + screenshots + metadata into a zip via `:common:zip` `ZipPacker` - `inst-zip`
4. [x] - `p1` - Encrypt the archive to `Config.publicKeyPem` → `.cpb` (`EncryptFileUseCase`), then move it to a report-id-scoped path (`problem_report_<reportId>.cpb`) - `inst-encrypt`
5. [x] - `p1` - **RETURN** `ProblemReportResult(reportId, cpbPath)` - `inst-result`

Each report is isolated in its own temp directory, so concurrent ViewModels or rapid repeated
submissions cannot overwrite each other's artifacts or replace a `cpbPath` already returned to
an earlier report. A `finally` block always deletes that temp directory, so the plaintext zip +
metadata never linger on disk whether encryption succeeds or fails.

Evidence: `gears/feature/problem-report/data/.../CreateProblemReportUseCaseImpl.kt`,
`.../EncryptFileUseCaseImpl.kt`, `.../CpbFormat.kt`, `gears/common/zip/.../ZipPackerImpl.kt`.

### Clean up old report bundles

Encrypted `.cpb` bundles can linger after the host has shared them. On every
`Gears.initialize(...)`, `CleanupProblemReportsUseCase` runs asynchronously and deletes any `.cpb`
in the app directory older than 24h, bounding how long report data stays on device. Supporting
maintenance behavior (no `@cpt` marker; not part of the traced flow).

Evidence: `gears/feature/problem-report/data/.../CleanupProblemReportsUseCaseImpl.kt`,
`gears/gears/src/commonMain/kotlin/app/constructor/gears/GearsInitializer.kt`.

## 4. States (CDSL)

### Submission modal state machine

- [x] `p2` - **ID**: `cpt-cyberfabricmobile-state-problem-report-modal`

**States**: None, Submitting, Error

**Initial State**: None

**Transitions**:
1. [x] - `p1` - **FROM** None **TO** Submitting **WHEN** `Action.Submit` dispatched **and** the form is submittable (`isSubmitEnabled`, not already submitting) - `inst-t-submit`
2. [x] - `p1` - **FROM** Submitting **TO** None **WHEN** archive created → `ReadyToShare` emitted - `inst-t-success`
3. [x] - `p1` - **FROM** Submitting **TO** Error **WHEN** assembly/encryption throws - `inst-t-error`
4. [x] - `p1` - **FROM** Error **TO** None **WHEN** `Action.DismissModal` - `inst-t-dismiss`

Evidence: `ProblemReport.ModalState` (`None`/`Submitting`/`Error`) in `ProblemReport.kt`.

## 5. Definitions of Done

### Form capture and validation

- [x] `p1` - **ID**: `cpt-cyberfabricmobile-dod-problem-report-form`

The system **MUST** expose the full `Action`/`UiState` contract (category, description, repro
steps, screenshots, include-logs), truncate text at `MAX_TEXT_LENGTH`, and enable Submit only
when the description is non-blank. Submit is additionally guarded at dispatch time against the
authoritative ViewModel state, so an invalid or in-flight submission is ignored rather than
relying on the UI's enabled flag alone.

**Steps**:
1. [x] - `p1` - On init, populate problem-type options, support email, and any auto-captured screenshot - `inst-init`
2. [x] - `p1` - Truncate description / steps input at `MAX_TEXT_LENGTH` - `inst-truncate`
3. [x] - `p1` - Compute `isSubmitEnabled` (non-blank description, a selected type, input enabled) - `inst-guard`

**Implements**: `cpt-cyberfabricmobile-flow-problem-report-submit`

**Touches**:
- Entities: `ProblemReportData`, `ProblemType`, `ProblemReport.UiState`

### Encrypted packaging

- [x] `p1` - **ID**: `cpt-cyberfabricmobile-dod-problem-report-encrypt`

The system **MUST** package the report into a zip and encrypt it to `Config.publicKeyPem`,
producing a `.cpb` whose path is returned in `ProblemReportResult`.

**Steps**:
1. [x] - `p1` - Generate an AES key and RSA-encrypt it with `Config.publicKeyPem` - `inst-keys`
2. [x] - `p1` - Write the `.cpb` envelope (header + encrypted key + IV + AES-GCM-encrypted zip stream) - `inst-envelope`
3. [x] - `p1` - **RETURN** the `.cpb` path - `inst-return`

**Implements**: `cpt-cyberfabricmobile-algo-problem-report-create`

**Constraints**: `cpt-cyberfabricmobile-constraint-android-init`

**Touches**:
- Entities: `ProblemReportResult`
- Crypto: `cryptography-kotlin` (RSA), `CpbFormat`

## 6. Acceptance Criteria

- [ ] Initial state exposes all problem-type options and the configured support email.
- [ ] A blank description keeps `isSubmitEnabled = false`; a non-blank one enables Submit.
- [ ] Description and repro-steps fields truncate at 1000 characters.
- [ ] Adding then removing a screenshot leaves the expected set; auto-captured screenshot from `Config` is pre-attached.
- [ ] Submit emits `Event.ReadyToShare` with a `ProblemReportResult`, and the use case receives the captured `ProblemReportData`.
- [ ] Submit dispatched while the form is invalid (or a submission is already running) is ignored — no `Submitting` transition and no use-case call.
- [ ] Each report produces a unique, report-id-scoped `cpbPath`; the plaintext zip + metadata are deleted after encryption (success or failure).
- [ ] `.cpb` bundles older than 24h are deleted on `Gears.initialize(...)`; newer bundles and non-`.cpb` files are left untouched.
- [ ] On use-case failure, `modalState` becomes `Error` and input is disabled; `DismissModal` restores `None` + input.

> Acceptance criteria above are **covered by existing tests**:
> `ProblemReportViewModelImplTest` (commonTest), `ProblemReportStringsLocaleTest` (jvmTest),
> `CreateProblemReportUseCaseImplTest`, `EncryptFileUseCaseImplTest`, `CleanupProblemReportsUseCaseImplTest`.
