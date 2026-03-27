# Release Android SDK

## ✨With Claude Code (recommended)

1. Run `/bump-version` — picks the correct version according to [Semantic Versioning](https://semver.org/) and prepends a new entry to `CHANGELOG.md`.
2. Merge into `main`.
3. The release workflow starts automatically on merge to `main`. Optionally run `/release` to trigger it manually.
4. Verify the new version on [Maven Central](https://central.sonatype.com/artifact/com.meshconnect/link).

## 🙋‍♂️ Manually

1. Update [mesh-link](https://github.com/FrontFin/mesh-android-sdk/blob/main/gradle/libs.versions.toml) version according to [Semantic Versioning](https://semver.org/) and prepend a matching entry to `CHANGELOG.md`.
2. Merge into `main`.
3. The release workflow starts automatically on merge to `main`. Optionally trigger [Release](https://github.com/FrontFin/mesh-android-sdk/actions/workflows/release.yaml) workflow manually.
4. Verify the new version on [Maven Central](https://central.sonatype.com/artifact/com.meshconnect/link).

> [!NOTE]
> Publication on Maven Central can take **up to about 15 minutes** after the workflow finishes before the new version is visible and resolvable in builds. You can check the deployment on Sonatype’s [Deployments](https://central.sonatype.com/publishing/deployments) page. For publishing issues, contact [@vitalii-movchan-mesh](https://github.com/vitalii-movchan-mesh).
