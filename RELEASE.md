# Release Android SDK

## With Claude Code (recommended)

1. Run `/bump-version` — diffs HEAD against the latest tag, picks the correct MAJOR/MINOR/PATCH bump, updates `mesh-link` in `gradle/libs.versions.toml`, and prepends a new entry to `CHANGELOG.md`.
2. Merge into `main`.
3. The release workflow starts automatically on merge to `main`. Optionally run `/release` to trigger it manually or monitor its progress — it validates the version and changelog before triggering, and is safe to run even if the auto-trigger already fired (it will stop if no new version is detected).
4. Verify the new version appears on [Maven Central](https://central.sonatype.com/artifact/com.meshconnect/link).

## Manually

1. Update [mesh-link](https://github.com/FrontFin/mesh-android-sdk/blob/main/gradle/libs.versions.toml) version according to [Semantic Versioning](https://semver.org/) and prepend a matching entry to `CHANGELOG.md`.
2. Merge into `main`.
3. The release workflow starts automatically on merge to `main`. Optionally trigger [Release](https://github.com/FrontFin/mesh-android-sdk/actions/workflows/release.yaml) workflow manually.
4. Verify the new version appears on [Maven Central](https://central.sonatype.com/artifact/com.meshconnect/link). You can check progress on [Deployments](https://central.sonatype.com/publishing).
