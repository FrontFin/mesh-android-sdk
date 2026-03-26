# Mesh Connect Android SDK — CLAUDE.md

## Project Overview

This is the **Mesh Connect Android SDK** (`com.meshconnect:link`), a library that enables Android apps to integrate with the Mesh Connect platform via a WebView-based UI flow. Users authenticate with brokerages/crypto wallets and the SDK returns tokens and transfer results to the host app.

- **Current version:** see `mesh-link` in `gradle/libs.versions.toml`
- **Maven coordinates:** `com.meshconnect:link`
- **GitHub:** https://github.com/FrontFin/mesh-android-sdk
- **License:** MIT

---

## Project Structure

```
mesh-android-sdk/
├── app/                        # Example/demo application
│   └── src/main/               # Demo app showing SDK integration
├── link/                       # Main SDK library module (published AAR)
│   ├── src/main/java/com/meshconnect/link/
│   │   ├── converter/          # JSON serialization/deserialization (Gson)
│   │   ├── entity/             # Public data models (LinkConfiguration, LinkPayload, etc.)
│   │   ├── ui/                 # Activity, ViewModel, JSBridge, LaunchLink contract
│   │   ├── usecase/            # Business logic (deserialize, broadcast, filter messages)
│   │   └── utils/              # Extensions (createURL, decodeToken, whitelisted origins)
│   └── src/test/kotlin/        # Unit tests (27 files)
├── gradle/
│   └── libs.versions.toml      # Centralized dependency/version catalog
├── .github/workflows/
│   ├── primary.yaml            # CI: lint + tests + build on PRs to main
│   └── release.yaml            # CD: publish, tag, GitHub Release, Slack announcement
├── .claude/
│   └── commands/               # Claude slash commands (bump-version, release)
├── RELEASE.md                  # Manual release process documentation
├── detekt.yml                  # Detekt static analysis configuration
└── build.gradle                # Root build file
```

---

## Build System

- **Build tool:** Gradle with Groovy DSL (`build.gradle` files, version catalog)
- **AGP:** 8.6.1
- **Kotlin:** 2.1.0
- **Compile SDK:** 34 | **Target SDK:** 34
- **Min SDK:** 24 (link module), 26 (app module)
- **Java compatibility:** 1.8

### Common Gradle Tasks

```bash
# Build
./gradlew :link:assembleRelease

# Tests
./gradlew :link:test

# Code coverage (min 45% enforced)
./gradlew :link:jacocoCoverageVerification

# Lint
./gradlew :link:lintRelease

# KtLint format check
./gradlew ktlintCheck

# KtLint auto-fix
./gradlew ktlintFormat

# Detekt static analysis
./gradlew detekt

# All CI checks (matches primary.yaml)
./gradlew ktlintCheck detekt :link:lintRelease :link:jacocoCoverageVerification :link:assembleRelease

# Publish to Maven Central (requires credentials)
./gradlew publishAndReleaseToMavenCentral
```

---

## Architecture

### Pattern: MVVM + Use Cases

```
LaunchLink (ActivityResultContract)
    └── LinkActivity
            ├── WebView  ←→  JSBridge (JS ↔ Native)
            └── LinkViewModel
                    ├── DeserializeLinkMessageUseCase  → parses JSON → LinkEvent
                    ├── FilterLinkMessage              → maps JsType → payload type
                    └── BroadcastLinkMessageUseCase    → emits to SharedFlow/LiveData
```

### Communication Flow

1. Host app registers `LaunchLink` contract and launches it with a `LinkConfiguration`
2. `LinkActivity` opens, loads the Mesh Connect WebView UI
3. JS calls `JSBridge.sendNativeMessage(json)` to send events to native
4. `LinkViewModel` deserializes the JSON, filters the event type, and emits payloads
5. On close/done, `LinkActivity` sets the `ActivityResult` (`LinkSuccess` or `LinkExit`)
6. Host app receives `LinkResult` in its callback

---

## Public API

### Entry Point

