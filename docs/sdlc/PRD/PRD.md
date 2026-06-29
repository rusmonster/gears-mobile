# PRD — Gears Mobile SDK


<!-- toc -->

- [1. Overview](#1-overview)
  - [1.1 Purpose](#11-purpose)
  - [1.2 Background / Problem Statement](#12-background--problem-statement)
  - [1.3 Goals (Business Outcomes)](#13-goals-business-outcomes)
  - [1.4 Glossary](#14-glossary)
- [2. Actors](#2-actors)
  - [2.1 Human Actors](#21-human-actors)
  - [2.2 System Actors](#22-system-actors)
- [3. Operational Concept & Environment](#3-operational-concept--environment)
  - [3.1 Module-Specific Environment Constraints](#31-module-specific-environment-constraints)
- [4. Scope](#4-scope)
  - [4.1 In Scope](#41-in-scope)
  - [4.2 Out of Scope](#42-out-of-scope)
- [5. Functional Requirements](#5-functional-requirements)
  - [5.1 Problem Reporting](#51-problem-reporting)
- [6. Non-Functional Requirements](#6-non-functional-requirements)
  - [6.1 NFR Inclusions](#61-nfr-inclusions)
  - [6.2 NFR Exclusions](#62-nfr-exclusions)
- [7. Public Library Interfaces](#7-public-library-interfaces)
  - [7.1 Public API Surface](#71-public-api-surface)
  - [7.2 External Integration Contracts](#72-external-integration-contracts)
- [8. Use Cases](#8-use-cases)
- [9. Acceptance Criteria](#9-acceptance-criteria)
- [10. Dependencies](#10-dependencies)
- [11. Assumptions](#11-assumptions)
- [12. Risks](#12-risks)

<!-- /toc -->

> Reconstructed from code via `cf-sdlc-reverse-engineer`, then **reviewed and confirmed by the
> product stakeholder (D. Kalita, 2026-06-23)**. The functional/non-functional requirements,
> actors, and use cases below are approved as the intended product behavior — not merely a
> description of the current code.
## 1. Overview

### 1.1 Purpose

Gears Mobile is a Kotlin Multiplatform SDK that lets host apps embed shared, native feature
behavior on both iOS and Android from one codebase. This build ships a single feature —
**problem reporting** — that captures a diagnostics bundle, encrypts it, and hands it back
to the host to share.

### 1.2 Background / Problem Statement

Mobile teams typically implement the same feature twice (Swift + Kotlin), and the two
implementations drift. Gears Mobile factors feature behavior into KMP modules so it is
written once and compiled to an Android AAR and an iOS XCFramework, while each host keeps
its native UI. Problem reporting is the first such feature.

### 1.3 Goals (Business Outcomes)

- One implementation of feature behavior shared across iOS and Android (no platform drift).
- A drop-in problem-report flow any host app can present and share.
- Diagnostics confidentiality: report payloads are encrypted before leaving the SDK.

### 1.4 Glossary

| Term | Definition |
|------|------------|
| Gear | A self-contained feature module set (domain/data/presentation). |
| `.cpb` | Constructor problem-report bundle: the encrypted report archive. |
| Host app | The native iOS/Android app embedding the SDK. |

## 2. Actors

### 2.1 Human Actors

#### End user

**ID**: `cpt-cyberfabricmobile-actor-end-user`

**Role**: A person using the host app who hits a problem and fills out the report form.
**Needs**: A quick, guided way to describe an issue and attach evidence (logs, screenshots).

### 2.2 System Actors

#### Host application

**ID**: `cpt-cyberfabricmobile-actor-host-app`

**Role**: The embedding app. Initializes the SDK, renders the native UI for the
problem-report ViewModel, and shares the resulting `.cpb` (e.g. via an email intent).

## 3. Operational Concept & Environment

### 3.1 Module-Specific Environment Constraints

- **Android**: host **MUST** call `Gears.init(context)` once before creating a ViewModel (resolves the app files directory). Minimum SDK and toolchain are pinned in `gradle/libs.versions.toml`.
- **iOS**: distributed as `Gears.xcframework` (device + simulator), consumed via `import Gears`; built on macOS with Xcode.
- No backend or network runtime — the SDK operates entirely on-device.

## 4. Scope

### 4.1 In Scope

- A problem-report capture flow (category, description, repro steps, screenshots, logs toggle).
- Local packaging + encryption of the report into a `.cpb`.
- Distribution as an Android AAR and an iOS XCFramework.

> **Roadmap (stakeholder-confirmed)**: problem-report is the **first gear** in a multi-feature
> SDK. The modular "one gear per feature" architecture is intentional; additional feature gears
> are planned and will ship through the same `Gears` umbrella. This PRD covers the
> problem-report gear.

### 4.2 Out of Scope

- Transmitting the report (the host shares it; the SDK never uploads).
- Backend/ingestion of reports; analytics; UI components (UI is the host's).

## 5. Functional Requirements

### 5.1 Problem Reporting

#### Report a problem

- [ ] `p1` - **ID**: `cpt-cyberfabricmobile-fr-report-a-problem`

The system **MUST** let an end user select a problem category, enter a description and
optional repro steps, attach screenshots, choose whether to include device logs, and submit
a packaged, encrypted report.

**Rationale**: Core capability of the SDK; the only shipped feature.

**Actors**: `cpt-cyberfabricmobile-actor-end-user`

**Acceptance Evidence**: `gears/feature/problem-report/presentation/impl/src/commonTest/.../ProblemReportViewModelImplTest.kt`

#### Ship one implementation to both platforms

- [ ] `p1` - **ID**: `cpt-cyberfabricmobile-fr-share-to-both-platforms`

The system **MUST** build the same feature code into an Android library (AAR) and an iOS
framework (XCFramework) without a second implementation.

**Rationale**: The reason the SDK is KMP.

**Actors**: `cpt-cyberfabricmobile-actor-host-app`

**Verification Method**: demonstration — `./gradlew assembleAndroidMain` and `:gears:assembleGearsReleaseXCFramework`.

## 6. Non-Functional Requirements

### 6.1 NFR Inclusions

#### No cross-platform behavior drift

- [ ] `p1` - **ID**: `cpt-cyberfabricmobile-nfr-no-platform-drift`

Feature behavior **MUST** be defined once in `commonMain`; only platform glue may differ
(`expect`/`actual`).

**Threshold**: Zero feature logic duplicated per platform; shared `commonTest` runs on JVM and iOS.

**Rationale**: Eliminating drift is the SDK's core value.

#### Report confidentiality

- [ ] `p1` - **ID**: `cpt-cyberfabricmobile-nfr-report-confidentiality`

The report archive **MUST** be encrypted to a host-supplied RSA public key before it leaves
the SDK boundary.

**Threshold**: Report content is encrypted with **AES-256-GCM**; the AES key is wrapped with
**RSA-OAEP / SHA-256** using the host-supplied `Config.publicKeyPem`. The output is a
self-contained `.cpb`; the plaintext archive is never surfaced to the host.

**Rationale**: Reports may contain logs/screenshots with sensitive data. *(Crypto scheme
confirmed by stakeholder, 2026-06-23.)*

**Verification Method**: `EncryptFileUseCaseImplTest`.

#### Native runtime footprint

- [ ] `p1` - **ID**: `cpt-cyberfabricmobile-nfr-native-footprint`

The SDK **MUST** ship as native artifacts with no embedded VM or JS bridge.

**Threshold**: iOS via Kotlin/Native (no GC runtime added); Android as a standard library.

### 6.2 NFR Exclusions

- No availability/latency/throughput SLOs apply — the SDK is an on-device library with no service surface.

## 7. Public Library Interfaces

### 7.1 Public API Surface

#### Gears entry point

- [ ] `p1` - **ID**: `cpt-cyberfabricmobile-interface-gears-public`

**Type**: Kotlin public API (Android AAR) / Swift via SKIE (iOS XCFramework)

**Stability**: stable

**Description**: `Gears.newProblemReportViewModel(ProblemReport.Config)` returns the feature's
MVI `ViewModel`; `Gears.init(Context)` (Android only) supplies the app context.

**Breaking Change Policy**: Major version bump for any change to `Gears` or `ProblemReport.Config`.

### 7.2 External Integration Contracts

#### Report bundle (`.cpb`)

- [ ] `p2` - **ID**: `cpt-cyberfabricmobile-contract-cpb-bundle`

**Direction**: provided by library

**Protocol/Format**: RSA-encrypted archive at the path in `ProblemReportResult.cpbPath`.

**Compatibility**: The host shares the opaque `.cpb`; its internal format is owned by the SDK.

## 8. Use Cases

#### Report a problem

- [ ] `p2` - **ID**: `cpt-cyberfabricmobile-usecase-report-a-problem`

**Actor**: `cpt-cyberfabricmobile-actor-end-user`

**Preconditions**:
- Host created the ViewModel via `Gears.newProblemReportViewModel(Config)` (Android: after `Gears.init(context)`).

**Main Flow**:
1. End user selects a category and writes a description.
2. End user optionally adds repro steps, screenshots, and toggles log inclusion.
3. End user submits.
4. System packages + encrypts the report and emits `ReadyToShare`.
5. Host presents a share/email intent addressed to `Config.supportEmail`.

**Postconditions**:
- An encrypted `.cpb` exists at the returned path, ready to share.

**Alternative Flows**:
- **Packaging/encryption fails**: system shows an error modal; end user dismisses and input is restored.

## 9. Acceptance Criteria

- [ ] A host can obtain and drive a problem-report ViewModel on both Android and iOS from the same API.
- [ ] Submitting a valid form yields an encrypted `.cpb` and a `ReadyToShare` event.
- [ ] The plaintext report is never returned to the host.

## 10. Dependencies

| Dependency | Description | Criticality |
|------------|-------------|-------------|
| `cryptography-kotlin` | RSA encryption of the report archive | p1 |
| `kmp-zip` / `kotlinx-io` | Cross-platform archive packing | p1 |
| SKIE | Idiomatic Swift API for the iOS framework | p2 |

## 11. Assumptions

- The host supplies a valid support email and RSA public key via `Config`.
- The host owns transport (sharing/emailing the `.cpb`).

## 12. Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Compose-resources `getString()` deadlock on iOS simulator | Flaky iOS tests | `TestStringProvider` fake; documented in DESIGN |
| Product intent originally inferred from code | Requirements might not match intent | ✅ Resolved — PRD stakeholder-reviewed and confirmed 2026-06-23 |
