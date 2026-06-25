# Changelog

## 3.4.3

### Added
- Added `platform=android` query parameter to the WebView URL so Link v2 correctly identifies the SDK platform on first render.
- Added missing event types to the `onEvent` stream: `integrationConnected`, `transferMfaEntered`, `homePageLoaded`, `defiWalletError`, `paypalComplianceDeclined`.

### Fixed
- `integrationConnected` events sourced from `brokerageAccountAccessToken` / `delayedAuthentication` no longer leak token payload data into the `LinkEvents` stream.

## 3.4.2

### Fixed
- Removed `-flattenpackagehierarchy` from the rules shipped to consuming apps, so the SDK no longer repackages and renames host-app classes — fixing unreadable, meshconnect-prefixed class names in consumer logs.

### Changed
- Split the SDK's ProGuard config into `consumer-rules.pro` (public-API keep rules shipped in the AAR) and `proguard-rules.pro` (library-only build options), preventing global obfuscation directives from leaking into host-app builds.

## 3.4.1

### Fixed
- `LinkActivity` no longer covers the status bar and bottom navigation bar on Android — system bar insets are now applied as padding to the root view.

### Changed
- Updated `compileSdk`/`targetSdk` to 36.

## 3.4.0

### Changed
- `LinkExit` now exposes `errorMessage: String?` as a direct field instead of deriving it from a `Throwable`. This eliminates a Parcelable crash (`Class.ifTable` NPE) seen in Crashlytics. 
- The old `LinkExit(Throwable?)` constructor and `error` property are retained as deprecated — they compile without errors and map to `errorMessage` internally.
- Enabled `-keepattributes SourceFile,LineNumberTable` in consumer ProGuard rules so Crashlytics stack traces are human-readable.

## 3.3.1

### Fixed
- Added `onResume`/`onPause` WebView lifecycle calls to ensure JavaScript timers are properly resumed when the host activity returns to the foreground.

## 3.3.0

### Added
- `language` parameter now accepts `"system"` to automatically use the device locale.
- `displayFiatCurrency` parameter to `LinkConfiguration` — ISO 4217 code for fiat equivalent display in the Link UI (e.g. `"USD"`).
- `theme` parameter to `LinkConfiguration` — accepts `LinkTheme.LIGHT`, `LinkTheme.DARK`, or `LinkTheme.SYSTEM` (follows device setting).

## 3.2.0

### Removed
- `transferDestinationTokens` from `LinkConfiguration`.
