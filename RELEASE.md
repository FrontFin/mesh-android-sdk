# Release Android SDK

## ✨ With Claude Code (recommended)

1. Run `/bump-version` — bumps version according to [Semantic Versioning][semver] and prepends a new entry to `CHANGELOG.md`.
2. Merge into `main`.
3. The release workflow starts automatically. Optionally run [Release workflow][release-workflow] manually.
4. Verify the new version appears on [Maven Central][maven-central].

## ✍🏼️ Manually

1. Update [mesh-link][libs-versions] version according to [Semantic Versioning][semver].
2. Add a new entry to `CHANGELOG.md`.
3. Merge into `main`.
4. The release workflow starts automatically. Optionally run [Release workflow][release-workflow] manually.
5. Verify the new version appears on [Maven Central][maven-central].


> [!NOTE]
> Publication on Maven Central can take **up to about 15 minutes** after the workflow finishes before the new version is visible.

[semver]: https://semver.org/
[release-workflow]: https://github.com/FrontFin/mesh-android-sdk/actions/workflows/release.yml
[maven-central]: https://central.sonatype.com/artifact/com.meshconnect/link
[libs-versions]: https://github.com/FrontFin/mesh-android-sdk/blob/main/gradle/libs.versions.toml
