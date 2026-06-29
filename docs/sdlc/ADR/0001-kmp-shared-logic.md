---
status: accepted
date: 2026-06-13
decision-makers: reconstructed from code (cf-sdlc-reverse-engineer)
---

# Use Kotlin Multiplatform for shared feature logic, native UI per platform


<!-- toc -->

- [Context and Problem Statement](#context-and-problem-statement)
- [Decision Drivers](#decision-drivers)
- [Considered Options](#considered-options)
- [Decision Outcome](#decision-outcome)
  - [Consequences](#consequences)
  - [Confirmation](#confirmation)
- [Pros and Cons of the Options](#pros-and-cons-of-the-options)
  - [Kotlin Multiplatform (shared logic, native UI)](#kotlin-multiplatform-shared-logic-native-ui)
  - [Cross-platform UI runtime (Flutter / React Native)](#cross-platform-ui-runtime-flutter--react-native)
  - [Duplicate native implementations](#duplicate-native-implementations)
- [More Information](#more-information)
- [Traceability](#traceability)

<!-- /toc -->

**ID**: `cpt-cyberfabricmobile-adr-kmp-shared-logic`
## Context and Problem Statement

The SDK must deliver the same feature behavior on iOS and Android without maintaining two
implementations. How should shared logic be built and distributed while keeping native UI?

## Decision Drivers

* `cpt-cyberfabricmobile-nfr-no-platform-drift` — one behavior, no per-platform divergence
* `cpt-cyberfabricmobile-nfr-native-footprint` — no embedded VM / JS bridge
* `cpt-cyberfabricmobile-fr-share-to-both-platforms` — one codebase → both platform artifacts

## Considered Options

* Kotlin Multiplatform (shared logic, native UI)
* A cross-platform UI runtime (Flutter / React Native)
* Duplicate native implementations (Swift + Kotlin)

## Decision Outcome

Chosen option: "Kotlin Multiplatform", because it shares business logic as native artifacts
(Android AAR, iOS XCFramework via Kotlin/Native + SKIE) while letting each host keep native
UI — satisfying both the no-drift and native-footprint drivers.

### Consequences

* Good, because feature behavior is written and tested once (`commonMain` / `commonTest`).
* Good, because iOS gets a native framework with no added GC/VM runtime.
* Bad, because Kotlin/Native iOS link times are slow and the K/N↔Swift boundary has edges (smoothed by SKIE).

### Confirmation

`gears/settings.gradle.kts` + per-module `convention.*` plugins declare `jvm`, `android`,
`iosArm64`, `iosSimulatorArm64`; `:gears:assembleGearsReleaseXCFramework` and
`assembleAndroidMain` both build from the same sources.

## Pros and Cons of the Options

### Kotlin Multiplatform (shared logic, native UI)

* Good, because business logic is written and tested once and compiles to native artifacts.
* Good, because iOS gets a native framework (no GC/VM runtime added).
* Neutral, because UI is still written twice (native per platform) — by design.
* Bad, because Kotlin/Native iOS link times are slow.

### Cross-platform UI runtime (Flutter / React Native)

* Good, because UI is also shared.
* Bad, because UI is non-native and a VM/bridge runtime is added — violates the native-footprint driver.

### Duplicate native implementations

* Good, because fully idiomatic per platform.
* Bad, because the same behavior is implemented twice and drifts — the problem this SDK exists to solve.

## More Information

Platform specifics are isolated behind `expect`/`actual` (`FileSystem`, `DeviceInfo`,
`ConsoleLogWriter`).

## Traceability

This decision directly addresses:

* `cpt-cyberfabricmobile-nfr-no-platform-drift` — shared `commonMain` logic
* `cpt-cyberfabricmobile-nfr-native-footprint` — Kotlin/Native artifacts
* `cpt-cyberfabricmobile-fr-share-to-both-platforms` — one codebase → AAR + XCFramework
* `cpt-cyberfabricmobile-design-gears-sdk` — overall architecture
