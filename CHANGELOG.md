# Changelog

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