```kotlin
// In your Fragment/Activity
val launcher = registerForActivityResult(LaunchLink()) { result: LinkResult? ->
    when (result) {
        is LinkResult.LinkSuccess -> { /* result.payloads: List<LinkPayload> */ }
        is LinkResult.LinkExit   -> { /* result.errorMessage, result.exception */ }
        null -> { /* cancelled */ }
    }
}
launcher.launch(LinkConfiguration(token = "your-link-token"))
```

### `LinkConfiguration` (`entity/LinkConfiguration.kt`)

| Field | Type | Default | Description |
|---|---|---|---|
| `token` | `String` | required | Link token from Mesh backend |
| `accessTokens` | `List<IntegrationAccessToken>?` | null | Pre-populate existing tokens |
| `disableDomainWhiteList` | `Boolean?` | false | Bypass domain whitelisting |
| `language` | `String?` | null | BCP-47 language tag; pass `"system"` to use the device locale |
| `displayFiatCurrency` | `String?` | null | ISO 4217 code for fiat equivalent display (e.g. `"USD"`) |
| `theme` | `LinkTheme?` | null | UI theme — `LIGHT`, `DARK`, or `SYSTEM` (follows device dark-mode) |

### `LinkResult` (sealed interface, `ui/LinkResult.kt`)

- `LinkResult.LinkSuccess(payloads: List<LinkPayload>)`
- `LinkResult.LinkExit(errorMessage: String?, exception: Exception?)`

### `LinkPayload` (sealed interface, `entity/LinkPayload.kt`)

| Subtype | Key Fields |
|---|---|
| `AccessTokenPayload` | `accountTokens: List<AccountToken>`, `brokerType`, `brokerName` |
| `DelayedAuthPayload` | `refreshTokenExpirationDate`, `brokerType`, `brokerName` |
| `TransferFinishedSuccessPayload` | `txId`, `fromAddress`, `toAddress`, `symbol`, `amount`, `networkId` |
| `TransferFinishedErrorPayload` | `errorMessage`, `txId` |

### Real-time Streams (collect alongside activity result)

```kotlin
// Collect individual payloads as they arrive
LinkPayloads.collect { payload: LinkPayload -> ... }

// Collect raw event maps
LinkEvents.collect { event: Map<String, *> -> ... }
```

Both are `SharedFlow` instances in `link/src/main/java/com/meshconnect/link/LinkPayloads.kt` and `link/src/main/java/com/meshconnect/link/LinkEvents.kt`.

---

## Key Files Reference

| File | Purpose |
|---|---|
| `link/src/main/java/.../ui/LaunchLink.kt` | Public entry point — ActivityResultContract |
| `link/src/main/java/.../ui/LinkActivity.kt` | WebView host, URL loading, JS bridge wiring |
| `link/src/main/java/.../ui/LinkViewModel.kt` | Message processing, state, SharedFlow emission |
| `link/src/main/java/.../ui/JSBridge.kt` | `@JavascriptInterface` — receives JS messages |
| `link/src/main/java/.../entity/` | All public data models |
| `link/src/main/java/.../entity/LinkTheme.kt` | `LIGHT`/`DARK`/`SYSTEM` enum |
| `link/src/main/java/.../usecase/DeserializeLinkMessageUseCase.kt` | JSON → LinkEvent |
| `link/src/main/java/.../usecase/BroadcastLinkMessageUseCase.kt` | LinkEvent → SharedFlow |
| `link/src/main/java/.../usecase/FilterLinkMessage.kt` | JsType → payload routing |
| `link/src/main/java/.../converter/JsonConverter.kt` | Gson setup with custom deserializers |
| `link/src/main/java/.../utils/WhitelistedOrigins.kt` | Allowed WebView domains list |
| `link/src/main/java/.../utils/CreateURL.kt` | Builds link URL from token/config (`lng`, `fiatCur`, `th` params) |
| `link/src/main/java/.../utils/DecodeToken.kt` | Base64/URL token decoding |
| `link/src/main/java/.../utils/Language.kt` | `systemLanguage` property + `resolveLanguage()` — device locale resolution |
| `link/src/main/java/.../utils/Theme.kt` | `getThemeName()`, `resolveTheme()`, `getThemeFromUrl()`, `isSystemThemeDark()` |
| `link/src/main/AndroidManifest.xml` | Permissions, LinkActivity declaration |
| `link/proguard-rules.pro` | R8/ProGuard rules for SDK consumers |
| `gradle/libs.versions.toml` | All versions — update `mesh-link` to bump SDK version |

