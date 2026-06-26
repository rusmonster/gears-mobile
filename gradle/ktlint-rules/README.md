# Custom ktlint Rules

Custom lint rules enforcing [FeatureSpecification.md](../../../docs/architecture/FeatureSpecification.md) conventions. These rules run automatically via `ktlintCheck` in CI.

## Suppressing Rules

Use `@Suppress("ktlint:constructor:<rule-id>")` on the flagged element:

```kotlin
// On a class or interface
@Suppress("ktlint:constructor:missing-interface-kdoc")
interface MyContract { ... }

// On a constructor parameter
data class MyViewItem(
    @Suppress("ktlint:constructor:viewitem-no-raw-types")
    val progress: Float,
)

// At file level (for file-scoped rules like constants-at-top or domain imports)
@file:Suppress("ktlint:constructor:constants-at-top")
```

## Rule Reference

### KDoc Enforcement

| Rule ID | Flags | Suppress on |
|---------|-------|-------------|
| `missing-interface-kdoc` | Interfaces in `domain`/`presentation.api` without KDoc | `interface` |
| `missing-method-kdoc` | Methods on those interfaces without KDoc | `fun` |
| `missing-enum-entry-kdoc` | Enum entries in `domain` without KDoc | enum entry |
| `missing-sealed-subtype-kdoc` | `data object`/`data class` in sealed UiState/Action/Event without KDoc | subtype declaration |
| `missing-uistate-property-kdoc` | Properties in `UiState.Data` missing `@property` KDoc tag | `val` parameter |

```kotlin
// missing-interface-kdoc
@Suppress("ktlint:constructor:missing-interface-kdoc")
interface CourseRepository { ... }

// missing-method-kdoc
interface CourseRepository {
    @Suppress("ktlint:constructor:missing-method-kdoc")
    suspend fun getCourses(): List<Course>
}

// missing-enum-entry-kdoc
enum class CourseState {
    @Suppress("ktlint:constructor:missing-enum-entry-kdoc")
    UNKNOWN,
}

// missing-sealed-subtype-kdoc
sealed interface UiState {
    @Suppress("ktlint:constructor:missing-sealed-subtype-kdoc")
    data object Error : UiState
}

// missing-uistate-property-kdoc
data class Data(
    @Suppress("ktlint:constructor:missing-uistate-property-kdoc")
    val items: List<String>,
) : UiState
```

### @Immutable Enforcement

| Rule ID | Flags | Suppress on |
|---------|-------|-------------|
| `missing-immutable-uistate` | UiState `data class`/`data object` subtypes missing `@Immutable` | subtype declaration |
| `missing-immutable-viewitem` | `*ViewItem` data classes missing `@Immutable` | `data class` |

```kotlin
// missing-immutable-uistate
@Suppress("ktlint:constructor:missing-immutable-uistate")
data class Data(val x: String) : UiState

// missing-immutable-viewitem
@Suppress("ktlint:constructor:missing-immutable-viewitem")
data class LegacyViewItem(val title: String)
```

### Naming Conventions

| Rule ID | Flags | Suppress on |
|---------|-------|-------------|
| `fetch-load-get-naming` | Wrong fetch/load/get semantics; wrong Cache write method names | `fun` |
| `boolean-visibility-naming` | ViewItem boolean fields not using `is<Noun>Visible`/`Enabled` pattern | `val` parameter |
| `no-load-data-action` | Action subtypes named `LoadData` | subtype declaration |
| `test-method-naming` | `@Test` methods without underscore-separated naming | `fun` |

```kotlin
// fetch-load-get-naming
@Suppress("ktlint:constructor:fetch-load-get-naming")
suspend fun saveCourse(id: String)

// boolean-visibility-naming
data class MyViewItem(
    @Suppress("ktlint:constructor:boolean-visibility-naming")
    val isNew: Boolean,
)

// no-load-data-action
@Suppress("ktlint:constructor:no-load-data-action")
data object LoadData : Action

// test-method-naming
@Suppress("ktlint:constructor:test-method-naming")
@Test
fun testLegacyName() { ... }
```

### ViewItem Type Safety

| Rule ID | Flags | Suppress on |
|---------|-------|-------------|
| `viewitem-no-raw-types` | `Float`, `Double`, `Instant`, `LocalDate`, etc. in ViewItem fields | `val` parameter |

```kotlin
data class CourseViewItem(
    @Suppress("ktlint:constructor:viewitem-no-raw-types")
    val progress: Float,
)
```

### Structure & Layer Isolation

| Rule ID | Flags | Suppress on |
|---------|-------|-------------|
| `constants-at-top` | File-level `const val` / `private val` declared after classes/functions | `@file:Suppress` |
| `class-suffix-package` | Class suffix doesn't match expected package (e.g. `*Cache` not in `.domain.`) | `class`/`interface` |
| `domain-no-data-or-presentation-import` | Domain package importing from `.data.` or `.presentation.` | `@file:Suppress` |
| `no-uistate-cast` | `as` / `as?` casts to `UiState` subtypes | expression / `@file:Suppress` |
| `no-run-catching` | `runCatching` usage (use `runCatchingCancellable` instead) | expression / `val` |
| `no-hardcoded-strings-composable` | Hardcoded string literals in `@Composable` functions | `fun` / `@file:Suppress` |

```kotlin
// constants-at-top
@file:Suppress("ktlint:constructor:constants-at-top")

// class-suffix-package
@Suppress("ktlint:constructor:class-suffix-package")
class MigratedUseCase

// domain-no-data-or-presentation-import
@file:Suppress("ktlint:constructor:domain-no-data-or-presentation-import")

// no-uistate-cast
@Suppress("ktlint:constructor:no-uistate-cast")
val data = state as? UiState.Data

// no-run-catching
@Suppress("ktlint:constructor:no-run-catching")
val result = runCatching { parse(input) }

// no-hardcoded-strings-composable
@Suppress("ktlint:constructor:no-hardcoded-strings-composable")
@Composable
fun LegacyScreen() { Text("TODO: migrate to string resources") }
```
