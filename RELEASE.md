# 🚀 Release Android SDK

1. Update [mesh-link](https://github.com/FrontFin/mesh-android-sdk/blob/main/gradle/libs.versions.toml#L26) version according to [Semantic Versioning](https://semver.org/).
2. Merge into `main`.
3. Run [Deploy Link SDK](https://github.com/FrontFin/mesh-android-sdk/actions/workflows/deploy.yaml).
4. Verify new version appears on [Maven Central](https://central.sonatype.com/artifact/com.meshconnect/link). You can check the progress on [Deployments](https://central.sonatype.com/publishing).
5. [Draft a new release](https://github.com/FrontFin/mesh-android-sdk/releases/new).
6. "Select a tag", according to the version.
7. “Generate release notes”, use [Mastering SDK Release on GitHub](https://www.notion.so/Mastering-SDK-Release-on-GitHub-2e3f862e950a8079b8d9ccc7a2007a1a?pvs=21). 
8. “Publish release”.