---

## Dependencies

### Runtime (shipped in AAR)
| Dependency | Version | Purpose |
|---|---|---|
| `androidx.appcompat` | catalog | Activity/UI base |
| `androidx.core-ktx` | catalog | Kotlin extensions |
| `androidx.lifecycle-viewmodel-ktx` | catalog | ViewModel + coroutines |
| `material` | catalog | Material theme for LinkActivity |
| `kotlinx-coroutines-android` | 1.6.3 | Async/concurrency |
| `gson` | 2.11.0 | JSON serialization |

### Test Only
| Dependency | Purpose |
|---|---|
| `junit` | Test runner |
| `mockk` (1.13.7) | Kotlin-idiomatic mocking |
| `kluent` (1.73) | Fluent assertion DSL |
| `kotlinx-coroutines-test` | Coroutine testing utilities |
| `androidx.arch.core:core-testing` | LiveData testing |
| `org.json:json` | Real `JSONObject` implementation for unit tests (Android stub replacement) |

---

## Testing

Tests live in `link/src/test/kotlin/com/meshconnect/link/`.

### Key Test Files

| Test File | Covers |
|---|---|
| `LinkViewModelTest` | ViewModel message processing, state emission |
| `DeserializeLinkMessageUseCaseTest` | JSON parsing into LinkEvent |
| `BroadcastLinkMessageUseCaseTest` | Event routing to SharedFlow |
| `FilterLinkMessageTest` | JsType mapping logic |
| `JsonConverterTest` | Gson deserializer setup |
| `DecodeTokenTest` | Token decoding edge cases |
| `CreateURLTest` | URL builder |
| `WhitelistedOriginsTest` | Domain allowlist logic |
| `LanguageTest` | `getSystemLanguage` and `resolveLanguage` including `"system"` passthrough |
| `ThemeTest` | `getThemeName`, `resolveTheme`, `getThemeFromUrl` — `decodeBase64` mocked via `mockkStatic` |

### Running Tests

```bash
./gradlew :link:test                        # all unit tests
./gradlew :link:testDebugUnitTest           # debug variant
./gradlew :link:jacocoCoverageVerification  # with coverage check (min 45%)
```

### Test Utilities

- `MainDispatcherRule` — replaces `Dispatchers.Main` with `TestCoroutineDispatcher`
- `TestObserver` — helper for observing LiveData in tests
- Use `runTest` + `advanceUntilIdle()` for coroutine tests

---

## Code Quality

### KtLint
- Config: standard rules + custom settings in root build
- **Always run** `./gradlew ktlintFormat` before committing to auto-fix formatting
- CI blocks PRs on `ktlintCheck` failure

### Detekt
- Config file: `detekt.yml` in project root
- Scans `src/main/java` and `src/main/kotlin`
- Run: `./gradlew detekt`

### Android Lint
- Run: `./gradlew :link:lintRelease`
- Results in `link/build/reports/lint-results-release.html`

### SonarQube
- Integrated in CI via `sonarqube` Gradle task
- Requires `SONAR_TOKEN` and `SONAR_HOST_URL` env vars (set in GitHub secrets)

---

## Domain Whitelist

`WhitelistedOrigins.kt` contains allowed domains for WebView navigation. When `disableDomainWhiteList = false` (default), only these domains load in the WebView:

- `meshconnect.com` (and subdomains)
- `walletconnect.com`, `coinbase.com`, `okx.com`, `gemini.com`
- `stripe.com`, `recaptcha` (Google), `revolut.com`
- And others for OAuth flows

To allow all domains (e.g. for testing): set `disableDomainWhiteList = true` in `LinkConfiguration`.

---

