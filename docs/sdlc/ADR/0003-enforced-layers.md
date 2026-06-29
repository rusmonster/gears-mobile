---
status: accepted
date: 2026-06-13
decision-makers: reconstructed from code (cf-sdlc-reverse-engineer)
---

# Enforce clean module layering via Gradle convention plugins


<!-- toc -->

- [Context and Problem Statement](#context-and-problem-statement)
- [Decision Drivers](#decision-drivers)
- [Considered Options](#considered-options)
- [Decision Outcome](#decision-outcome)
  - [Consequences](#consequences)
  - [Confirmation](#confirmation)
- [Pros and Cons of the Options](#pros-and-cons-of-the-options)
  - [Convention plugins enforce per-layer dependencies](#convention-plugins-enforce-per-layer-dependencies)
  - [Documented conventions enforced in review only](#documented-conventions-enforced-in-review-only)
  - [One module per feature (no layer split)](#one-module-per-feature-no-layer-split)
- [More Information](#more-information)
- [Traceability](#traceability)

<!-- /toc -->

**ID**: `cpt-cyberfabricmobile-adr-enforced-layers`
## Context and Problem Statement

A long-lived SDK rots when layers leak (e.g. `domain` taking a UI/IO dependency). How do we
keep the layering honest as the codebase grows and is edited by humans and AI agents?

## Decision Drivers

* Maintainability and testability of `domain` (must stay framework-free)
* Consistency across feature gears
* Deterministic enforcement, not reviewer memory

## Considered Options

* Convention plugins that decide each module's allowed dependencies by layer
* Documented conventions enforced only in code review
* A single module per feature (no layer split)

## Decision Outcome

Chosen option: "Convention plugins", because a module declares its layer by applying
`convention.module-{domain,data,presentation-api,presentation-impl,feature}`, and the plugin
fixes what it may depend on — making the clean-layered path the only path, enforced by the
build alongside ktlint/detekt and `allWarningsAsErrors`.

### Consequences

* Good, because `domain` cannot import UI/IO; `presentation:api` exposes only the contract.
* Good, because new gears get the same structure for free.
* Bad, because adding a module means choosing or authoring the right convention plugin.

### Confirmation

`gradle/plugins/plugin/src/main/kotlin/convention/module-*.gradle.kts`; the problem-report
gear's four modules each apply the matching convention plugin.

## Pros and Cons of the Options

### Convention plugins enforce per-layer dependencies

* Good, because the clean-layered path is the only path the build allows.
* Good, because new feature gears inherit the structure for free.
* Bad, because adding a module means choosing or authoring the right convention plugin.

### Documented conventions enforced in review only

* Good, because zero build machinery.
* Bad, because enforcement depends on reviewer memory and decays over time.

### One module per feature (no layer split)

* Good, because simplest layout.
* Bad, because `domain` and IO/UI concerns mix, hurting testability and portability.

## More Information

See `gradle/plugins/plugin/src/main/kotlin/convention/module-*.gradle.kts`.

## Traceability

This decision directly addresses:

* `cpt-cyberfabricmobile-principle-enforced-layers` — design principle realized
* `cpt-cyberfabricmobile-constraint-build-gates` — enforced in the build gate
* `cpt-cyberfabricmobile-design-gears-sdk` — layered architecture
