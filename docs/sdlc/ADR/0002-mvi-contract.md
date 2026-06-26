---
status: accepted
date: 2026-06-13
decision-makers: reconstructed from code (cf-sdlc-reverse-engineer)
---

# Expose features through a uniform MVI contract


<!-- toc -->

- [Context and Problem Statement](#context-and-problem-statement)
- [Decision Drivers](#decision-drivers)
- [Considered Options](#considered-options)
- [Decision Outcome](#decision-outcome)
  - [Consequences](#consequences)
  - [Confirmation](#confirmation)
- [Pros and Cons of the Options](#pros-and-cons-of-the-options)
  - [MVI (`sealed` Action/UiState/Event + `MviViewModel`)](#mvi-sealed-actionuistateevent--mviviewmodel)
  - [Per-feature ad-hoc state holders](#per-feature-ad-hoc-state-holders)
  - [Duplicated platform-native patterns](#duplicated-platform-native-patterns)
- [More Information](#more-information)
- [Traceability](#traceability)

<!-- /toc -->

**ID**: `cpt-cyberfabricmobile-adr-mvi-contract`
## Context and Problem Statement

Features need a consistent, testable way to expose state and behavior to two different native
UIs. What presentation contract should every feature follow?

## Decision Drivers

* `cpt-cyberfabricmobile-nfr-no-platform-drift` — both UIs consume the same contract
* `cpt-cyberfabricmobile-fr-report-a-problem` — the report form needs clear state + events
* Testability of feature behavior without a UI

## Considered Options

* MVI: `sealed` `Action`/`UiState`/`Event` + `MviViewModel`
* Per-feature ad-hoc state holders
* Platform-native patterns duplicated (Combine on iOS, ViewModel on Android)

## Decision Outcome

Chosen option: "MVI", because a single unidirectional contract (`MviViewModel<Action,
UiState, Event>`) is consumed identically by SwiftUI and Compose, is exhaustively typed via
`sealed` hierarchies, and is unit-testable on the JVM.

### Consequences

* Good, because hosts dispatch `Action`s, observe a `UiState` `Flow`, and handle one-shot `Event`s the same way on both platforms.
* Good, because SKIE renders the `sealed`/`Flow` contract idiomatically in Swift; Android bridges via `CSDKViewModel` (Hilt).
* Bad, because simple screens still pay the MVI ceremony.

### Confirmation

`gears/common/mvi` (`MviViewModel`, `BaseMviViewModelImpl`); `ProblemReport` exposes
`Action`/`UiState`/`Event`/`ModalState` and `typealias ViewModel = MviViewModel<...>`.

## Pros and Cons of the Options

### MVI (`sealed` Action/UiState/Event + `MviViewModel`)

* Good, because one unidirectional contract is consumed identically by SwiftUI and Compose.
* Good, because `sealed` types are exhaustive and the contract is JVM-unit-testable.
* Bad, because simple screens still pay the MVI ceremony.

### Per-feature ad-hoc state holders

* Good, because minimal boilerplate for trivial screens.
* Bad, because every feature invents its own shape; no uniform host integration.

### Duplicated platform-native patterns

* Good, because idiomatic per platform.
* Bad, because state logic is written twice and drifts.

## More Information

`gears/common/mvi` provides the shared base; `CSDKViewModel` bridges to Hilt on Android,
and SKIE renders the contract idiomatically in Swift.

## Traceability

This decision directly addresses:

* `cpt-cyberfabricmobile-fr-report-a-problem` — the feature's state/behavior contract
* `cpt-cyberfabricmobile-nfr-no-platform-drift` — one contract, both UIs
* `cpt-cyberfabricmobile-featstatus-problem-report` — applied by the shipped feature