## Release Process

See `RELEASE.md` for full details. Summary:

1. Run `/bump-version` — automatically diffs HEAD against the latest tag, bumps `mesh-link` in `gradle/libs.versions.toml` using semantic versioning, and prepends a new entry to `CHANGELOG.md`
2. Merge to `main`
3. Run `/release` — pre-flight checks, then triggers `release.yaml` which publishes to Maven Central, tags the repo, creates the GitHub Release, and posts a Slack announcement
4. Verify artifact on Maven Central

### Publishing Secrets (GitHub)
- `MAVEN_USERNAME` / `MAVEN_PASSWORD` — Sonatype credentials
- `SIGNING_KEY` / `SIGNING_PASSWORD` — GPG signing key

---

## CI/CD Workflows

### `primary.yaml` — runs on PRs to `main`
1. `ktlintCheck` + `detekt` — formatting and static analysis
2. `lintRelease` — Android lint
3. `jacocoCoverageVerification` — coverage ≥ 45%
4. SonarQube analysis

### `release.yaml` — push to `main` or manual trigger
- Detects new version (compares `mesh-link` to latest tag) — skips if already released
- Validates CHANGELOG has a matching entry
- Runs all CI checks (ktlint+detekt, lint, coverage, Sonar)
- Publishes to Maven Central
- Creates and pushes git tag `vX.Y.Z`
- Creates GitHub Release with changelog notes and full-diff link
- Posts Slack announcement


---

## Claude Slash Commands

Commands live in `.claude/commands/` and are invoked from within Claude Code with `/command-name`.

| Command | What it does |
|---|---|
| `/bump-version` | Diffs HEAD vs latest tag, classifies changes as MAJOR/MINOR/PATCH, bumps `mesh-link` in `libs.versions.toml`, and prepends a new entry to `CHANGELOG.md` |
| `/release` | Pre-flight check (version bump detected + changelog entry present), then triggers `release.yaml` and monitors the run to completion |

Typical release flow:
1. `/bump-version` — sets the version and writes the changelog
2. Merge the version bump PR to `main`
3. `/release` — triggers and watches the full release pipeline

---

## Development Tips

- **WebView debugging:** Enable `WebView.setWebContentsDebuggingEnabled(true)` in debug builds (already handled in `LinkActivity`)
- **Token format:** Tokens are either Base64-encoded JSON or raw HTTPS URLs — `DecodeToken.kt` handles both
- **Adding a new payload type:**
  1. Add a new `JsType` enum value in `entity/`
  2. Add a new `LinkPayload` subtype in `entity/`
  3. Update `FilterLinkMessage` to route the new type
  4. Add a response class in `entity/` and register a Gson deserializer in `JsonConverter`
  5. Add tests in the corresponding test files
- **Adding a whitelisted domain:** Edit `WhitelistedOrigins.kt` and add a test case in `WhitelistedOriginsTest`
- **Changing the public API:** Update ProGuard rules in `link/proguard-rules.pro` if new public classes are added
- **`LinkTheme` string values:** Always use the `THEME_LIGHT`, `THEME_DARK`, `THEME_SYSTEM` constants defined in `Theme.kt` instead of raw string literals — never hardcode `"light"`, `"dark"`, `"system"` for theme
- **`LinkTheme.valueOf` safety:** Use `LinkTheme.entries.find { it.name == name }` instead of `valueOf()` to avoid `IllegalArgumentException` on unrecognised values
- **Mocking Android statics in tests:** Use `mockkStatic(::functionRef)` for top-level functions (e.g. `decodeBase64`, `isSystemThemeDark`) and `mockkStatic(ClassName::class)` for class statics (e.g. `Uri`, `Log`). Always pair with `unmockkStatic` in `@After`
- **URL query parameters added by SDK:** `lng` (language), `fiatCur` (display fiat currency), `th` (theme) — see `CreateURL.kt`
- **Theme resolution from URL:** `getThemeFromUrl(url)` checks the `th` query param first; falls back to the `th` field inside the Base64-encoded `link_style` param if `th` is absent or blank
