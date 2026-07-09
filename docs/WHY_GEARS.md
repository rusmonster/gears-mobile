# Why Should I Use Constructor Fabric Gears Mobile (Kotlin)?

<!-- toc -->

- [Executive Summary](#executive-summary)
  - [At a glance: Native ×2 vs Flutter/RN vs KMP vs KMP + Gears Mobile](#at-a-glance-native-2-vs-flutterrn-vs-kmp-vs-kmp--gears-mobile)
- [Part A — Where Kotlin (KMP) has advantages for shared mobile code](#part-a--where-kotlin-kmp-has-advantages-for-shared-mobile-code)
  - [A.1 Null safety is part of the type system](#a1-null-safety-is-part-of-the-type-system)
  - [A.2 Sealed hierarchies make illegal states unrepresentable](#a2-sealed-hierarchies-make-illegal-states-unrepresentable)
  - [A.3 Exhaustive `when` makes state evolution safer](#a3-exhaustive-when-makes-state-evolution-safer)
  - [A.4 Structured concurrency instead of two threading models](#a4-structured-concurrency-instead-of-two-threading-models)
  - [A.5 `expect`/`actual` — one API, platform-specific bodies](#a5-expectactual--one-api-platform-specific-bodies)
  - [A.6 Value classes make identity mix-ups a compile error](#a6-value-classes-make-identity-mix-ups-a-compile-error)
  - [A.7 Compiler plugins and codegen move rules into build time](#a7-compiler-plugins-and-codegen-move-rules-into-build-time)
  - [A.8 One language, native binaries on both platforms](#a8-one-language-native-binaries-on-both-platforms)
  - [A.9 Tooling and static analysis as a first-class citizen](#a9-tooling-and-static-analysis-as-a-first-class-citizen)
- [Part B — Why "just KMP" is not enough: what Gears Mobile adds](#part-b--why-just-kmp-is-not-enough-what-gears-mobile-adds)
  - [B.1 A pre-integrated modular feature backbone](#b1-a-pre-integrated-modular-feature-backbone)
  - [B.2 Layer isolation by default](#b2-layer-isolation-by-default)
  - [B.3 One consistent UI contract: MVI](#b3-one-consistent-ui-contract-mvi)
  - [B.4 Prewritten architecture lints](#b4-prewritten-architecture-lints)
  - [B.5 Composable gears: one codebase, two distribution shapes](#b5-composable-gears-one-codebase-two-distribution-shapes)
  - [B.6 A typed public surface, idiomatic on both sides](#b6-a-typed-public-surface-idiomatic-on-both-sides)
  - [B.7 Runtime toolkit capabilities](#b7-runtime-toolkit-capabilities)
  - [B.8 Shared resources and localization](#b8-shared-resources-and-localization)
  - [B.9 Preconfigured build-gated safety](#b9-preconfigured-build-gated-safety)
  - [B.10 Local-first, shift-left testing](#b10-local-first-shift-left-testing)
  - [B.11 Supply-chain and security policy as code](#b11-supply-chain-and-security-policy-as-code)
  - [B.12 Spec-driven development with Constructor Studio](#b12-spec-driven-development-with-constructor-studio)
- [When Gears Mobile is (and isn't) the right choice](#when-gears-mobile-is-and-isnt-the-right-choice)
- [Get started](#get-started)

<!-- /toc -->

> A guide for **iOS (Swift) and Android (Kotlin) developers** evaluating **Constructor
> Fabric Gears Mobile** — a secure, modular **mobile SDK framework** built on Kotlin
> Multiplatform that ships shared business logic to both platforms from one codebase.

**Repository layout**

- **KMP project** — [`gears/`](../gears)
- **Convention plugins** — [`gradle/plugins/plugin/src/main/kotlin/convention`](../gradle/plugins/plugin/src/main/kotlin/convention)
- **Version catalog** — [`gradle/libs.versions.toml`](../gradle/libs.versions.toml)
- **Custom architecture lints** — [`gradle/ktlint-rules`](../gradle/ktlint-rules)
- **CI pipeline** — [`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

---

## Executive Summary

If you ship a feature to **both iOS and Android**, you are repeatedly solving the same
problem twice: the same form validation, the same state machine, the same networking,
the same error handling — once in Swift and once in Kotlin, drifting apart over time.
Native development is excellent for UI, and cross-platform UI runtimes (Flutter, React
Native) solve the duplication by replacing the native layer wholesale.

Gears Mobile takes a different position, in two layers:

1. **Kotlin Multiplatform as the language layer** moves whole classes of bugs — null
   dereferences, unhandled states, "forgot to update the other platform" drift — into
   the type system. Business logic is written **once**, compiled to a native iOS
   framework (via Kotlin/Native, no extra runtime) and an Android library, while each
   platform keeps its **native UI**.

2. **Gears Mobile as the framework** provides the platform layer around KMP: a modular
   "one gear per feature" architecture with enforced layer boundaries, a uniform MVI
   UI contract, a shared toolkit (DI, logging, files), build-gated
   architecture lints, a single umbrella that emits both an Android **AAR** and an iOS
   **XCFramework**, and local-first testing of the full logic on a laptop.

The result is a stable foundation for **long-living mobile features**: product teams
share one correctness-checked implementation of behavior across platforms instead of
maintaining two. This structure is especially useful for AI-driven development: coding
agents work better when the architecture is expressed as module types, convention
plugins, lints, and tests that give deterministic feedback — not only as prose.

### At a glance: Native ×2 vs Flutter/RN vs KMP vs KMP + Gears Mobile

| # | Concern | Native ×2 (Swift + Kotlin) | Flutter / React Native | KMP (plain) | **KMP + Gears Mobile** |
|---:|---|---|---|---|---|
| 1 | **UI** | fully native | non-native (engine / bridge) | fully native | fully native |
| 2 | **Logic sharing** | 0% — written twice | shared, non-native | shared (you wire it up) | **shared feature modules, pre-wired** |
| 3 | **Runtime footprint** | none added | Dart VM / JS bridge added | native; no GC on iOS (K/N) | same, + a thin SDK |
| 4 | **Null safety** | Swift optionals / Kotlin; ObjC `nil`, JS none | Dart sound nullability | Kotlin null safety | Kotlin null safety |
| 5 | **Illegal states** | structs + flags, by convention | classes + flags | `sealed` hierarchies | `sealed` **MVI contracts** |
| 6 | **State evolution** | `switch` may miss a case | `switch` may miss a case | exhaustive `when` | exhaustive `when` + lints |
| 7 | **Concurrency** | GCD/Combine *and* coroutines/threads — twice | isolates / event loop | coroutines + structured concurrency, shared | same + flow test tooling |
| 8 | **Platform-specific code** | separate codebases | platform channels | `expect`/`actual` | `expect`/`actual` + toolkit |
| 9 | **Architecture** | per-app, per-platform | per-app | per-team | **enforced layers** (domain/data/presentation/api) |
| 10 | **UI state management** | per-app (×2) | setState / BLoC / Redux | per-team | **MVI contract built-in** |
| 11 | **DI** | per-platform | `get_it` / context | per-team | shared DI modules + Hilt bridge |
| 12 | **Resources / i18n** | per-platform string files | framework intl | Compose resources | shared Compose-resources strings |
| 13 | **Architecture policy** | SwiftLint / Detekt, per-team | linter, per-team | ktlint / Detekt | **prewritten ktlint + Detekt + custom rules** |
| 14 | **Distribution** | per-platform | package | manual | **one umbrella → AAR + XCFramework (SKIE)** |
| 15 | **Testing** | two test suites | framework test | jvm/native tests | **local-first `jvmTest` + commonTest + Turbine/mokkery** |
| 16 | **Build-gated safety** | per-team | analyzer | warnings | `allWarningsAsErrors` + ktlint + Detekt + sort-deps |
| 17 | **Security scanning** | per-team | per-team | per-team | **CI SAST** (Trivy/Semgrep/TruffleHog/MobSF) |
| 18 | **Spec ↔ code traceability** | docs drift, per-team | docs drift, per-team | docs drift, per-team | **PRD→ADR→DESIGN→FEATURE with `@cpt` code↔doc markers, validated by `cfs`** |

## Part A — Where Kotlin (KMP) has advantages for shared mobile code

This section is about the **language**. Gears Mobile is built on Kotlin Multiplatform
because it targets *long-lived, shared feature code* where correctness and avoiding
two-platform drift matter more than time-to-first-prototype.

### A.1 Null safety is part of the type system

Swift has optionals and Kotlin has nullable types, but the moment you cross into
Objective-C interop, implicitly-unwrapped optionals, or a JS bridge, "this might be
absent" stops being checked. In Kotlin, nullability is in every signature and the
compiler refuses to let you dereference a `T?` without handling the `null`.

```kotlin
// Kotlin — the compiler will not let you ignore absence.
fun loadUser(id: String): User? = repo.findUser(id)

val user = loadUser("42")
println(user.name)        // compile error: user is User?
println(user?.name ?: "") // you must handle the null branch
```

Whole categories of `NullPointerException` / `nil`-crash become compile-time failures
instead of review conventions — and that guarantee holds identically on iOS and Android
because it is the *same* code.

### A.2 Sealed hierarchies make illegal states unrepresentable

Modeling UI state with a struct full of optional fields means "which combinations are
valid?" lives in a comment. Kotlin `sealed` interfaces carry data per-variant, so
invalid combinations cannot be constructed. This is exactly how the problem-report
feature models its modal state ([`ProblemReport.kt`](../gears/feature/problem-report/presentation/api/src/commonMain/kotlin/app/constructor/csdk/problemreport/presentation/api/ProblemReport.kt)):

```kotlin
sealed interface ModalState {
    data object None : ModalState
    data object Submitting : ModalState
    data class Error(val message: String) : ModalState
}
```

There is no way to be `Submitting` *and* carry an error string — the type system forbids
the ambiguous flag-bag that the equivalent struct-with-booleans invites.

### A.3 Exhaustive `when` makes state evolution safer

In Swift or Dart, adding a new case to an enum may leave old `switch` statements quietly
compiling. In Kotlin, a `when` over a sealed type is **exhaustive**: add a variant and
every `when` that forgot it fails to compile until you decide what the new state means.

```kotlin
// Add ModalState.Retrying later and this stops compiling until handled.
val label = when (state) {
    is ModalState.None -> ""
    is ModalState.Submitting -> "Sending…"
    is ModalState.Error -> state.message
}
```

The same pattern runs through the feature's `Action` and `Event` contracts. When the
behavior evolves over years across two platforms, the compiler — not a reviewer's
memory — points at every place that needs updating, once.

### A.4 Structured concurrency instead of two threading models

Without sharing, asynchronous work is written twice — GCD/Combine on iOS, coroutines or
threads on Android — with two cancellation models and two sets of bugs. KMP coroutines
give one structured-concurrency model, written once, with scoped cancellation that
propagates correctly. The problem-report ViewModel runs its submit flow in a single
`viewModelScope`-style coroutine pipeline that both platforms share verbatim.

### A.5 `expect`/`actual` — one API, platform-specific bodies

When something genuinely differs per platform — the files directory, device metadata,
the console logger — KMP lets you declare one `expect` signature in common code and
provide an `actual` body per target, instead of forking the whole module. The SDK uses
this for `FileSystem`, `DeviceInfo`, and `ConsoleLogWriter`:

```kotlin
// commonMain
expect fun FileSystem(): FileSystem

// androidMain → app files dir via Context;  iosMain → NSFileManager;  jvmMain → java.io
```

Callers in common code depend only on the `expect` contract; the platform difference is
contained to a few `actual` files rather than smeared across the feature.

### A.6 Value classes make identity mix-ups a compile error

Most features have IDs and tokens that are all `String` underneath but should never be
interchangeable. Kotlin `@JvmInline value class` gives a distinct, **zero-cost** type:
it compiles to the underlying `String` but the compiler rejects mixing them up.

```kotlin
@JvmInline value class UserId(val value: String)
@JvmInline value class TenantId(val value: String)

fun loadTenant(id: TenantId) { /* … */ }

loadTenant(UserId("u_1")) // compile error — distinct types, no runtime cost
```

Gears Mobile encourages this for domain identities so that a wrong-argument bug is
caught at build time on both platforms rather than shipped.

### A.7 Compiler plugins and codegen move rules into build time

Kotlin has no Rust-style macros, but its compiler-plugin and codegen ecosystem plays the
same role: repetitive correctness rules live next to the type and are checked by the
build, not by hand.

- **`kotlinx.serialization`** — `@Serializable` generates serializers; no hand-written JSON glue.
- **Compose-resources** — generates a typed `Res` accessor for strings/files from `composeResources/`.
- **SKIE** — a Kotlin/Native compiler plugin that turns Kotlin `sealed`/`enum`/flows/suspend into idiomatic Swift at build time.
- **KSP** — annotation processing (e.g. Hilt on the Android side) generates wiring that stays type-checked.

This is the bridge from plain Kotlin to Gears Mobile: the framework leans on these so
that conventions become generated, compiler-verified code rather than runtime
reflection or copy-paste.

### A.8 One language, native binaries on both platforms

Kotlin/Native compiles the shared code to a real native iOS framework — **no GC on
iOS, no embedded VM, no JS bridge**. Android gets a normal library. You write behavior
once and get native artifacts on both sides, which is exactly what you want for SDKs
embedded into host apps that care about size and startup.

### A.9 Tooling and static analysis as a first-class citizen

`gradle`, `ktlint`, `detekt`, and the Kotlin compiler's warning controls give a
consistent toolchain. Crucially, ktlint's ruleset is **extensible** — which is the hook
Gears Mobile uses to enforce *architecture* at build time (see Part B).

---

## Part B — Why "just KMP" is not enough: what Gears Mobile adds

KMP gives you a shared, safe language. It does **not** give you a feature architecture,
a UI state contract, enforced layer boundaries, a distribution story, or a toolkit. In
native, Flutter, or KMP, teams still choose or build those conventions per project.

Gears Mobile is the **framework** that provides shared implementations and makes the
secure, modular path the standard one.

### B.1 A pre-integrated modular feature backbone

A feature is a **gear**: a self-contained set of modules with a clean four-layer split,
sitting on a shared common toolkit. The problem-report gear is the worked example:

```
feature/problem-report
├── domain          # pure Kotlin: entities, use-case contracts (no framework)
├── data            # use-case impls, encryption, platform device info
├── presentation/api  # the public MVI contract (Action / UiState / Event)
├── presentation/impl # the ViewModel implementation
└── (aggregate)     # DI wiring: ProblemReportModule.newProblemReportViewModel()

common/{di, mvi, logging, files, resources, zip, annotations, common}  # shared toolkit
```

Each common module is a regular, replaceable dependency with its own scope. Adding a
second feature means adding another `feature/<name>` gear next to this one — not
restructuring the app.

### B.2 Layer isolation by default

One of the highest-risk forms of rot in a long-living codebase is the domain layer
quietly taking a dependency on a framework, making it untestable and unportable. Gears
Mobile encodes the layering in **convention plugins**
([`gradle/plugins/.../convention`](../gradle/plugins/plugin/src/main/kotlin/convention)):

- `convention.module-domain` — pure Kotlin + serialization, **no UI/IO dependencies**.
- `convention.module-data` — implementation layer.
- `convention.module-presentation-api` — exposes only the contract; automatically depends on `:common:mvi` and `:common:annotations`.
- `convention.module-presentation-impl` — Compose-resources + the ViewModel impl.
- `convention.module-feature` — the DI aggregate.

> The architecture makes the **clean-layered path the normal path**. A module declares
> *what kind of layer it is* by applying a convention plugin; the plugin decides what it
> is allowed to depend on.

### B.3 One consistent UI contract: MVI

In native ×2 or per-team KMP, every screen invents its own state-management shape. Gears
Mobile standardizes on one: a feature declares a `sealed` **Action / UiState / Event**
contract in its `presentation/api`, and the ViewModel is a
`MviViewModel<Action, UiState, Event>` (a typealias over the shared `:common:mvi`):

```kotlin
interface ProblemReport {
    sealed interface Action { /* SelectProblemType, Submit, … */ }
    data class UiState(/* … */)
    sealed interface Event { data class ReadyToShare(val result: ProblemReportResult) : Event }
    typealias ViewModel = MviViewModel<Action, UiState, Event>
}
```

Both platforms consume the same unidirectional contract: dispatch `Action`s, observe a
`UiState` flow, react to one-shot `Event`s. On Android, `CSDKViewModel` bridges it into
a Hilt `@HiltViewModel`; on iOS, SKIE exposes the flows idiomatically to Swift.

### B.4 Prewritten architecture lints

This is where Gears Mobile uses Kotlin's lint model as a platform feature. It is not
different *in kind* from any team adopting Detekt — the value is that the rules already
exist and the build fails on violation:

- **ktlint** with `ktlint_code_style = android_studio` (see [`.editorconfig`](../.editorconfig)), plus a **custom ktlint ruleset** in [`gradle/ktlint-rules`](../gradle/ktlint-rules) that enforces feature-specification conventions.
- **Detekt** with a shared config ([`config/detekt/detekt.yml`](../config/detekt/detekt.yml)).
- **`allWarningsAsErrors = true`** in the base convention — a Kotlin warning is a build failure.
- **`com.squareup.sort-dependencies`** — dependency blocks must stay sorted.

> Documentation decays. These checks make selected architecture and style rules
> executable in CI ([`.github/workflows/ci.yml`](../.github/workflows/ci.yml) runs `ktlintCheck` + `detekt`),
> so violations are caught by tools, not only by reviewers.

### B.5 Composable gears: one codebase, two distribution shapes

The umbrella `gears` module assembles every gear into a single deliverable, and the same
Kotlin produces both platform artifacts with no code change:

- **Android** → `./gradlew assembleAndroidMain` → an **AAR** (`gears/build/outputs/aar/gears.aar`).
- **iOS** → `./gradlew :gears:assembleGearsReleaseXCFramework` → an **XCFramework** (`Gears.xcframework`, device + simulator slices) built with SKIE.

> Write the logic once → ship it as an Android library and an iOS framework — **no
> rewrites**, no second implementation to keep in sync.

### B.6 A typed public surface, idiomatic on both sides

The SDK exposes one entry point — the `Gears` object — and keeps the surface deliberately
small. `Gears.newProblemReportViewModel(...)` returns the feature's MVI `ViewModel`;
`Gears.initialize(...)` must be called once before creating a ViewModel — on Android it also
supplies the application context (`Gears.initialize(context)`), while on iOS it takes no argument
(`Gears.initialize()`) — and it enables SDK file logging and sweeps report bundles older than 24h
on both platforms. Because the public
contract is plain Kotlin `sealed`/`data` types and `Flow`s, **SKIE** renders them as
native Swift enums, sealed types, and async sequences — so iOS consumers get an
idiomatic API, not a stringly-typed Objective-C bridge.

### B.7 Runtime toolkit capabilities

The common toolkit provides the cross-cutting capabilities a feature shouldn't
re-invent, each shared across platforms via `expect`/`actual`:

- **`:common:logging`** — leveled logging with console and file writers.
- **`:common:files`** — a `FileSystem` abstraction over the platform file APIs.
- **`:common:zip`** — archive packing (used to bundle a problem report).
- **`:common:resources`** — a `StringProvider` over Compose-resources for localized strings.
- **`:common:di` / `:common:mvi` / `:common:annotations`** — DI module wiring, the MVI base, and `@Immutable`-style markers.

### B.8 Shared resources and localization

Localized strings live once in the feature's `composeResources/values-*/strings.xml` and
are reached through a generated typed `Res` accessor — the same strings render on both
platforms. A `StringProvider` indirection lets tests substitute a fake (avoiding a known
Compose-resources main-thread quirk on the iOS simulator), keeping the localization path
testable.

### B.9 Preconfigured build-gated safety

Gears Mobile defines a workspace safety floor so "the preferred path" is concrete build
rules, not style advice:

- `allWarningsAsErrors = true` across modules.
- ktlint (`android_studio` style) + custom feature-spec rules + Detekt, wired into the same gate as tests.
- `-Xexpect-actual-classes` and explicit `optIn`s declared centrally in the base convention.
- A single **version catalog** ([`gradle/libs.versions.toml`](../gradle/libs.versions.toml)) as the one source of dependency versions, trimmed to what the SDK actually uses.
- Sorted dependency blocks enforced by `sort-dependencies`.

### B.10 Local-first, shift-left testing

Because gears are libraries, the **entire business logic runs and is tested on a
laptop** with no device or emulator: `./gradlew jvmTest` exercises every common-test
suite on the JVM. Tests are written once in `commonTest` and run on JVM and native; the
shared `:common:test-utils` module plus **Turbine** (flow assertions) and **mokkery**
(multiplatform mocking) are pre-wired by the base convention. The same suites run again
on iOS (`iosSimulatorArm64Test`) and Android instrumentation in CI.

> This local-first loop lets developers — and AI agents — catch logic and cross-module
> issues *before* a PR is opened, long before device CI.

### B.11 Supply-chain and security policy as code

Security is part of the build, not a separate step. The CI pipeline
([`.github/workflows/ci.yml`](../.github/workflows/ci.yml)) runs **Trivy** (filesystem/dependency scan),
**Semgrep** (SAST), and **TruffleHog** (secret scanning) on merge requests, plus
**MobSF** mobile-specific scans in the Android and iOS child pipelines. Dependency
versions are pinned centrally in the version catalog, and scan exclusions are explicit,
reviewable files (`.semgrepignore`, `.trufflehogignore`, `.mobsf-config.yml`).

### B.12 Spec-driven development with Constructor Studio

This repository uses **[Constructor Studio](https://github.com/constructorfabric/studio)**
(`cfs` + the SDLC kit) as the spec-driven layer around the code. Architecture and
requirements live **alongside the implementation** in [`docs/sdlc/`](sdlc/) — a
`PRD → ADR → DESIGN → FEATURE` chain — instead of a wiki that drifts.

What makes it more than Markdown:

- **Traceable `cpt-*` identifiers** connect specs, code, and tests. Each spec element has a
  stable ID (`cpt-cyberfabricmobile-flow-…`, `-algo-…`, `-state-…`, `-dod-…`), and the
  implementation carries matching **`@cpt-*` code markers**. `cfs where-defined` /
  `where-used` jump between a requirement and the exact `file:line` that implements it.
- **Deterministic validation.** `cfs validate` checks ID definitions/references, document
  structure, table-of-contents, and **code↔doc coverage** — not prose review. The
  problem-report FEATURE is registered as `traceability = FULL`: every `flow`/`algo`/`state`/
  `dod` ID is traced to a code marker (**coverage 6/6**), and the gate runs locally and in CI.
- **AI-assisted SDLC workflows.** The kit ships role-based workflows — `cf-sdlc-doc-prd`,
  `-doc-adr`, `-doc-design`, `-doc-feature`, `-implement` (writes code *with* markers),
  `-reverse-engineer` (reconstructs specs from existing code), and `-pr-review`. Coding
  agents work better when correctness rules are expressed as IDs, markers, templates, and
  deterministic checks rather than only prose.
- **Living, reviewable docs.** The current `docs/sdlc/` set was reverse-engineered from this
  codebase, then PR-reviewed and **stakeholder-signed-off**; the same IDs keep code and spec
  in lock-step as the SDK grows feature gears.

> For long-living mobile SDKs this matters as much as the runtime framework: requirements,
> decisions, and feature contracts stay navigable and auditable, and the build can prove the
> code still implements them.

---

## When Gears Mobile is (and isn't) the right choice

**Choose Gears Mobile when you are:**

- Shipping the **same feature behavior to iOS and Android** and tired of maintaining it twice.
- A team that wants **native UI** but **shared, correctness-checked logic**.
- Building features whose **state machines and validation** must not drift between platforms.
- Investing in a **long-living** mobile codebase where enforced architecture pays off.

**Gears Mobile is deliberately *not*:**

- A cross-platform **UI** toolkit — UI stays native (SwiftUI/UIKit, Compose/Views).
- The lowest-barrier way to prototype a single-platform app.
- A hot-reload-first developer experience — Kotlin/Native iOS link times are real.
- A general app template — it is the *foundation* features are built on.

---

## Get started

```bash
git clone <repo-url> cyberfabric-mobile
cd cyberfabric-mobile/gears

# Run the full business logic locally on the JVM — no device needed.
./gradlew jvmTest

# Build the Android library (AAR).
./gradlew assembleAndroidMain

# Build the iOS framework (device + simulator) — requires macOS + Xcode.
./gradlew :gears:assembleGearsReleaseXCFramework

# Quality gate.
./gradlew ktlintCheck detekt
```

**Next steps**

- Browse the [`gears/feature/problem-report`](../gears/feature/problem-report) gear as the reference feature.
- Read a [convention plugin](../gradle/plugins/plugin/src/main/kotlin/convention) to see how a module declares its layer.
- See the [CI pipeline](../.github/workflows/ci.yml) for how build, test, and security gates fit together.

---

*Constructor Fabric Gears Mobile (Kotlin) ·
Secure · Modular · Composable · Native on both platforms.*
