# Mesh Connect Android SDK — CLAUDE.md

## Project Overview

This is the **Mesh Connect Android SDK** (`com.meshconnect:link`), a library that enables Android apps to integrate with the Mesh Connect platform via a WebView-based UI flow. Users authenticate with brokerages/crypto wallets and the SDK returns tokens and transfer results to the host app.

- **Current version:** 3.2.0 (defined in `gradle/libs.versions.toml`)
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
│   ├── deploy.yaml             # CD: manual deploy to Maven Central
│   └── release-announcement.yaml
├── RELEASE.md                  # Manual release process documentation
├── detekt.yml                  # Detekt static analysis configuration
└── build.gradle                # Root build file
```

---

## Build System

- **Build tool:** Gradle with Kotlin DSL (`.gradle` files, version catalog)
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
| `language` | `String?` | null | UI language override |

### `LinkResult` (sealed interface, `entity/LinkResult.kt`)

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
LinkPayloads.flow.collect { payload: LinkPayload -> ... }

// Collect raw event maps
LinkEvents.flow.collect { event: Map<String, *> -> ... }
```

Both are `SharedFlow` instances in `entity/LinkPayloads.kt` and `entity/LinkEvents.kt`.

---

## Key Files Reference

| File | Purpose |
|---|---|
| `link/src/main/java/.../ui/LaunchLink.kt` | Public entry point — ActivityResultContract |
| `link/src/main/java/.../ui/LinkActivity.kt` | WebView host, URL loading, JS bridge wiring |
| `link/src/main/java/.../ui/LinkViewModel.kt` | Message processing, state, SharedFlow emission |
| `link/src/main/java/.../ui/JSBridge.kt` | `@JavascriptInterface` — receives JS messages |
| `link/src/main/java/.../entity/` | All public data models |
| `link/src/main/java/.../usecase/DeserializeLinkMessageUseCase.kt` | JSON → LinkEvent |
| `link/src/main/java/.../usecase/BroadcastLinkMessageUseCase.kt` | LinkEvent → SharedFlow |
| `link/src/main/java/.../usecase/FilterLinkMessage.kt` | JsType → payload routing |
| `link/src/main/java/.../converter/JsonConverter.kt` | Gson setup with custom deserializers |
| `link/src/main/java/.../utils/WhitelistedOrigins.kt` | Allowed WebView domains list |
| `link/src/main/java/.../utils/CreateURL.kt` | Builds link URL from token/config |
| `link/src/main/java/.../utils/DecodeToken.kt` | Base64/URL token decoding |
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

1. Update version in `gradle/libs.versions.toml` (`mesh-link = "X.Y.Z"`)
2. Merge to `main`
3. Trigger **Deploy** workflow manually in GitHub Actions
4. Verify artifact on Maven Central
5. Create GitHub Release with tag `vX.Y.Z` → triggers Slack announcement

### Publishing Secrets (GitHub)
- `MAVEN_USERNAME` / `MAVEN_PASSWORD` — Sonatype credentials
- `SIGNING_KEY` / `SIGNING_PASSWORD` — GPG signing key

---

## CI/CD Workflows

### `primary.yaml` — runs on PRs to `main`
1. `ktlintCheck` — formatting
2. `detekt` — static analysis
3. `lintRelease` — Android lint
4. `jacocoCoverageVerification` — coverage ≥ 45%
5. `assembleRelease` — build AAR
6. SonarQube analysis

### `deploy.yaml` — manual trigger
- Runs all CI checks, then publishes to Maven Central
- Creates and pushes a git tag `vX.Y.Z`

### `release-announcement.yaml` — on GitHub Release published
- Posts Slack notification with version details

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
